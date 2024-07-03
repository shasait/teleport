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

package de.hasait.teleport.domain;


import de.hasait.common.util.FsbFormat;

public enum VmState {

    RUNNING(FsbFormat.LIGHT_GREEN) //
    , //
    SHUTOFF(FsbFormat.LIGHT_BLUE) //
    , //
    OTHER(FsbFormat.LIGHT_YELLOW) //
    ;

    private final FsbFormat format;

    VmState(FsbFormat format) {
        this.format = format;
    }

    public FsbFormat getFormat() {
        return format;
    }

}
