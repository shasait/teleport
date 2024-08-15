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

package de.hasait.teleport.service.storage;

import de.hasait.common.service.AbstractRefreshableDriverService;
import de.hasait.teleport.api.HostReferenceTO;
import de.hasait.teleport.api.StorageReferenceTO;
import de.hasait.teleport.api.VolumeCreateTO;
import de.hasait.teleport.api.VolumeReferenceTO;
import de.hasait.teleport.api.VolumeSnapshotReferenceTO;
import de.hasait.teleport.api.VolumeTO;
import de.hasait.teleport.domain.HasStorage;
import de.hasait.teleport.domain.HostPO;
import de.hasait.teleport.domain.HostRepository;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.StorageRepository;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeSnapshotPO;
import de.hasait.teleport.service.CanResult;
import de.hasait.teleport.spi.storage.StorageDriver;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class StorageServiceImpl extends AbstractRefreshableDriverService<StorageDriver, StoragePO, StorageRepository> implements StorageService {

    public static final int STORAGE_REFRESH_PRIORITY = 10000;

    private final HostRepository hostRepository;

    public StorageServiceImpl(StorageRepository repository, StorageDriver[] drivers, HostRepository hostRepository) {
        super(StoragePO.class, repository, drivers);
        this.hostRepository = hostRepository;
    }

    @Override
    public int refreshPriority() {
        return STORAGE_REFRESH_PRIORITY;
    }

    @Override
    public List<HostReferenceTO> canCreateVolume(VolumeCreateTO volumeCreateTO) {
        return List.of(); // TODO implement
    }

    @Override
    public CanResult canCreateVolume(HostReferenceTO hostReferenceTO, VolumeCreateTO volumeCreateTO) {
        return canCreateVolume(findHost(hostReferenceTO).orElseThrow(), volumeCreateTO);
    }

    @Override
    public CanResult canCreateVolume(HostPO host, VolumeCreateTO volumeCreateTO) {
        return null; // TODO implement
    }

    @Override
    public VolumeTO createVolume(HostReferenceTO hostReferenceTO, VolumeCreateTO volumeCreateTO) {
        VolumePO volume = createVolume(findHost(hostReferenceTO).orElseThrow(), volumeCreateTO);
        return null; // TODO mapper
    }

    @Override
    public VolumePO createVolume(HostPO host, VolumeCreateTO volumeCreateTO) {
        StoragePO storage = host.findStorageByName(volumeCreateTO.getStorageName()).orElseThrow();
        return getProviderByIdNotNull(storage.getDriver()).create(storage, volumeCreateTO);
    }

    @Override
    public CanResult canActivateVolume(VolumeReferenceTO volumeReferenceTO) {
        return canActivateVolume(findVolume(volumeReferenceTO).orElseThrow());
    }

    @Override
    public CanResult canActivateVolume(VolumePO volume) {
        if (volume.stateIsActive()) {
            return CanResult.hasNoEffect("Volume " + volume + " already active");
        }

        return CanResult.valid();
    }

    @Override
    public boolean activateVolume(VolumeReferenceTO volumeReferenceTO) {
        return activateVolume(findVolume(volumeReferenceTO).orElseThrow());
    }

    @Override
    public boolean activateVolume(VolumePO volume) {
        CanResult canResult = canActivateVolume(volume);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        getProviderByIdNotNull(volume).activate(volume);
        return true;
    }

    @Override
    public CanResult canDeactivateVolume(VolumeReferenceTO volumeReferenceTO) {
        return canDeactivateVolume(findVolume(volumeReferenceTO).orElseThrow());
    }

    @Override
    public CanResult canDeactivateVolume(VolumePO volume) {
        if (volume.stateIsDirtyOrInactive()) {
            return CanResult.hasNoEffect("Volume " + volume + " already non-active");
        }

        if (volume.getVolumeAttachments().stream().anyMatch(va -> va.getVirtualMachine().stateIsNotShutOff())) {
            return CanResult.invalid("Volume " + volume + " in use by VM");
        }

        return CanResult.valid();
    }

    @Override
    public boolean deactivateVolume(VolumeReferenceTO volumeReferenceTO) {
        return deactivateVolume(findVolume(volumeReferenceTO).orElseThrow());
    }

    @Override
    public boolean deactivateVolume(VolumePO volume) {
        CanResult canResult = canDeactivateVolume(volume);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        getProviderByIdNotNull(volume).deactivate(volume);
        return true;
    }

    @Override
    public CanResult canDeleteVolume(VolumeReferenceTO volumeReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canDeleteVolume(VolumePO volume) {
        return null; // TODO implement
    }

    @Override
    public boolean deleteVolume(VolumeReferenceTO volumeReferenceTO) {
        return false; // TODO implement
    }

    @Override
    public boolean deleteVolume(VolumePO volume) {
        return false; // TODO implement
    }

    @Override
    public VolumeTO getVolume(VolumeReferenceTO volumeReferenceTO) {
        return null; // TODO implement
    }

    @Override
    public CanResult canTakeSnapshot(String snapshotName, VolumeReferenceTO... volumes) {
        return canTakeSnapshot(snapshotName, Arrays.stream(volumes).map(v -> findVolume(v).orElseThrow()).toArray(VolumePO[]::new));
    }

    @Override
    public CanResult canTakeSnapshot(String snapshotName, VolumePO... volumes) {
        StoragePO storage = null;
        int totalCount = 0;
        int existCount = 0;
        for (var volume : volumes) {
            totalCount++;
            if (storage == null) {
                storage = volume.getStorage();
            } else {
                if (storage != volume.getStorage()) {
                    return CanResult.invalid("Cannot take atomic snapshot for multiple storages: " + storage + " vs. " + volume.getStorage());
                }
            }
            if (volume.findSnapshotByName(snapshotName).isPresent()) {
                existCount++;
            }
        }

        if (storage == null) {
            return CanResult.invalid("No volumes provided");
        }

        if (existCount > 0) {
            if (totalCount != existCount) {
                return CanResult.invalid("Cannot take atomic snapshot if some volumes already have the snapshot");
            } else {
                return CanResult.hasNoEffect("Snapshot " + snapshotName + " already exist");
            }
        }

        return CanResult.valid().putContext(StoragePO.class, storage);
    }

    @Override
    public boolean takeSnapshot(String snapshotName, VolumeReferenceTO... volumes) {
        return takeSnapshot(snapshotName, Arrays.stream(volumes).map(v -> findVolume(v).orElseThrow()).toArray(VolumePO[]::new));
    }

    @Override
    public boolean takeSnapshot(String snapshotName, VolumePO... volumes) {
        CanResult canResult = canTakeSnapshot(snapshotName, volumes);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        StoragePO storage = canResult.getContext(StoragePO.class);
        getProviderByIdNotNull(storage.getDriver()).takeSnapshot(snapshotName, volumes);
        return true;
    }

    @Override
    public CanResult canFullSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeReferenceTO tgtVolumeTO, boolean replaceExisting) {
        return null; // TODO implement
    }

    @Override
    public CanResult canFullSync(VolumeSnapshotPO srcVolumeSnapshot, StoragePO tgtStorage, String volumeName, boolean replaceExisting) {
        return null; // TODO implement
    }

    @Override
    public boolean fullSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeReferenceTO tgtVolumeTO, boolean replaceExisting) {
        return fullSync(findVolumeSnapshot(srcVolumeSnapshotTO).orElseThrow(), findStorage(tgtVolumeTO.getStorage()).orElseThrow(), tgtVolumeTO.getName(), replaceExisting);
    }

    @Override
    public boolean fullSync(VolumeSnapshotPO srcVolumeSnapshot, StoragePO tgtStorage, String volumeName, boolean replaceExisting) {
        if (srcVolumeSnapshot.obtainStorage().getDriver().equals(tgtStorage.getDriver())) {
            getProviderByIdNotNull(srcVolumeSnapshot.obtainStorage().getDriver()).syncVolumeFull(srcVolumeSnapshot, tgtStorage, volumeName, replaceExisting);
            return true;
        } else {
            // TODO generic sync via dd and ssh
            throw new RuntimeException("NYI");
        }
    }

    private Optional<VolumeSnapshotPO> findVolumeSnapshot(VolumeSnapshotReferenceTO to) {
        return findVolume(to.getVolume()).flatMap(v -> v.findSnapshotByName(to.getName()));
    }

    private Optional<VolumePO> findVolume(VolumeReferenceTO to) {
        return findStorage(to.getStorage()).flatMap(s -> s.findVolumeByName(to.getName()));
    }

    private Optional<StoragePO> findStorage(StorageReferenceTO to) {
        return repository.findByHostAndName(to.getHost().getName(), to.getName());
    }

    private Optional<HostPO> findHost(HostReferenceTO to) {
        return hostRepository.findByName(to.getName());
    }

    private StorageDriver getProviderByIdNotNull(HasStorage hasStorage) {
        return getProviderByIdNotNull(hasStorage.obtainStorage().getDriver());
    }

}
