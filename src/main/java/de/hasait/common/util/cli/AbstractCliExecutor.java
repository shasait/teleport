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

package de.hasait.common.util.cli;

import de.hasait.common.util.SplittedTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractCliExecutor implements CliExecutor {

    private final Logger log = LoggerFactory.getLogger(AbstractCliExecutor.class);

    private final boolean dryMode;

    public AbstractCliExecutor(boolean dryMode) {
        this.dryMode = dryMode;
    }

    protected List<String> completeCommand(List<String> command) {
        return command;
    }

    @Override
    public final String commandToString(List<String> command, ProcessIOStrategy stdinStrategy, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy) {
        return String.join(" ", completeCommand(command)) + (stdinStrategy != null ? stdinStrategy.appendToCommandToString() : "") + stdoutStrategy.appendToCommandToString() + stderrStrategy.appendToCommandToString();
    }

    @Override
    public final ProcessBuilder prepare(List<String> command) {
        List<String> completeCommand = completeCommand(command);
        return new ProcessBuilder(completeCommand);
    }

    @Override
    public final Integer executeAndWait(List<String> command, ProcessIOStrategy stdinStrategy, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy, long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry) {
        if (dryMode && skipIfDry) {
            if (log.isWarnEnabled()) {
                log.warn("Dry mode: " + commandToString(command, stdinStrategy, stdoutStrategy, stderrStrategy));
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Executing... " + commandToString(command, stdinStrategy, stdoutStrategy, stderrStrategy));
        }

        ProcessBuilder pb = prepare(command);
        pb.redirectInput(stdinStrategy.getRedirect());
        pb.redirectOutput(stdoutStrategy.getRedirect());
        pb.redirectError(stderrStrategy.getRedirect());

        SplittedTimeout timeout = new SplittedTimeout(timeoutValue, timeoutUnit);

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new CliException("Cannot start process: " + command, command, e);
        }

        log.trace("beginning {}", ProcessFd.STDIN);
        stdinStrategy.begin(process, ProcessFd.STDIN);
        log.trace("beginning {}", ProcessFd.STDOUT);
        stdoutStrategy.begin(process, ProcessFd.STDOUT);
        log.trace("beginning {}", ProcessFd.STDERR);
        stderrStrategy.begin(process, ProcessFd.STDERR);

        log.trace("joining {}", ProcessFd.STDIN);
        stdinStrategy.join(timeout.remainingMillis(1));
        log.trace("joining {}", ProcessFd.STDOUT);
        stdoutStrategy.join(timeout.remainingMillis(1));
        log.trace("joining {}", ProcessFd.STDERR);
        stderrStrategy.join(timeout.remainingMillis(1));

        log.trace("waiting for process {}", process);
        boolean exited = false;
        try {
            exited = process.waitFor(timeout.remainingMillis(0), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // just continue
        }

        if (!exited) {
            log.trace("destroying process {}", process);
            process.destroyForcibly();
        }

        log.trace("close {}", ProcessFd.STDIN);
        stdinStrategy.close(process, ProcessFd.STDIN);
        log.trace("close {}", ProcessFd.STDOUT);
        stdoutStrategy.close(process, ProcessFd.STDOUT);
        log.trace("close {}", ProcessFd.STDERR);
        stderrStrategy.close(process, ProcessFd.STDERR);

        if (!exited) {
            throw new CliTimeoutException(command);
        }

        int exitValue = process.exitValue();
        log.trace("returning exitValue {}", exitValue);
        return exitValue;
    }

    @Override
    public boolean checkReachable() {
        try {
            executeAndWaitExit0(10, TimeUnit.SECONDS, false, "hostname", "-f");
            return true;
        } catch (CliNonZeroExitCodeException e) {
            log.warn("Host not reachable: {}", getHostname());
            return false;
        } catch (RuntimeException e) {
            log.warn("Host not reachable: {}", getHostname(), e);
            return false;
        }
    }

    @Override
    public boolean createOrUpdateFile(String file, String chown, String chmod, String content, boolean skipIfDry) {
        executeAndWaitExit0(10, TimeUnit.SECONDS, skipIfDry, "touch", file);
        executeAndWaitExit0(10, TimeUnit.SECONDS, skipIfDry, "chown", chown, file);
        executeAndWaitExit0(10, TimeUnit.SECONDS, skipIfDry, "chmod", chmod, file);
        return executeAndWaitExit0(List.of("dd", "of=" + file), new WriteBytesPIOStrategy(content), InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, 30, TimeUnit.SECONDS, skipIfDry);
    }

}
