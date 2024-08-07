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

import de.hasait.common.domain.SearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolumeRepository extends SearchableRepository<VolumePO, Long> {

    @Override
    @Query("SELECT r FROM VolumePO r WHERE r.name LIKE %:search%")
    Page<VolumePO> search(String search, Pageable pageable);

    @Override
    @Query("SELECT COUNT(r) FROM VolumePO r WHERE r.name LIKE %:search%")
    long searchCount(String search);

    @Query("SELECT v FROM VolumePO v WHERE v.dev = :dev AND v.storage.host.name = :host")
    List<VolumePO> findByHostAndDev(String host, String dev);

}
