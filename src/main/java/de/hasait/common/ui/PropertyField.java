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
import com.vaadin.flow.data.binder.Binder;

public abstract class PropertyField<BEAN> {

    private final String propertyName;
    private final int layoutPriority;

    public PropertyField(String propertyName, int layoutPriority) {
        this.propertyName = propertyName;
        this.layoutPriority = layoutPriority;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public int getLayoutPriority() {
        return layoutPriority;
    }

    public abstract void addFieldAndBind(FormLayout formLayout, Binder<BEAN> binder);

}
