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

import de.hasait.common.domain.DriverInstancePO;
import de.hasait.common.ui.puif.TextAreaForStringPui;
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
@Table(name = "HYPERVISOR", uniqueConstraints = { //
        @UniqueConstraint(name = "UC_HV_HOST_NAME", columnNames = {"HOST_ID", "NAME"}) //
})
public class HypervisorPO implements DriverInstancePO, HasHypervisor {

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

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "DRIVER", nullable = false)
    private String driver;

    @Size(max = 512)
    @Column(name = "DRIVER_CONFIG")
    @TextAreaForStringPui
    private String driverConfig;

    @Column(name = "LAST_REFRESH")
    private LocalDateTime lastRefresh;

    @OneToMany(mappedBy = "hypervisor", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<VirtualMachinePO> virtualMachines = new ArrayList<>();

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

    @Override
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

    public LocalDateTime getLastRefresh() {
        return lastRefresh;
    }

    @Override
    public void setLastRefresh(LocalDateTime lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public List<VirtualMachinePO> getVirtualMachines() {
        return virtualMachines;
    }

    public Optional<VirtualMachinePO> findVirtualMachineByName(String name) {
        return getVirtualMachines().stream().filter(it -> it.getName().equals(name)).findAny();
    }

    @Override
    public HypervisorPO obtainHypervisor() {
        return this;
    }

    public String toFqName() {
        return host.toFqName() + "/" + name;
    }

}
