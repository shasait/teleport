/*
 * Copyright (C) 2022 by Sebastian Hasait (sebastian at hasait dot de)
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

package de.hasait.common.util;

import java.util.concurrent.TimeUnit;

public final class SplittedTimeout {

    private final long timeoutMillis;
    private final long startNanoTime;

    public SplittedTimeout(long timeoutValue, TimeUnit timeoutUnit) {
        this.timeoutMillis = timeoutUnit.toMillis(timeoutValue);
        this.startNanoTime = System.nanoTime();
    }

    public long remainingMillis(long minValue) {
        return Math.max(minValue, timeoutMillis - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));
    }

}
