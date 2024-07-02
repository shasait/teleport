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
import de.hasait.common.ui.puif.TextAreaForStringPui;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "VIRTUAL_MACHINE", uniqueConstraints = { //
        @UniqueConstraint(name = "UC_VM_HV_NAME", columnNames = {"HYPERVISOR_ID", "NAME"}), //
        @UniqueConstraint(name = "UC_VM_HV_UUID", columnNames = {"HYPERVISOR_ID", "UUID"}) //
})
public class VirtualMachinePO implements IdAndVersion {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "HYPERVISOR_ID", nullable = false)
    private HypervisorPO hypervisor;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @Size(max = 128)
    @Column(name = "DESCRIPTION")
    private String description;

    @Size(min = 1, max = 36)
    @NotNull
    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "DRIVER", nullable = false)
    private String driver;

    @Size(max = 512)
    @Column(name = "DRIVER_CONFIG")
    @TextAreaForStringPui
    private String driverConfig;

    @Min(1)
    @Column(name = "CORES")
    private int cores;

    @Min(1)
    @Column(name = "MEM_MB")
    private int memMb;

    @Min(0)
    @Column(name = "SWAP_MB")
    private int swapMb;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "VIDEO_MODEL", nullable = false)
    private String videoModel;

    @Min(1)
    @Column(name = "VGA_MEM_KB")
    private int vgaMemKb;

    @OneToMany(mappedBy = "virtualMachine", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<VolumeAttachmentPO> volumeAttachments = new ArrayList<>();

    @OneToMany(mappedBy = "virtualMachine", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private List<NetworkInterfacePO> networkInterfaces = new ArrayList<>();

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

    public HypervisorPO getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(HypervisorPO hypervisor) {
        this.hypervisor = hypervisor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriverConfig() {
        return driverConfig;
    }

    public void setDriverConfig(String driverConfig) {
        this.driverConfig = driverConfig;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getMemMb() {
        return memMb;
    }

    public void setMemMb(int memMb) {
        this.memMb = memMb;
    }

    public int getSwapMb() {
        return swapMb;
    }

    public void setSwapMb(int swapMb) {
        this.swapMb = swapMb;
    }

    public String getVideoModel() {
        return videoModel;
    }

    public void setVideoModel(String videoModel) {
        this.videoModel = videoModel;
    }

    public int getVgaMemKb() {
        return vgaMemKb;
    }

    public void setVgaMemKb(int vgaMemKb) {
        this.vgaMemKb = vgaMemKb;
    }

    public List<VolumeAttachmentPO> getVolumeAttachments() {
        return volumeAttachments;
    }

    public List<NetworkInterfacePO> getNetworkInterfaces() {
        return networkInterfaces;
    }

}
