/*
 * Copyright (C) 2024 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.teleport.spi.vm.proxmox;

import com.google.gson.Gson;
import de.hasait.common.service.AbstractRefreshableDriver;
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.teleport.CliConfig;
import de.hasait.teleport.api.VmCreateTO;
import de.hasait.teleport.api.VmState;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeRepository;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ProxmoxDriver extends AbstractRefreshableDriver<HypervisorPO, ProxmoxDriverConfig> implements HypervisorDriver {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CliConfig cliConfig;
    private final VolumeRepository volumeRepository;
    private final VirtualMachineRepository virtualMachineRepository;

    public ProxmoxDriver(@Autowired CliConfig cliConfig, VolumeRepository volumeRepository, VirtualMachineRepository virtualMachineRepository) {
        super("proxmox", "Proxmox Hypervisor Driver", null);

        this.cliConfig = cliConfig;
        this.volumeRepository = volumeRepository;
        this.virtualMachineRepository = virtualMachineRepository;
    }

    @Override
    protected ProxmoxDriverConfig parseConfigText(String configText) {
        return new Gson().fromJson(configText, ProxmoxDriverConfig.class);
    }

    @Override
    protected void refresh(HypervisorPO po, ProxmoxDriverConfig config) {
        CliExecutor exe = cliConfig.createCliConnector(po);
        ProxmoxUtils.qmListFull(exe).forEach(it -> processListEntry(po, config, exe, it));
    }

    @Override
    public void create(HypervisorPO hypervisor, VmCreateTO config, boolean runInstallation) {

    }

    @Override
    public void start(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsRunning()) {
            throw new IllegalStateException("VM already running: " + virtualMachine);
        }

        if (virtualMachine.stateIsShutOff()) {
            update(virtualMachine);
        }

        log.info("Starting VM {}...", virtualMachine);

        CliExecutor exe = cliConfig.createCliConnector(virtualMachine);

        List<String> command = new ArrayList<>();
        command.add("qm");
        command.add("start");
        command.add(virtualMachine.getHvid());

        boolean dryMode = !exe.executeAndWaitExit0(command, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            virtualMachine.setState(VmState.RUNNING);
        } else {
            waitForState(exe, virtualMachine, VmState.RUNNING);
        }

        log.info("VM {} started", virtualMachine);

        if (!dryMode) {
            refresh(virtualMachine.getHypervisor());
        }
    }

    @Override
    public void shutdown(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsShutOff()) {
            throw new IllegalStateException("VM already shut off: " + virtualMachine);
        }

        log.info("Shutting off VM {}...", virtualMachine);

        CliExecutor exe = cliConfig.createCliConnector(virtualMachine);

        List<String> command = new ArrayList<>();
        command.add("qm");
        command.add("shutdown");
        command.add(virtualMachine.getHvid());

        boolean dryMode = !exe.executeAndWaitExit0(command, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            virtualMachine.setState(VmState.SHUTOFF);
        } else {
            waitForState(exe, virtualMachine, VmState.SHUTOFF);
        }

        log.info("VM {} shut off", virtualMachine);

        if (!dryMode) {
            refresh(virtualMachine.getHypervisor());
        }
    }

    @Override
    public void kill(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsShutOff()) {
            throw new IllegalStateException("VM already shut off: " + virtualMachine);
        }

        log.info("Killing VM {}...", virtualMachine);

        CliExecutor exe = cliConfig.createCliConnector(virtualMachine);

        List<String> command = new ArrayList<>();
        command.add("qm");
        command.add("stop");
        command.add(virtualMachine.getHvid());

        boolean dryMode = !exe.executeAndWaitExit0(command, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            virtualMachine.setState(VmState.SHUTOFF);
        } else {
            waitForState(exe, virtualMachine, VmState.SHUTOFF);
        }

        log.info("VM {} killed", virtualMachine);

        if (!dryMode) {
            refresh(virtualMachine.getHypervisor());
        }
    }

    @Override
    public void update(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void delete(VirtualMachinePO virtualMachine) {

    }

    private void waitForState(CliExecutor exe, VirtualMachinePO virtualMachine, VmState successState) {
        while (true) {
            VmState state = ProxmoxUtils.qmStatus(exe, virtualMachine.getHvid());
            if (state == successState) {
                break;
            }
            log.debug("VM {} is {}", virtualMachine.getName(), state);
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processListEntry(HypervisorPO hypervisor, ProxmoxDriverConfig config, CliExecutor exe, QmListE entry) {
        String name = entry.getName();
        VmState state = entry.getState();

        VirtualMachinePO existingVirtualMachine = hypervisor.findVirtualMachineByName(name).orElse(null);
        VirtualMachinePO virtualMachine;
        if (existingVirtualMachine != null) {
            existingVirtualMachine.setState(state);
            virtualMachine = existingVirtualMachine;
        } else {
            virtualMachine = new VirtualMachinePO(hypervisor, name, state);
        }

        virtualMachine.setHvid(entry.getId());

        Map<String, String> qmConfig = ProxmoxUtils.qmConfig(exe, virtualMachine.getHvid());

        String smbios1 = qmConfig.get("smbios1");
        if (smbios1 != null && smbios1.startsWith("uuid=")) {
            virtualMachine.setUuid(smbios1.substring("uuid=".length()));
        }

        int sockets = Integer.parseInt(qmConfig.get("sockets"));
        int cores = Integer.parseInt(qmConfig.get("cores"));
        int vcpu = sockets * cores;
        virtualMachine.setCores(vcpu);

        int memMb = Integer.parseInt(qmConfig.get("memory"));
        virtualMachine.setMemMb(memMb);

        // default video
        int vgaMemKb = 64 * 1024;
        virtualMachine.setVgaMemKb(vgaMemKb);
        String videoModel = "qxl";
        virtualMachine.setVideoModel(videoModel);

        virtualMachineRepository.save(virtualMachine);

        Set<VolumeAttachmentPO> volumeAttachments = new HashSet<>();

        int i = -1;
        char tdevletter = 'a';
        tdevletter--;
        while (true) {
            i++;
            tdevletter++;
            String scsiValue = qmConfig.get("scsi" + i);
            if (scsiValue == null) {
                break;
            }
            String[] split = scsiValue.split("[,:]");
            String storageName = split[0];
            String volumeName = split[1];

            StoragePO storage = virtualMachine.obtainHost().findStorageByName(storageName).orElse(null);
            if (storage == null) {
                continue;
            }

            VolumePO volume = storage.findVolumeByName(volumeName).orElse(null);
            if (volume == null) {
                continue;
            }

            String tdev = "sd" + tdevletter;

            VolumeAttachmentPO existingVolumeAttachment = virtualMachine.findVolumeAttachmentByDev(tdev).orElse(null);
            VolumeAttachmentPO volumeAttachment;
            if (existingVolumeAttachment != null) {
                volumeAttachment = existingVolumeAttachment;
                volumeAttachment.setVolume(volume);
            } else {
                volumeAttachment = new VolumeAttachmentPO(virtualMachine, tdev, volume);
            }

            volumeAttachments.add(volumeAttachment);
        }

        virtualMachine.getVolumeAttachments().retainAll(volumeAttachments);
    }

}
