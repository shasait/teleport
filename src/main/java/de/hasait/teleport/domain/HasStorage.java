package de.hasait.teleport.domain;

public interface HasStorage extends HasHypervisor {

    @Override
    default HypervisorPO obtainHypervisor() {
        return obtainStorage().getHypervisor();
    }

    StoragePO obtainStorage();

}
