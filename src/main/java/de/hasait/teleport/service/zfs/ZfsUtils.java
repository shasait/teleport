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

import com.google.gson.Gson;
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.teleport.domain.HasStorage;
import de.hasait.teleport.domain.SnapshotData;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VolumeGroupPO;
import de.hasait.teleport.domain.VolumeGroupSnapshotPO;
import de.hasait.teleport.domain.VolumePO;
import de.hasait.teleport.domain.VolumeSnapshotPO;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ZfsUtils {

    private ZfsUtils() {
        super();
    }

    public static void validateZfsDataset(String zfsDataset) {
        if (zfsDataset == null) {
            throw new NullPointerException("zfsDataset");
        }
        if (zfsDataset.startsWith("/")) {
            throw new IllegalArgumentException("Invalid ZFS base object - cannot start with a slash: " + zfsDataset);
        }
        if (zfsDataset.endsWith("/")) {
            throw new IllegalArgumentException("Invalid ZFS base object - cannot end with a slash: " + zfsDataset);
        }
    }

    public static String determineZfsDataset(StoragePO storage) {
        if (ZfsDriver.DRIVER_ID.equals(storage.getDriver())) {
            ZfsDriverConfig zfsDriverConfig = new Gson().fromJson(storage.getDriverConfig(), ZfsDriverConfig.class);
            String dataset = zfsDriverConfig.getDataset();
            ZfsUtils.validateZfsDataset(dataset);
            return dataset;
        }
        throw new IllegalArgumentException("Not a ZFS storage: " + storage);
    }

    public static String determineZfsDataset(VolumeGroupPO volumeGroup) {
        return determineZfsDataset(volumeGroup.getStorage(), volumeGroup.getName());
    }

    public static String determineZfsDataset(StoragePO storage, String volumeGroupName) {
        return determineZfsDataset(storage) + "/" + volumeGroupName;
    }

    public static String determineZfsVolume(VolumePO volume) {
        return determineZfsVolume(volume.getVolumeGroup(), volume.getName());
    }

    public static String determineZfsVolume(VolumeGroupPO volumeGroup, String volumeName) {
        return determineZfsDataset(volumeGroup) + "/" + volumeName;
    }

    public static String determineZfsObject(HasStorage hasStorage) {
        if (hasStorage instanceof StoragePO storage) {
            return determineZfsDataset(storage);
        }
        if (hasStorage instanceof VolumeGroupPO volumeGroup) {
            return determineZfsDataset(volumeGroup);
        }
        if (hasStorage instanceof VolumePO volume) {
            return determineZfsVolume(volume);
        }
        if (hasStorage instanceof VolumeGroupSnapshotPO snapshot) {
            return determineZfsDataset(snapshot.getVolumeGroup()) + "@" + snapshot.getData().getName();
        }
        if (hasStorage instanceof VolumeSnapshotPO snapshot) {
            return determineZfsVolume(snapshot.getVolume()) + "@" + snapshot.getData().getName();
        }
        throw new RuntimeException("Unsupported hasStorage: " + hasStorage);
    }

    public static String determineZfsObject(HasStorage hasStorage, SnapshotData snapshotData) {
        if (hasStorage instanceof VolumeGroupPO || hasStorage instanceof VolumePO) {
            return determineZfsObject(hasStorage) + "@" + snapshotData.getName();
        }
        throw new RuntimeException("Unsupported hasStorage: " + hasStorage);
    }

    public static String determineZfsObjectForOtherStorage(HasStorage hasStorage, StoragePO otherStorage) {
        String objectStorageDataset = determineZfsDataset(hasStorage.obtainStorage());
        String zfsObject = determineZfsObject(hasStorage);
        String otherStorageDataset = determineZfsDataset(otherStorage);
        return otherStorageDataset + zfsObject.substring(objectStorageDataset.length());
    }

    public static String determineDevice(VolumePO volume) {
        return "/dev/zvol/" + determineZfsVolume(volume);
    }

    public static void zfsListAll(CliExecutor exe, String baseZfsDataset, String propsCsv, Consumer<String> lineProcessor) {
        validateZfsDataset(baseZfsDataset);
        List<String> command = List.of("zfs", "list", "-H", "-p", "-r", "-t", "filesystem,volume,snapshot", "-o", propsCsv, baseZfsDataset);
        exe.executeAndWaitAndReturnStdoutLines(command, 60, TimeUnit.SECONDS, false).forEach(lineProcessor);
    }

    public static void zfsGetR(CliExecutor exe, String baseZfsDataset, Consumer<String> lineProcessor) {
        validateZfsDataset(baseZfsDataset);
        List<String> command = List.of("zfs", "get", "-H", "-p", "-r", "-t", "filesystem", "-o", "name,property,value", "-s", "local", "all", baseZfsDataset);
        exe.executeAndWaitAndReturnStdoutLines(command, 60, TimeUnit.SECONDS, false).forEach(lineProcessor);
    }

}
