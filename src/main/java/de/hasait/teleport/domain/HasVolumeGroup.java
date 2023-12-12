package de.hasait.teleport.domain;

public interface HasVolumeGroup extends HasStorage {

    default StoragePO obtainStorage() {
        return obtainVolumeGroup().getStorage();
    }

    VolumeGroupPO obtainVolumeGroup();

}
