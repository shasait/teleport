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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.domain.IdAndVersion;
import de.hasait.common.domain.SearchableRepository;
import de.hasait.common.ui.JpaRepositoryDataProvider;

import java.util.function.Supplier;

public abstract class AbstractToOnePuiFactory<PO extends IdAndVersion, R extends SearchableRepository<PO, ?>, C> extends AbstractPuiFactory<PO, PO, ComboBox<PO>, C> {

    private final R repository;

    public AbstractToOnePuiFactory(Class<PO> poClass, Supplier<C> contextFactory, R repository) {
        super(poClass, 0, contextFactory);

        this.repository = repository;
    }

    @Override
    protected ComboBox<PO> createAndAddField(FormLayout formLayout, String caption, C context) {
        ComboBox<PO> field = new ComboBox<>(caption);
        field.setItems(new JpaRepositoryDataProvider<>(repository));
        field.setItemLabelGenerator(this::getPoLabel);
        formLayout.add(field, 1);
        return field;
    }

    protected abstract String getPoLabel(PO po);

    @Override
    protected <B> Binder.BindingBuilder<B, PO> customizeBinding(Binder.BindingBuilder<B, PO> bindingBuilder, C context) {
        return bindingBuilder;
    }

    @Override
    protected <B> Grid.Column<B> addColumn(String propertyName, String label, Grid<B> grid, C context) {
        return super.addColumn(propertyName + "." + getColumnLabelProperty(), label, grid, context);
    }

    protected abstract String getColumnLabelProperty();

}
