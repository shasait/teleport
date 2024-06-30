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

package de.hasait.teleport;

import com.google.gson.Gson;
import de.hasait.common.util.cli.CliExecutor;
import de.hasait.common.util.cli.LocalCliExecutor;
import de.hasait.common.util.cli.SshCliExecutor;
import de.hasait.teleport.domain.HasHypervisor;
import de.hasait.teleport.domain.HostConnectConfigPO;
import de.hasait.teleport.domain.HostConnectConfigRepository;
import de.hasait.teleport.service.ssh.SshHostConnectConfig;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

@Service
public class CliConfig {

    private final HostConnectConfigRepository repository;

    private boolean dryMode;
    private boolean disableLocking;
    private String configPathOrNull;

    private String localHostname;

    public CliConfig(HostConnectConfigRepository repository) {
        this.repository = repository;
    }

    public boolean isDryMode() {
        return dryMode;
    }

    public void setDryMode(boolean dryMode) {
        this.dryMode = dryMode;
    }

    public boolean isDisableLocking() {
        return disableLocking;
    }

    public void setDisableLocking(boolean disableLocking) {
        this.disableLocking = disableLocking;
    }

    public String getConfigPathOrNull() {
        return configPathOrNull;
    }

    public void setConfigPathOrNull(String configPathOrNull) {
        this.configPathOrNull = configPathOrNull;
    }

    public String getLocalHostname() {
        return localHostname;
    }

    public void setLocalHostname(String localHostname) {
        this.localHostname = localHostname;
    }

    public InputStream getInStream() {
        return System.in;
    }

    public PrintStream getOutStream() {
        return System.out;
    }

    public PrintStream getErrStream() {
        return System.err;
    }

    public CliExecutor createCliConnector(HasHypervisor hasHypervisor) {
        return createCliConnector(hasHypervisor.obtainHypervisor().getName());
    }

    public CliExecutor createCliConnector(String hostname) {
        CliExecutor cliExecutor;
        if (false /*localHostname.equals(hostname)*/) {
            cliExecutor = new LocalCliExecutor(dryMode);
        } else {
            HostConnectConfigPO hostConnectConfig = repository.findAll().stream() //
                    .filter(it -> it.getFromName().equals(localHostname)) //
                    .filter(it -> it.getToName().equals(hostname)) //
                    .findAny().orElse(null);
            if (hostConnectConfig != null) {
                if ("ssh".equals(hostConnectConfig.getMethod())) {
                    SshHostConnectConfig config = new Gson().fromJson(hostConnectConfig.getConfig(), SshHostConnectConfig.class);
                    cliExecutor = new SshCliExecutor(dryMode, config.getUser(), config.getHost(), config.getArgs());
                } else {
                    throw new UnsupportedOperationException("hostConnectConfig method: " + hostConnectConfig.getMethod());
                }
            } else {
                cliExecutor = new SshCliExecutor(dryMode, SshHostConnectConfig.DEFAULT_SSH_USER, hostname, SshHostConnectConfig.DEFAULT_SSH_ARGS);
            }
        }
        return cliExecutor;
    }

    public void appendSsh(HasHypervisor from, HasHypervisor to, StringBuilder result) {
        appendSsh(from.obtainHypervisor().getName(), to.obtainHypervisor().getName(), result);
    }

    public void appendSsh(String fromHostname, String toHostname, StringBuilder result) {
        HostConnectConfigPO hostConnectConfig = repository.findAll().stream() //
                .filter(it -> it.getFromName().equals(fromHostname)) //
                .filter(it -> it.getToName().equals(toHostname)) //
                .findAny().orElse(null);
        String sshUser;
        String sshHost;
        List<String> sshArgs;
        if (hostConnectConfig != null) {
            if ("ssh".equals(hostConnectConfig.getMethod())) {
                SshHostConnectConfig config = new Gson().fromJson(hostConnectConfig.getConfig(), SshHostConnectConfig.class);
                sshUser = config.getUser();
                sshHost = config.getHost();
                sshArgs = config.getArgs();
            } else {
                throw new UnsupportedOperationException("hostConnectConfig method: " + hostConnectConfig.getMethod());
            }
        } else {
            sshUser = SshHostConnectConfig.DEFAULT_SSH_USER;
            sshHost = toHostname;
            sshArgs = SshHostConnectConfig.DEFAULT_SSH_ARGS;
        }

        result.append(String.join(" ", SshCliExecutor.buildCommand(sshUser, sshHost, sshArgs, List.of())));
        result.append(" ");
    }

}
