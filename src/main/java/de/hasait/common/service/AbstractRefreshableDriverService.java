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

package de.hasait.common.service;

import de.hasait.common.domain.DriverInstancePO;
import de.hasait.teleport.service.refresh.RefreshableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class AbstractRefreshableDriverService<P extends RefreshableDriver<PO>, PO extends DriverInstancePO, R extends JpaRepository<PO, ?>> extends AbstractDriverService<P> implements RefreshableService<PO> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final Class<PO> poClass;
    protected final R repository;

    public AbstractRefreshableDriverService(Class<PO> poClass, R repository, P[] providers) {
        super(providers);

        this.poClass = poClass;
        this.repository = repository;
    }

    @Override
    public final Class<PO> getSupportedRefreshType() {
        return poClass;
    }

    @Override
    @Transactional
    public final void refreshAll() {
        List<PO> poList = repository.findAll();
        for (PO po : poList) {
            try {
                refresh(po);
            } catch (RuntimeException e) {
                log.warn("Refresh failed for {}", po, e);
            }
        }
    }

    @Override
    @Transactional
    public final void refresh(PO po) {
        P provider = getProviderByIdNotNull(po.getDriver());
        provider.refresh(po);
        po.setLastRefresh(LocalDateTime.now());
    }

}
