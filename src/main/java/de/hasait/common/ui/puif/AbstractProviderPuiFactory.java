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

package de.hasait.common.ui.puif;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.service.Provider;
import de.hasait.common.service.ProviderService;
import de.hasait.common.util.ReflectionUtil;

import java.beans.PropertyDescriptor;
import java.util.function.Supplier;

public abstract class AbstractProviderPuiFactory<P extends Provider, C> extends AbstractPuiFactory<String, String, ComboBox<String>, C> {

    private final Class<P> providerClass;

    private final ProviderService<P> providerService;

    public AbstractProviderPuiFactory(Class<P> providerClass, Supplier<C> contextFactory, ProviderService<P> providerService) {
        super(String.class, 0, contextFactory);

        this.providerClass = providerClass;
        this.providerService = providerService;
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, C context) {
        if (super.canHandle(beanClass, propertyDescriptor, context)) {
            ProvderPui annotation = ReflectionUtil.findAnnotation(beanClass, propertyDescriptor, ProvderPui.class);
            if (annotation != null && annotation.provider().equals(providerClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ComboBox<String> createAndAddField(FormLayout formLayout, String caption, C context) {
        ComboBox<String> field = new ComboBox<>(caption);
        field.setItems(providerService.findAllIds());
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <B> Binder.BindingBuilder<B, String> customizeBinding(Binder.BindingBuilder<B, String> bindingBuilder, C context) {
        return bindingBuilder;
    }

}
