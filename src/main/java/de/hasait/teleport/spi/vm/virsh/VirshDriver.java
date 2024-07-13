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

package de.hasait.teleport.spi.vm.virsh;

import com.google.gson.Gson;
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.common.util.xml.XmlDocument;
import de.hasait.common.util.xml.XmlElement;
import de.hasait.common.util.xml.XmlElements;
import de.hasait.teleport.CliConfig;
import de.hasait.teleport.api.VirtualMachineCreateTO;
import de.hasait.teleport.api.VmState;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.NetworkInterfacePO;
import de.hasait.teleport.domain.NetworkPO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeRepository;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VirshDriver implements HypervisorDriver {

    public static final String DRIVER_ID = "virsh";

    private static VirshDriverConfig parseConfig(String config) {
        VirshDriverConfig driverConfig = new Gson().fromJson(config, VirshDriverConfig.class);
        return driverConfig;
    }

    private static VirshDriverConfig parseConfig(HypervisorPO hypervisor) {
        if (DRIVER_ID.equals(hypervisor.getDriver())) {
            return parseConfig(hypervisor.getDriverConfig());
        }
        throw new IllegalArgumentException("Not a virsh hypervisor: " + hypervisor);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CliConfig cliConfig;
    private final VolumeRepository volumeRepository;
    private final VirtualMachineRepository virtualMachineRepository;

    public VirshDriver(@Autowired CliConfig cliConfig, VolumeRepository volumeRepository, VirtualMachineRepository virtualMachineRepository) {
        this.cliConfig = cliConfig;
        this.volumeRepository = volumeRepository;
        this.virtualMachineRepository = virtualMachineRepository;
    }

    @Override
    @Nonnull
    public String getId() {
        return DRIVER_ID;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Virsh Hypervisor Driver";
    }

    @Nullable
    @Override
    public String getDisabledReason() {
        return null;
    }

    @Nullable
    @Override
    public String validateConfig(@Nullable String config) {
        try {
            parseConfig(config);
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    @Override
    public void refresh(HypervisorPO hypervisor) {
        VirshDriverConfig driverConfig = parseConfig(hypervisor);
        CliExecutor exe = cliConfig.createCliConnector(hypervisor);
        VirshUtils.virshListAll(exe).forEach(it -> processListEntry(hypervisor, driverConfig, exe, it));
    }

    @Override
    public void create(HypervisorPO hypervisor, VirtualMachineCreateTO config, boolean runInstallation) {

    }

    @Override
    public void start(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void shutdown(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void kill(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void update(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void delete(VirtualMachinePO virtualMachine) {

    }

    private void processListEntry(HypervisorPO hypervisor, VirshDriverConfig driverConfig, CliExecutor exe, VirshListE entry) {
        String name = entry.getName();
        VmState state = entry.getState();

        VirtualMachinePO existingVirtualMachine = hypervisor.findVirtualMachineByName(name).orElse(null);
        VirtualMachinePO virtualMachine;
        if (existingVirtualMachine != null) {
            existingVirtualMachine.setState(state);
            virtualMachine = existingVirtualMachine;
        } else {
            virtualMachine = new VirtualMachinePO(hypervisor, name, state);
        }

        String domainXml = VirshUtils.virshDumpXml(exe, name);

        XmlDocument doc = new XmlDocument(domainXml);

        XmlElement documentElement = doc.getDocumentElement();
        String uuid = documentElement.getFirstElement("uuid").orElseThrow().getTextContent();
        virtualMachine.setUuid(uuid);

        XmlElement firstMemoryElement = documentElement.getFirstElement("memory").orElseThrow();
        int memValue = firstMemoryElement.getTextContentAsInt();
        String memUnit = firstMemoryElement.getAttribute("unit");

        int memMb;
        if (memUnit.equals("KiB")) {
            memMb = memValue / 1024;
        } else {
            throw new RuntimeException("Unknown memUnit: " + memUnit);
        }
        virtualMachine.setMemMb(memMb);

        virtualMachine.setMemHugePages(documentElement.getFirstElement("memoryBacking", "hugepages").isPresent());

        int vcpu = documentElement.getFirstElement("vcpu").orElseThrow().getTextContentAsInt();
        virtualMachine.setCores(vcpu);

        XmlElement firstDevicesElement = documentElement.getFirstElement("devices").orElseThrow();

        XmlElement firstVideoElement = firstDevicesElement.getFirstElement("video").orElseThrow();
        XmlElement firstVideoModelElement = firstVideoElement.getFirstElement("model").orElseThrow();

        int videoRam = firstVideoModelElement.getAttributeAsInt("ram");
        virtualMachine.setVgaMemKb(videoRam);
        virtualMachine.setVideoModel(firstVideoModelElement.getAttribute("type"));

        virtualMachineRepository.save(virtualMachine);

        Set<VolumeAttachmentPO> volumeAttachments = new HashSet<>();

        XmlElements diskElements = firstDevicesElement.getElementsByTagName("disk");
        for (int i = 0; i < diskElements.getLength(); i++) {
            XmlElement diskElement = diskElements.element(i);
            XmlElement firstSourceElement = diskElement.getFirstElement("source").orElse(null);
            if (firstSourceElement == null) {
                continue;
            }
            String sdev = firstSourceElement.getAttribute("dev");

            List<VolumePO> volumes = volumeRepository.findByHostAndDev(virtualMachine.getHypervisor().getHost().getName(), sdev);
            if (volumes.size() == 1) {
                VolumePO volume = volumes.get(0);

                XmlElement firstTargetElement = diskElement.getFirstElement("target").orElseThrow();
                String tdev = firstTargetElement.getAttribute("dev");

                VolumeAttachmentPO existingVolumeAttachment = virtualMachine.findVolumeAttachmentByDev(tdev).orElse(null);
                VolumeAttachmentPO volumeAttachment;
                if (existingVolumeAttachment != null) {
                    volumeAttachment = existingVolumeAttachment;
                    volumeAttachment.setVolume(volume);
                } else {
                    volumeAttachment = new VolumeAttachmentPO(virtualMachine, tdev, volume);
                }

                volumeAttachments.add(volumeAttachment);
            }
        }

        virtualMachine.getVolumeAttachments().retainAll(volumeAttachments);

        Set<NetworkInterfacePO> networkInterfaces = new HashSet<>();

        XmlElements interfaceElements = firstDevicesElement.getElementsByTagName("interface");
        for (int i = 0; i < interfaceElements.getLength(); i++) {
            String iname = "eth" + i;

            XmlElement interfaceElement = interfaceElements.element(i);
            if (!"ethernet".equals(interfaceElement.getAttribute("type"))) {
                continue;
            }

            String modelType = interfaceElement.getFirstElement("model").orElseThrow().getAttribute("type");
            String macAddress = interfaceElement.getFirstElement("mac").orElseThrow().getAttribute("address");
            String scriptPath = interfaceElement.getFirstElement("script").orElseThrow().getAttribute("path");
            int vlan = Integer.parseInt(StringUtils.substringBefore(StringUtils.substringAfterLast(scriptPath, "_"), "."));
            boolean trunk = scriptPath.contains("vlan-trunk_");

            NetworkPO network = virtualMachine.obtainLocation().findNetworkByVlan(vlan).orElse(null);
            if (network == null) {
                continue;
            }

            NetworkInterfacePO existingInterface = virtualMachine.findNetworkInterfaceByMac(macAddress).orElse(null);
            NetworkInterfacePO networkInterface;
            if (existingInterface != null) {
                existingInterface.setName(iname);
                existingInterface.setModel(modelType);
                existingInterface.setNetwork(network);
                networkInterface = existingInterface;
            } else {
                networkInterface = new NetworkInterfacePO(virtualMachine, iname, modelType, macAddress, network);
            }

            networkInterface.setTrunk(trunk);

            networkInterfaces.add(networkInterface);
        }

        virtualMachine.getNetworkInterfaces().retainAll(networkInterfaces);
    }

}
