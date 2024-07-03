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

package de.hasait.teleport.domain;

import de.hasait.common.domain.IdAndVersion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "VOLUME", uniqueConstraints = { //
        @UniqueConstraint(name = "UC_V_VG_NAME", columnNames = {"STORAGE_ID", "NAME"}) //
})
public final class VolumePO implements IdAndVersion, HasVolume {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "STORAGE_ID", nullable = false)
    private StoragePO storage;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @Size(min = 1, max = 256)
    @NotNull
    @Column(name = "DEV", nullable = false)
    private String dev;

    @Min(0)
    @Column(name = "SIZE_BYTES", nullable = false)
    private long sizeBytes;

    @NotNull
    @Column(name = "STATE")
    private VolumeState state;

    @Min(0)
    @Column(name = "USED_BYTES", nullable = false)
    private long usedBytes;

    @OneToMany(mappedBy = "volume", cascade = CascadeType.ALL)
    private List<VolumeSnapshotPO> snapshots = new ArrayList<>();

    @OneToMany(mappedBy = "volume", cascade = CascadeType.ALL)
    private List<VolumeAttachmentPO> volumeAttachments = new ArrayList<>();

    public VolumePO() {
    }

    public VolumePO(StoragePO storage, String name, String dev, long sizeBytes, VolumeState state, long usedBytes) {
        this.storage = storage;
        this.name = name;
        this.dev = dev;
        this.sizeBytes = sizeBytes;
        this.state = state;
        this.usedBytes = usedBytes;

        storage.getVolumes().add(this);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    public StoragePO getStorage() {
        return storage;
    }

    public void setStorage(StoragePO storage) {
        this.storage = storage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDev() {
        return dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public VolumeState getState() {
        return state;
    }

    public void setState(VolumeState state) {
        this.state = state;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
    }

    public List<VolumeSnapshotPO> getSnapshots() {
        return snapshots;
    }

    public List<VolumeAttachmentPO> getVolumeAttachments() {
        return volumeAttachments;
    }

    public Optional<VolumeSnapshotPO> findSnapshotByName(String name) {
        return getSnapshots().stream().filter(it -> it.getData().getName().equals(name)).findAny();
    }

    @Override
    public VolumePO obtainVolume() {
        return this;
    }

    public boolean isActive() {
        return state == VolumeState.ACTIVE;
    }

    public boolean isDirty() {
        return state == VolumeState.DIRTY;
    }

    public boolean isActiveOrDirty() {
        return isActive() || isDirty();
    }

    public boolean isInactive() {
        return state == VolumeState.INACTIVE;
    }

    public boolean isDirtyOrInactive() {
        return isDirty() || isInactive();
    }

}
