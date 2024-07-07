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

package de.hasait.teleport.api;


import de.hasait.common.util.FsbFormat;

public enum VolumeState {

    ACTIVE("active", FsbFormat.LIGHT_PURPLE) //
    , //
    DIRTY("dirty", FsbFormat.LIGHT_YELLOW) //
    , //
    INACTIVE("inactive", FsbFormat.LIGHT_BLUE) //
    ;

    public static VolumeState valueOfExternalValue(String externalValue) {
        for (VolumeState resourceState : values()) {
            if (resourceState.externalValue.equals(externalValue)) {
                return resourceState;
            }
        }
        throw new RuntimeException("Unsupported externalValue: " + externalValue);
    }

    private final String externalValue;

    private final FsbFormat format;

    VolumeState(String externalValue, FsbFormat format) {
        this.externalValue = externalValue;
        this.format = format;
    }

    public String getExternalValue() {
        return externalValue;
    }

    public FsbFormat getFormat() {
        return format;
    }

}
