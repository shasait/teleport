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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.Converter;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.function.Supplier;

public abstract class AbstractTextFieldForNumberPuiFactory<N extends Number, C> extends AbstractPuiFactory<N, String, TextField, C> {


    protected final Class<?> primitiveClass;
    protected final Converter<String, N> converter;

    public AbstractTextFieldForNumberPuiFactory(Class<N> propertyClass, Class<?> primitiveClass, Supplier<C> contextFactory, Converter<String, N> converter) {
        super(propertyClass, 2000, contextFactory);

        this.primitiveClass = primitiveClass;
        this.converter = converter;
    }

    @Override
    protected TextField createAndAddField(FormLayout formLayout, String caption, C context) {
        TextField field = new TextField(caption);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <B> Binder.BindingBuilder<B, N> customizeBinding(Binder.BindingBuilder<B, String> bindingBuilder, C context) {
        return bindingBuilder //
                .withNullRepresentation(StringUtils.EMPTY) //
                .withConverter(converter) //
                ;
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, C context) {
        return super.canHandle(beanClass, propertyDescriptor, context) || primitiveClass.equals(propertyDescriptor.getPropertyType());
    }

}
