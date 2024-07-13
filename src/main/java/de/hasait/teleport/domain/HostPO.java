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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "HOST", uniqueConstraints = { //
        @UniqueConstraint(name = "UC_HOST_L_NAME", columnNames = {"LOCATION_ID", "NAME"}) //
})
public class HostPO implements IdAndVersion, HasHost {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "LOCATION_ID", nullable = false)
    private LocationPO location;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @Size(max = 128)
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LAST_SEEN")
    private LocalDateTime lastSeen;

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<StoragePO> storages = new ArrayList<>();

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<HypervisorPO> hypervisors = new ArrayList<>();

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

    public LocationPO getLocation() {
        return location;
    }

    public void setLocation(LocationPO location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public List<StoragePO> getStorages() {
        return storages;
    }

    public List<HypervisorPO> getHypervisors() {
        return hypervisors;
    }

    public Optional<StoragePO> findStorageByName(String name) {
        return getStorages().stream().filter(it -> it.getName().equals(name)).findAny();
    }

    public Optional<HypervisorPO> findHypervisorByName(String name) {
        return getHypervisors().stream().filter(it -> it.getName().equals(name)).findAny();
    }

    @Override
    public HostPO obtainHost() {
        return this;
    }

}
