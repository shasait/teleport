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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class PipeToProcessPIOStrategy extends AbstractPIOStrategy {
    private final CliExecutor exe;
    private final List<String> command;
    private final ProcessIOStrategy stdoutStrategy;
    private final ProcessIOStrategy stderrStrategy;

    private Thread thread;

    public PipeToProcessPIOStrategy(CliExecutor exe, List<String> command, ProcessIOStrategy stdoutStrategy, ProcessIOStrategy stderrStrategy) {
        super(ProcessBuilder.Redirect.PIPE);

        this.exe = exe;
        this.command = command;
        this.stdoutStrategy = stdoutStrategy;
        this.stderrStrategy = stderrStrategy;
    }

    @Override
    public void begin(Process process, ProcessFd fd) {
        InputStream in = fd.getInputStream(process);
        Transfer transfer = new Transfer(in);

        thread = new Thread(() -> exe.executeAndWait(command, transfer, stdoutStrategy, stderrStrategy, Long.MAX_VALUE, TimeUnit.MILLISECONDS, false));
        thread.start();
    }

    @Override
    public void join(long millis) {
        try {
            thread.join(millis);
        } catch (InterruptedException e) {
            // just continue
        }
    }

    @Override
    public void close(Process process, ProcessFd fd) {
        if (thread.isAlive()) {
            thread.interrupt();
        }
        InputStream in = fd.getInputStream(process);
        try {
            in.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public String appendToCommandToString() {
        return " | " + exe.commandToString(command, null, stdoutStrategy, stderrStrategy);
    }

    private static class Transfer extends AbstractPIOStrategy implements Runnable {

        private final InputStream in;
        private OutputStream out;

        private Thread thread;

        public Transfer(InputStream in) {
            super(ProcessBuilder.Redirect.PIPE);

            this.in = in;
        }

        @Override
        public void begin(Process process, ProcessFd fd) {
            out = fd.getOutputStream(process);
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void join(long millis) {
            try {
                thread.join(millis);
            } catch (InterruptedException e) {
                // just continue
            }
        }

        @Override
        public void close(Process process, ProcessFd fd) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
            try {
                out.close();
            } catch (IOException e) {
                // ignore
            }
        }

        @Override
        public void run() {
            try {
                byte[] buf = new byte[64 * 1024];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    out.flush();
                }
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


}
