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

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;

@Service
public class CheckBoxForBooleanPuiFactory extends AbstractPuiFactory<Boolean, Boolean, Checkbox, Void> {

    public CheckBoxForBooleanPuiFactory() {
        super(Boolean.class, 1000, () -> null);
    }

    @Override
    protected Checkbox createAndAddField(FormLayout formLayout, String caption, Void context) {
        Checkbox field = new Checkbox(caption);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <T> Binder.BindingBuilder<T, Boolean> customizeBinding(Binder.BindingBuilder<T, Boolean> bindingBuilder, Void context) {
        return bindingBuilder;
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, Void context) {
        return super.canHandle(beanClass, propertyDescriptor, context) || Boolean.TYPE.equals(propertyDescriptor.getPropertyType());
    }

}
