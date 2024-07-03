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


import de.hasait.teleport.domain.VmState;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VirshListE {

    private static final String STATE_RUNNING = "running";
    private static final String STATE_SHUT_OFF = "shut off";

    private static final Pattern LINE_PATTERN = Pattern.compile("\\s*(?<id>\\d+|-)\\s+(?<name>\\S+)\\s+(?<state>.+)");

    private final String id;
    private final String name;
    private final String rawState;
    private final VmState state;

    public static List<VirshListE> parse(List<String> lines) {
        List<VirshListE> result = new ArrayList<>();
        for (String line : lines) {
            Matcher m = LINE_PATTERN.matcher(line);
            if (m.matches()) {
                result.add(new VirshListE(m.group("id"), m.group("name"), m.group("state")));
            }
        }

        return result;
    }

    public VirshListE(String id, String name, String rawState) {
        this.id = id;
        this.name = name;
        this.rawState = rawState;
        this.state = parseState(rawState);
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

    public static VmState parseState(String state) {
        if (state.equals(STATE_RUNNING)) {
            return VmState.RUNNING;
        }
        if (state.equals(STATE_SHUT_OFF)) {
            return VmState.SHUTOFF;
        }
        return VmState.OTHER;
    }

}
