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

@Entity
@Table(name = "VOLUME", uniqueConstraints = {
        @UniqueConstraint(name = "UC_V_VG_NAME", columnNames = {"VG_ID", "NAME"})
})
public class VolumePO implements IdAndVersion, HasVolume {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "VG_ID", nullable = false)
    private VolumeGroupPO volumeGroup;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @Min(0)
    @Column(name = "SIZE_BYTES", nullable = false)
    private long sizeBytes;

    @Min(0)
    @Column(name = "USED_BYTES", nullable = false)
    private long usedBytes;

    @OneToMany(mappedBy = "volume", cascade = CascadeType.ALL)
    private List<VolumeSnapshotPO> snapshots = new ArrayList<>();

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

    public VolumeGroupPO getVolumeGroup() {
        return volumeGroup;
    }

    public void setVolumeGroup(VolumeGroupPO volumeGroup) {
        this.volumeGroup = volumeGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
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

    @Override
    public VolumePO obtainVolume() {
        return this;
    }

}
