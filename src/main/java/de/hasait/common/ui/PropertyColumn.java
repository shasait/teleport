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

import com.vaadin.flow.component.grid.Grid;

import java.beans.PropertyDescriptor;

public abstract class PropertyColumn<B> {

    private final PropertyDescriptor propertyDescriptor;
    private final int layoutPriority;

    public PropertyColumn(PropertyDescriptor propertyDescriptor, int layoutPriority) {
        this.propertyDescriptor = propertyDescriptor;
        this.layoutPriority = layoutPriority;
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public String getPropertyName() {
        return propertyDescriptor.getName();
    }

    public int getLayoutPriority() {
        return layoutPriority;
    }

    public abstract Grid.Column<B> addColumn(Grid<B> grid);

}
