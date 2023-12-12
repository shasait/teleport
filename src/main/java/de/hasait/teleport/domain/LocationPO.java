package de.hasait.teleport.domain;

import de.hasait.common.domain.IdAndVersion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LOCATION")
public class LocationPO implements IdAndVersion, HasLocation {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Size(max = 128)
    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<NetworkPO> networks = new ArrayList<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
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

    public List<NetworkPO> getNetworks() {
        return networks;
    }

    public List<HypervisorPO> getHypervisors() {
        return hypervisors;
    }

    @Override
    public LocationPO obtainLocation() {
        return this;
    }

}
