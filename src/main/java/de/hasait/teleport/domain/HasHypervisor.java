package de.hasait.teleport.domain;

public interface HasHypervisor extends HasLocation {

    default LocationPO obtainLocation() {
        return obtainHypervisor().getLocation();
    }

    HypervisorPO obtainHypervisor();

}
