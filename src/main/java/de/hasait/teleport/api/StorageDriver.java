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

import de.hasait.common.service.Provider;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.SnapshotData;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeSnapshotPO;
import de.hasait.teleport.domain.VolumeState;

public interface StorageDriver extends Provider {

    void populateHypervisor(HypervisorPO hypervisor, StoragePO storage);

    void create(StoragePO storage, VolumeTO config);

    CanResult canUpdate(VolumePO volume, VolumeTO config);

    boolean update(VolumePO volume, VolumeTO config);

    /**
     * Return device path for attaching to a {@link de.hasait.teleport.domain.VirtualMachinePO}.
     */
    String determineDevice(VolumePO volume);

    /**
     * Change state to {@link VolumeState#ACTIVE} and make writable.
     */
    void activate(VolumePO volume);

    /**
     * Change state to {@link VolumeState#DIRTY} and set to readonly.
     */
    void deactivate(VolumePO volume);

    /**
     * Delete volume.
     */
    void delete(VolumePO volume);

    /**
     * Forcefully change state of resource without any validation.
     */
    boolean forceState(VolumePO volume, VolumeState state);

    /**
     * Atomically create {@link VolumeSnapshotPO}s.
     */
    void takeSnapshot(SnapshotData snapshotData, VolumePO... volumes);

    /**
     * Delete {@link VolumeSnapshotPO}.
     */
    void deleteSnapshot(VolumeSnapshotPO snapshot);

    void syncVolumeIncr(VolumeSnapshotPO sender, VolumeSnapshotPO receiver);

    void syncVolumeFull(VolumeSnapshotPO sender, StoragePO receiverStorage, String receiverVolumeName);

}
