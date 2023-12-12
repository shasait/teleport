/*
 * Copyright (C) 2023 by Sebastian Hasait (sebastian at hasait dot de)
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

import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.SnapshotData;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VolumeGroupPO;
import de.hasait.teleport.domain.VolumeGroupSnapshotPO;
import de.hasait.teleport.domain.VolumeGroupState;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeSnapshotPO;

public interface StorageDriver {

    String getId();

    void populateHypervisor(HypervisorPO hypervisor, StoragePO storage);

    void create(StoragePO storage, CreateVolumeGroupTO config);

    CanResult canUpdate(VolumeGroupPO volumeGroup);

    boolean update(VolumeGroupPO volumeGroup);

    /**
     * Return device path for attaching to a {@link de.hasait.teleport.domain.VirtualMachinePO}.
     */
    String determineDevice(VolumePO volume);

    /**
     * Change state to {@link VolumeGroupState#ACTIVE} and make writable.
     */
    void activate(VolumeGroupPO volumeGroup);

    /**
     * Change state to {@link VolumeGroupState#DIRTY} and set to readonly.
     */
    void deactivate(VolumeGroupPO volumeGroup);

    /**
     * Delete resource AND volumes.
     */
    void delete(VolumeGroupPO volumeGroup);

    /**
     * Forcefully change state of resource without any validation.
     */
    boolean forceResourceState(VolumeGroupPO volumeGroup, VolumeGroupState state);

    /**
     * Atomically create {@link VolumeGroupSnapshotPO} and {@link VolumeSnapshotPO}s.
     */
    void takeSnapshot(VolumeGroupPO volumeGroup, SnapshotData snapshotData);

    /**
     * Delete {@link VolumeGroupSnapshotPO} and corresponding {@link VolumeSnapshotPO}s.
     */
    void deleteSnapshot(VolumeGroupSnapshotPO snapshot);

    void syncResourceIncr(VolumeGroupSnapshotPO sender, VolumeGroupSnapshotPO receiver);

    void syncVolumeIncr(VolumeSnapshotPO sender, VolumeSnapshotPO receiver);

    void syncResourceFull(VolumeGroupSnapshotPO sender, StoragePO receiver);

    void syncVolumeFull(VolumeSnapshotPO sender, VolumeGroupPO receiver);

}
