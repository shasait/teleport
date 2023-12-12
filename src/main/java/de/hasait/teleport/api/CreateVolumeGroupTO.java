package de.hasait.teleport.api;

import java.io.Serializable;
import java.util.List;

public class CreateVolumeGroupTO implements Serializable {

    private String name;

    private List<CreateVolumeTO> volumes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CreateVolumeTO> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<CreateVolumeTO> volumes) {
        this.volumes = volumes;
    }

}
