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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.ui.PropertyColumn;
import de.hasait.common.ui.PropertyField;
import de.hasait.common.ui.PropertyUiFactory;
import de.hasait.common.util.ReflectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractPuiFactory<P, V, F extends Component & HasValue<?, V>, C> implements PropertyUiFactory<P> {

    private final Class<P> propertyClass;
    private final int priority;
    private final Supplier<C> contextFactory;

    private MessageSource messageSource;

    protected AbstractPuiFactory(Class<P> propertyClass, int priority, Supplier<C> contextFactory) {
        this.propertyClass = propertyClass;
        this.priority = priority;
        this.contextFactory = contextFactory;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public final Class<P> getPropertyClass() {
        return propertyClass;
    }

    @Override
    public final int getPriority() {
        return priority;
    }

    @Override
    public final <B> PropertyField<B> createField(Class<B> beanClass, PropertyDescriptor propertyDescriptor) {
        C context = contextFactory.get();
        if (!canHandle(beanClass, propertyDescriptor, context)) {
            return null;
        }

        String propertyName = propertyDescriptor.getName();
        int layoutPriority = determineLayoutPriority(propertyName);
        boolean required = ReflectionUtil.isRequired(beanClass, propertyDescriptor);

        return new PropertyField<>(propertyName, layoutPriority) {
            @Override
            public void addFieldAndBind(FormLayout formLayout, Binder<B> binder) {
                String label = determineLabel(propertyName);
                F field = createAndAddField(formLayout, label, context);
                Binder.BindingBuilder<B, V> bindingBuilder = binder.forField(field);
                if (required) {
                    bindingBuilder = bindingBuilder.asRequired();
                }
                customizeBinding(bindingBuilder, context).bind(propertyName);
            }
        };
    }

    @Override
    public final <B> PropertyColumn<B> createColumn(Class<B> beanClass, PropertyDescriptor propertyDescriptor) {
        C context = contextFactory.get();
        if (!canHandle(beanClass, propertyDescriptor, context)) {
            return null;
        }

        String propertyName = propertyDescriptor.getName();
        int layoutPriority = determineLayoutPriority(propertyName);

        return new PropertyColumn<>(propertyName, layoutPriority) {
            @Override
            public Grid.Column<B> addColumn(Grid<B> grid) {
                return AbstractPuiFactory.this.addColumn(propertyName, determineLabel(propertyName), grid, context);
            }
        };
    }

    protected abstract F createAndAddField(FormLayout formLayout, String caption, C context);

    protected abstract <B> Binder.BindingBuilder<B, P> customizeBinding(Binder.BindingBuilder<B, V> bindingBuilder, C context);

    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, C context) {
        return propertyClass.isAssignableFrom(propertyDescriptor.getPropertyType());
    }

    protected <B> Grid.Column<B> addColumn(String propertyName, String label, Grid<B> grid, C context) {
        Grid.Column<B> column = grid.addColumn(propertyName);
        column.setHeader(label);
        return column;
    }

    private String determineLabel(String propertyName) {
        return messageSource.getMessage(propertyName + ".label", null, propertyName, Locale.getDefault());
    }

    private int determineLayoutPriority(String propertyName) {
        String layoutPriorityString = messageSource.getMessage(propertyName + ".layoutPriority", null, "1000", Locale.getDefault());
        return Integer.parseInt(Objects.requireNonNull(layoutPriorityString));
    }

}
