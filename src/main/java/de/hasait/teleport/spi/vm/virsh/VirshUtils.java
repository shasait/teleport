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


import de.hasait.common.util.cli.CliExecutor;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class VirshUtils {

    private VirshUtils() {
        super();
    }

    public static List<VirshListE> virshListAll(CliExecutor exe) {
        List<String> command = List.of("virsh", "list", "--all");
        List<String> lines = exe.executeAndWaitAndReturnStdoutLines(command, 1, TimeUnit.MINUTES, false);
        return VirshListE.parse(lines);
    }

    public static String virshDomState(CliExecutor exe, String virtualMachineName) {
        List<String> command = List.of("virsh", "domstate", virtualMachineName);
        List<String> lines = exe.executeAndWaitAndReturnStdoutLines(command, 10, TimeUnit.SECONDS, false);
        return lines.get(0);
    }

    public static String virshDumpXml(CliExecutor exe, String virtualMachineName) {
        List<String> command = List.of("virsh", "dumpxml", virtualMachineName);
        List<String> lines = exe.executeAndWaitAndReturnStdoutLines(command, 10, TimeUnit.SECONDS, false);
        return String.join("\n", lines);
    }

}
