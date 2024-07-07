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
import de.hasait.common.util.XmlUtil;
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.teleport.CliConfig;
import de.hasait.teleport.api.VirtualMachineCreateTO;
import de.hasait.teleport.api.VmState;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeRepository;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        Document doc;
        try {
            doc = builder.parse(new ByteArrayInputStream(domainXml.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Element documentElement = doc.getDocumentElement();
        String uuid = documentElement.getElementsByTagName("uuid").item(0).getTextContent();
        virtualMachine.setUuid(uuid);

        Element firstMemoryElement = XmlUtil.getFirstElement(documentElement, "memory").orElseThrow();
        int memValue = Integer.parseInt(firstMemoryElement.getTextContent());
        String memUnit = firstMemoryElement.getAttribute("unit");

        int memMb;
        if (memUnit.equals("KiB")) {
            memMb = memValue / 1024;
        } else {
            throw new RuntimeException("Unknown memUnit: " + memUnit);
        }
        virtualMachine.setMemMb(memMb);

        virtualMachine.setMemHugePages(XmlUtil.getFirstElement(documentElement, "memoryBacking", "hugepages").isPresent());

        int vcpu = Integer.parseInt(XmlUtil.getFirstElement(documentElement, "vcpu").orElseThrow().getTextContent());
        virtualMachine.setCores(vcpu);

        Element firstDevicesElement = XmlUtil.getFirstElement(documentElement, "devices").orElseThrow();

        Element firstVideoElement = XmlUtil.getFirstElement(firstDevicesElement, "video").orElseThrow();
        Element firstVideoModelElement = XmlUtil.getFirstElement(firstVideoElement, "model").orElseThrow();

        int videoRam = XmlUtil.getIntAttribute(firstVideoModelElement, "ram");
        virtualMachine.setVgaMemKb(videoRam);
        virtualMachine.setVideoModel(firstVideoModelElement.getAttribute("type"));

        Set<VolumeAttachmentPO> volumeAttachments = new HashSet<>();

        NodeList diskElements = firstDevicesElement.getElementsByTagName("disk");
        for (int i = 0; i < diskElements.getLength(); i++) {
            Element diskElement = (Element) diskElements.item(i);
            Element firstSourceElement = XmlUtil.getFirstElement(diskElement, "source").orElse(null);
            if (firstSourceElement == null) {
                continue;
            }
            String sdev = firstSourceElement.getAttribute("dev");

            List<VolumePO> volumes = volumeRepository.findByHostAndDev(virtualMachine.getHypervisor().getHost().getName(), sdev);
            if (volumes.size() == 1) {
                VolumePO volume = volumes.get(0);

                Element firstTargetElement = (Element) diskElement.getElementsByTagName("target").item(0);
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
    }

}
