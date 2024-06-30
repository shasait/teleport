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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

public enum ProcessFd {

    STDIN(null, null, Process::getOutputStream, Process::outputWriter), //
    STDOUT(Process::getInputStream, Process::inputReader, null, null), //
    STDERR(Process::getErrorStream, Process::errorReader, null, null);

    private final Function<Process, InputStream> is;
    private final Function<Process, BufferedReader> br;
    private final Function<Process, OutputStream> os;
    private final Function<Process, BufferedWriter> bw;

    ProcessFd(Function<Process, InputStream> is, Function<Process, BufferedReader> br, Function<Process, OutputStream> os, Function<Process, BufferedWriter> bw) {
        this.is = is;
        this.br = br;
        this.os = os;
        this.bw = bw;
    }

    public InputStream getInputStream(Process process) {
        if (is == null) {
            throw new RuntimeException("Not STDIN: " + this);
        }
        return is.apply(process);
    }

    public BufferedReader getBufferedReader(Process process) {
        if (br == null) {
            throw new RuntimeException("Not STDIN: " + this);
        }
        return br.apply(process);
    }

    public OutputStream getOutputStream(Process process) {
        if (os == null) {
            throw new RuntimeException("Not STDOUT or STDERR: " + this);
        }
        return os.apply(process);
    }

    public BufferedWriter getBufferedWriter(Process process) {
        if (bw == null) {
            throw new RuntimeException("Not STDOUT or STDERR: " + this);
        }
        return bw.apply(process);
    }

}
