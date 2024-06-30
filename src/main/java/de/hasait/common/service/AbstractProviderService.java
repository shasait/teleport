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

package de.hasait.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class AbstractProviderService<P extends Provider> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractProviderService.class);

    private final Map<String, P> providersById = new HashMap<>();

    public AbstractProviderService(P[] providers) {
        super();

        for (P provider : providers) {
            String providerId = provider.getId();
            P previousProvider = providersById.put(providerId, provider);
            if (previousProvider != null) {
                throw new RuntimeException("Duplicate providerId: " + providerId);
            }
        }
    }

    public final List<String> findAllIds() {
        return providersById.keySet().stream().sorted().collect(Collectors.toList());
    }

    public final List<P> findAll() {
        return providersById.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public final String validateProviderConfig(String providerId, String providerConfig) {
        try {
            P provider = getProviderByIdNotNull(providerId);
            String errorMessage = provider.validateConfig(providerConfig);
            if (errorMessage != null) {
                return "Invalid providerConfig: " + errorMessage;
            }
            return null;
        } catch (InvalidProviderIdException e) {
            return "Provider not found: " + providerId;
        } catch (RuntimeException e) {
            LOG.debug("validateProviderConfig: providerId={}, providerConfig={}", providerId, providerConfig, e);
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    @Nonnull
    protected final P getProviderByIdNotNull(String providerId) {
        P provider = providersById.get(providerId);
        if (provider == null) {
            throw new InvalidProviderIdException(providerId);
        }
        return provider;
    }

}
