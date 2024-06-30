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

package de.hasait.teleport.domain;

import de.hasait.teleport.service.SnapshotNameGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

@Embeddable
public class SnapshotData {

    @Size(min = 1, max = 128)
    @NotNull
    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATION")
    private LocalDateTime creation;

    @Column(name = "CONSISTENT")
    private boolean consistent;

    @Column(name = "SNAP_VERSION")
    private int snapVersion;

    public SnapshotData() {
    }

    public SnapshotData(String name, LocalDateTime creation) {
        this.name = name;
        this.creation = creation;

        Matcher nameMatcher = SnapshotNameGenerator.NAME_PATTERN.matcher(name);
        if (!nameMatcher.matches()) {
            throw new IllegalArgumentException("Invalid snapshot name: " + name);
        }
        snapVersion = Integer.parseInt(nameMatcher.group(SnapshotNameGenerator.NAME_PATTERN_VERSION_GROUPNAME));
        consistent = "c".equals(nameMatcher.group(SnapshotNameGenerator.NAME_PATTERN_CONSISTENT_GROUPNAME));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public boolean isConsistent() {
        return consistent;
    }

    public void setConsistent(boolean consistent) {
        this.consistent = consistent;
    }

    public int getSnapVersion() {
        return snapVersion;
    }

    public void setSnapVersion(int snapVersion) {
        this.snapVersion = snapVersion;
    }

}
