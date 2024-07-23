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

package de.hasait.teleport.service.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RefreshServiceImpl implements RefreshService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final List<RefreshableService<?>> refreshableServiceList;

    public RefreshServiceImpl(List<RefreshableService<?>> refreshableServiceList) {
        this.refreshableServiceList = refreshableServiceList;
    }

    @Override
    @Transactional
    public void refresh() {
        log.info("Refreshing...");
        for (RefreshableService<?> refreshableService : refreshableServiceList) {
            refreshableService.refreshAll();
        }
    }

}
