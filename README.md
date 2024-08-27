# TELEPORT

VM manager utilizing ZFS send/receive for replication.

## Supported technologies

### Hypervisors

* [libvirt](https://www.libvirt.org)
    * Driver uses virsh command via ssh
* [proxmox](https://en.wikipedia.org/wiki/Proxmox_Virtual_Environment)
    * Driver uses qm command via ssh

### Storage

* ZFS
    * Driver uses zfs command via ssh

### Networking

* VLAN based separation

## Supported actions

### Create Volume

* Create new Volume on Storage

### Update Volume

* Update Volume, e.g. resize

### Delete Volume

* Destroy Volume

### Activate Volume

* Make Volume writable
* Ensure only one Volume is active at a time in a replication group (TODO)

### Deactivate Volume

* Make Volume readonly

### Take Snapshot of Volumes

* Create atomic Snapshot on Volumes

### FullSync Volume

* Sync VolumeSnapshot to target Storage

### IncrSync Volume

* Sync VolumeSnapshot to target Volume using base VolumeSnapshot

### Create VirtualMachine

* Create VM on Hypervisor
* Create Volumes

### Update VirtualMachine (TODO)

* Update VM, e.g. memory settings
* Create, Update or Delete Volumes

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
* Take Snapshot of Volumes
* FullSync of Volumes

### IncrSync VirtualMachine (TODO)

* Update VM on target hypervisor (translate config if needed)
* Take Snapshot of Volumes
* IncrSync Volume if already existing on target, otherwise do FullSync

### Migrate VirtualMachine (TODO)

* Shutdown VM if running
* IncrSync VM if already existing on target hypervisor, otherwise do FullSync
* Start VM on target hypervisor if it was running on source before

... more to come
