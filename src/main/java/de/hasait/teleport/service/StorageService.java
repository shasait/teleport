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

package de.hasait.teleport.service;

import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.StorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final StorageRepository storageRepository;
    private final StorageDriverService storageDriverService;

    public StorageService(StorageRepository storageRepository, StorageDriverService storageDriverService) {
        this.storageRepository = storageRepository;
        this.storageDriverService = storageDriverService;
    }
    
    public void refreshStorages() {
        List<StoragePO> storages = storageRepository.findAll();
        for (StoragePO storage : storages) {
            storageDriverService.refreshStorage(storage);
        }
    }


}