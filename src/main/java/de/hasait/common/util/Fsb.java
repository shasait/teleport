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

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Formatted StringBuilder.
 */
public class Fsb {

    public enum FsbOutput {ANSI, HTML, NOFORMAT}

    private final List<Entry> entries = new ArrayList<>();

    public Fsb a(Object v, FsbFormat... formats) {
        entries.add(new Entry(Objects.toString(v), formats));
        return this;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public String toString() {
        return toString(FsbOutput.ANSI);
    }

    public String toString(FsbOutput fsbOutput) {
        StringBuilder sb = new StringBuilder();
        for (Entry entry : entries) {
            if (entry.formats.length == 0 || fsbOutput == FsbOutput.NOFORMAT) {
                if (fsbOutput == FsbOutput.ANSI || fsbOutput == FsbOutput.NOFORMAT) {
                    sb.append(entry.text);
                } else {
                    sb.append(StringEscapeUtils.escapeHtml4(entry.text));
                }
            } else {
                FsbFormat[] formats = entry.formats;
                for (FsbFormat format : formats) {
                    if (fsbOutput == FsbOutput.ANSI) {
                        sb.append(format.ansi.sequence);
                    } else {
                        sb.append(format.htmlStart);
                    }
                }
                if (fsbOutput == FsbOutput.ANSI) {
                    sb.append(entry.text);
                    sb.append(Ansi.NORMAL.sequence);
                } else {
                    sb.append(StringEscapeUtils.escapeHtml4(entry.text));
                    for (int i = formats.length - 1; i >= 0; i--) {
                        FsbFormat format = formats[i];
                        sb.append(format.htmlEnd);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static class Entry {

        public final String text;
        public final FsbFormat[] formats;

        public Entry(String text, FsbFormat[] formats) {
            this.text = text;
            this.formats = formats;
        }

    }

}
