package de.hasait.teleport.domain;

import de.hasait.common.domain.IdAndVersion;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "VOLUME_SNAPSHOT")
public class VolumeSnapshotPO implements IdAndVersion, HasVolume {

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

    @Override
    public VolumePO obtainVolume() {
        return volume;
    }

}
