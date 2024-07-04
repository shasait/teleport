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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class UnitTest {

    @Test
    void toHuman_0() {
        long longValue = 0L;
        assertEquals("0B", Unit.B1024.toHuman(longValue));
        assertEquals("0GiB", Unit.GIB.toHuman(longValue));
        assertEquals("0B", Unit.B1000.toHuman(longValue));
        assertEquals("0GB", Unit.GB.toHuman(longValue));
        assertEquals("0s", Unit.S.toHuman(longValue));
        assertEquals("0d", Unit.DAY.toHuman(longValue));
    }

    @Test
    void toHuman_1() {
        long longValue = 1L;
        assertEquals("1B", Unit.B1024.toHuman(longValue));
        assertEquals("1GiB", Unit.GIB.toHuman(longValue));
        assertEquals("1B", Unit.B1000.toHuman(longValue));
        assertEquals("1GB", Unit.GB.toHuman(longValue));
        assertEquals("1s", Unit.S.toHuman(longValue));
        assertEquals("1d", Unit.DAY.toHuman(longValue));
    }

    @Test
    void toHuman_minus_1() {
        long longValue = -1L;
        assertEquals("-1B", Unit.B1024.toHuman(longValue));
        assertEquals("-1GiB", Unit.GIB.toHuman(longValue));
        assertEquals("-1B", Unit.B1000.toHuman(longValue));
        assertEquals("-1GB", Unit.GB.toHuman(longValue));
        assertEquals("-1s", Unit.S.toHuman(longValue));
        assertEquals("-1d", Unit.DAY.toHuman(longValue));
    }

    @Test
    void toHuman_2000() {
        long longValue = 2000L;
        assertEquals("1KiB 976B", Unit.B1024.toHuman(longValue));
        assertEquals("1TiB 976GiB", Unit.GIB.toHuman(longValue));
        assertEquals("2KB", Unit.B1000.toHuman(longValue));
        assertEquals("2TB", Unit.GB.toHuman(longValue));
        assertEquals("33m 20s", Unit.S.toHuman(longValue));
        assertEquals("5y 25w 5d", Unit.DAY.toHuman(longValue));
    }

    @Test
    void toHuman_max() {
        long longValue = Long.MAX_VALUE;
        assertEquals("7EiB 1023PiB 1023TiB 1023GiB 1023MiB 1023KiB 1023B", Unit.B1024.toHuman(longValue));
        assertEquals("8191YiB 1023ZiB 1023EiB 1023PiB 1023TiB 1023GiB", Unit.GIB.toHuman(longValue));
        assertEquals("9EB 223PB 372TB 36GB 854MB 775KB 807B", Unit.B1000.toHuman(longValue));
        assertEquals("9223YB 372ZB 36EB 854PB 775TB 807GB", Unit.GB.toHuman(longValue));
        assertEquals("293eon 2747010cen 9y 3w 3d 15h 30m 7s", Unit.S.toHuman(longValue));
        assertEquals("25338934eon 1671834cen 50y 1w", Unit.DAY.toHuman(longValue));
    }

    @Test
    void toHuman_limit_positive() {
        long longValue = Long.MAX_VALUE;
        assertEquals("8191YiB 1023ZiB 1023EiB", Unit.GIB.toHuman(longValue, 3));
        assertEquals("8191YiB 1023ZiB", Unit.GIB.toHuman(longValue, 2));
    }

    @Test
    void toHuman_limit_0() {
        assertThrowsExactly(IllegalArgumentException.class, () -> Unit.GIB.toHuman(1L, 0), "limit < 1: 0");
    }

    @Test
    void toHuman_limit_negative() {
        assertThrowsExactly(IllegalArgumentException.class, () -> Unit.GIB.toHuman(1L, -1), "limit < 1: -1");
        assertThrowsExactly(IllegalArgumentException.class, () -> Unit.GIB.toHuman(1L, -1000), "limit < 1: -1000");
    }

    @Test
    void fromHuman_0() {
        long longValue = 0L;
        assertEquals(longValue, Unit.B1024.fromHuman("0B"));
        assertEquals(longValue, Unit.GIB.fromHuman("0GiB"));
        assertEquals(longValue, Unit.B1000.fromHuman("0B"));
        assertEquals(longValue, Unit.GB.fromHuman("0GB"));
        assertEquals(longValue, Unit.S.fromHuman("0s"));
        assertEquals(longValue, Unit.DAY.fromHuman("0d"));
    }

    @Test
    void fromHuman_1() {
        long longValue = 1L;
        assertEquals(longValue, Unit.B1024.fromHuman("1B"));
        assertEquals(longValue, Unit.GIB.fromHuman("1GiB"));
        assertEquals(longValue, Unit.B1000.fromHuman("1B"));
        assertEquals(longValue, Unit.GB.fromHuman("1GB"));
        assertEquals(longValue, Unit.S.fromHuman("1s"));
        assertEquals(longValue, Unit.DAY.fromHuman("1d"));
    }

    @Test
    void fromHuman_minus_1() {
        long longValue = -1L;
        assertEquals(longValue, Unit.B1024.fromHuman("-1B"));
        assertEquals(longValue, Unit.GIB.fromHuman("-1GiB"));
        assertEquals(longValue, Unit.B1000.fromHuman("-1B"));
        assertEquals(longValue, Unit.GB.fromHuman("-1GB"));
        assertEquals(longValue, Unit.S.fromHuman("-1s"));
        assertEquals(longValue, Unit.DAY.fromHuman("-1d"));
    }

    @Test
    void fromHuman_2000() {
        long longValue = 2000L;
        assertEquals(longValue, Unit.B1024.fromHuman("1KiB 976B"));
        assertEquals(longValue, Unit.GIB.fromHuman("1TiB 976GiB"));
        assertEquals(longValue, Unit.B1000.fromHuman("2KB"));
        assertEquals(longValue, Unit.GB.fromHuman("2TB"));
        assertEquals(longValue, Unit.S.fromHuman("33m 20s"));
        assertEquals(longValue, Unit.DAY.fromHuman("5y 25w 5d"));
    }

    @Test
    void fromHuman_max() {
        long longValue = Long.MAX_VALUE;
        assertEquals(longValue, Unit.B1024.fromHuman("7EiB 1023PiB 1023TiB 1023GiB 1023MiB 1023KiB 1023B"));
        assertEquals(longValue, Unit.GIB.fromHuman("8191YiB 1023ZiB 1023EiB 1023PiB 1023TiB 1023GiB"));
        assertEquals(longValue, Unit.B1000.fromHuman("9EB 223PB 372TB 36GB 854MB 775KB 807B"));
        assertEquals(longValue, Unit.GB.fromHuman("9223YB 372ZB 36EB 854PB 775TB 807GB"));
        assertEquals(longValue, Unit.S.fromHuman("293eon 2747010cen 9y 3w 3d 15h 30m 7s"));
        assertEquals(longValue, Unit.DAY.fromHuman("25338934eon 1671834cen 50y 1w"));
    }

}
