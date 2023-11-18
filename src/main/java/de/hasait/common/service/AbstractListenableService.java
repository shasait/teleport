/*
 * Copyright (C) 2021 by Sebastian Hasait (sebastian at hasait dot de)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class AbstractListenableService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractListenableService.class);

    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    public final void addListener(@Nonnull Runnable listener) {
        changeListeners.add(listener);
    }

    public final void removeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    protected final void notifyListeners() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                LOG.warn("ignored listener failure", e);
            }
        }
    }

}
