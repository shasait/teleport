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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class Util {

    public static final Random RANDOM = new Random();

    private static final List<Pair<Long, String>> UNITS;

    static {
        List<Pair<Long, String>> units = new ArrayList<>();
        units.add(Pair.of(TimeUnit.DAYS.toMillis(365), "y"));
        units.add(Pair.of(TimeUnit.DAYS.toMillis(1), "d"));
        units.add(Pair.of(TimeUnit.HOURS.toMillis(1), "h"));
        units.add(Pair.of(TimeUnit.MINUTES.toMillis(1), "m"));
        units.add(Pair.of(TimeUnit.SECONDS.toMillis(1), "s"));
        units.add(Pair.of(1L, "ms"));
        UNITS = Collections.unmodifiableList(units);
    }

    public static String millisToHuman(LocalDateTime seed, LocalDateTime next, int limit) {
        long millis = Duration.between(seed, next).toMillis();
        return millisToHuman(millis, limit);
    }

    public static String millisToHuman(long millis, int limit) {
        List<String> result = new ArrayList<>();

        Iterator<Pair<Long, String>> i = UNITS.iterator();
        while (i.hasNext() && millis > 0 && result.size() < limit) {
            Pair<Long, String> pair = i.next();
            millis = appendUnit(millis, pair.getLeft(), pair.getRight(), result);
        }

        return String.join(" ", result);
    }

    public static String secondsToHuman(int seconds, int limit) {
        return millisToHuman(TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS), limit);
    }

    private static long appendUnit(long millis, long unitMillis, String unit, List<String> result) {
        if (millis >= unitMillis) {
            long units = millis / unitMillis;
            result.add(units + unit);
            return millis - units * unitMillis;
        }
        return millis;
    }

    public static <T> void parse(String string, String typeHint, Function<String, T> parser, String name, Consumer<T> consumer) {
        T value;
        try {
            value = parser.apply(string);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(name + " - invalid " + typeHint + ": " + string, e);
        }
        consumer.accept(value);
    }

    public static LocalDateTime determineNext(String cronExpressionString, LocalDateTime seed) {
        if (StringUtils.isNotBlank(cronExpressionString) && seed != null) {
            CronExpression cronExpression = CronExpression.parse(cronExpressionString);
            return cronExpression.next(seed);
        }
        return null;
    }

    public static String determineNextRelative(LocalDateTime seed, LocalDateTime next, int limit) {
        if (seed == null || next == null) {
            return StringUtils.EMPTY;
        }
        return "in " + Util.millisToHuman(seed, next, limit);
    }

    public static String determineNextRelative(String cronExpressionString, LocalDateTime seed, int limit) {
        return Util.determineNextRelative(seed, Util.determineNext(cronExpressionString, seed), limit);
    }

    public static void registerScheduledFuture(long id, ScheduledFuture<?> scheduledFuture, ConcurrentHashMap<Long, List<ScheduledFuture<?>>> scheduledFutures) {
        List<ScheduledFuture<?>> futureList = scheduledFutures.computeIfAbsent(id, ignored -> new CopyOnWriteArrayList<>());
        futureList.removeIf(Future::isDone);
        futureList.add(scheduledFuture);
    }

}
