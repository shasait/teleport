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
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.StorageRepository;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeSnapshotPO;
import de.hasait.teleport.service.CanResult;
import de.hasait.teleport.service.mapper.AllMapper;
import de.hasait.teleport.spi.storage.StorageDriver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StorageServiceImpl extends AbstractRefreshableDriverService<StorageDriver, StoragePO, StorageRepository> implements StorageService {

    public static final int STORAGE_REFRESH_PRIORITY = 10000;

    private final AllMapper allMapper;

    public StorageServiceImpl(StorageRepository repository, StorageDriver[] drivers, AllMapper allMapper) {
        super(StoragePO.class, repository, drivers);
        this.allMapper = allMapper;
    }

    @Override
    public int refreshPriority() {
        return STORAGE_REFRESH_PRIORITY;
    }

    @Override
    public List<HostReferenceTO> canCreateVolume(VolumeCreateTO volumeCreateTO) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public CanResult canCreateVolume(HostReferenceTO hostReferenceTO, VolumeCreateTO volumeCreateTO) {
        return allMapper.canFindHost(hostReferenceTO).merge(canResult -> canCreateVolume(canResult.getContextNotNull(HostPO.class), volumeCreateTO));
    }

    @Override
    public CanResult canCreateVolume(HostPO host, VolumeCreateTO volumeCreateTO) {
        CanResult canFindStorage = allMapper.canFindStorage(new StorageReferenceTO(host.getName(), volumeCreateTO.getStorageName()));
        if (!canFindStorage.isValid()) {
            return canFindStorage;
        }

        StoragePO storage = canFindStorage.getContextNotNull(StoragePO.class);
        return storage.findVolumeByName(volumeCreateTO.getName()) //
                .map(volumePO -> CanResult.invalid("Volume " + volumePO.toFqName() + " already exists")) //
                .orElseGet(() -> CanResult.valid().putContext(StoragePO.class, storage)) //
                ;
    }

    @Override
    public VolumeTO createVolume(HostReferenceTO hostReferenceTO, VolumeCreateTO volumeCreateTO) {
        CanResult canResult = canCreateVolume(hostReferenceTO, volumeCreateTO);
        VolumePO volume = createVolume(canResult, volumeCreateTO);
        return allMapper.mapToVolumeTO(volume);
    }

    @Override
    public VolumePO createVolume(HostPO host, VolumeCreateTO volumeCreateTO) {
        CanResult canResult = canCreateVolume(host, volumeCreateTO);
        return createVolume(canResult, volumeCreateTO);
    }

    private VolumePO createVolume(CanResult canResult, VolumeCreateTO volumeCreateTO) {
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return canResult.getContextNotNull(VolumePO.class);
        }

        StoragePO storage = canResult.getContextNotNull(StoragePO.class);
        return getProviderByIdNotNull(storage.getDriver()).create(storage, volumeCreateTO);
    }

    @Override
    public CanResult canActivateVolume(VolumeReferenceTO volumeReferenceTO) {
        return allMapper.canFindVolume(volumeReferenceTO).merge(canResult -> canActivateVolume(canResult.getContextNotNull(VolumePO.class)));
    }

    @Override
    public CanResult canActivateVolume(VolumePO volume) {
        if (volume.stateIsActive()) {
            return CanResult.hasNoEffect("Volume " + volume + " already active");
        }

        return CanResult.valid().putContext(VolumePO.class, volume);
    }

    @Override
    public boolean activateVolume(VolumeReferenceTO volumeReferenceTO) {
        CanResult canResult = canActivateVolume(volumeReferenceTO);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        VolumePO volume = canResult.getContextNotNull(VolumePO.class);
        getProviderByIdNotNull(volume).activate(volume);
        return true;
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
        return allMapper.canFindVolume(volumeReferenceTO).merge(canResult -> canDeactivateVolume(canResult.getContextNotNull(VolumePO.class)));
    }

    @Override
    public CanResult canDeactivateVolume(VolumePO volume) {
        if (volume.stateIsDirtyOrInactive()) {
            return CanResult.hasNoEffect("Volume " + volume + " already non-active");
        }

        if (volume.getVolumeAttachments().stream().anyMatch(va -> va.getVirtualMachine().stateIsNotShutOff())) {
            return CanResult.invalid("Volume " + volume + " in use by VM");
        }

        return CanResult.valid().putContext(VolumePO.class, volume);
    }

    @Override
    public boolean deactivateVolume(VolumeReferenceTO volumeReferenceTO) {
        CanResult canResult = canDeactivateVolume(volumeReferenceTO);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        VolumePO volume = canResult.getContextNotNull(VolumePO.class);
        getProviderByIdNotNull(volume).deactivate(volume);
        return true;
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
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public CanResult canDeleteVolume(VolumePO volume) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean deleteVolume(VolumeReferenceTO volumeReferenceTO) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean deleteVolume(VolumePO volume) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public VolumeTO getVolume(VolumeReferenceTO volumeReferenceTO) {
        CanResult canResult = allMapper.canFindVolume(volumeReferenceTO);
        canResult.ensureValidAndReturnHasNoEffect();
        return allMapper.mapToVolumeTO(canResult.getContextNotNull(VolumePO.class));
    }

    @Override
    public CanResult canTakeSnapshot(String snapshotName, VolumeReferenceTO... volumeTOs) {
        List<VolumePO> volumes = new ArrayList<>();
        for (VolumeReferenceTO volumeTO : volumeTOs) {
            CanResult canResult = allMapper.canFindVolume(volumeTO);
            if (!canResult.isValid()) {
                return canResult;
            }
            volumes.add(canResult.getContextNotNull(VolumePO.class));
        }
        return canTakeSnapshot(snapshotName, volumes.toArray(VolumePO[]::new));
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

        return CanResult.valid().putContext(StoragePO.class, storage).putContext(VolumePO[].class, volumes);
    }

    @Override
    public boolean takeSnapshot(String snapshotName, VolumeReferenceTO... volumeTOs) {
        CanResult canResult = canTakeSnapshot(snapshotName, volumeTOs);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        StoragePO storage = canResult.getContext(StoragePO.class);
        getProviderByIdNotNull(storage.getDriver()).takeSnapshot(snapshotName, canResult.getContextNotNull(VolumePO[].class));
        return true;
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
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public CanResult canFullSync(VolumeSnapshotPO srcVolumeSnapshot, StoragePO tgtStorage, String volumeName, boolean replaceExisting) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean fullSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeReferenceTO tgtVolumeTO, boolean replaceExisting) {
        return fullSync(allMapper.findVolumeSnapshot(srcVolumeSnapshotTO).orElseThrow(), allMapper.findStorage(tgtVolumeTO.getStorage()).orElseThrow(), tgtVolumeTO.getName(), replaceExisting);
    }

    @Override
    public boolean fullSync(VolumeSnapshotPO srcVolumeSnapshot, StoragePO tgtStorage, String volumeName, boolean replaceExisting) {
        if (srcVolumeSnapshot.obtainStorage().getDriver().equals(tgtStorage.getDriver())) {
            getProviderByIdNotNull(srcVolumeSnapshot.obtainStorage().getDriver()).syncVolumeFull(srcVolumeSnapshot, tgtStorage, volumeName, replaceExisting);
            return true;
        } else {
            throw new RuntimeException("NYI"); // TODO implement generic sync via dd and ssh
        }
    }

    @Override
    public CanResult canIncrSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeSnapshotReferenceTO tgtVolumeSnapshotTO) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public CanResult canIncrSync(VolumeSnapshotPO srcVolumeSnapshot, VolumeSnapshotPO tgtVolumeSnapshot) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean incrSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeSnapshotReferenceTO tgtVolumeSnapshotTO) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    @Override
    public boolean incrSync(VolumeSnapshotPO srcVolumeSnapshot, VolumeSnapshotPO tgtVolumeSnapshot) {
        throw new RuntimeException("NYI"); // TODO implement
    }

    private StorageDriver getProviderByIdNotNull(HasStorage hasStorage) {
        return getProviderByIdNotNull(hasStorage.obtainStorage().getDriver());
    }

}
