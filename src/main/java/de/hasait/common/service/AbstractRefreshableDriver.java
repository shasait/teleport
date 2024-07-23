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

import javax.annotation.Nullable;

public abstract class AbstractRefreshableDriver<PO extends DriverInstancePO, C> extends AbstractDriver implements RefreshableDriver<PO> {

    protected AbstractRefreshableDriver(String id, String description, String disabledReason) {
        super(id, description, disabledReason);
    }

    @Nullable
    @Override
    public final String validateConfigText(@Nullable String configText) {
        try {
            parseConfigText(configText);
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    protected final C parseConfigText(PO po) {
        if (!getId().equals(po.getDriver())) {
            throw new IllegalArgumentException("Not a driver instance of " + getId() + ": " + po.getDriver());
        }
        return parseConfigText(po.getDriverConfig());
    }

    protected abstract C parseConfigText(String configText);

    @Override
    public final void refresh(PO po) {
        C config = parseConfigText(po);
        refresh(po, config);
    }

    protected abstract void refresh(PO po, C config);

}
