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
import de.hasait.common.service.AbstractDriverService;
import de.hasait.common.service.Driver;

/**
 *
 */
public class AbstractProvidersView<P extends Driver, S extends AbstractDriverService<P>> extends AbstractGridView<P> {

    private final S providerService;

    public AbstractProvidersView(Class<P> providerClass, S providerService) {
        super(providerClass, 1);

        this.providerService = providerService;

        Grid.Column<P> providerIdColumn = beanGrid.addColumn(Driver::getId);
        providerIdColumn.setHeader("Provider");

        Grid.Column<P> descriptionColumn = beanGrid.addColumn(Driver::getDescription);
        descriptionColumn.setHeader("Description");

        Grid.Column<P> disabledReasonColumn = beanGrid.addColumn(Driver::getDisabledReason);
        disabledReasonColumn.setHeader("Disabled Reason");
    }

    @Override
    protected final void updateGrid() {
        super.updateGrid();

        beanGrid.setItems(providerService.findAll());
    }

}
