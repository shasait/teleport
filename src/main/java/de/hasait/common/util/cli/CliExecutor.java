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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface CliExecutor {

    String getHostname();

    String commandToString(List<String> command, ProcessIOStrategy stdinStrategy, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy);

    ProcessBuilder prepare(List<String> command);

    Integer executeAndWait(List<String> command, ProcessIOStrategy stdinStrategy, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy, long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry);

    default boolean executeAndWaitExit0(List<String> command, ProcessIOStrategy stdinStrategy, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy, long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry) {
        Integer exitCode = executeAndWait(command, stdinStrategy, stdoutStrategy, stderrStrategy, timeoutValue, timeoutUnit, skipIfDry);
        if (exitCode == null) {
            return false;
        }
        if (exitCode != 0) {
            throw new CliNonZeroExitCodeException(command, exitCode);
        }
        return true;
    }

    default boolean executeAndWaitExit0(List<String> command, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy, long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry) {
        return executeAndWaitExit0(command, WriteBytesPIOStrategy.NULL, stdoutStrategy, stderrStrategy, timeoutValue, timeoutUnit, skipIfDry);
    }

    default boolean executeAndWaitExit0(List<String> command, ProcessIOStrategy stdoutStrategy, long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry) {
        return executeAndWaitExit0(command, WriteBytesPIOStrategy.NULL, stdoutStrategy, InheritPIOStrategy.INSTANCE, timeoutValue, timeoutUnit, skipIfDry);
    }

    default boolean executeAndWaitExit0(List<String> command, long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry) {
        return executeAndWaitExit0(command, InheritPIOStrategy.INSTANCE, timeoutValue, timeoutUnit, skipIfDry);
    }

    default boolean executeAndWaitExit0(long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry, String... command) {
        return executeAndWaitExit0(Arrays.asList(command), timeoutValue, timeoutUnit, skipIfDry);
    }

    default List<String> executeAndWaitAndReturnStdoutLines(List<String> command, long timeoutValue, TimeUnit timeoutUnit, boolean skipIfDry) {
        List<String> lines = new ArrayList<>();
        ConsumeLinesPIOStrategy stdoutStrategy = new ConsumeLinesPIOStrategy(lines::add);
        if (!executeAndWaitExit0(command, WriteBytesPIOStrategy.NULL, stdoutStrategy, InheritPIOStrategy.INSTANCE, timeoutValue, timeoutUnit, skipIfDry)) {
            // dry mode
            return null;
        }
        return lines;
    }

    default int executeInteractive(List<String> command, boolean skipIfDry) {
        return executeAndWait(command, InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, InheritPIOStrategy.INSTANCE, Long.MAX_VALUE, TimeUnit.MILLISECONDS, skipIfDry);
    }

    default PipeToProcessPIOStrategy pipeTo(List<String> command, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy) {
        return new PipeToProcessPIOStrategy(this, command, stdoutStrategy, stderrStrategy);
    }

    default PipeToProcessPIOStrategy pipeTo(List<String> command, ProcessIOStrategy stdoutStrategy) {
        return pipeTo(command, stdoutStrategy, InheritPIOStrategy.INSTANCE);
    }

    default PipeToProcessPIOStrategy pipeTo(List<String> command) {
        return pipeTo(command, InheritPIOStrategy.INSTANCE);
    }

    boolean checkReachable();

    boolean createOrUpdateFile(String file, String chown, String chmod, String content, boolean skipIfDry);

}
