package de.hasait.teleport.domain;

public interface HasVolume extends HasVolumeGroup {

    default VolumeGroupPO obtainVolumeGroup() {
        return obtainVolume().getVolumeGroup();
    }

    VolumePO obtainVolume();

}
