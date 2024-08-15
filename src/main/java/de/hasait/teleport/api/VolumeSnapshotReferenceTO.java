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

import java.io.Serializable;

public class VolumeSnapshotReferenceTO implements Serializable {

    private VolumeReferenceTO volume;

    private String name;

    public VolumeSnapshotReferenceTO() {
    }

    public VolumeSnapshotReferenceTO(VolumeReferenceTO volume, String name) {
        this.volume = volume;
        this.name = name;
    }

    public VolumeSnapshotReferenceTO(String hostName, String storageName, String volumeName, String name) {
        this(new VolumeReferenceTO(hostName, storageName, volumeName), name);
    }

    public VolumeReferenceTO getVolume() {
        return volume;
    }

    public void setVolume(VolumeReferenceTO volume) {
        this.volume = volume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
