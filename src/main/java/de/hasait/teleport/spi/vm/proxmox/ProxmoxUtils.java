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

package de.hasait.teleport.spi.vm.proxmox;


import de.hasait.common.util.cli.CliExecutor;
import de.hasait.teleport.api.VmState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ProxmoxUtils {

    private ProxmoxUtils() {
        super();
    }

    public static List<QmListE> qmListFull(CliExecutor exe) {
        List<String> command = List.of("qm", "list", "-full");
        List<String> lines = exe.executeAndWaitAndReturnStdoutLines(command, 1, TimeUnit.MINUTES, false);
        return QmListE.parse(lines);
    }

    public static VmState qmStatus(CliExecutor exe, String vmid) {
        List<String> command = List.of("qm", "status", vmid);
        List<String> lines = exe.executeAndWaitAndReturnStdoutLines(command, 10, TimeUnit.SECONDS, false);
        String line = lines.get(0);
        return parseStatus(line.split(" ")[1]);
    }

    public static Map<String, String> qmConfig(CliExecutor exe, String vmid) {
        List<String> command = List.of("qm", "config", vmid);
        List<String> lines = exe.executeAndWaitAndReturnStdoutLines(command, 10, TimeUnit.SECONDS, false);
        Map<String, String> linesByKeyword = new LinkedHashMap<>();
        lines.stream().map(line -> line.split(": ", 2)).forEach(it -> linesByKeyword.put(it[0], it[1]));
        return linesByKeyword;
    }

    static VmState parseStatus(String s) {
        if ("running".equals(s)) {
            return VmState.RUNNING;
        } else if ("stopped".equals(s)) {
            return VmState.SHUTOFF;
        } else {
            // TODO add more
            return VmState.OTHER;
        }
    }

}
