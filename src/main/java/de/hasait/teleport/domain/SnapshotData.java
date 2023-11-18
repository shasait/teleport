package de.hasait.teleport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Embeddable
public class SnapshotData {

    @Size(min = 1, max = 128)
    @NotNull
    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATION")
    private LocalDateTime creation;

    @Column(name = "CONSISTENT")
    private Boolean consistent;

    @Column(name = "SNAP_VERSION")
    private Integer snapVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public Boolean getConsistent() {
        return consistent;
    }

    public void setConsistent(Boolean consistent) {
        this.consistent = consistent;
    }

    public Integer getSnapVersion() {
        return snapVersion;
    }

    public void setSnapVersion(Integer snapVersion) {
        this.snapVersion = snapVersion;
    }

}
