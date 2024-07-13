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


import de.hasait.teleport.api.VmState;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QmListE {

    private static final Pattern LINE_PATTERN = Pattern.compile("\\s*(?<id>\\d+|-)\\s+(?<name>\\S+)\\s+(?<status>.+?)\\s+.*");

    private final String id;
    private final String name;
    private final String rawStatus;
    private final VmState state;

    public static List<QmListE> parse(List<String> lines) {
        List<QmListE> result = new ArrayList<>();
        for (String line : lines) {
            Matcher m = LINE_PATTERN.matcher(line);
            if (m.matches()) {
                result.add(new QmListE(m.group("id"), m.group("name"), m.group("status")));
            }
        }

        return result;
    }

    public QmListE(String id, String name, String rawStatus) {
        this.id = id;
        this.name = name;
        this.rawStatus = rawStatus;
        this.state = ProxmoxUtils.parseStatus(rawStatus);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VmState getState() {
        return state;
    }

}
