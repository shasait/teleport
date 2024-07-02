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
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;

@Service
public class EnumPuiFactory<E extends Enum<E>> extends AbstractPuiFactory<E, E, ComboBox<E>, EnumPuiConfig<E>> {

    public EnumPuiFactory() {
        super((Class<E>) ((Class) Enum.class), 1000, EnumPuiConfig::new);
    }

    @Override
    protected ComboBox<E> createAndAddField(FormLayout formLayout, String caption, EnumPuiConfig<E> context) {
        ComboBox<E> field = new ComboBox<>(caption);
        field.setItems(context.clazz.getEnumConstants());
        field.setItemLabelGenerator(Object::toString);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <B> Binder.BindingBuilder<B, E> customizeBinding(Binder.BindingBuilder<B, E> bindingBuilder, EnumPuiConfig<E> context) {
        return bindingBuilder;
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, EnumPuiConfig<E> context) {
        if (super.canHandle(beanClass, propertyDescriptor, context)) {
            context.clazz = (Class<E>) propertyDescriptor.getPropertyType();
            return true;
        }
        return false;
    }

}
