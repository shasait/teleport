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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractDriver implements Driver {

    private final String id;
    private final String description;
    private final String disabledReason;

    protected AbstractDriver(String id, String description, String disabledReason) {
        this.id = id;
        this.description = description;
        this.disabledReason = disabledReason;
    }

    @Nonnull
    @Override
    public final String getId() {
        return id;
    }

    @Nonnull
    @Override
    public final String getDescription() {
        return description;
    }

    @Nullable
    @Override
    public final String getDisabledReason() {
        return disabledReason;
    }

}
