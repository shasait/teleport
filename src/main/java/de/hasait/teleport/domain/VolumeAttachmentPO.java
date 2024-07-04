/*
 * Copyright (C) 2024 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.teleport.domain;

import de.hasait.common.domain.IdAndVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "VOLUME_ATTACHMENT", uniqueConstraints = { //
        @UniqueConstraint(name = "UC_VA_VM_DEV", columnNames = {"VM_ID", "DEV"}), //
        @UniqueConstraint(name = "UC_VA_VM_VOL", columnNames = {"VM_ID", "VOLUME_ID"}) //
})
public class VolumeAttachmentPO implements IdAndVersion, HasVirtualMachine, HasVolume {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "VM_ID", nullable = false)
    private VirtualMachinePO virtualMachine;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "DEV", nullable = false)
    private String dev;

    @ManyToOne
    @JoinColumn(name = "VOLUME_ID", nullable = false)
    private VolumePO volume;

    public VolumeAttachmentPO() {
    }

    public VolumeAttachmentPO(VirtualMachinePO virtualMachine, String dev, VolumePO volume) {
        this.virtualMachine = virtualMachine;
        this.dev = dev;
        this.volume = volume;

        virtualMachine.getVolumeAttachments().add(this);
        volume.getVolumeAttachments().add(this);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    public VirtualMachinePO getVirtualMachine() {
        return virtualMachine;
    }

    public void setVirtualMachine(VirtualMachinePO virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    public String getDev() {
        return dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public VolumePO getVolume() {
        return volume;
    }

    public void setVolume(VolumePO volume) {
        this.volume = volume;
    }

    @Override
    public VirtualMachinePO obtainVirtualMachine() {
        return virtualMachine;
    }

    @Override
    public VolumePO obtainVolume() {
        return volume;
    }

    @Override
    public HostPO obtainHost() {
        return virtualMachine.obtainHost();
    }

}
