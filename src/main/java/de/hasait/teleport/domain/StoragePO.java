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
import de.hasait.common.ui.puif.ProvderPui;
import de.hasait.common.ui.puif.StringSetPui;
import de.hasait.common.ui.puif.TextAreaForStringPui;
import de.hasait.teleport.spi.storage.StorageDriver;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "STORAGE", uniqueConstraints = { //
        @UniqueConstraint(name = "UC_ST_HOST_NAME", columnNames = {"HOST_ID", "NAME"}) //
})
public class StoragePO implements IdAndVersion, HasStorage {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "HOST_ID", nullable = false)
    private HostPO host;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @Size(max = 128)
    @Column(name = "DESCRIPTION")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "STORAGE_ALIASES", joinColumns = @JoinColumn(name = "STORAGE_ID"))
    @StringSetPui
    private Set<String> aliases = new HashSet<>();

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "DRIVER", nullable = false)
    @ProvderPui(provider = StorageDriver.class)
    private String driver;

    @Size(max = 512)
    @Column(name = "DRIVER_CONFIG")
    @TextAreaForStringPui
    private String driverConfig;

    @Min(0)
    @Column(name = "AVAIL_BYTES", nullable = false)
    private long availBytes;

    @OneToMany(mappedBy = "storage", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<VolumePO> volumes = new ArrayList<>();

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

    public HostPO getHost() {
        return host;
    }

    public void setHost(HostPO host) {
        this.host = host;
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

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriverConfig() {
        return driverConfig;
    }

    public void setDriverConfig(String driverConfig) {
        this.driverConfig = driverConfig;
    }

    public long getAvailBytes() {
        return availBytes;
    }

    public void setAvailBytes(long availBytes) {
        this.availBytes = availBytes;
    }

    public List<VolumePO> getVolumes() {
        return volumes;
    }

    @Override
    public StoragePO obtainStorage() {
        return this;
    }

    public Optional<VolumePO> findVolumeByName(String name) {
        return getVolumes().stream().filter(it -> it.getName().equals(name)).findAny();
    }

}
