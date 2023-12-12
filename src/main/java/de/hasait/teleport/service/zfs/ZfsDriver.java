/*
 * Copyright (C) 2023 by Sebastian Hasait (sebastian at hasait dot de)
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

package de.hasait.teleport.service.zfs;

import de.hasait.common.util.cli.CliExecutor;
import de.hasait.teleport.CliConfig;
import de.hasait.teleport.api.CreateVolumeGroupTO;
import de.hasait.teleport.api.StorageDriver;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VolumeGroupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ZfsDriver implements StorageDriver {

     static final String DRIVER_ID = "zfs";

    private static final String RESOURCE_REGEX = "[^/]+";
    private static final String RESOURCE_SNAPSHOT_REGEX = "(?<r>" + RESOURCE_REGEX + ")@(?<rs>[^/]+)";
    private static final String VOLUME_REGEX = "(?<r>" + RESOURCE_REGEX + ")/v(?<v>\\d+)";
    private static final String VOLUME_SNAPSHOT_REGEX = "(?<rv>" + VOLUME_REGEX + ")@(?<vs>[^/]+)";
    private static final Pattern RESOURCE_PATTERN = Pattern.compile(RESOURCE_REGEX);
    private static final Pattern RESOURCE_SNAPSHOT_PATTERN = Pattern.compile(RESOURCE_SNAPSHOT_REGEX);
    private static final Pattern VOLUME_PATTERN = Pattern.compile(VOLUME_REGEX);
    private static final Pattern VOLUME_SNAPSHOT_PATTERN = Pattern.compile(VOLUME_SNAPSHOT_REGEX);

    private static Long parseZfsPropAsOptionalLong(String raw) {
        return "-".equals(raw) ? null : Long.parseLong(raw);
    }

    private static LocalDateTime parseZfsPropAsLocalDateTime(String raw) {
        long rawCreation = Long.parseLong(raw);
        return LocalDateTime.ofEpochSecond(rawCreation, 0, ZoneOffset.UTC);
    }

    private static VolumeGroupState parseZfsPropZrmanState(String raw) {
        if ("-".equals(raw)) {
            return null;
        }
        return VolumeGroupState.valueOfExternalValue(raw);
    }

    private static final ZfsProperty<String> ZFS_PROP___NAME = new ZfsProperty<>("name", Function.identity());
    private static final ZfsProperty<LocalDateTime> ZFS_PROP___CREATION = new ZfsProperty<>("creation", ZfsDriver::parseZfsPropAsLocalDateTime);
    private static final ZfsProperty<Long> ZFS_PROP___AVAILABLE = new ZfsProperty<>("available", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<Long> ZFS_PROP___VOLSIZE = new ZfsProperty<>("volsize", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<Long> ZFS_PROP___USED = new ZfsProperty<>("used", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<Long> ZFS_PROP___USED_BY_DATASET = new ZfsProperty<>("usedbydataset", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<VolumeGroupState> ZFS_PROP___ZRMAN___STATE = new ZfsProperty<>("zrman:state", ZfsDriver::parseZfsPropZrmanState);
    private static final List<ZfsProperty<?>> zfsProperties = List.of( //
            ZFS_PROP___NAME, //
            ZFS_PROP___CREATION, //
            ZFS_PROP___AVAILABLE, //
            ZFS_PROP___VOLSIZE, //
            ZFS_PROP___USED, //
            ZFS_PROP___USED_BY_DATASET, //
            ZFS_PROP___ZRMAN___STATE //
    );
    private static final String zfsPropertiesCsv = zfsProperties.stream().map(ZfsProperty::getName).collect(Collectors.joining(","));

    private static final String ZFS_PROP_NAME_PREFIX___ZRMAN___LAST_SNAP_ON = "zrman:last_snap_on_";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CliConfig cliConfig;

    public ZfsDriver(@Autowired CliConfig cliConfig) {
        this.cliConfig = cliConfig;
    }

    @Override
    public String getId() {
        return DRIVER_ID;
    }

    @Override
    public void populateHypervisor(Hypervisor hypervisor, StorageBaseConfig config) {
        String baseZfsDataset = Utils.expectString(config.driverSpecific, "dataset");
        ZfsStorageBase base = getOrCreateZfsStorageBase(hypervisor, config, baseZfsDataset);
        refreshBase(base, true);
    }

    @Override
    public void create(StoragePO storage, CreateVolumeGroupTO config) {
        log.info("Creating VolumeGroup {} at {}...", config.getName(), storage);

        CliExecutor exe = cliConfig.createCliConnector(storage);

        List<String> zfsCreateResourceCommand = new ArrayList<>();
        zfsCreateResourceCommand.add("zfs");
        zfsCreateResourceCommand.add("create");
        zfsCreateResourceCommand.add(ZfsUtils.determineZfsDataset(base, config.name));

        boolean dryMode = !exe.executeAndWaitExit0(zfsCreateResourceCommand, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            new Resource(base, config.name, VolumeGroupState.INACTIVE, 0, 0);
        }

        log.info("Resource {} created at {}", config.name, base);

        if (!dryMode) {
            refreshBase((ZfsStorageBase) base, false);
        }

        Resource resource = base.getResourceContainer().getNotNull(config.name);

        int number = 0;
        for (int sizeMb : config.storage.sizesMb) {
            List<String> zfsCreateVolumeCommand = new ArrayList<>();
            zfsCreateVolumeCommand.add("zfs");
            zfsCreateVolumeCommand.add("create");
            zfsCreateVolumeCommand.add("-o");
            zfsCreateVolumeCommand.add("volmode=default");
            zfsCreateVolumeCommand.add("-s");
            zfsCreateVolumeCommand.add("-V");
            zfsCreateVolumeCommand.add(sizeMb + "M");
            zfsCreateVolumeCommand.add("-b");
            zfsCreateVolumeCommand.add("128k");
            zfsCreateVolumeCommand.add(ZfsUtils.determineZfsVolume(resource, number));
            exe.executeAndWaitExit0(zfsCreateVolumeCommand, 1, TimeUnit.MINUTES, true);
            if (dryMode) {
                new Volume(resource, number, sizeMb * 1024L * 1024L, 0);
            }

            log.info("Volume {} created for {}", number, resource);

            number++;
        }

        if (!dryMode) {
            refreshBase((ZfsStorageBase) base, false);
        }
    }

    @Override
    public CanResult canUpdate(Resource resource) {
        if (resource.isActive()) {
            return CanResult.invalid("Resource " + resource + " is active");
        }

        for (Volume volume : resource.getVolumeContainer()) {
            Long cfgSizeBytes = volume.determineCfgSizeBytes();
            if (cfgSizeBytes != null && cfgSizeBytes != volume.getSizeBytes()) {
                return CanResult.valid();
            }
        }

        return CanResult.hasNoEffect("Resource " + resource + " already up-to-date");
    }

    @Override
    public boolean update(Resource resource) {
        CanResult canResult = canUpdate(resource);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        activate(resource);

        CliExecutor exe = cliConfig.createCliConnector(resource);

        for (Volume volume : resource.getVolumeContainer()) {
            Long cfgSizeBytes = volume.determineCfgSizeBytes();
            if (cfgSizeBytes != null && cfgSizeBytes != volume.getSizeBytes()) {
                List<String> zfsCreateVolumeCommand = new ArrayList<>();
                zfsCreateVolumeCommand.add("zfs");
                zfsCreateVolumeCommand.add("set");
                zfsCreateVolumeCommand.add("volsize=" + cfgSizeBytes);
                zfsCreateVolumeCommand.add("volmode=default");
                zfsCreateVolumeCommand.add(ZfsUtils.determineZfsVolume(volume));
                boolean dryMode = !exe.executeAndWaitExit0(zfsCreateVolumeCommand, 1, TimeUnit.MINUTES, true);
                if (dryMode) {
                    volume.setSizeBytes(cfgSizeBytes);
                }

                log.info("Volume {} updated", volume);
            }
        }

        deactivate(resource);

        return true;
    }

    @Override
    public String determineDevice(Volume volume) {
        return ZfsUtils.determineDevice(volume);
    }

    @Override
    public void activate(Resource resource) {
        changeVolumeGroupState(resource, VolumeGroupState.ACTIVE, false);
    }

    @Override
    public void deactivate(Resource resource) {
        // this check is needed to prevent changing INACTIVE to DIRTY
        if (!resource.isActive()) {
            throw new IllegalStateException("Resource is not active: " + resource);
        }

        changeVolumeGroupState(resource, VolumeGroupState.DIRTY, true);
    }

    @Override
    public void delete(Resource resource) {
        if (resource.isActiveOrDirty()) {
            throw new IllegalStateException("Resource is active or dirty: " + resource);
        }

        log.info("Deleting Resource {}...", resource);

        CliExecutor exe = cliConfig.createCliConnector(resource);

        List<String> zfsDestroyCommand = new ArrayList<>();
        zfsDestroyCommand.add("zfs");
        zfsDestroyCommand.add("destroy");
        zfsDestroyCommand.add("-r");
        zfsDestroyCommand.add(ZfsUtils.determineZfsObject(resource));

        boolean dryMode = !exe.executeAndWaitExit0(zfsDestroyCommand, 5, TimeUnit.MINUTES, true);
        if (dryMode) {
            resource.getParent().getResourceContainer().remove(resource);
        }

        log.info("Resource {} deleted", resource);

        if (!dryMode) {
            refreshBase((ZfsStorageBase) resource.getBase(), true);
        }
    }

    @Override
    public boolean forceVolumeGroupState(Resource resource, VolumeGroupState state) {
        if (state == null) {
            throw new NullPointerException("state");
        }
        if (resource.getState() == state) {
            return false;
        }
        if (state == VolumeGroupState.ACTIVE) {
            activate(resource);
        } else {
            changeVolumeGroupState(resource, state, true);
        }
        return true;
    }

    @Override
    public void takeSnapshot(Resource resource, SnapshotData snapshotData) {
        String snapshotFullName = resource + "@" + snapshotData.getName();
        if (resource.getSnapshotContainer().containsKey(snapshotData.getName())) {
            throw new RuntimeException("Snapshot already exists: " + snapshotFullName);
        }

        if (resource.isActive() && snapshotData.isConsistent()) {
            throw new IllegalArgumentException("Cannot create consistent Snapshot for active Resource: " + snapshotFullName);
        }
        if (resource.isDirtyOrInactive() && !snapshotData.isConsistent()) {
            throw new IllegalArgumentException("Snapshot is marked as inconsistent although Resource is not active: " + snapshotFullName);
        }

        if (resource.isInactive()) {
            log.warn("Taking recursive Snapshot {} of inactive Resource...", snapshotFullName);
        } else {
            log.info("Taking recursive Snapshot {}...", snapshotFullName);
        }

        CliExecutor exe = cliConfig.createCliConnector(resource);

        List<String> zfsSnapshotCommand = new ArrayList<>();
        zfsSnapshotCommand.add("zfs");
        zfsSnapshotCommand.add("snapshot");
        zfsSnapshotCommand.add("-r");
        zfsSnapshotCommand.add(ZfsUtils.determineZfsObject(resource, snapshotData));

        boolean dryMode = !exe.executeAndWaitExit0(zfsSnapshotCommand, 30, TimeUnit.SECONDS, true);
        if (dryMode) {
            LocalDateTime creation = LocalDateTime.now();
            new ResourceSnapshot(resource, snapshotData.getName(), 0, creation);
            resource.getVolumeContainer().forEach(volume -> new VolumeSnapshot(volume, snapshotData.getName(), 0, creation));
        }

        log.info("Snapshot {} recursively created", snapshotFullName);

        if (resource.getState() == VolumeGroupState.DIRTY) {
            changeVolumeGroupState(resource, VolumeGroupState.INACTIVE, null);
        } else {
            if (!dryMode) {
                refreshResource(resource, false);
            }
        }
    }

    @Override
    public void deleteSnapshot(ResourceSnapshot snapshot) {
        deleteSnapshot(snapshot, true);
        for (Volume volume : snapshot.getTarget().getVolumeContainer()) {
            VolumeSnapshot volumeSnapshot = volume.getSnapshotContainer().get(snapshot.getName());
            if (volumeSnapshot != null) {
                deleteSnapshot(volumeSnapshot, true);
            }
        }
        if (!cliConfig.isDryMode()) {
            refreshResource(snapshot.getTarget(), true);
        }
    }

    @Override
    public void syncResourceIncr(ResourceSnapshot sender, ResourceSnapshot receiver) {
        syncIncr(sender, receiver, () -> {
            new ResourceSnapshot(receiver.getTarget(), sender.getName(), sender.getUsed(), LocalDateTime.now());
        });
    }

    @Override
    public void syncVolumeIncr(VolumeSnapshot sender, VolumeSnapshot receiver) {
        syncIncr(sender, receiver, () -> {
            new VolumeSnapshot(receiver.getTarget(), sender.getName(), sender.getUsed(), LocalDateTime.now());
        });
    }

    private <PP extends HasSnapshots<PP, P, S>, P extends AbstractSnapshotContainer<PP, P, S>, S extends AbstractSnapshot<PP, P, S>> void syncIncr(S sender, S receiver, Runnable dryModeLogic) {
        var senderCommon = sender.getParent().getNotNull(receiver.getName());

        log.info("Incremental syncing Snapshot {} -> {}...", sender, receiver);

        CliExecutor exe = cliConfig.createCliConnector(sender);

        StringBuilder bashZfsPipe = new StringBuilder();
        bashZfsPipe.append("zfs send -c -v -i");
        bashZfsPipe.append(" '").append(ZfsUtils.determineZfsObject(senderCommon)).append("'");
        bashZfsPipe.append(" '").append(ZfsUtils.determineZfsObject(sender)).append("'");
        bashZfsPipe.append(" | ");
        cliConfig.appendSsh(sender, receiver, bashZfsPipe);
        bashZfsPipe.append("zfs recv -F -v");
        bashZfsPipe.append(" '").append(ZfsUtils.determineZfsObject(receiver.getTarget())).append("'");

        List<String> bashCommand = new ArrayList<>();
        bashCommand.add("bash");
        bashCommand.add("-s");

        boolean dryMode = !exe.executeAndWaitExit0(bashCommand, new WriteBytesPIOStrategy(bashZfsPipe.toString()), InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, Long.MAX_VALUE, TimeUnit.MILLISECONDS, true);
        if (dryMode) {
            dryModeLogic.run();
        }

        log.info("Incremental sync completed: {} -> {}", sender, receiver);

        if (!dryMode) {
            refreshResource(receiver.getTarget().getResource(), false);
        }
    }

    @Override
    public void syncResourceFull(ResourceSnapshot sender, AbstractStorageBase receiver) {
        syncFull(sender, (ZfsStorageBase) receiver, () -> {
            Resource senderResource = sender.getTarget();
            Resource receiverResource = new Resource(receiver, senderResource.getName(), VolumeGroupState.INACTIVE, senderResource.getAvailBytes(), senderResource.getUsedBytes());
            new ResourceSnapshot(receiverResource, sender.getName(), sender.getUsed(), LocalDateTime.now());
        });
    }

    @Override
    public void syncVolumeFull(VolumeSnapshot sender, Resource receiver) {
        syncFull(sender, (ZfsStorageBase) receiver.getBase(), () -> {
            Volume senderVolume = sender.getTarget();
            Volume receiverVolume = new Volume(receiver, senderVolume.getNumber(), senderVolume.getSizeBytes(), senderVolume.getUsedBytes());
            new VolumeSnapshot(receiverVolume, sender.getName(), sender.getUsed(), LocalDateTime.now());
        });
    }

    private <PP extends HasSnapshots<PP, P, S>, P extends AbstractSnapshotContainer<PP, P, S>, S extends AbstractSnapshot<PP, P, S>> void syncFull(S sender, ZfsStorageBase receiver, Runnable dryModeLogic) {
        log.info("Full syncing Snapshot {} -> {}...", sender, receiver);

        CliExecutor sexe = cliConfig.createCliConnector(sender);

        StringBuilder bashZfsPipe = new StringBuilder();
        bashZfsPipe.append("zfs send -c -v");
        bashZfsPipe.append(" '").append(ZfsUtils.determineZfsObject(sender)).append("'");
        bashZfsPipe.append(" | ");
        cliConfig.appendSsh(sender, receiver, bashZfsPipe);
        bashZfsPipe.append("zfs recv -v");
        bashZfsPipe.append(" '").append(ZfsUtils.determineZfsObjectForOtherBase(sender.getTarget(), receiver)).append("'");

        List<String> bashCommand = new ArrayList<>();
        bashCommand.add("bash");
        bashCommand.add("-s");

        boolean dryMode = !sexe.executeAndWaitExit0(bashCommand, new WriteBytesPIOStrategy(bashZfsPipe.toString()), InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, Long.MAX_VALUE, TimeUnit.MILLISECONDS, true);
        if (dryMode) {
            dryModeLogic.run();
        }

        log.info("Full sync completed: {} -> {}", sender, receiver);

        if (!dryMode) {
            refreshBase(receiver, false);
        }
    }

    private void refreshBase(ZfsStorageBase base, boolean clear) {
        if (clear) {
            base.getResourceContainer().clear();
        }
        refresh(base, base.getZfsDataset());
    }

    private void refreshResource(Resource resource, boolean clear) {
        if (clear) {
            resource.getVolumeContainer().clear();
            resource.getSnapshotContainer().clear();
        }
        ZfsStorageBase base = (ZfsStorageBase) resource.getBase();
        String zfsDataset = ZfsUtils.determineZfsDataset(resource);
        refresh(base, zfsDataset);
    }

    private void refresh(ZfsStorageBase base, String zfsDataset) {
        CliExecutor exe = cliConfig.createCliConnector(base);

        ZfsUtils.zfsListAll(exe, zfsDataset, zfsPropertiesCsv, line -> processListLine(base, line));
        ZfsUtils.zfsGetR(exe, zfsDataset, line -> processGetLine(base, line));
    }

    private ZfsStorageBase getOrCreateZfsStorageBase(Hypervisor hypervisor, StorageBaseConfig config, String baseZfsDataset) {
        ZfsUtils.validateZfsDataset(baseZfsDataset);
        return new ZfsStorageBase(hypervisor, this, baseZfsDataset, config);
    }

    private void processListLine(ZfsStorageBase base, String line) {
        try {
            Map<String, Object> properties = new LinkedHashMap<>();

            String[] tokens = line.split("\t");
            int i = 0;
            for (ZfsProperty<?> zfsProperty : zfsProperties) {
                zfsProperty.putInto(properties, tokens[i++]);
            }

            String baseZfsDataset = base.getZfsDataset();
            String zfsObject = ZFS_PROP___NAME.getFrom(properties);

            if (zfsObject.equals(baseZfsDataset)) {
                return;
            }

            if (!zfsObject.startsWith(baseZfsDataset + "/")) {
                throw new IllegalArgumentException("Invalid ZFS object: " + zfsObject + " vs. base " + baseZfsDataset);
            }

            String remaining = zfsObject.substring(baseZfsDataset.length() + 1);

            Matcher volumeSnapshotMatcher = VOLUME_SNAPSHOT_PATTERN.matcher(remaining);
            if (volumeSnapshotMatcher.matches()) {
                Resource resource = base.getResourceContainer().getNotNull(volumeSnapshotMatcher.group("r"));
                int number = Integer.parseInt(volumeSnapshotMatcher.group("v"));
                Volume volume = resource.getVolumeContainer().getNotNull(number);
                String name = volumeSnapshotMatcher.group("vs");
                if (SnapshotNameGenerator.NAME_PATTERN.matcher(name).matches()) {
                    VolumeSnapshot existingSnapshot = volume.getSnapshotContainer().get(name);
                    VolumeSnapshot snapshot;
                    Long used = ZFS_PROP___USED.getFrom(properties);
                    if (existingSnapshot != null) {
                        existingSnapshot.setUsed(used);
                        snapshot = existingSnapshot;
                    } else {
                        LocalDateTime creation = ZFS_PROP___CREATION.getFrom(properties);
                        snapshot = new VolumeSnapshot(volume, name, used, creation);
                    }
                    return;
                }
                // foreign snapshot ignored;
                log.warn("Ignored foreign snapshot: {}", name);
                return;
            }
            Matcher volumeMatcher = VOLUME_PATTERN.matcher(remaining);
            if (volumeMatcher.matches()) {
                Resource resource = base.getResourceContainer().getNotNull(volumeMatcher.group("r"));
                int number = Integer.parseInt(volumeMatcher.group("v"));
                Volume existingVolume = resource.getVolumeContainer().get(number);
                Volume volume;
                Long volsize = ZFS_PROP___VOLSIZE.getFrom(properties);
                Long usedbydataset = ZFS_PROP___USED_BY_DATASET.getFrom(properties);
                if (volsize == null) {
                    throw new IllegalArgumentException("volsize missing");
                }
                if (usedbydataset == null) {
                    throw new IllegalArgumentException("usedbydataset missing");
                }
                if (existingVolume != null) {
                    existingVolume.setSizeBytes(volsize);
                    existingVolume.setUsedBytes(usedbydataset);
                    volume = existingVolume;
                } else {
                    volume = new Volume(resource, number, volsize, usedbydataset);
                }
                return;
            }
            Matcher resourceSnapshotMatcher = RESOURCE_SNAPSHOT_PATTERN.matcher(remaining);
            if (resourceSnapshotMatcher.matches()) {
                Resource resource = base.getResourceContainer().getNotNull(resourceSnapshotMatcher.group("r"));
                String name = resourceSnapshotMatcher.group("rs");
                if (SnapshotNameGenerator.NAME_PATTERN.matcher(name).matches()) {
                    ResourceSnapshot existingSnapshot = resource.getSnapshotContainer().get(name);
                    ResourceSnapshot snapshot;
                    Long used = ZFS_PROP___USED.getFrom(properties);
                    if (existingSnapshot != null) {
                        existingSnapshot.setUsed(used);
                        snapshot = existingSnapshot;
                    } else {
                        LocalDateTime creation = ZFS_PROP___CREATION.getFrom(properties);
                        snapshot = new ResourceSnapshot(resource, name, used, creation);
                    }
                    return;
                }
                // foreign snapshot ignored;
                log.warn("Ignored foreign snapshot: {}", name);
                return;
            }
            if (RESOURCE_PATTERN.matcher(remaining).matches()) {
                Resource existingResource = base.getResourceContainer().get(remaining);
                VolumeGroupState resourceState = Optional.ofNullable(ZFS_PROP___ZRMAN___STATE.getFrom(properties)).orElse(VolumeGroupState.INACTIVE);
                Resource resource;
                Long used = ZFS_PROP___USED.getFrom(properties);
                Long available = ZFS_PROP___AVAILABLE.getFrom(properties);
                if (available == null) {
                    throw new IllegalArgumentException("available missing");
                }
                if (existingResource != null) {
                    existingResource.setState(resourceState);
                    existingResource.setAvailBytes(available);
                    existingResource.setUsedBytes(used);
                    resource = existingResource;
                } else {
                    resource = new Resource(base, remaining, resourceState, available, used);
                }
                return;
            }

            throw new IllegalArgumentException("Unknown zfsObject: " + zfsObject + ", remaining: " + remaining);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Cannot process list line: " + line, e);
        }
    }

    private void processGetLine(ZfsStorageBase base, String line) {
        try {
            String baseZfsDataset = base.getZfsDataset();

            String[] tokens = line.split("\t");

            int tokenI = 0;
            String zfsObject = tokens[tokenI++];
            String property = tokens[tokenI++];
            String value = tokens[tokenI++];

            if (zfsObject.equals(baseZfsDataset)) {
                return;
            }

            if (!zfsObject.startsWith(baseZfsDataset + "/")) {
                throw new IllegalArgumentException("Invalid ZFS object: " + zfsObject + " vs. base " + baseZfsDataset);
            }

            String remaining = zfsObject.substring(baseZfsDataset.length() + 1);
            Resource existingResource = base.getResourceContainer().get(remaining);
            if (existingResource != null) {
                if (property.startsWith(ZFS_PROP_NAME_PREFIX___ZRMAN___LAST_SNAP_ON)) {
                    String hvName = property.substring(ZFS_PROP_NAME_PREFIX___ZRMAN___LAST_SNAP_ON.length());
                    existingResource.setLatestSnapshotNameOnHv(hvName, value);
                    return;
                }
            }

            log.debug("processGetLine - ignored: {}, {}, {}", zfsObject, property, value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Cannot process line: " + line, e);
        }
    }

    private void changeVolumeGroupState(Resource resource, VolumeGroupState newState, Boolean readonly) {
        if (resource.getState() == newState) {
            throw new IllegalStateException("Resource is already " + newState + ": " + resource);
        }

        log.info("Changing state of Resource {} to {}...", resource, newState);

        CliExecutor exe = cliConfig.createCliConnector(resource);

        List<String> zfsSetCommand = new ArrayList<>();
        zfsSetCommand.add("zfs");
        zfsSetCommand.add("set");
        if (readonly != null) {
            zfsSetCommand.add("readonly=" + (readonly ? "on" : "off"));
        }
        zfsSetCommand.add(ZFS_PROP___ZRMAN___STATE.getName() + "=" + newState.getExternalValue());
        zfsSetCommand.add(ZfsUtils.determineZfsObject(resource));

        boolean dryMode = !exe.executeAndWaitExit0(zfsSetCommand, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            resource.setState(newState);
        }

        log.info("Resource {} set to {}", resource, newState);

        if (!dryMode) {
            refreshResource(resource, false);
        }
    }

    public <PP extends HasSnapshots<PP, P, S>, P extends AbstractSnapshotContainer<PP, P, S>, S extends AbstractSnapshot<PP, P, S>> void deleteSnapshot(S snapshot, boolean skipRefresh) {
        log.info("Deleting Snapshot {}...", snapshot);

        CliExecutor exe = cliConfig.createCliConnector(snapshot);

        List<String> zfsSnapshotCommand = new ArrayList<>();
        zfsSnapshotCommand.add("zfs");
        zfsSnapshotCommand.add("destroy");
        zfsSnapshotCommand.add(ZfsUtils.determineZfsObject(snapshot));

        boolean dryMode = !exe.executeAndWaitExit0(zfsSnapshotCommand, 30, TimeUnit.SECONDS, true);
        if (dryMode) {
            snapshot.getParent().remove(snapshot);
        }

        log.info("Snapshot {} deleted", snapshot);

        if (!dryMode && !skipRefresh) {
            refreshResource(snapshot.getTarget().getResource(), true);
        }
    }

    @Override
    public void storeLastUsedSnapshot(Resource resource, String hvName, String snapshotName) {
        CliExecutor exe = cliConfig.createCliConnector(resource);

        List<String> zfsSetCommand = new ArrayList<>();
        zfsSetCommand.add("zfs");
        zfsSetCommand.add("set");
        zfsSetCommand.add(ZFS_PROP_NAME_PREFIX___ZRMAN___LAST_SNAP_ON + hvName + "=" + snapshotName);
        zfsSetCommand.add(ZfsUtils.determineZfsObject(resource));

        boolean dryMode = !exe.executeAndWaitExit0(zfsSetCommand, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            resource.setLatestSnapshotNameOnHv(hvName, snapshotName);
        }

        log.debug("Set last used snapshot for Resource {} to {}", resource, snapshotName);

        if (!dryMode) {
            refreshResource(resource, false);
        }
    }

}
