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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "VOLUME_GROUP", uniqueConstraints = {
        @UniqueConstraint(name = "UC_VG_ST_NAME", columnNames = {"STORAGE_ID", "NAME"})
})
public class VolumeGroupPO implements IdAndVersion {

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

    @Size(max = 128)
    @Column(name = "DESCRIPTION")
    private String description;

    @NotNull
    @Column(name = "STATE")
    private VolumeGroupState state;

    @Min(0)
    @Column(name = "USED_BYTES", nullable = false)
    private long usedBytes;

    @OneToMany(mappedBy = "volumeGroup", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<VolumePO> volumes = new ArrayList<>();

    @OneToMany(mappedBy = "volumeGroup", cascade = CascadeType.ALL)
    private List<VolumeGroupSnapshotPO> snapshots = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VolumeGroupState getState() {
        return state;
    }

    public void setState(VolumeGroupState state) {
        this.state = state;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
    }

    public List<VolumePO> getVolumes() {
        return volumes;
    }

    public List<VolumeGroupSnapshotPO> getSnapshots() {
        return snapshots;
    }

}
