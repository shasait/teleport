package de.hasait.teleport.api;

import java.io.Serializable;

public class CreateVolumeTO implements Serializable {

    private String name;

    private long sizeBytes;

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

}
