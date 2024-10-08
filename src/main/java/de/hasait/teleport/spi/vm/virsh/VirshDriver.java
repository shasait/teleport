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
import de.hasait.common.service.AbstractRefreshableDriver;
import de.hasait.common.util.Util;
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.common.util.cli.InheritPIOStrategy;
import de.hasait.common.util.xml.XmlDocument;
import de.hasait.common.util.xml.XmlElement;
import de.hasait.common.util.xml.XmlElements;
import de.hasait.teleport.CliConfig;
import de.hasait.teleport.api.VmCreateTO;
import de.hasait.teleport.api.VmState;
import de.hasait.teleport.api.VolumeCreateTO;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.NetworkInterfacePO;
import de.hasait.teleport.domain.NetworkPO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeRepository;
import de.hasait.teleport.service.storage.StorageService;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class VirshDriver extends AbstractRefreshableDriver<HypervisorPO, VirshDriverConfig> implements HypervisorDriver {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CliConfig cliConfig;
    private final VolumeRepository volumeRepository;
    private final VirtualMachineRepository virtualMachineRepository;
    private final StorageService storageService;

    public VirshDriver(CliConfig cliConfig, VolumeRepository volumeRepository, VirtualMachineRepository virtualMachineRepository, StorageService storageService) {
        super("virsh", "Virsh Hypervisor Driver", null);

        this.cliConfig = cliConfig;
        this.volumeRepository = volumeRepository;
        this.virtualMachineRepository = virtualMachineRepository;
        this.storageService = storageService;
    }

    @Override
    protected VirshDriverConfig parseConfigText(String configText) {
        return new Gson().fromJson(configText, VirshDriverConfig.class);
    }

    @Override
    protected void refresh(HypervisorPO po, VirshDriverConfig config) {
        CliExecutor exe = cliConfig.createCliConnector(po);
        VirshUtils.virshListAll(exe).forEach(it -> processListEntry(po, config, exe, it));
    }

    @Override
    public VirtualMachinePO create(HypervisorPO hypervisor, VmCreateTO config, boolean runInstallation) {
        boolean dryMode;
        if (runInstallation) {
            throw new RuntimeException("NYI"); // TODO implement
        } else {
            dryMode = virshDefineFromTemplate(hypervisor, config);
        }

        if (dryMode) {
            new VirtualMachinePO(hypervisor, config.getName(), VmState.SHUTOFF);
        }

        log.info("VM {} created", config.getName());

        if (!dryMode) {
            refresh(hypervisor);
        }

        return hypervisor.findVirtualMachineByName(config.getName()).orElseThrow();
    }

    @Override
    public void start(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsRunning()) {
            throw new IllegalStateException("VM already running: " + virtualMachine);
        }

        if (virtualMachine.stateIsShutOff()) {
            update(virtualMachine);
        }

        log.info("Starting VM {}...", virtualMachine);

        CliExecutor exe = cliConfig.createCliConnector(virtualMachine);

        List<String> command = new ArrayList<>();
        command.add("virsh");
        command.add("start");
        command.add(virtualMachine.getName());

        boolean dryMode = !exe.executeAndWaitExit0(command, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            virtualMachine.setState(VmState.RUNNING);
        } else {
            waitForState(exe, virtualMachine, VmState.RUNNING);
        }

        log.info("VM {} started", virtualMachine);

        if (!dryMode) {
            refresh(virtualMachine.getHypervisor());
        }
    }

    @Override
    public void shutdown(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsShutOff()) {
            throw new IllegalStateException("VM already shut off: " + virtualMachine);
        }

        log.info("Shutting off VM {}...", virtualMachine);

        CliExecutor exe = cliConfig.createCliConnector(virtualMachine);

        List<String> command = new ArrayList<>();
        command.add("virsh");
        command.add("shutdown");
        command.add(virtualMachine.getName());

        boolean dryMode = !exe.executeAndWaitExit0(command, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            virtualMachine.setState(VmState.SHUTOFF);
        } else {
            waitForState(exe, virtualMachine, VmState.SHUTOFF);
        }

        log.info("VM {} shut off", virtualMachine);

        if (!dryMode) {
            refresh(virtualMachine.getHypervisor());
        }
    }

    @Override
    public void kill(VirtualMachinePO virtualMachine) {
        if (virtualMachine.stateIsShutOff()) {
            throw new IllegalStateException("VM already shut off: " + virtualMachine);
        }

        log.info("Killing VM {}...", virtualMachine);

        CliExecutor exe = cliConfig.createCliConnector(virtualMachine);

        List<String> command = new ArrayList<>();
        command.add("virsh");
        command.add("destroy");
        command.add(virtualMachine.getName());

        boolean dryMode = !exe.executeAndWaitExit0(command, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            virtualMachine.setState(VmState.SHUTOFF);
        } else {
            waitForState(exe, virtualMachine, VmState.SHUTOFF);
        }

        log.info("VM {} killed", virtualMachine);

        if (!dryMode) {
            refresh(virtualMachine.getHypervisor());
        }
    }

    @Override
    public void update(VirtualMachinePO virtualMachine) {

    }

    @Override
    public void delete(VirtualMachinePO virtualMachine) {

    }

    private void waitForState(CliExecutor exe, VirtualMachinePO virtualMachine, VmState successState) {
        while (true) {
            VmState state = VirshUtils.virshDomState(exe, virtualMachine.getName());
            if (state == successState) {
                break;
            }
            log.debug("VM {} is {}", virtualMachine.getName(), state);
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processListEntry(HypervisorPO hypervisor, VirshDriverConfig config, CliExecutor exe, VirshListE entry) {
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
                String tbus = firstTargetElement.getAttribute("bus");
                String tdev = firstTargetElement.getAttribute("dev");

                VolumeAttachmentPO existingVolumeAttachment = virtualMachine.findVolumeAttachmentByDev(tdev).orElse(null);
                VolumeAttachmentPO volumeAttachment;
                if (existingVolumeAttachment != null) {
                    volumeAttachment = existingVolumeAttachment;
                    volumeAttachment.setType(tbus);
                    volumeAttachment.setVolume(volume);
                } else {
                    volumeAttachment = new VolumeAttachmentPO(virtualMachine, tbus, tdev, volume);
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

    private boolean virshDefineFromTemplate(HypervisorPO hypervisor, VmCreateTO config) {
        String configXml;
        try {
            StringWriter configXmlStringWriter = new StringWriter();
            Version version = Configuration.VERSION_2_3_32;
            Configuration configuration = new Configuration(version);
            configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
            configuration.setClassForTemplateLoading(getClass(), "");
            configuration.setNumberFormat("c");
            BeansWrapperBuilder beansWrapperBuilder = new BeansWrapperBuilder(version);
            beansWrapperBuilder.setExposeFields(true);
            configuration.setObjectWrapper(beansWrapperBuilder.build());
            String type = "linux-64";
            Template template = configuration.getTemplate("config-" + type + ".xml.ftl");

            VirshCreateTemplateModel templateModel = new VirshCreateTemplateModel();
            templateModel.config = config;
            templateModel.cpu = new VirshCreateTemplateCpu();
            // TODO templateModel.cpu.topoextPolicy = hypervisor.getInfo().isTopoext() ? "require" : "optional";
            int vaIndex = 0;
            for (var vaConfig : config.getVolumeAttachments()) {
                VolumeCreateTO vConfig = new VolumeCreateTO();
                vConfig.setSizeBytes(vaConfig.getSizeBytes());
                vConfig.setStorageName(vaConfig.getStorageName());
                vConfig.setName(createVolumeName(config.getName(), vaIndex));
                VolumePO volume = storageService.createVolume(hypervisor.getHost(), vConfig);
                String devOnHv = volume.getDev();
                String devInVm = vaConfig.getDev();
                VirshCreateTemplateDisk disk = new VirshCreateTemplateDisk();
                disk.srcDev = devOnHv;
                disk.tgtDev = devInVm;
                templateModel.disks.add(disk);
                vaIndex++;
            }

            template.process(templateModel, configXmlStringWriter);
            configXml = configXmlStringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        CliExecutor exe = cliConfig.createCliConnector(hypervisor);
        String file = "/tmp/" + Util.nextRandomString(8) + ".xml";

        boolean dryMode = !exe.createOrUpdateFile(file, "root:root", "600", configXml, true);
        exe.executeAndWaitExit0(List.of("virsh", "define", file), InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, 30, TimeUnit.SECONDS, true);

        exe.executeAndWaitExit0(List.of("rm", "-v", file), InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, 30, TimeUnit.SECONDS, true);

        return dryMode;
    }

    private String createVolumeName(String vmName, int volumeNumber) {
        return vmName + "/v" + volumeNumber;
    }

}
