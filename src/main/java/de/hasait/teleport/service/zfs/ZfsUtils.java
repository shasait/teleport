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

package de.hasait.teleport.service.zfs;

import de.hasait.common.util.cli.CliExecutor;

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
