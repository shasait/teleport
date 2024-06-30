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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public final class WriteBytesPIOStrategy extends AbstractPIOStrategy {

    public static final WriteBytesPIOStrategy NULL = new WriteBytesPIOStrategy((Iterable<byte[]>) null);

    private final Iterable<byte[]> outputProducer;

    private final boolean appendToCommand;

    private Thread thread;

    public WriteBytesPIOStrategy(String output) {
        this(output, true);
    }

    public WriteBytesPIOStrategy(String output, boolean addNewLine) {
        this(List.of((output + (addNewLine ? "\n" : "")).getBytes(StandardCharsets.UTF_8)), true);
    }

    public WriteBytesPIOStrategy(Iterable<byte[]> outputProducer) {
        this(outputProducer, false);
    }

    private WriteBytesPIOStrategy(Iterable<byte[]> outputProducer, boolean appendToCommand) {
        super(ProcessBuilder.Redirect.PIPE);

        this.outputProducer = outputProducer;
        this.appendToCommand = appendToCommand;
    }

    @Override
    public String appendToCommandToString() {
        StringBuilder result = new StringBuilder();
        if (appendToCommand) {
            result.append(" < ");
            if (outputProducer != null) {
                for (byte[] bytes : outputProducer) {
                    result.append(new String(bytes, StandardCharsets.UTF_8));
                }
            } else {
                result.append("NULL");
            }
        }
        return result.toString();
    }

    @Override
    public void begin(Process process, ProcessFd fd) {
        OutputStream out = fd.getOutputStream(process);
        thread = new Thread(new Transfer(out));
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
        OutputStream out = fd.getOutputStream(process);
        try {
            out.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private class Transfer implements Runnable {

        private final OutputStream out;

        public Transfer(OutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            if (outputProducer != null) {
                Iterator<byte[]> iterator = outputProducer.iterator();
                try {
                    while (iterator.hasNext()) {
                        out.write(iterator.next());
                        out.flush();
                    }
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
