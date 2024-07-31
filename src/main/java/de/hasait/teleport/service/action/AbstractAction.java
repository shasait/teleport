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

package de.hasait.teleport.service.action;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAction<R> implements Action<R> {

    private final String description;

    private final Set<String> uiBindings = new HashSet<>();

    protected AbstractAction(String description) {
        this.description = description;
    }

    protected final void addUiBinding(String o) {
        uiBindings.add(o);
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public Set<String> getUiBindings() {
        return uiBindings;
    }

    @Override
    public final String toString() {
        return description;
    }

}
