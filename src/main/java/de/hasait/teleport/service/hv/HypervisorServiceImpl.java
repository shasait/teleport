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
import de.hasait.teleport.api.VmState;
import de.hasait.teleport.api.VolumeAttachmentCreateTO;
import de.hasait.teleport.api.VolumeReferenceTO;
import de.hasait.teleport.domain.HasHypervisor;
import de.hasait.teleport.domain.HostPO;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.HypervisorRepository;
import de.hasait.teleport.domain.NetworkInterfacePO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.service.CanResult;
import de.hasait.teleport.service.SnapshotNameGenerator;
import de.hasait.teleport.service.action.ActionService;
import de.hasait.teleport.service.mapper.AllMapper;
import de.hasait.teleport.service.storage.FullSyncVolumeAction;
import de.hasait.teleport.service.storage.StorageService;
import de.hasait.teleport.service.storage.TakeSnapshotAction;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HypervisorServiceImpl extends AbstractRefreshableDriverService<HypervisorDriver, HypervisorPO, HypervisorRepository> implements HypervisorService {

    private final VirtualMachineRepository virtualMachineRepository;
    private final AllMapper allMapper;
    private final ActionService actionService;
    private final StorageService storageService;

    public HypervisorServiceImpl(HypervisorRepository repository, HypervisorDriver[] drivers, VirtualMachineRepository virtualMachineRepository, AllMapper allMapper, ActionService actionService, StorageService storageService) {
        super(HypervisorPO.class, repository, drivers);
        this.virtualMachineRepository = virtualMachineRepository;
        this.allMapper = allMapper;
        this.actionService = actionService;
        this.storageService = storageService;
    }

    @Override
    public List<HostReferenceTO> canCreateVm(VmCreateTO vmCreateTO) {
        List<HostReferenceTO> result = new ArrayList<>();
        for (HypervisorPO hypervisor : repository.findAll()) {
            if (hypervisor.getName().equals(vmCreateTO.getHypervisorName())) {
                HostPO host = hypervisor.obtainHost();
                if (canCreateVm(host, vmCreateTO).isValidWithEffect()) {
                    result.add(new HostReferenceTO(host.getName()));
                }
            }
        }
        return result;
    }

    @Override
    public CanResult canCreateVm(HostReferenceTO hostReferenceTO, VmCreateTO vmCreateTO) {
        return allMapper.canFindHost(hostReferenceTO).merge(canResult -> canCreateVm(canResult.getContextNotNull(HostPO.class), vmCreateTO));
    }

    @Override
    public CanResult canCreateVm(HostPO host, VmCreateTO vmCreateTO) {
        CanResult canFindHypervisor = allMapper.canFindHypervisor(new HypervisorReferenceTO(host.getName(), vmCreateTO.getHypervisorName()));
        if (!canFindHypervisor.isValid()) {
            return canFindHypervisor;
        }

        HypervisorPO hypervisor = canFindHypervisor.getContextNotNull(HypervisorPO.class);

        return hypervisor.findVirtualMachineByName(vmCreateTO.getName()) //
                .map(virtualMachinePO -> CanResult.invalid("VM " + virtualMachinePO.toFqName() + " already exists")) //
                .orElseGet(() -> CanResult.valid().putContext(HypervisorPO.class, hypervisor)) //
                ;
    }

    @Override
    public VirtualMachineTO createVm(HostReferenceTO hostReferenceTO, VmCreateTO vmCreateTO) {
        CanResult canResult = canCreateVm(hostReferenceTO, vmCreateTO);
        VirtualMachinePO vm = createVm(canResult, vmCreateTO);
        return allMapper.mapToVirtualMachineTO(vm);
    }

    @Override
    public VirtualMachinePO createVm(HostPO host, VmCreateTO vmCreateTO) {
        CanResult canResult = canCreateVm(host, vmCreateTO);
        return createVm(canResult, vmCreateTO);
    }

    private VirtualMachinePO createVm(CanResult canResult, VmCreateTO vmCreateTO) {
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return canResult.getContextNotNull(VirtualMachinePO.class);
        }

        HypervisorPO hypervisor = canResult.getContextNotNull(HypervisorPO.class);
        return getProviderByIdNotNull(hypervisor.getDriver()).create(hypervisor, vmCreateTO, false);
    }

    @Override
    public VirtualMachineTO getVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        VirtualMachinePO virtualMachine = allMapper.findVm(virtualMachineReferenceTO).orElseThrow();
        return allMapper.mapToVirtualMachineTO(virtualMachine);
    }

    @Override
    public CanResult canStartVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return allMapper.canFindVm(virtualMachineReferenceTO).merge(canResult -> canStartVm(canResult.getContextNotNull(VirtualMachinePO.class)));
    }

    @Override
    public CanResult canStartVm(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsRunning()) {
            return CanResult.hasNoEffect("VM " + virtualMachine + " already running");
        }

        Optional<VirtualMachinePO> otherActive = virtualMachineRepository.findOthers(virtualMachine.obtainHost().getName(), virtualMachine.obtainHypervisor().getName(), virtualMachine.getName()).stream().filter(VirtualMachinePO::stateIsNotShutOff).findAny();
        if (otherActive.isPresent()) {
            return CanResult.invalid("VM active on other host: " + otherActive.get());
        }

        CanResult current = CanResult.valid();
        for (VolumeAttachmentPO va : virtualMachine.getVolumeAttachments()) {
            current = current.merge(canResult -> storageService.canActivateVolume(va.getVolume()));
        }
        if (current.isValidWithEffect()) {
            current.putContext(VirtualMachinePO.class, virtualMachine);
        }
        return current;
    }

    @Override
    public boolean startVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        CanResult canResult = canStartVm(virtualMachineReferenceTO);
        return startVm(canResult);
    }

    @Override
    public boolean startVm(VirtualMachinePO virtualMachine) {
        CanResult canResult = canStartVm(virtualMachine);
        return startVm(canResult);
    }

    private boolean startVm(CanResult canResult) {
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        VirtualMachinePO virtualMachine = canResult.getContextNotNull(VirtualMachinePO.class);

        for (var va : virtualMachine.getVolumeAttachments()) {
            storageService.activateVolume(va.getVolume());
        }
        getProviderByIdNotNull(virtualMachine).start(virtualMachine);
        return true;
    }

    @Override
    public CanResult canShutdownVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return allMapper.canFindVm(virtualMachineReferenceTO).merge(canResult -> canShutdownVm(canResult.getContextNotNull(VirtualMachinePO.class)));
    }

    @Override
    public CanResult canShutdownVm(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsShutOff()) {
            return CanResult.hasNoEffect("VM " + virtualMachine + " already shut off");
        }

        virtualMachine.pushTransientState(VmState.SHUTOFF);
        try {
            CanResult current = CanResult.valid();
            for (VolumeAttachmentPO va : virtualMachine.getVolumeAttachments()) {
                current = current.merge(canResult -> storageService.canDeactivateVolume(va.getVolume()));
            }
            if (current.isValidWithEffect()) {
                current.putContext(VirtualMachinePO.class, virtualMachine);
            }
            return current;
        } finally {
            virtualMachine.popTransientState();
        }
    }

    @Override
    public boolean shutdownVm(String virtualMachineName) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean shutdownVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        CanResult canResult = canShutdownVm(virtualMachineReferenceTO);
        return shutdownVm(canResult);
    }

    @Override
    public boolean shutdownVm(VirtualMachinePO virtualMachine) {
        CanResult canResult = canShutdownVm(virtualMachine);
        return shutdownVm(canResult);
    }

    private boolean shutdownVm(CanResult canResult) {
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        VirtualMachinePO virtualMachine = canResult.getContextNotNull(VirtualMachinePO.class);

        getProviderByIdNotNull(virtualMachine).shutdown(virtualMachine);
        for (var va : virtualMachine.getVolumeAttachments()) {
            storageService.deactivateVolume(va.getVolume());
        }
        return true;
    }

    @Override
    public CanResult canKillVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return allMapper.canFindVm(virtualMachineReferenceTO).merge(canResult -> canKillVm(canResult.getContextNotNull(VirtualMachinePO.class)));
    }

    @Override
    public CanResult canKillVm(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean killVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return killVm(allMapper.findVm(virtualMachineReferenceTO).orElseThrow());
    }

    @Override
    public boolean killVm(VirtualMachinePO virtualMachine) {
        // TODO can
        getProviderByIdNotNull(virtualMachine).kill(virtualMachine);
        return true;
    }

    @Override
    public CanResult canUpdateVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return allMapper.canFindVm(virtualMachineReferenceTO).merge(canResult -> canUpdateVm(canResult.getContextNotNull(VirtualMachinePO.class)));
    }

    @Override
    public CanResult canUpdateVm(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean updateVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean updateVm(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public CanResult canDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return allMapper.canFindVm(virtualMachineReferenceTO).merge(canResult -> canDeleteVm(canResult.getContextNotNull(VirtualMachinePO.class)));
    }

    @Override
    public CanResult canDeleteVm(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean deleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean deleteVm(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public CanResult canFullDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        return allMapper.canFindVm(virtualMachineReferenceTO).merge(canResult -> canFullDeleteVm(canResult.getContextNotNull(VirtualMachinePO.class)));
    }

    @Override
    public CanResult canFullDeleteVm(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean fullDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean fullDeleteVm(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public void fullSyncVmToOtherHv(VirtualMachineReferenceTO srcVmTO, HostReferenceTO tgtHostTO) {
        VirtualMachinePO srcVm = allMapper.findVm(srcVmTO).orElseThrow();
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
            volumeAttachmentCreateTO.setStorageName(volume.getStorage().getName());
            volumeAttachmentCreateTO.setSizeBytes(volume.getSizeBytes());

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

    private HypervisorDriver getProviderByIdNotNull(HasHypervisor hasHypervisor) {
        return getProviderByIdNotNull(hasHypervisor.obtainHypervisor().getDriver());
    }

}
