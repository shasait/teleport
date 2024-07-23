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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "NETWORK", uniqueConstraints = { //
        @UniqueConstraint(name = "UC_NW_L_NAME", columnNames = {"LOCATION_ID", "NAME"}) //
})
public class NetworkPO implements PersistantObject {

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

    @Size(min = 1, max = 128)
    @NotNull
    @Column(name = "DOMAIN")
    private String domain;

    @Size(max = 18)
    @Column(name = "CIDR_4")
    private String cidr4;

    @Size(max = 43)
    @Column(name = "CIDR_6")
    private String cidr6;

    @Column(name = "VLAN")
    private Integer vlan;

    @OneToMany(mappedBy = "network", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<NetworkInterfacePO> networkInterfaces = new ArrayList<>();

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCidr4() {
        return cidr4;
    }

    public void setCidr4(String cidr4) {
        this.cidr4 = cidr4;
    }

    public String getCidr6() {
        return cidr6;
    }

    public void setCidr6(String cidr6) {
        this.cidr6 = cidr6;
    }

    public Integer getVlan() {
        return vlan;
    }

    public void setVlan(Integer vlan) {
        this.vlan = vlan;
    }

    public List<NetworkInterfacePO> getNetworkInterfaces() {
        return networkInterfaces;
    }

    public Optional<NetworkInterfacePO> findNetworkInterfaceByMac(String mac) {
        return getNetworkInterfaces().stream().filter(it -> it.getMac().equals(mac)).findAny();
    }

}
