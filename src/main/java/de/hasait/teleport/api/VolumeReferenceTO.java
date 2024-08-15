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

public class VolumeReferenceTO implements Serializable {

    private StorageReferenceTO storage;

    private String name;

    public VolumeReferenceTO() {
    }

    public VolumeReferenceTO(StorageReferenceTO storage, String name) {
        this.storage = storage;
        this.name = name;
    }

    public VolumeReferenceTO(String hostName, String storageName, String name) {
        this(new StorageReferenceTO(hostName, storageName), name);
    }

    public StorageReferenceTO getStorage() {
        return storage;
    }

    public void setStorage(StorageReferenceTO storage) {
        this.storage = storage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
