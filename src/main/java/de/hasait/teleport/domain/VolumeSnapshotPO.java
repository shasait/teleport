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

import de.hasait.common.domain.PersistantObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Entity
@Table(name = "VOLUME_SNAPSHOT")
public final class VolumeSnapshotPO implements PersistantObject, HasVolume {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "VOLUME_ID", nullable = false)
    private VolumePO volume;

    @Embedded
    private SnapshotData data;

    @Min(0)
    @Column(name = "USED_BYTES", nullable = false)
    private long usedBytes;

    public VolumeSnapshotPO() {
    }

    public VolumeSnapshotPO(VolumePO volume, String name, long usedBytes, LocalDateTime creation) {
        this.volume = volume;
        this.data = new SnapshotData(name, creation);
        this.usedBytes = usedBytes;

        volume.getSnapshots().add(this);
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

    public VolumePO getVolume() {
        return volume;
    }

    public void setVolume(VolumePO volume) {
        this.volume = volume;
    }

    public SnapshotData getData() {
        return data;
    }

    public void setData(SnapshotData data) {
        this.data = data;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
    }

    @Override
    public VolumePO obtainVolume() {
        return volume;
    }

}
