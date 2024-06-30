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

import de.hasait.common.domain.IdAndVersion;
import de.hasait.common.ui.puif.TextAreaForStringPui;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "HOST_CONN_CONFIG", uniqueConstraints = {
        @UniqueConstraint(name = "UC_HCC_FROM_TO", columnNames = {"FROM_NAME", "TO_NAME"})
})
public class HostConnectConfigPO implements IdAndVersion {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "FROM_NAME", nullable = false)
    private String fromName;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "TO_NAME", nullable = false)
    private String toName;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "METHOD", nullable = false)
    private String method;

    @Size(max = 512)
    @Column(name = "CONFIG")
    @TextAreaForStringPui
    private String config;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

}
