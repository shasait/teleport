# TELEPORT

VM manager utilizing ZFS replication

## Supported technologies

### Hypervisors

* virsh
* proxmox

### Storage

* ZFS

### Networking

* VLAN based separation

## Supported actions

### Activate Volume

* Make Volume writable
* TODO Ensure only one Volume is active at a time in a replication group

### Deactivate Volume

* Make Volume readonly

### FullSync Volume

* Sync VolumeSnapshot to target Storage

### IncrSync Volume

* Sync VolumeSnapshot to target Volume using base VolumeSnapshot

### Start VirtualMachine

* Activate Volumes
* Start VM

### Shutdown VirtualMachine

* Shutdown VM
* Deactivate Volumes

### Kill VirtualMachine

* Kill VM
* Deactivate Volumes

### FullSync VirtualMachine

* Create VM on target hypervisor (translate config if needed)
* FullSync of Volumes

... more to come
