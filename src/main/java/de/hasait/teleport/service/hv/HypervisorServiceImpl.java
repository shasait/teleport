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

package de.hasait.teleport.service.hv;

import de.hasait.common.service.AbstractRefreshableDriverService;
import de.hasait.teleport.api.HostReferenceTO;
import de.hasait.teleport.api.HypervisorReferenceTO;
import de.hasait.teleport.api.VirtualMachineReferenceTO;
import de.hasait.teleport.api.VirtualMachineTO;
import de.hasait.teleport.api.VmCreateNetIfTO;
import de.hasait.teleport.api.VmCreateTO;
import de.hasait.teleport.api.VolumeAttachmentCreateTO;
import de.hasait.teleport.api.VolumeCreateTO;
import de.hasait.teleport.api.VolumeReferenceTO;
import de.hasait.teleport.domain.HasHypervisor;
import de.hasait.teleport.domain.HostPO;
import de.hasait.teleport.domain.HostRepository;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.HypervisorRepository;
import de.hasait.teleport.domain.NetworkInterfacePO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.service.CanResult;
import de.hasait.teleport.service.SnapshotNameGenerator;
import de.hasait.teleport.service.action.ActionService;
import de.hasait.teleport.service.storage.FullSyncVolumeAction;
import de.hasait.teleport.service.storage.StorageService;
import de.hasait.teleport.service.storage.TakeSnapshotAction;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HypervisorServiceImpl extends AbstractRefreshableDriverService<HypervisorDriver, HypervisorPO, HypervisorRepository> implements HypervisorService {

    private final HostRepository hostRepository;
    private final StorageService storageService;
    private final ActionService actionService;

    public HypervisorServiceImpl(HypervisorRepository repository, HypervisorDriver[] drivers, HostRepository hostRepository, StorageService storageService, ActionService actionService) {
        super(HypervisorPO.class, repository, drivers);
        this.hostRepository = hostRepository;
        this.storageService = storageService;
        this.actionService = actionService;
    }

    @Override
    public List<HostReferenceTO> canCreateVm(VmCreateTO vmCreateTO) {
        return List.of(); // TODO implement
    }

    @Override
    public CanResult canCreateVm(HostReferenceTO hostReferenceTO, VmCreateTO vmCreateTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canCreateVm(HostPO host, VmCreateTO vmCreateTO) {
        return null; // TODO implement
    }

    @Override
    public VirtualMachineTO createVm(HostReferenceTO hostReferenceTO, VmCreateTO vmCreateTO) {
        VirtualMachinePO virtualMachine = createVm(findHost(hostReferenceTO).orElseThrow(), vmCreateTO);
        return null; // TODO mapper
    }

    @Override
    public VirtualMachinePO createVm(HostPO host, VmCreateTO vmCreateTO) {
        HypervisorPO hypervisor = host.findHypervisorByName(vmCreateTO.getHypervisorName()).orElseThrow();
        return getProviderByIdNotNull(hypervisor.getDriver()).create(hypervisor, vmCreateTO, false);
    }

    @Override
    public CanResult canStartVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canStartVm(VirtualMachinePO virtualMachine) {
        return null; // TODO implement
    }

    @Override
    public boolean startVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return startVm(findVm(virtualMachineReferenceTO).orElseThrow());
    }

    @Override
    public boolean startVm(VirtualMachinePO virtualMachine) {
        // TODO can
        for (var va : virtualMachine.getVolumeAttachments()) {
            storageService.activateVolume(va.getVolume());
        }
        getProviderByIdNotNull(virtualMachine).start(virtualMachine);
        return true;
    }

    @Override
    public CanResult canShutdownVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canShutdownVm(VirtualMachinePO virtualMachine) {
        return null; // TODO implement
    }

    @Override
    public boolean shutdownVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return shutdownVm(findVm(virtualMachineReferenceTO).orElseThrow());
    }

    @Override
    public boolean shutdownVm(String virtualMachineName) {
        return false; // TODO implement
    }

    @Override
    public boolean shutdownVm(VirtualMachinePO virtualMachine) {
        // TODO can
        getProviderByIdNotNull(virtualMachine).shutdown(virtualMachine);
        for (var va : virtualMachine.getVolumeAttachments()) {
            storageService.deactivateVolume(va.getVolume());
        }
        return true;
    }

    @Override
    public CanResult canKillVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canKillVm(VirtualMachinePO virtualMachine) {
        return null; // TODO implement
    }

    @Override
    public boolean killVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return killVm(findVm(virtualMachineReferenceTO).orElseThrow());
    }

    @Override
    public boolean killVm(VirtualMachinePO virtualMachine) {
        // TODO can
        getProviderByIdNotNull(virtualMachine).kill(virtualMachine);
        return true;
    }

    @Override
    public CanResult canUpdateVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canUpdateVm(VirtualMachinePO virtualMachine) {
        return null; // TODO implement
    }

    @Override
    public boolean updateVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return false; // TODO implement
    }

    @Override
    public boolean updateVm(VirtualMachinePO virtualMachine) {
        return false; // TODO implement
    }

    @Override
    public CanResult canDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canDeleteVm(VirtualMachinePO virtualMachine) {
        return null; // TODO implement
    }

    @Override
    public boolean deleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return false; // TODO implement
    }

    @Override
    public boolean deleteVm(VirtualMachinePO virtualMachine) {
        return false; // TODO implement
    }

    @Override
    public boolean fullDeleteVm(VirtualMachinePO virtualMachine) {
        return false; // TODO implement
    }

    @Override
    public VirtualMachineTO getVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canFullDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public boolean fullDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return false; // TODO implement
    }

    @Override
    public void fullSyncVmToOtherHv(VirtualMachineReferenceTO srcVmTO, HostReferenceTO tgtHostTO) {
        VirtualMachinePO srcVm = findVm(srcVmTO).orElseThrow();
        HypervisorPO tgtHv = repository.findByHostAndName(tgtHostTO.getName(), srcVmTO.getHypervisor().getName()).orElseThrow();

        VirtualMachinePO existingTgtVm = tgtHv.findVirtualMachineByName(srcVm.getName()).orElse(null);
        if (existingTgtVm != null) {
            throw new IllegalArgumentException("Target VM already exists");
        }

        HypervisorDriver tgtHvDriver = getProviderByIdNotNull(tgtHv.getDriver());

        VmCreateTO vmCreateTO = mapToVmCreateTO(srcVm);

        tgtHvDriver.create(tgtHv, vmCreateTO, false);

        VirtualMachinePO tgtVm = tgtHv.findVirtualMachineByName(srcVm.getName()).orElseThrow();

        Map<VolumePO, VolumePO> srcToTgt = new LinkedHashMap<>();
        for (VolumeAttachmentPO srcVa : srcVm.getVolumeAttachments()) {
            VolumeAttachmentPO tgtVa = tgtVm.findVolumeAttachmentByDev(srcVa.getDev()).orElseThrow();
            srcToTgt.put(srcVa.getVolume(), tgtVa.getVolume());
        }

        String snapshotName = SnapshotNameGenerator.create(true);
        actionService.submit(new TakeSnapshotAction(snapshotName, srcToTgt.keySet().stream().map(volume -> new VolumeReferenceTO(volume.obtainHost().getName(), volume.obtainStorage().getName(), volume.getName())).collect(Collectors.toSet())));
        srcToTgt.forEach((srcVolume, tgtVolume) -> actionService.submit(new FullSyncVolumeAction(srcVolume.obtainHost().getName(), srcVolume.obtainStorage().getName(), srcVolume.getName(), snapshotName, tgtVolume.obtainHost().getName(), tgtVolume.obtainStorage().getName(), tgtVolume.getName(), true)));
    }

    private VmCreateTO mapToVmCreateTO(VirtualMachinePO srcVm) {
        VmCreateTO vmCreateTO = new VmCreateTO();
        vmCreateTO.setHypervisorName(srcVm.obtainHypervisor().getName());
        vmCreateTO.setName(srcVm.getName());
        vmCreateTO.setDescription(srcVm.getDescription());
        vmCreateTO.setCores(srcVm.getCores());
        vmCreateTO.setMemMb(srcVm.getMemMb());
        vmCreateTO.setMemHugePages(srcVm.isMemHugePages());
        vmCreateTO.setVideoModel(srcVm.getVideoModel());
        vmCreateTO.setVgaMemKb(srcVm.getVgaMemKb());

        for (VolumeAttachmentPO volumeAttachment : srcVm.getVolumeAttachments()) {
            VolumeAttachmentCreateTO volumeAttachmentCreateTO = new VolumeAttachmentCreateTO();
            volumeAttachmentCreateTO.setType(volumeAttachment.getType());
            volumeAttachmentCreateTO.setDev(volumeAttachment.getDev());

            VolumePO volume = volumeAttachment.getVolume();
            VolumeCreateTO volumeCreateTO = new VolumeCreateTO();
            volumeCreateTO.setStorageName(volume.getStorage().getName());
            volumeCreateTO.setName(volume.getName());
            volumeCreateTO.setSizeBytes(volume.getSizeBytes());

            volumeAttachmentCreateTO.setVolume(volumeCreateTO);
            vmCreateTO.getVolumeAttachments().add(volumeAttachmentCreateTO);
        }
        for (NetworkInterfacePO networkInterface : srcVm.getNetworkInterfaces()) {
            VmCreateNetIfTO netIfTO = new VmCreateNetIfTO();
            netIfTO.setModel(networkInterface.getModel());
            netIfTO.setName(networkInterface.getName());
            netIfTO.setMac(networkInterface.getMac());
            netIfTO.setIpv4(networkInterface.getIpv4());
            netIfTO.setIpv6(networkInterface.getIpv6());
            netIfTO.setVlan(networkInterface.getNetwork().getVlan());
            netIfTO.setTrunk(networkInterface.isTrunk());
            vmCreateTO.getNetworkInterfaces().add(netIfTO);
        }

        return vmCreateTO;
    }

    private Optional<VirtualMachinePO> findVm(VirtualMachineReferenceTO vm) {
        HypervisorReferenceTO hypervisor = vm.getHypervisor();
        HostReferenceTO host = hypervisor.getHost();
        return findVm(host.getName(), hypervisor.getName(), vm.getName());
    }

    private Optional<VirtualMachinePO> findVm(String hostName, String hvName, String vmName) {
        return repository.findByHostAndName(hostName, hvName).flatMap(hv -> hv.findVirtualMachineByName(vmName));
    }

    private Optional<HostPO> findHost(HostReferenceTO to) {
        return hostRepository.findByName(to.getName());
    }

    private HypervisorDriver getProviderByIdNotNull(HasHypervisor hasHypervisor) {
        return getProviderByIdNotNull(hasHypervisor.obtainHypervisor().getDriver());
    }

}
