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

import de.hasait.teleport.service.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshAction extends Action<Void> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RefreshService refreshService;

    public RefreshAction(RefreshService refreshService) {
        super("Refresh state from drivers");
        this.refreshService = refreshService;
    }

    @Override
    public Void call() throws Exception {
        try {
            refreshService.refresh();
        } catch (Throwable t) {
            log.warn("Failed", t);
        }
        return null;
    }

}
