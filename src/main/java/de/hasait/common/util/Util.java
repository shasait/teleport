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

package de.hasait.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.support.CronExpression;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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

    private static final LinkedList<Pair<Long, String>> TIME_UNITS;

    static {
        List<Pair<Long, String>> units = new ArrayList<>();
        units.add(Pair.of(TimeUnit.DAYS.toMillis(365), "y"));
        units.add(Pair.of(TimeUnit.DAYS.toMillis(1), "d"));
        units.add(Pair.of(TimeUnit.HOURS.toMillis(1), "h"));
        units.add(Pair.of(TimeUnit.MINUTES.toMillis(1), "m"));
        units.add(Pair.of(TimeUnit.SECONDS.toMillis(1), "s"));
        units.add(Pair.of(1L, "ms"));
        TIME_UNITS = new LinkedList<>(units);
    }

    private static final LinkedList<Pair<Long, String>> BYTE_UNITS;

    static {
        long kib = 1024;
        long mib = kib * 1024;
        long gib = mib * 1024;
        long tib = gib * 1024;
        List<Pair<Long, String>> units = new ArrayList<>();
        units.add(Pair.of(tib, "TiB"));
        units.add(Pair.of(gib, "GiB"));
        units.add(Pair.of(mib, "MiB"));
        units.add(Pair.of(kib, "KiB"));
        units.add(Pair.of(1L, "B"));
        BYTE_UNITS = new LinkedList<>(units);
    }

    public static DecimalFormat DECIMAL_FORMAT_0_0 = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    public static DecimalFormat DECIMAL_FORMAT_0_00 = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    public static DecimalFormat DECIMAL_FORMAT_0_000 = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    public static String millisToHuman(LocalDateTime seed, LocalDateTime next, int limit) {
        long millis = Duration.between(seed, next).toMillis();
        return millisToHuman(millis, limit);
    }

    public static String secondsToHuman(int seconds, int limit) {
        return millisToHuman(TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS), limit);
    }

    public static String millisToHuman(long millis, int limit) {
        return toHuman(millis, limit, TIME_UNITS);
    }

    public static String bytesToHuman(long bytes) {
        return toHuman(bytes, 100, BYTE_UNITS);
    }

    public static long humanToBytes(String stringValue) {
        return fromHuman(stringValue, BYTE_UNITS);
    }

    private static String toHuman(long longValue, int limit, LinkedList<Pair<Long, String>> unitList) {
        if (longValue == 0) {
            return "0" + unitList.getLast().getRight();
        }

        List<String> result = new ArrayList<>();

        Iterator<Pair<Long, String>> i = unitList.iterator();
        while (i.hasNext() && longValue > 0 && result.size() < limit) {
            Pair<Long, String> pair = i.next();
            longValue = appendUnit(longValue, pair.getLeft(), pair.getRight(), result);
        }

        return String.join(" ", result);
    }

    private static long fromHuman(String stringValue, LinkedList<Pair<Long, String>> unitList) {
        String[] split = stringValue.split(" ");

        long result = 0;

        int index = 0;

        Iterator<Pair<Long, String>> i = unitList.iterator();
        while (i.hasNext() && index < split.length) {
            Pair<Long, String> pair = i.next();
            String unit = pair.getRight();
            String s = split[index];
            if (s.endsWith(unit)) {
                long units = Long.parseLong(s.substring(0, s.length() - unit.length()));
                result += units * pair.getLeft();
                index++;
            }
        }

        return result;
    }

    private static long appendUnit(long value, long unitValue, String unit, List<String> result) {
        if (value >= unitValue) {
            long units = value / unitValue;
            result.add(units + unit);
            return value - units * unitValue;
        }
        return value;
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
