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

import de.hasait.common.service.DriverService;
import de.hasait.teleport.api.StorageApi;
import de.hasait.teleport.api.VolumeCreateTO;
import de.hasait.teleport.domain.HostPO;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeSnapshotPO;
import de.hasait.teleport.service.CanResult;
import de.hasait.teleport.service.refresh.RefreshableService;
import de.hasait.teleport.spi.storage.StorageDriver;

public interface StorageService extends StorageApi, RefreshableService<StoragePO>, DriverService<StorageDriver> {

    // Create

    CanResult canCreateVolume(HostPO host, VolumeCreateTO volumeCreateTO);

    VolumePO createVolume(HostPO host, VolumeCreateTO volumeCreateTO);

    // State

    CanResult canActivateVolume(VolumePO volume);

    boolean activateVolume(VolumePO volume);

    CanResult canDeactivateVolume(VolumePO volume);

    boolean deactivateVolume(VolumePO volume);

    // Delete

    CanResult canDeleteVolume(VolumePO volume);

    boolean deleteVolume(VolumePO volume);

    // Sync

    CanResult canTakeSnapshot(String snapshotName, VolumePO... volumes);

    boolean takeSnapshot(String snapshotName, VolumePO... volumes);

    CanResult canFullSync(VolumeSnapshotPO srcVolumeSnapshot, StoragePO tgtStorage, String volumeName, boolean replaceExisting);

    boolean fullSync(VolumeSnapshotPO srcVolumeSnapshot, StoragePO tgtStorage, String volumeName, boolean replaceExisting);

}
