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

package de.hasait.teleport.service.mapper;

import de.hasait.teleport.api.HostReferenceTO;
import de.hasait.teleport.api.HypervisorReferenceTO;
import de.hasait.teleport.api.StorageReferenceTO;
import de.hasait.teleport.api.VirtualMachineReferenceTO;
import de.hasait.teleport.api.VirtualMachineTO;
import de.hasait.teleport.api.VolumeReferenceTO;
import de.hasait.teleport.api.VolumeSnapshotReferenceTO;
import de.hasait.teleport.api.VolumeTO;
import de.hasait.teleport.domain.HostPO;
import de.hasait.teleport.domain.HostRepository;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.HypervisorRepository;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.StorageRepository;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeSnapshotPO;
import de.hasait.teleport.service.CanResult;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AllMapper {

    private final HostRepository hostRepository;
    private final StorageRepository storageRepository;
    private final HypervisorRepository hypervisorRepository;

    public AllMapper(HostRepository hostRepository, StorageRepository storageRepository, HypervisorRepository hypervisorRepository) {
        this.hostRepository = hostRepository;
        this.storageRepository = storageRepository;
        this.hypervisorRepository = hypervisorRepository;
    }

    public Optional<HostPO> findHost(HostReferenceTO to) {
        return hostRepository.findByName(to.getName());
    }

    public CanResult canFindHost(HostReferenceTO to) {
        return findHost(to).map(po -> CanResult.valid().putContext(HostPO.class, po)).orElseGet(() -> CanResult.invalid("Host " + to.toFqName() + " does not exist"));
    }

    public Optional<StoragePO> findStorage(StorageReferenceTO to) {
        return storageRepository.findByHostAndName(to.getHost().getName(), to.getName());
    }

    public CanResult canFindStorage(StorageReferenceTO to) {
        return findStorage(to).map(po -> CanResult.valid().putContext(StoragePO.class, po)).orElseGet(() -> CanResult.invalid("Storage " + to.toFqName() + " does not exist"));
    }

    public Optional<VolumePO> findVolume(VolumeReferenceTO to) {
        return findStorage(to.getStorage()).flatMap(s -> s.findVolumeByName(to.getName()));
    }

    public CanResult canFindVolume(VolumeReferenceTO to) {
        return findVolume(to).map(po -> CanResult.valid().putContext(VolumePO.class, po)).orElseGet(() -> CanResult.invalid("Volume " + to.toFqName() + " does not exist"));
    }

    public Optional<VolumeSnapshotPO> findVolumeSnapshot(VolumeSnapshotReferenceTO to) {
        return findVolume(to.getVolume()).flatMap(v -> v.findSnapshotByName(to.getName()));
    }

    public CanResult canFindVolumeSnapshot(VolumeSnapshotReferenceTO to) {
        return findVolumeSnapshot(to).map(po -> CanResult.valid().putContext(VolumeSnapshotPO.class, po)).orElseGet(() -> CanResult.invalid("VolumeSnapshot " + to.toFqName() + " does not exist"));
    }

    public Optional<HypervisorPO> findHypervisor(HypervisorReferenceTO to) {
        return hypervisorRepository.findByHostAndName(to.getHost().getName(), to.getName());
    }

    public CanResult canFindHypervisor(HypervisorReferenceTO to) {
        return findHypervisor(to).map(po -> CanResult.valid().putContext(HypervisorPO.class, po)).orElseGet(() -> CanResult.invalid("Hypervisor " + to.toFqName() + " does not exist"));
    }

    public Optional<VirtualMachinePO> findVm(VirtualMachineReferenceTO vm) {
        HypervisorReferenceTO hypervisor = vm.getHypervisor();
        HostReferenceTO host = hypervisor.getHost();
        return findVm(host.getName(), hypervisor.getName(), vm.getName());
    }

    public Optional<VirtualMachinePO> findVm(String hostName, String hvName, String vmName) {
        return hypervisorRepository.findByHostAndName(hostName, hvName).flatMap(hv -> hv.findVirtualMachineByName(vmName));
    }

    public CanResult canFindVm(VirtualMachineReferenceTO to) {
        return findVm(to).map(po -> CanResult.valid().putContext(VirtualMachinePO.class, po)).orElseGet(() -> CanResult.invalid("VirtualMachine " + to.toFqName() + " does not exist"));
    }

    public VolumeTO mapToVolumeTO(VolumePO volume) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    public VirtualMachineTO mapToVirtualMachineTO(VirtualMachinePO virtualMachine) {
        throw new RuntimeException("NYI"); // TODO implement
    }

}
