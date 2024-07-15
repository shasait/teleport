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

import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.StorageRepository;
import de.hasait.teleport.spi.storage.StorageDriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final StorageRepository repository;
    private final StorageDriverService driverService;

    public StorageServiceImpl(StorageRepository repository, StorageDriverService driverService) {
        this.repository = repository;
        this.driverService = driverService;
    }

    @Override
    public void refreshAll() {
        List<StoragePO> storages = repository.findAll();
        for (StoragePO storage : storages) {
            try {
                driverService.refresh(storage);
            } catch (RuntimeException e) {
                log.warn("Refresh failed for {}", storage, e);
            }
        }
    }


}
