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

package de.hasait.teleport.service.storage.zfs;

import com.google.gson.Gson;
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.common.util.cli.InheritPIOStrategy;
import de.hasait.common.util.cli.WriteBytesPIOStrategy;
import de.hasait.teleport.CliConfig;
import de.hasait.teleport.api.CanResult;
import de.hasait.teleport.api.StorageDriver;
import de.hasait.teleport.api.VolumeTO;
import de.hasait.teleport.domain.HasStorage;
import de.hasait.teleport.domain.SnapshotData;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeRepository;
import de.hasait.teleport.domain.VolumeSnapshotPO;
import de.hasait.teleport.domain.VolumeSnapshotRepository;
import de.hasait.teleport.domain.VolumeState;
import de.hasait.teleport.service.SnapshotNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    public static final String DRIVER_ID = "zfs";

    private static ZfsDriverConfig parseConfig(String config) {
        ZfsDriverConfig zfsDriverConfig = new Gson().fromJson(config, ZfsDriverConfig.class);
        String dataset = zfsDriverConfig.getDataset();
        ZfsUtils.validateZfsDataset(dataset);
        return zfsDriverConfig;
    }

    public static String determineZfsDataset(StoragePO storage) {
        if (ZfsDriver.DRIVER_ID.equals(storage.getDriver())) {
            ZfsDriverConfig zfsDriverConfig = parseConfig(storage.getDriverConfig());
            return zfsDriverConfig.getDataset();
        }
        throw new IllegalArgumentException("Not a ZFS storage: " + storage);
    }

    public static String determineZfsVolume(VolumePO volume) {
        return determineZfsVolume(volume.getStorage(), volume.getName());
    }

    public static String determineZfsVolume(StoragePO storage, String volumeName) {
        return determineZfsDataset(storage) + "/" + volumeName;
    }

    public static String determineZfsObject(HasStorage hasStorage) {
        if (hasStorage instanceof StoragePO storage) {
            return determineZfsDataset(storage);
        }
        if (hasStorage instanceof VolumePO volume) {
            return determineZfsVolume(volume);
        }
        if (hasStorage instanceof VolumeSnapshotPO snapshot) {
            return determineZfsVolume(snapshot.getVolume()) + "@" + snapshot.getData().getName();
        }
        throw new RuntimeException("Unsupported hasStorage: " + hasStorage);
    }

    public static String determineZfsObject(HasStorage hasStorage, SnapshotData snapshotData) {
        if (hasStorage instanceof VolumePO) {
            return determineZfsObject(hasStorage) + "@" + snapshotData.getName();
        }
        throw new RuntimeException("Unsupported hasStorage: " + hasStorage);
    }

    private static final String VOLUME_REGEX = "(?<v>[^@]+)";
    private static final String VOLUME_SNAPSHOT_REGEX = VOLUME_REGEX + "@(?<vs>[^/]+)";
    private static final Pattern VOLUME_PATTERN = Pattern.compile(VOLUME_REGEX);
    private static final Pattern VOLUME_SNAPSHOT_PATTERN = Pattern.compile(VOLUME_SNAPSHOT_REGEX);

    private static Long parseZfsPropAsOptionalLong(String raw) {
        return "-".equals(raw) ? null : Long.parseLong(raw);
    }

    private static LocalDateTime parseZfsPropAsLocalDateTime(String raw) {
        long rawCreation = Long.parseLong(raw);
        return LocalDateTime.ofEpochSecond(rawCreation, 0, ZoneOffset.UTC);
    }

    private static VolumeState parseZfsPropZrmanState(String raw) {
        if ("-".equals(raw)) {
            return null;
        }
        return VolumeState.valueOfExternalValue(raw);
    }

    private static final ZfsProperty<String> ZFS_PROP___TYPE = new ZfsProperty<>("type", Function.identity());
    private static final ZfsProperty<String> ZFS_PROP___NAME = new ZfsProperty<>("name", Function.identity());
    private static final ZfsProperty<LocalDateTime> ZFS_PROP___CREATION = new ZfsProperty<>("creation", ZfsDriver::parseZfsPropAsLocalDateTime);
    private static final ZfsProperty<Long> ZFS_PROP___AVAILABLE = new ZfsProperty<>("available", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<Long> ZFS_PROP___VOLSIZE = new ZfsProperty<>("volsize", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<Long> ZFS_PROP___USED = new ZfsProperty<>("used", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<Long> ZFS_PROP___USED_BY_DATASET = new ZfsProperty<>("usedbydataset", ZfsDriver::parseZfsPropAsOptionalLong);
    private static final ZfsProperty<VolumeState> ZFS_PROP___ZRMAN___STATE = new ZfsProperty<>("zrman:state", ZfsDriver::parseZfsPropZrmanState);
    private static final List<ZfsProperty<?>> zfsProperties = List.of( //
            ZFS_PROP___TYPE, //
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
    private final VolumeRepository volumeRepository;
    private final VolumeSnapshotRepository volumeSnapshotRepository;

    public ZfsDriver(@Autowired CliConfig cliConfig, VolumeRepository volumeRepository, VolumeSnapshotRepository volumeSnapshotRepository) {
        this.cliConfig = cliConfig;
        this.volumeRepository = volumeRepository;
        this.volumeSnapshotRepository = volumeSnapshotRepository;
    }

    @Override
    @Nonnull
    public String getId() {
        return DRIVER_ID;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "ZFS Storage Driver";
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
    public void refresh(StoragePO storage) {
        refresh(storage, determineZfsDataset(storage));
        // TODO remove objects not found during refresh
    }

    @Override
    public void create(StoragePO storage, VolumeTO config) {
        log.info("Creating Volume {} at {}...", config.getName(), storage);

        CliExecutor exe = cliConfig.createCliConnector(storage);

        List<String> zfsCreateVolumeCommand = new ArrayList<>();
        zfsCreateVolumeCommand.add("zfs");
        zfsCreateVolumeCommand.add("create");
        zfsCreateVolumeCommand.add("-o");
        zfsCreateVolumeCommand.add("volmode=default");
        zfsCreateVolumeCommand.add("-s");
        zfsCreateVolumeCommand.add("-V");
        zfsCreateVolumeCommand.add(config.getSizeBytes() + "");
        zfsCreateVolumeCommand.add("-b");
        zfsCreateVolumeCommand.add("128k");
        zfsCreateVolumeCommand.add(determineZfsVolume(storage, config.getName()));
        boolean dryMode = !exe.executeAndWaitExit0(zfsCreateVolumeCommand, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            VolumePO volume = new VolumePO();
            volume.setName(config.getName());
            volume.setSizeBytes(config.getSizeBytes());
            volume.setUsedBytes(0);
            volume.setStorage(storage);
            storage.getVolumes().add(volume);
        }

        log.info("Volume {} created at {}", config.getName(), storage);

        if (!dryMode) {
            refresh(storage);
        }
    }

    @Override
    public CanResult canUpdate(VolumePO volume, VolumeTO config) {
        if (volume.isActive()) {
            return CanResult.invalid("Volume " + volume + " is active");
        }
        if (!volume.getName().equals(config.getName())) {
            return CanResult.invalid("Config name mismatch: " + config.getName() + " vs. " + volume.getName());
        }

        if (config.getSizeBytes() != volume.getSizeBytes()) {
            return CanResult.valid();
        }

        return CanResult.hasNoEffect("Volume " + volume + " already up-to-date");
    }

    @Override
    public boolean update(VolumePO volume, VolumeTO config) {
        CanResult canResult = canUpdate(volume, config);
        if (canResult.ensureValidAndReturnHasNoEffect()) {
            return false;
        }

        boolean dryMode = cliConfig.isDryMode();

        activate(volume);

        CliExecutor exe = cliConfig.createCliConnector(volume);

        if (config.getSizeBytes() != volume.getSizeBytes()) {
            List<String> zfsCreateVolumeCommand = new ArrayList<>();
            zfsCreateVolumeCommand.add("zfs");
            zfsCreateVolumeCommand.add("set");
            zfsCreateVolumeCommand.add("volsize=" + config.getSizeBytes());
            zfsCreateVolumeCommand.add("volmode=default");
            zfsCreateVolumeCommand.add(determineZfsVolume(volume));
            exe.executeAndWaitExit0(zfsCreateVolumeCommand, 1, TimeUnit.MINUTES, true);
            if (dryMode) {
                volume.setSizeBytes(config.getSizeBytes());
                volumeRepository.save(volume);
            }

            log.info("Volume {} updated", volume);
        }

        if (!dryMode) {
            refreshVolume(volume);
        }

        deactivate(volume);

        return true;
    }

    @Override
    public String determineDevice(VolumePO volume) {
        return "/dev/zvol/" + determineZfsVolume(volume);
    }

    @Override
    public void activate(VolumePO volume) {
        changeVolumeState(volume, VolumeState.ACTIVE, false);
    }

    @Override
    public void deactivate(VolumePO volume) {
        // this check is needed to prevent changing INACTIVE to DIRTY
        if (!volume.isActive()) {
            throw new IllegalStateException("Volume is not active: " + volume);
        }

        changeVolumeState(volume, VolumeState.DIRTY, true);
    }

    @Override
    public void delete(VolumePO volume) {
        if (volume.isActiveOrDirty()) {
            throw new IllegalStateException("Volume is active or dirty: " + volume);
        }

        log.info("Deleting Volume {}...", volume);

        StoragePO storage = volume.getStorage();

        CliExecutor exe = cliConfig.createCliConnector(volume);

        List<String> zfsDestroyCommand = new ArrayList<>();
        zfsDestroyCommand.add("zfs");
        zfsDestroyCommand.add("destroy");
        zfsDestroyCommand.add(determineZfsObject(volume));

        boolean dryMode = !exe.executeAndWaitExit0(zfsDestroyCommand, 5, TimeUnit.MINUTES, true);
        if (dryMode) {
            storage.getVolumes().remove(volume);
            volumeRepository.delete(volume);
        }

        log.info("Volume {} deleted", volume);

        if (!dryMode) {
            refresh(storage);
        }
    }

    @Override
    public boolean forceState(VolumePO volume, VolumeState state) {
        if (state == null) {
            throw new NullPointerException("state");
        }
        if (volume.getState() == state) {
            return false;
        }
        if (state == VolumeState.ACTIVE) {
            activate(volume);
        } else {
            changeVolumeState(volume, state, true);
        }
        return true;
    }

    @Override
    public void takeSnapshot(SnapshotData snapshotData, VolumePO... volumes) {
        StoragePO storage = null;
        List<String> zfsObjects = new ArrayList<>();

        for (VolumePO volume : volumes) {
            String snapshotFullName = volume + "@" + snapshotData.getName();
            if (volume.findSnapshotByName(snapshotData.getName()).isPresent()) {
                throw new RuntimeException("Snapshot already exists: " + snapshotFullName);
            }

            if (volume.isActive() && snapshotData.isConsistent()) {
                throw new IllegalArgumentException("Cannot create consistent Snapshot for active Volume: " + snapshotFullName);
            }
            if (volume.isDirtyOrInactive() && !snapshotData.isConsistent()) {
                throw new IllegalArgumentException("Snapshot is marked as inconsistent although Volume is not active: " + snapshotFullName);
            }

            if (storage == null) {
                storage = volume.getStorage();
            } else if (storage != volume.getStorage()) {
                throw new IllegalArgumentException("Cannot take atomic snapshot across different storages: " + snapshotFullName + " vs. " + storage);
            }
        }

        for (VolumePO volume : volumes) {
            String snapshotFullName = volume + "@" + snapshotData.getName();

            if (volume.isInactive()) {
                log.warn("Taking Snapshot {} of inactive Volume...", snapshotFullName);
            } else {
                log.info("Taking Snapshot {}...", snapshotFullName);
            }

            zfsObjects.add(determineZfsObject(volume, snapshotData));
        }

        if (storage == null) {
            throw new IllegalArgumentException("Could not determine storage for volumes: " + volumes);
        }

        CliExecutor exe = cliConfig.createCliConnector(storage);

        List<String> zfsSnapshotCommand = new ArrayList<>();
        zfsSnapshotCommand.add("zfs");
        zfsSnapshotCommand.add("snapshot");
        zfsSnapshotCommand.addAll(zfsObjects);

        boolean dryMode = !exe.executeAndWaitExit0(zfsSnapshotCommand, 30, TimeUnit.SECONDS, true);
        if (dryMode) {
            LocalDateTime creation = LocalDateTime.now();
            for (VolumePO volume : volumes) {
                new VolumeSnapshotPO(volume, snapshotData.getName(), 0, creation);
            }
        }

        log.info("Snapshots {} created", zfsObjects);

        if (!dryMode) {
            refresh(storage);
        }
    }

    @Override
    public void deleteSnapshot(VolumeSnapshotPO snapshot) {
        log.info("Deleting Snapshot {}...", snapshot);

        CliExecutor exe = cliConfig.createCliConnector(snapshot);

        List<String> zfsSnapshotCommand = new ArrayList<>();
        zfsSnapshotCommand.add("zfs");
        zfsSnapshotCommand.add("destroy");
        zfsSnapshotCommand.add(determineZfsObject(snapshot));

        boolean dryMode = !exe.executeAndWaitExit0(zfsSnapshotCommand, 30, TimeUnit.SECONDS, true);
        if (dryMode) {
            snapshot.getVolume().getSnapshots().remove(snapshot);
        }

        log.info("Snapshot {} deleted", snapshot);

        if (!dryMode) {
            refreshVolume(snapshot.obtainVolume());
        }
    }

    @Override
    public void syncVolumeIncr(VolumeSnapshotPO sender, VolumeSnapshotPO receiver) {
        VolumeSnapshotPO senderCommon = sender.getVolume().findSnapshotByName(receiver.getData().getName()).orElseThrow();

        log.info("Incremental syncing Snapshot {} -> {}...", sender, receiver);

        CliExecutor exe = cliConfig.createCliConnector(sender);

        StringBuilder bashZfsPipe = new StringBuilder();
        bashZfsPipe.append("zfs send -c -v -i");
        bashZfsPipe.append(" '").append(determineZfsObject(senderCommon)).append("'");
        bashZfsPipe.append(" '").append(determineZfsObject(sender)).append("'");
        bashZfsPipe.append(" | ");
        cliConfig.appendSsh(sender, receiver, bashZfsPipe);
        bashZfsPipe.append("zfs recv -F -v");
        bashZfsPipe.append(" '").append(determineZfsObject(receiver.getVolume())).append("'");

        List<String> bashCommand = new ArrayList<>();
        bashCommand.add("bash");
        bashCommand.add("-s");

        boolean dryMode = !exe.executeAndWaitExit0(bashCommand, new WriteBytesPIOStrategy(bashZfsPipe.toString()), InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, Long.MAX_VALUE, TimeUnit.MILLISECONDS, true);
        if (dryMode) {
            new VolumeSnapshotPO(receiver.getVolume(), sender.getData().getName(), sender.getUsedBytes(), LocalDateTime.now());
        }

        log.info("Incremental sync completed: {} -> {}", sender, receiver);

        if (!dryMode) {
            refreshVolume(receiver.obtainVolume());
        }
    }

    @Override
    public void syncVolumeFull(VolumeSnapshotPO sender, StoragePO receiverStorage, String receiverVolumeName) {
        log.info("Full syncing Snapshot {} -> {} as {}...", sender, receiverStorage, receiverVolumeName);

        CliExecutor sexe = cliConfig.createCliConnector(sender);

        StringBuilder bashZfsPipe = new StringBuilder();
        bashZfsPipe.append("zfs send -c -v");
        bashZfsPipe.append(" '").append(determineZfsObject(sender)).append("'");
        bashZfsPipe.append(" | ");
        cliConfig.appendSsh(sender, receiverStorage, bashZfsPipe);
        bashZfsPipe.append("zfs recv -v");
        bashZfsPipe.append(" '").append(determineZfsVolume(receiverStorage, receiverVolumeName)).append("'");

        List<String> bashCommand = new ArrayList<>();
        bashCommand.add("bash");
        bashCommand.add("-s");

        boolean dryMode = !sexe.executeAndWaitExit0(bashCommand, new WriteBytesPIOStrategy(bashZfsPipe.toString()), InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, Long.MAX_VALUE, TimeUnit.MILLISECONDS, true);
        if (dryMode) {
            VolumePO senderVolume = sender.getVolume();
            VolumePO receiverVolume = new VolumePO(receiverStorage, receiverVolumeName, senderVolume.getSizeBytes(), VolumeState.INACTIVE, senderVolume.getUsedBytes());
            new VolumeSnapshotPO(receiverVolume, sender.getData().getName(), sender.getUsedBytes(), LocalDateTime.now());
        }

        log.info("Full sync completed: {} -> {} as {}", sender, receiverStorage, receiverVolumeName);

        if (!dryMode) {
            refresh(receiverStorage);
        }
    }

    private void refreshVolume(VolumePO volume) {
        StoragePO storage = volume.getStorage();
        refresh(storage, determineZfsVolume(volume));
        // TODO remove objects not found during refresh
    }

    private void refresh(StoragePO storage, String zfsObject) {
        CliExecutor exe = cliConfig.createCliConnector(storage);

        ZfsUtils.zfsListAll(exe, zfsObject, zfsPropertiesCsv, line -> processListLine(storage, line));
    }

    private void processListLine(StoragePO storage, String line) {
        try {
            Map<String, Object> properties = new LinkedHashMap<>();

            String[] tokens = line.split("\t");
            int i = 0;
            for (ZfsProperty<?> zfsProperty : zfsProperties) {
                zfsProperty.putInto(properties, tokens[i++]);
            }

            String storageZfsDataset = determineZfsDataset(storage);
            String zfsObject = ZFS_PROP___NAME.getFrom(properties);

            if (zfsObject.equals(storageZfsDataset)) {
                return;
            }

            if (!zfsObject.startsWith(storageZfsDataset + "/")) {
                throw new IllegalArgumentException("Invalid ZFS object: " + zfsObject + " vs. storage " + storageZfsDataset);
            }

            String relative = zfsObject.substring(storageZfsDataset.length() + 1);

            String zfsType = ZFS_PROP___TYPE.getFrom(properties);

            if (zfsType.equals("snapshot")) {
                Matcher volumeSnapshotMatcher = VOLUME_SNAPSHOT_PATTERN.matcher(relative);
                if (volumeSnapshotMatcher.matches()) {
                    String volumeName = volumeSnapshotMatcher.group("v");
                    String snapshotName = volumeSnapshotMatcher.group("vs");
                    VolumePO volume = storage.findVolumeByName(volumeName).orElse(null);
                    if (volume != null) {
                        if (SnapshotNameGenerator.NAME_PATTERN.matcher(snapshotName).matches()) {
                            VolumeSnapshotPO existingSnapshot = volume.findSnapshotByName(snapshotName).orElse(null);
                            VolumeSnapshotPO snapshot;
                            LocalDateTime creation = ZFS_PROP___CREATION.getFrom(properties);
                            if (creation == null) {
                                throw new IllegalArgumentException("creation missing");
                            }
                            Long used = ZFS_PROP___USED.getFrom(properties);
                            if (used == null) {
                                throw new IllegalArgumentException("used missing");
                            }
                            if (existingSnapshot != null) {
                                snapshot = existingSnapshot;
                                snapshot.getData().setCreation(creation);
                            } else {
                                snapshot = new VolumeSnapshotPO();
                                snapshot.setVolume(volume);
                                snapshot.setData(new SnapshotData(snapshotName, creation));
                            }
                            snapshot.setUsedBytes(used);
                            volumeSnapshotRepository.save(snapshot);
                            return;
                        }
                    }
                }
            }

            if (zfsType.equals("volume")) {
                Matcher volumeMatcher = VOLUME_PATTERN.matcher(relative);
                if (volumeMatcher.matches()) {
                    String volumeName = volumeMatcher.group("v");
                    VolumePO existingVolume = storage.findVolumeByName(volumeName).orElse(null);
                    VolumePO volume;
                    Long volsize = ZFS_PROP___VOLSIZE.getFrom(properties);
                    Long usedbydataset = ZFS_PROP___USED_BY_DATASET.getFrom(properties);
                    VolumeState volumeState = Optional.ofNullable(ZFS_PROP___ZRMAN___STATE.getFrom(properties)).orElse(VolumeState.INACTIVE);
                    if (volsize == null) {
                        throw new IllegalArgumentException("volsize missing");
                    }
                    if (usedbydataset == null) {
                        throw new IllegalArgumentException("usedbydataset missing");
                    }
                    if (existingVolume != null) {
                        volume = existingVolume;
                        volume.setSizeBytes(volsize);
                        volume.setUsedBytes(usedbydataset);
                        volume.setState(volumeState);
                    } else {
                        volume = new VolumePO(storage, volumeName, volsize, volumeState, usedbydataset);
                    }
                    volumeRepository.save(volume);
                    return;
                }
            }

            log.warn("Ignored zfsObject=" + zfsObject + "; relative=" + relative);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Cannot process list line: " + line, e);
        }
    }

    private void changeVolumeState(VolumePO volume, VolumeState newState, Boolean readonly) {
        if (volume.getState() == newState) {
            throw new IllegalStateException("Volume is already " + newState + ": " + volume);
        }

        log.info("Changing state of Volume {} to {}...", volume, newState);

        CliExecutor exe = cliConfig.createCliConnector(volume);

        List<String> zfsSetCommand = new ArrayList<>();
        zfsSetCommand.add("zfs");
        zfsSetCommand.add("set");
        if (readonly != null) {
            zfsSetCommand.add("readonly=" + (readonly ? "on" : "off"));
        }
        zfsSetCommand.add(ZFS_PROP___ZRMAN___STATE.getName() + "=" + newState.getExternalValue());
        zfsSetCommand.add(determineZfsVolume(volume));

        boolean dryMode = !exe.executeAndWaitExit0(zfsSetCommand, 1, TimeUnit.MINUTES, true);
        if (dryMode) {
            volume.setState(newState);
        }

        log.info("Volume {} set to {}", volume, newState);

        if (!dryMode) {
            refreshVolume(volume);
        }
    }

}
