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

package de.hasait.teleport.service;



import de.hasait.common.util.Util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public final class SnapshotNameGenerator {

    public static final Pattern NAME_PATTERN = Pattern.compile("(?<v>\\d+)(?<ci>[ci])(?:-(?<rp>[A-Za-z\\d]+))?");
    public static final String NAME_PATTERN_VERSION_GROUPNAME = "v";
    public static final String NAME_PATTERN_CONSISTENT_GROUPNAME = "ci";
    public static final String NAME_PATTERN_RANDOMPART_GROUPNAME = "rp";

    private static final String SNAPSHOT_NAME_RP_CHARS = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
    private static final int SNAPSHOT_NAME_RP_CHARS_LEN = SNAPSHOT_NAME_RP_CHARS.length();

    private final Map<Integer, String> randomPartByVersionCache = new ConcurrentHashMap<>();

    private final AtomicInteger maxSnapshotVersion = new AtomicInteger();

    public String createSnapshotName(boolean consistent, int version) {
        if (version < 0 || version > 99999999) {
            throw new IllegalArgumentException("version out of range: " + version);
        }
        String versionString = Integer.toString(version);
        return "0".repeat(8 - versionString.length()) + versionString + (consistent ? "c" : "i") + "-" + getOrCreateRandomPart(version);
    }

    public String getOrCreateRandomPart(int version) {
        return randomPartByVersionCache.computeIfAbsent(version, ignored -> createRandomPart());
    }

    public void registerRandomPart(int version, String randomPart) {
        randomPartByVersionCache.putIfAbsent(version, randomPart);
        maxSnapshotVersion.updateAndGet(current -> Math.max(current, version));
    }

    public int getMaxSnapshotVersion() {
        return maxSnapshotVersion.get();
    }

    private String createRandomPart() {
        StringBuilder randomPartSB = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            randomPartSB.append(SNAPSHOT_NAME_RP_CHARS.charAt(Util.RANDOM.nextInt(SNAPSHOT_NAME_RP_CHARS_LEN)));
        }
        return randomPartSB.toString();
    }

}
