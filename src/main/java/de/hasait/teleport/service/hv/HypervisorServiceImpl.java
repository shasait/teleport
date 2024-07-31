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
import de.hasait.teleport.api.VmCreateNetIfTO;
import de.hasait.teleport.api.VmCreateTO;
import de.hasait.teleport.api.VmCreateVolumeTO;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.HypervisorRepository;
import de.hasait.teleport.domain.NetworkInterfacePO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HypervisorServiceImpl extends AbstractRefreshableDriverService<HypervisorDriver, HypervisorPO, HypervisorRepository> implements HypervisorService {

    public HypervisorServiceImpl(HypervisorRepository repository, HypervisorDriver[] drivers) {
        super(HypervisorPO.class, repository, drivers);
    }

    @Override
    public void startVm(String hostName, String hvName, String vmName) {
        VirtualMachinePO vm = findVm(hostName, hvName, vmName).orElseThrow();
        getProviderByIdNotNull(vm).start(vm);
    }

    @Override
    public void shutdownVm(String hostName, String hvName, String vmName) {
        VirtualMachinePO vm = findVm(hostName, hvName, vmName).orElseThrow();
        getProviderByIdNotNull(vm).shutdown(vm);
    }

    @Override
    public void killVm(String hostName, String hvName, String vmName) {
        VirtualMachinePO vm = findVm(hostName, hvName, vmName).orElseThrow();
        getProviderByIdNotNull(vm).kill(vm);
    }

    @Override
    public void fullSyncVmToOtherHv(String srcHostName, String srcHvName, String srcVmName, String tgtHostName) {
        HypervisorPO srcHv = repository.findByHostAndName(srcHostName, srcHvName).orElseThrow();
        VirtualMachinePO srcVm = srcHv.findVirtualMachineByName(srcVmName).orElseThrow();

        HypervisorPO tgtHv = repository.findByHostAndName(tgtHostName, srcHvName).orElseThrow();
        VirtualMachinePO existingTgtVm = tgtHv.findVirtualMachineByName(srcVmName).orElse(null);
        if (existingTgtVm != null) {
            throw new IllegalArgumentException("Target VM already exists");
        }

        HypervisorDriver tgtHvDriver = getProviderByIdNotNull(tgtHv.getDriver());

        VmCreateTO vmCreateTO = mapToVmCreateTO(srcVm);

        tgtHvDriver.create(tgtHv, vmCreateTO, false);

        VirtualMachinePO tgtVm = tgtHv.findVirtualMachineByName(srcVmName).orElseThrow();
        // TODO submit actions for volume syncing
    }

    private VmCreateTO mapToVmCreateTO(VirtualMachinePO srcVm) {
        VmCreateTO vmCreateTO = new VmCreateTO();
        vmCreateTO.setName(srcVm.getName());
        vmCreateTO.setDescription(srcVm.getDescription());
        vmCreateTO.setCores(srcVm.getCores());
        vmCreateTO.setMemMb(srcVm.getMemMb());
        vmCreateTO.setMemHugePages(srcVm.isMemHugePages());
        vmCreateTO.setVideoModel(srcVm.getVideoModel());
        vmCreateTO.setVgaMemKb(srcVm.getVgaMemKb());

        for (VolumeAttachmentPO volumeAttachment : srcVm.getVolumeAttachments()) {
            VmCreateVolumeTO volumeTO = new VmCreateVolumeTO();
            volumeTO.setDev(volumeAttachment.getDev());
            volumeTO.setName(volumeAttachment.getVolume().getName());
            volumeTO.setSizeBytes(volumeAttachment.getVolume().getSizeBytes());
            vmCreateTO.getVolumes().add(volumeTO);
        }
        for (NetworkInterfacePO networkInterface : srcVm.getNetworkInterfaces()) {
            VmCreateNetIfTO netIfTO = new VmCreateNetIfTO();
            netIfTO.setModel(networkInterface.getModel());
            netIfTO.setName(networkInterface.getName());
            netIfTO.setMac(networkInterface.getMac());
            netIfTO.setIpv4(networkInterface.getIpv4());
            netIfTO.setIpv6(networkInterface.getIpv6());
            netIfTO.setVlan(networkInterface.getNetwork().getVlan());
            vmCreateTO.getNetworkInterfaces().add(netIfTO);
        }

        return vmCreateTO;
    }

    private Optional<VirtualMachinePO> findVm(String hostName, String hvName, String vmName) {
        return repository.findByHostAndName(hostName, hvName).flatMap(hv -> hv.findVirtualMachineByName(vmName));
    }

    private HypervisorDriver getProviderByIdNotNull(VirtualMachinePO vm) {
        return getProviderByIdNotNull(vm.getHypervisor().getDriver());
    }

}
