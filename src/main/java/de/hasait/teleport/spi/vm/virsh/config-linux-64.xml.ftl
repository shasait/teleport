<!--
  ~ Copyright (C) 2023 by Sebastian Hasait (sebastian at hasait dot de)
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<domain type='kvm'>
    <name>${config.name}</name>
    <uuid>${config.uuid}</uuid>
    <memory unit='MiB'>${config.basic.memMb}</memory>
    <currentMemory unit='MiB'>${config.basic.memMb}</currentMemory>
    <#if config.basic.hugepages>
    <memoryBacking>
        <hugepages/>
    </memoryBacking>
    </#if>
    <vcpu placement='static'>${config.basic.cores}</vcpu>
    <iothreads>2</iothreads>
    <os<#if config.basic.firmware = "efi"> firmware='${config.basic.firmware}'</#if>>
        <type arch='x86_64' machine='pc-i440fx-xenial'>hvm</type>
        <boot dev='hd'/>
    </os>
    <features>
        <acpi/>
        <apic/>
    </features>
    <cpu mode='host-model'>
        <topology sockets='1' dies='1' cores='${config.basic.cores / 2}' threads='2'/>
        <feature name='topoext' policy='${cpu.topoextPolicy}'/>
    </cpu>
    <clock offset='utc'>
        <timer name='rtc' tickpolicy='catchup'/>
        <timer name='pit' tickpolicy='delay'/>
        <timer name='hpet' present='no'/>
    </clock>
    <on_poweroff>destroy</on_poweroff>
    <on_reboot>restart</on_reboot>
    <on_crash>restart</on_crash>
    <pm>
        <suspend-to-mem enabled='no'/>
        <suspend-to-disk enabled='no'/>
    </pm>
    <devices>
        <emulator>/usr/bin/qemu-system-x86_64</emulator>
        <#list disks as disk>
        <disk type='block' device='disk'>
            <driver name='qemu' type='raw' cache='none' io='native' discard='unmap'/>
            <source dev='${disk.srcDev}'/>
            <target dev='${disk.tgtDev}' bus='scsi'/>
            <address type='drive' controller='0' bus='0' target='${disk?index}' unit='0'/>
        </disk>
        </#list>
        <controller type='usb' index='0' model='ich9-ehci1'>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x06' function='0x7'/>
        </controller>
        <controller type='usb' index='0' model='ich9-uhci1'>
            <master startport='0'/>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x06' function='0x0' multifunction='on'/>
        </controller>
        <controller type='usb' index='0' model='ich9-uhci2'>
            <master startport='2'/>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x06' function='0x1'/>
        </controller>
        <controller type='usb' index='0' model='ich9-uhci3'>
            <master startport='4'/>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x06' function='0x2'/>
        </controller>
        <controller type='pci' index='0' model='pci-root'/>
        <controller type='scsi' index='0' model='virtio-scsi'>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x0'/>
        </controller>
        <#list config.networkInterfaces as net>
        <interface type='ethernet'>
            <script path='/etc/libvirt/hooks/if-up_${net.ifup}.sh'/>
            <mac address='${net.mac}'/>
            <model type='${net.model}'/>
            <address type='pci' domain='0x0000' bus='0x01' slot='0x0${net?index + 3}' function='0x0'/>
        </interface>
        </#list>
        <serial type='pty'>
            <target port='0'/>
        </serial>
        <console type='pty'>
            <target type='serial' port='0'/>
        </console>
        <input type='mouse' bus='ps2'/>
        <input type='keyboard' bus='ps2'/>
        <#if config.video.type = "vnc">
        <graphics type='vnc' port='-1' autoport='yes' keymap='de'/>
        <video>
            <model type='qxl' ram='${config.video.vgamem * 4}' vram='${config.video.vgamem * 4}' vgamem='${config.video.vgamem}' heads='1' primary='yes'/>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x02' function='0x0'/>
        </video>
        </#if>
        <#if config.video.type = "spice">
        <channel type='spicevmc'>
            <target type='virtio' name='com.redhat.spice.0'/>
            <address type='virtio-serial' controller='0' bus='0' port='1'/>
        </channel>
        <sound model='ich6'>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x04' function='0x0'/>
        </sound>
        <graphics type='spice' port='-1' autoport='yes'>
            <image compression='off'/>
        </graphics>
        <video>
            <model type='qxl' ram='${config.video.vgamem * 4}' vram='${config.video.vgamem * 4}' vgamem='${config.video.vgamem}' heads='1' primary='yes'/>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x02' function='0x0'/>
        </video>
        <redirdev bus='usb' type='spicevmc'>
        </redirdev>
        <redirdev bus='usb' type='spicevmc'>
        </redirdev>
        </#if>
        <memballoon model='virtio'>
            <address type='pci' domain='0x0000' bus='0x00' slot='0x08' function='0x0'/>
        </memballoon>
    </devices>
</domain>
