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
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    @Size(min = 1, max = 128)
    @NotNull
    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATION")
    private LocalDateTime creation;

    @Column(name = "CONSISTENT")
    private boolean consistent;

    @Min(0)
    @Column(name = "USED_BYTES", nullable = false)
    private long usedBytes;

    public VolumeSnapshotPO() {
    }

    public VolumeSnapshotPO(VolumePO volume, String name, LocalDateTime creation, boolean consistent, long usedBytes) {
        this.volume = volume;
        this.name = name;
        this.creation = creation;
        this.consistent = consistent;
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

    public @Size(min = 1, max = 128) @NotNull String getName() {
        return name;
    }

    public void setName(@Size(min = 1, max = 128) @NotNull String name) {
        this.name = name;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public boolean isConsistent() {
        return consistent;
    }

    public void setConsistent(boolean consistent) {
        this.consistent = consistent;
    }

    @Min(0)
    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(@Min(0) long usedBytes) {
        this.usedBytes = usedBytes;
    }

    @Override
    public VolumePO obtainVolume() {
        return volume;
    }

}
