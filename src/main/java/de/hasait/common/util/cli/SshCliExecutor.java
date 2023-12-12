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

package de.hasait.common.util.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SshCliExecutor extends AbstractCliExecutor {

    private final String user;
    private final String host;
    private final List<String> sshArgs;

    public SshCliExecutor(boolean dryMode, String user, String host, List<String> sshArgs) {
        super(dryMode);

        this.user = user;
        this.host = host;
        this.sshArgs = sshArgs == null ? Collections.emptyList() : new ArrayList<>(sshArgs);
    }

    @Override
    public List<String> completeCommand(List<String> command) {
        return super.completeCommand(buildCommand(user, host, sshArgs, command));
    }

    public static List<String> buildCommand(String user, String host, List<String> sshArgs, List<String> command) {
        List<String> sshCommand = new ArrayList<>(1 + sshArgs.size() + 1 + command.size());
        sshCommand.add("ssh");
        sshCommand.addAll(sshArgs);
        sshCommand.add(user + "@" + host);
        sshCommand.addAll(command);
        return sshCommand;
    }

    @Override
    public String getHostname() {
        return host;
    }

}
