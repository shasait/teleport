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

package de.hasait.common.util;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;


/**
 * Assertions.
 */
public final class AssertUtil {

    public static AssertionException createFail(String pattern, Object... args) {
        return new AssertionException(MessageFormatUtil.format(pattern, args));
    }

    public static AssertionException createNotReachable() {
        return createNotReachable("not reachable");
    }

    public static AssertionException createNotReachable(String pattern, Object... args) {
        return createFail(pattern, args);
    }

    public static <T> void equals(T object1, T object2) {
        equals(object1, object2, "{0} not equals to {1}", object1, object2);
    }

    public static <T> void equals(T object1, T object2, String pattern, Object... args) {
        if (!Objects.equals(object1, object2)) {
            fail(pattern, args);
        }
    }

    public static AssertionException fail(String pattern, Object... args) {
        throw createFail(pattern, args);
    }

    public static void greater(int lowerBoundExclusive, int value) {
        greater(lowerBoundExclusive, value, "{0} is not greater than {1}", value, lowerBoundExclusive);
    }

    public static void greater(int lowerBoundExclusive, int value, String pattern, Object... args) {
        isTrue(value > lowerBoundExclusive, pattern, args);
    }

    public static void greater(long lowerBoundExclusive, long value) {
        greater(lowerBoundExclusive, value, "{0} is not greater than {1}", value, lowerBoundExclusive);
    }

    public static void greater(long lowerBoundExclusive, long value, String pattern, Object... args) {
        isTrue(value > lowerBoundExclusive, pattern, args);
    }

    public static void greater(byte lowerBoundExclusive, byte value) {
        greater(lowerBoundExclusive, value, "{0} is not greater than {1}", value, lowerBoundExclusive);
    }

    public static void greater(byte lowerBoundExclusive, byte value, String pattern, Object... args) {
        isTrue(value > lowerBoundExclusive, pattern, args);
    }

    public static void greater(short lowerBoundExclusive, short value) {
        greater(lowerBoundExclusive, value, "{0} is not greater than {1}", value, lowerBoundExclusive);
    }

    public static void greater(short lowerBoundExclusive, short value, String pattern, Object... args) {
        isTrue(value > lowerBoundExclusive, pattern, args);
    }

    public static void greaterOrEqual(int lowerBound, int value) {
        greaterOrEqual(lowerBound, value, "{0} is less than {1}", value, lowerBound);
    }

    public static void greaterOrEqual(int lowerBound, int value, String pattern, Object... args) {
        isTrue(value >= lowerBound, pattern, args);
    }

    public static void greaterOrEqual(long lowerBound, long value) {
        greaterOrEqual(lowerBound, value, "{0} is less than {1}", value, lowerBound);
    }

    public static void greaterOrEqual(long lowerBound, long value, String pattern, Object... args) {
        isTrue(value >= lowerBound, pattern, args);
    }

    public static void greaterOrEqual(byte lowerBound, byte value) {
        greaterOrEqual(lowerBound, value, "{0} is less than {1}", value, lowerBound);
    }

    public static void greaterOrEqual(byte lowerBound, byte value, String pattern, Object... args) {
        isTrue(value >= lowerBound, pattern, args);
    }

    public static void greaterOrEqual(short lowerBound, short value) {
        greaterOrEqual(lowerBound, value, "{0} is less than {1}", value, lowerBound);
    }

    public static void greaterOrEqual(short lowerBound, short value, String pattern, Object... args) {
        isTrue(value >= lowerBound, pattern, args);
    }

    public static void isFalse(boolean condition) {
        isFalse(condition, "value is true");
    }

    public static void isFalse(boolean condition, String pattern, Object... args) {
        if (condition) {
            fail(pattern, args);
        }
    }

    public static void isNull(Object value) {
        isNull(value, "value is not null");
    }

    public static void isNull(Object value, String pattern, Object... args) {
        isTrue(value == null, pattern, args);
    }

    public static void isTrue(boolean condition) {
        isTrue(condition, "value is false");
    }

    public static void isTrue(boolean condition, String pattern, Object... args) {
        if (!condition) {
            fail(pattern, args);
        }
    }

