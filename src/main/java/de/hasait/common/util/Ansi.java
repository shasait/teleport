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

package de.hasait.common.util;

public final class Ansi {

    public static final Ansi NORMAL = new Ansi("\u001B[0m");

    public static final Ansi BOLD = new Ansi("\u001B[1m");
    public static final Ansi ITALIC = new Ansi("\u001B[3m");
    public static final Ansi UNDERLINE = new Ansi("\u001B[4m");
    public static final Ansi BLINK = new Ansi("\u001B[5m");
    public static final Ansi RAPID_BLINK = new Ansi("\u001B[6m");
    public static final Ansi REVERSE_VIDEO = new Ansi("\u001B[7m");
    public static final Ansi INVISIBLE_TEXT = new Ansi("\u001B[8m");

    public static final Ansi BLACK = new Ansi("\u001B[30m");
    public static final Ansi RED = new Ansi("\u001B[31m");
    public static final Ansi GREEN = new Ansi("\u001B[32m");
    public static final Ansi YELLOW = new Ansi("\u001B[33m");
    public static final Ansi BLUE = new Ansi("\u001B[34m");
    public static final Ansi MAGENTA = new Ansi("\u001B[35m");
    public static final Ansi CYAN = new Ansi("\u001B[36m");
    public static final Ansi WHITE = new Ansi("\u001B[37m");

    public static final Ansi DARK_GRAY = new Ansi("\u001B[1;30m");
    public static final Ansi LIGHT_RED = new Ansi("\u001B[1;31m");
    public static final Ansi LIGHT_GREEN = new Ansi("\u001B[1;32m");
    public static final Ansi LIGHT_YELLOW = new Ansi("\u001B[1;33m");
    public static final Ansi LIGHT_BLUE = new Ansi("\u001B[1;34m");
    public static final Ansi LIGHT_PURPLE = new Ansi("\u001B[1;35m");
    public static final Ansi LIGHT_CYAN = new Ansi("\u001B[1;36m");

    public final String sequence;

    private Ansi(String sequence) {
        this.sequence = sequence;
    }

    public String format(String text) {
        return sequence + text + NORMAL.sequence;
    }

    public String format(Object object) {
        return format(String.valueOf(object));
    }

}