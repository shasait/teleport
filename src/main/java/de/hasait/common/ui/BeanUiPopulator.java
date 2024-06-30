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

package de.hasait.common.ui;


import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@Service
public class BeanUiPopulator {

    private static final Logger LOG = LoggerFactory.getLogger(BeanUiPopulator.class);

    private static final Set<String> IGNORE_PROPERTIES = Set.of("class", "id", "version");

    private final List<PropertyUiFactory<?>> propertyUiFactories;

    private final Map<Class<?>, List<PropertyField<?>>> propertyFieldsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<PropertyColumn<?>>> propertyColumnsCache = new ConcurrentHashMap<>();

    public BeanUiPopulator(List<PropertyUiFactory<?>> propertyUiFactories) {
        this.propertyUiFactories = new ArrayList<>(propertyUiFactories);
        this.propertyUiFactories.sort(Comparator.comparingInt(PropertyUiFactory::getPriority));
    }

    public <B> void populateForm(Class<B> beanClass, Binder<B> binder, FormLayout formLayout) {
        cachedDeterminePropertyFields(beanClass).forEach(it -> it.addFieldAndBind(formLayout, binder));
    }

    @SuppressWarnings("unchecked")
    private <B> List<PropertyField<B>> cachedDeterminePropertyFields(Class<B> beanClass) {
        Object o1 = propertyFieldsCache.computeIfAbsent(beanClass, ignored -> {
            Object o2 = determinePropertyFields(beanClass);
            return (List<PropertyField<?>>) o2;
        });
        return (List<PropertyField<B>>) o1;
    }

    private <B> List<PropertyField<B>> determinePropertyFields(Class<B> beanClass) {
        List<PropertyField<B>> fields = new ArrayList<>();

        ReflectionUtil.forEachProperty(beanClass, propertyDescriptor -> {
            if (propertyDescriptor.getReadMethod() == null) {
                return true;
            }

            String propertyName = propertyDescriptor.getName();
            if (IGNORE_PROPERTIES.contains(propertyName)) {
                return true;
            }

            PropertyField<B> field = null;
            for (PropertyUiFactory<?> factory : propertyUiFactories) {
                field = factory.createField(beanClass, propertyDescriptor);
                if (field != null) {
                    break;
                }
            }
            if (field != null) {
                fields.add(field);
            } else {
                LOG.warn("Unhandled property: {}", propertyName);
            }

            return true;
        });

        fields.sort(Comparator.<PropertyField<B>>comparingInt(PropertyField::getLayoutPriority) //
                .thenComparing(PropertyField::getPropertyName) //
        );

        return fields;
    }

    public <B> void populateGrid(Class<B> beanClass, Grid<B> beanGrid) {
        cachedDeterminePropertyColumns(beanClass).forEach(it -> it.addColumn(beanGrid));
    }

    @SuppressWarnings("unchecked")
    private <B> List<PropertyColumn<B>> cachedDeterminePropertyColumns(Class<B> beanClass) {
        Object o1 = propertyColumnsCache.computeIfAbsent(beanClass, ignored -> {
            Object o2 = determinePropertyColumns(beanClass);
            return (List<PropertyColumn<?>>) o2;
        });
        return (List<PropertyColumn<B>>) o1;
    }

    private <B> List<PropertyColumn<B>> determinePropertyColumns(Class<B> beanClass) {
        List<PropertyColumn<B>> columns = new ArrayList<>();

        ReflectionUtil.forEachProperty(beanClass, propertyDescriptor -> {
            if (propertyDescriptor.getReadMethod() == null) {
                return true;
            }

            String propertyName = propertyDescriptor.getName();
            if (IGNORE_PROPERTIES.contains(propertyName)) {
                return true;
            }

            PropertyColumn<B> column = null;
            for (PropertyUiFactory<?> factory : propertyUiFactories) {
                column = factory.createColumn(beanClass, propertyDescriptor);
                if (column != null) {
                    break;
                }
            }
            if (column != null) {
                columns.add(column);
            } else {
                LOG.warn("Unhandled property: {}", propertyName);
            }

            return true;
        });

        columns.sort(Comparator.<PropertyColumn<B>>comparingInt(PropertyColumn::getLayoutPriority) //
                .thenComparing(PropertyColumn::getPropertyName) //
        );

        return columns;
    }

}
