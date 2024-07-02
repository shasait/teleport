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

package de.hasait.teleport.service.storage.zfs;

import java.util.Map;
import java.util.function.Function;

public class ZfsProperty<T> {

    private final String name;
    private final Function<String, T> parser;

    public ZfsProperty(String name, Function<String, T> parser) {
        this.name = name;
        this.parser = parser;
    }

    public String getName() {
        return name;
    }

    public Function<String, T> getParser() {
        return parser;
    }


    public void putInto(Map<String, Object> properties, String raw) {
        properties.put(name, parser.apply(raw));
    }

    public T getFrom(Map<String, Object> properties) {
        return (T) properties.get(name);
    }

}
