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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unit {

    public static final Unit B1024 = new Unit(null, 0, "B");
    public static final Unit KIB = new Unit(B1024, 1024, "KiB");
    public static final Unit MIB = new Unit(KIB, 1024, "MiB");
    public static final Unit GIB = new Unit(MIB, 1024, "GiB");
    public static final Unit TIB = new Unit(GIB, 1024, "TiB");
    public static final Unit PIB = new Unit(TIB, 1024, "PiB");
    public static final Unit EIB = new Unit(PIB, 1024, "EiB");
    public static final Unit ZIB = new Unit(EIB, 1024, "ZiB");
    public static final Unit YIB = new Unit(ZIB, 1024, "YiB");

    public static final Unit B1000 = new Unit(null, 0, "B");
    public static final Unit KB = new Unit(B1000, 1000, "KB");
    public static final Unit MB = new Unit(KB, 1000, "MB");
    public static final Unit GB = new Unit(MB, 1000, "GB");
    public static final Unit TB = new Unit(GB, 1000, "TB");
    public static final Unit PB = new Unit(TB, 1000, "PB");
    public static final Unit EB = new Unit(PB, 1000, "EB");
    public static final Unit ZB = new Unit(EB, 1000, "ZB");
    public static final Unit YB = new Unit(ZB, 1000, "YB");

    public static final Unit NS = new Unit(null, 0, "ns");
    public static final Unit US = new Unit(NS, 1000, "us");
    public static final Unit MS = new Unit(US, 1000, "ms");
    public static final Unit S = new Unit(MS, 1000, "s");
    public static final Unit MINUTE = new Unit(S, 60, "m");
    public static final Unit HOUR = new Unit(MINUTE, 60, "h");
    public static final Unit DAY = new Unit(HOUR, 24, "d");
    public static final Unit WEEK = new Unit(DAY, 7, "w");
    public static final Unit YEAR = new Unit(WEEK, 52, "y");
    public static final Unit CENTURY = new Unit(YEAR, 100, "cen");
    public static final Unit EON = new Unit(CENTURY, 10000000, "eon");

    private final Unit smallerUnit;
    private final int smallerFactor;

    private final String name;
    private final Pattern pattern;

    private Unit biggerUnit;

    public Unit(Unit smallerUnit, int smallerFactor, String name) {
        this.smallerUnit = smallerUnit;
        this.smallerFactor = smallerFactor;
        this.name = name;
        this.pattern = Pattern.compile("(\\d+)\\Q" + name + "\\E");

        if (smallerUnit != null) {
            if (smallerUnit.biggerUnit != null) {
                throw new IllegalArgumentException(smallerUnit + " already has a biggerUnit: " + smallerUnit.biggerUnit);
            }
            smallerUnit.biggerUnit = this;
        }
    }

    public Unit getSmallerUnit() {
        return smallerUnit;
    }

    public int getSmallerFactor() {
        return smallerFactor;
    }

    public String getName() {
        return name;
    }

    public Unit getBiggerUnit() {
        return biggerUnit;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toHuman(long longValue) {
        return toHuman(longValue, Integer.MAX_VALUE);
    }

    public String toHuman(long longValue, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit < 1: " + limit);
        }
        if (longValue == 0) {
            return "0" + name;
        }

        if (longValue > 0) {
            return toHumanPositive(longValue, limit);
        } else {
            return "-" + toHumanPositive(-longValue, limit);
        }
    }

    public long fromHuman(String stringValue) {
        if (stringValue.startsWith("-")) {
            return -fromHumanPositive(stringValue.substring(1));
        }
        return fromHumanPositive(stringValue);
    }

    private String toHumanPositive(long longValue, int limit) {
        List<String> result = new ArrayList<>();
        toHuman(longValue, 1, result, limit);
        return String.join(" ", result);
    }

    private long toHuman(long longValue, long unitValue, List<String> result, int limit) {
        long remaining = toHumanBiggerUnit(longValue, unitValue, result, limit);

        if (remaining >= unitValue) {
            long units = remaining / unitValue;
            if (result.size() < limit) {
                result.add(units + name);
            }
            return remaining - units * unitValue;
        }

        return remaining;
    }

    private long toHumanBiggerUnit(long longValue, long unitValue, List<String> result, int limit) {
        if (biggerUnit != null) {
            long biggerUnitValue;
            try {
                biggerUnitValue = Math.multiplyExact(unitValue, biggerUnit.smallerFactor);
            } catch (ArithmeticException e) {
                return longValue;
            }
            if (longValue >= biggerUnitValue) {
                return biggerUnit.toHuman(longValue, biggerUnitValue, result, limit);
            }
        }
        return longValue;
    }

    private long fromHumanPositive(String stringValue) {
        String[] split = stringValue.split(" ");
        List<Long> result = new ArrayList<>();
        int index = fromHuman(split, 0, 1, result);
        if (index != split.length) {
            throw new IllegalArgumentException("Cannot parse: " + stringValue);
        }
        long sum = 0;
        for (long value : result) {
            sum = Math.addExact(sum, value);
        }
        return sum;
    }

    private int fromHuman(String[] split, int currentIndex, long unitValue, List<Long> result) {
        int index0 = fromHumanHere(split, currentIndex, unitValue, result);
        if (index0 != currentIndex) {
            return index0;
        }
        int index1 = fromHumanBiggerUnit(split, index0, unitValue, result);
        if (index1 == split.length) {
            return index1;
        }
        return fromHumanHere(split, index1, unitValue, result);
    }

    private int fromHumanHere(String[] split, int currentIndex, long unitValue, List<Long> result) {
        String s = split[currentIndex];
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            long units = Long.parseLong(matcher.group(1));
            result.add(units * unitValue);
            return currentIndex + 1;
        }
        return currentIndex;
    }

    private int fromHumanBiggerUnit(String[] split, int currentIndex, long unitValue, List<Long> result) {
        if (biggerUnit != null) {
            long biggerUnitValue;
            try {
                biggerUnitValue = Math.multiplyExact(unitValue, biggerUnit.smallerFactor);
            } catch (ArithmeticException e) {
                return currentIndex;
            }
            return biggerUnit.fromHuman(split, currentIndex, biggerUnitValue, result);
        }
        return currentIndex;
    }

}
