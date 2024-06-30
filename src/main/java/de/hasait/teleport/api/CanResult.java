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

package de.hasait.teleport.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CanResult {

    private final boolean hasNoEffect;

    private final boolean valid;

    private final String message;

    private final Map<Class, Object> context = new HashMap<>();

    public static CanResult hasNoEffect(String message) {
        return new CanResult(true, true, message);
    }

    public static CanResult valid() {
        return new CanResult(false, true, null);
    }

    public static CanResult valid(String message) {
        return new CanResult(false, true, message);
    }

    public static CanResult invalid(String message) {
        return new CanResult(false, false, message);
    }

    private CanResult(boolean hasNoEffect, boolean valid, String message) {
        this.hasNoEffect = hasNoEffect;
        this.valid = valid;
        this.message = message;
    }

    public boolean isHasNoEffect() {
        return hasNoEffect;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isValidWithEffect() {
        return valid && !hasNoEffect;
    }

    public String getMessage() {
        return message;
    }

    public <T> CanResult putContext(Class<T> type, T object) {
        context.put(type, object);
        return this;
    }

    public <T> T getContext(Class<T> type) {
        //noinspection unchecked
        return (T) context.get(type);
    }

    public <T> T getContextNotNull(Class<T> type) {
        return Optional.ofNullable(getContext(type)).orElseThrow();
    }

    public boolean ensureValidAndReturnHasNoEffect() {
        if (!valid) {
            throw new RuntimeException(message);
        }
        return hasNoEffect;
    }

    public CanResult merge(CanResult other) {
        if (other == null || other.hasNoEffect) {
            return this;
        }
        if (hasNoEffect) {
            return other;
        }
        if (!valid) {
            return this;
        }
        if (!other.valid) {
            return other;
        }
        return this;
    }

}