    public static void less(int upperBoundExclusive, int value) {
        less(upperBoundExclusive, value, "{0} is not less than {1}", value, upperBoundExclusive);
    }

    public static void less(int upperBoundExclusive, int value, String pattern, Object... args) {
        isTrue(value < upperBoundExclusive, pattern, args);
    }

    public static void less(long upperBoundExclusive, long value) {
        less(upperBoundExclusive, value, "{0} is not less than {1}", value, upperBoundExclusive);
    }

    public static void less(long upperBoundExclusive, long value, String pattern, Object... args) {
        isTrue(value < upperBoundExclusive, pattern, args);
    }

    public static void less(byte upperBoundExclusive, byte value) {
        less(upperBoundExclusive, value, "{0} is not less than {1}", value, upperBoundExclusive);
    }

    public static void less(byte upperBoundExclusive, byte value, String pattern, Object... args) {
        isTrue(value < upperBoundExclusive, pattern, args);
    }

    public static void less(short upperBoundExclusive, short value) {
        less(upperBoundExclusive, value, "{0} is not less than {1}", value, upperBoundExclusive);
    }

    public static void less(short upperBoundExclusive, short value, String pattern, Object... args) {
        isTrue(value < upperBoundExclusive, pattern, args);
    }

    public static void lessOrEqual(int upperBound, int value) {
        lessOrEqual(upperBound, value, "{0} is greater than {1}", value, upperBound);
    }

    public static void lessOrEqual(int upperBound, int value, String pattern, Object... args) {
        isTrue(value <= upperBound, pattern, args);
    }

    public static void lessOrEqual(long upperBound, long value) {
        lessOrEqual(upperBound, value, "{0} is greater than {1}", value, upperBound);
    }

    public static void lessOrEqual(long upperBound, long value, String pattern, Object... args) {
        isTrue(value <= upperBound, pattern, args);
    }

    public static void lessOrEqual(byte upperBound, byte value) {
        lessOrEqual(upperBound, value, "{0} is greater than {1}", value, upperBound);
    }

    public static void lessOrEqual(byte upperBound, byte value, String pattern, Object... args) {
        isTrue(value <= upperBound, pattern, args);
    }

    public static void lessOrEqual(short upperBound, short value) {
        lessOrEqual(upperBound, value, "{0} is greater than {1}", value, upperBound);
    }

    public static void lessOrEqual(short upperBound, short value, String pattern, Object... args) {
        isTrue(value <= upperBound, pattern, args);
    }

    public static String notBlank(String string) {
        return notBlank(string, "blank");
    }

    public static String notBlank(String string, String pattern, Object... args) {
        isFalse(StringUtils.isBlank(string), pattern, args);
        return string;
    }

    public static String notEmpty(String string) {
        return notEmpty(string, "empty");
    }

    public static String notEmpty(String string, String pattern, Object... args) {
        isFalse(StringUtils.isEmpty(string), pattern, args);
        return string;
    }

    public static <T> void notEquals(T object1, T object2) {
        notEquals(object1, object2, "{0} equals to {1}", object1, object2);
    }

    public static <T> void notEquals(T object1, T object2, String pattern, Object... args) {
        isFalse(Objects.equals(object1, object2), pattern, args);
    }

    public static <T> T notNull(T value) {
        return notNull(value, "value is null");
    }

    public static <T> T notNull(T value, String pattern, Object... args) {
        isTrue(value != null, pattern, args);
        return value;
    }

    public static <T> T same(T object1, T object2) {
        return same(object1, object2, "{0} not same to {1}", object1, object2);
    }

    public static <T> T same(T object1, T object2, String pattern, Object... args) {
        isTrue(object1 == object2, pattern, args);
        return object1;
    }

    public static String[] splittable(String string, char splitChar, int expectedPartCount) {
        notNull(string);
        List<String> strings = Splitter.on(splitChar).splitToList(string);
        String[] result = strings.toArray(new String[0]);
        equals(expectedPartCount, result.length);
        return result;
    }

    public static AssertionException unhandledEnum(Enum<?> enumValue) {
        throw createFail("unhandled enum value: {0}", enumValue);
    }

    private AssertUtil() {
        super();
    }

}
