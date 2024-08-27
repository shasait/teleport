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

package de.hasait.teleport.api;

import de.hasait.teleport.service.CanResult;

import java.util.List;

public interface StorageApi {

    // Create

    List<HostReferenceTO> canCreateVolume(VolumeCreateTO volumeCreateTO);

    CanResult canCreateVolume(HostReferenceTO hostReferenceTO, VolumeCreateTO volumeCreateTO);

    VolumeTO createVolume(HostReferenceTO hostReferenceTO, VolumeCreateTO volumeCreateTO);

    // State

    VolumeTO getVolume(VolumeReferenceTO volumeReferenceTO);

    CanResult canActivateVolume(VolumeReferenceTO volumeReferenceTO);

    boolean activateVolume(VolumeReferenceTO volumeReferenceTO);

    CanResult canDeactivateVolume(VolumeReferenceTO volumeReferenceTO);

    boolean deactivateVolume(VolumeReferenceTO volumeReferenceTO);

    // Delete

    CanResult canDeleteVolume(VolumeReferenceTO volumeReferenceTO);

    boolean deleteVolume(VolumeReferenceTO volumeReferenceTO);

    // Sync

    CanResult canTakeSnapshot(String snapshotName, VolumeReferenceTO... volumeTOs);

    boolean takeSnapshot(String snapshotName, VolumeReferenceTO... volumeTOs);

    CanResult canFullSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeReferenceTO tgtVolumeTO, boolean replaceExisting);

    boolean fullSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeReferenceTO tgtVolumeTO, boolean replaceExisting);

    CanResult canIncrSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeSnapshotReferenceTO tgtVolumeSnapshotTO);

    boolean incrSync(VolumeSnapshotReferenceTO srcVolumeSnapshotTO, VolumeSnapshotReferenceTO tgtVolumeSnapshotTO);

}
