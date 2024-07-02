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

package de.hasait.teleport.service.storage;

import de.hasait.common.service.AbstractProviderService;
import de.hasait.teleport.api.StorageDriver;
import de.hasait.teleport.domain.StoragePO;
import org.springframework.stereotype.Service;

@Service
public class StorageDriverService extends AbstractProviderService<StorageDriver> {

    public StorageDriverService(StorageDriver[] providers) {
        super(providers);
    }

    public void refresh(StoragePO storage) {
        StorageDriver storageDriver = getProviderByIdNotNull(storage.getDriver());
        storageDriver.refresh(storage);
    }

}
