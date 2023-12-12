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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;

public final class ConsumeLinesPIOStrategy extends AbstractPIOStrategy {

    private final Consumer<String> lineConsumer;

    private Thread thread;

    public ConsumeLinesPIOStrategy(Consumer<String> lineConsumer) {
        super(ProcessBuilder.Redirect.PIPE);

        this.lineConsumer = lineConsumer;
    }

    @Override
    public void begin(Process process, ProcessFd fd) {
        BufferedReader reader = fd.getBufferedReader(process);
        thread = new Thread(new Transfer(reader));
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
        BufferedReader reader = fd.getBufferedReader(process);
        try {
            reader.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private class Transfer implements Runnable {

        private final BufferedReader reader;

        public Transfer(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    lineConsumer.accept(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
