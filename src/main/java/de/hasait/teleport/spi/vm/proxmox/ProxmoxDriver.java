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
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.teleport.CliConfig;
import de.hasait.teleport.api.VirtualMachineCreateTO;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ProxmoxDriver implements HypervisorDriver {

    public static final String DRIVER_ID = "proxmox";

    private static ProxmoxDriverConfig parseConfig(String config) {
        ProxmoxDriverConfig driverConfig = new Gson().fromJson(config, ProxmoxDriverConfig.class);
        return driverConfig;
    }

    private static ProxmoxDriverConfig parseConfig(HypervisorPO hypervisor) {
        if (DRIVER_ID.equals(hypervisor.getDriver())) {
            return parseConfig(hypervisor.getDriverConfig());
        }
        throw new IllegalArgumentException("Not a proxmox hypervisor: " + hypervisor);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CliConfig cliConfig;
    private final VolumeRepository volumeRepository;
    private final VirtualMachineRepository virtualMachineRepository;

    public ProxmoxDriver(@Autowired CliConfig cliConfig, VolumeRepository volumeRepository, VirtualMachineRepository virtualMachineRepository) {
        this.cliConfig = cliConfig;
        this.volumeRepository = volumeRepository;
        this.virtualMachineRepository = virtualMachineRepository;
    }

    @Override
    @Nonnull
    public String getId() {
        return DRIVER_ID;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Virsh Hypervisor Driver";
    }

    @Nullable
    @Override
    public String getDisabledReason() {
        return null;
    }

    @Nullable
    @Override
    public String validateConfig(@Nullable String config) {
        try {
            parseConfig(config);
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    @Override
    public void refresh(HypervisorPO hypervisor) {
        ProxmoxDriverConfig driverConfig = parseConfig(hypervisor);
        CliExecutor exe = cliConfig.createCliConnector(hypervisor);
        ProxmoxUtils.qmListFull(exe).forEach(it -> processListEntry(hypervisor, driverConfig, exe, it));
        hypervisor.setLastSeen(LocalDateTime.now());
    }

    @Override
    public void create(HypervisorPO hypervisor, VirtualMachineCreateTO config, boolean runInstallation) {

    }

    @Override
    public void start(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void shutdown(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void kill(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void update(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void delete(VirtualMachinePO virtualMachine) {

    }

    private void processListEntry(HypervisorPO hypervisor, ProxmoxDriverConfig driverConfig, CliExecutor exe, QmListE entry) {
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
