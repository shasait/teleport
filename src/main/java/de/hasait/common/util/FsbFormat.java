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

public enum FsbFormat {

    BOLD(Ansi.BOLD, "<b>", "</b>"), //
    ITALIC(Ansi.ITALIC, "<i>", "</i>"),//
    UNDERLINE(Ansi.UNDERLINE, "<u>", "</u>"),//
    LIGHT_RED(Ansi.LIGHT_RED, "<font color='#ff8080'>", "</font>"),//
    LIGHT_YELLOW(Ansi.LIGHT_YELLOW, "<font color='#dddd00'>", "</font>"),//
    LIGHT_BLUE(Ansi.LIGHT_BLUE, "<font color='#8080ff'>", "</font>"),//
    LIGHT_PURPLE(Ansi.LIGHT_PURPLE, "<font color='#ff40ff'>", "</font>"),//
    LIGHT_GREEN(Ansi.LIGHT_GREEN, "<font color='#80ff80'>", "</font>"),//
    ;

    public final Ansi ansi;
    public final String htmlStart;
    public final String htmlEnd;

    FsbFormat(Ansi ansi, String htmlStart, String htmlEnd) {
        this.ansi = ansi;
        this.htmlStart = htmlStart;
        this.htmlEnd = htmlEnd;
    }

}
