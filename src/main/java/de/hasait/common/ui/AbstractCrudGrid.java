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
import com.vaadin.flow.data.selection.SelectionEvent;
import de.hasait.common.domain.PersistantObject;
import de.hasait.common.domain.SearchableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public abstract class AbstractCrudGrid<PO extends PersistantObject, R extends SearchableRepository<PO, Long>> extends AbstractGridView<PO> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCrudGrid.class);

    private final Class<PO> poClass;
    private final R repository;
    private final JpaRepositoryDataProvider<PO, R> dataProvider;
    private final CrudForm<PO, R> crudForm;

    public AbstractCrudGrid(Class<PO> poClass, R repository, int columns, BeanUiPopulator populator) {
        super(poClass, columns);

        this.poClass = poClass;
        this.repository = repository;

        dataProvider = new JpaRepositoryDataProvider<>(repository);
        beanGrid.setDataProvider(dataProvider);

        populator.populateGrid(poClass, beanGrid);

        Grid.Column<PO> idColumn = beanGrid.addColumn(PersistantObject::getId);
        idColumn.setHeader("Id");
        Grid.Column<PO> versionColumn = beanGrid.addColumn(PersistantObject::getVersion);
        versionColumn.setHeader("Version");

        customizeCrudGrid(beanGrid);

        crudForm = new CrudForm<>(poClass, repository, populator);
        customizeCrudForm(crudForm);
        add(crudForm);

        crudForm.addListener(this::updateGrid);
    }

    protected void customizeCrudGrid(Grid<PO> crudGrid) {
        // nop
    }

    protected void customizeCrudForm(CrudForm<PO, R> crudForm) {
        // nop
    }

    protected PO newPO() {
        try {
            return poClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onGridSelectionChanged(SelectionEvent<?, PO> event) {
        super.onGridSelectionChanged(event);

        updateCrudForm();
    }

    protected void updateGrid() {
        super.updateGrid();

        dataProvider.refreshAll();
        beanGrid.deselectAll();
        updateCrudForm();
    }

    private void updateCrudForm() {
        PO selection = getGridSelection();
        LOG.debug("updateBinder: selection={}", selection);
        PO bean = selection != null ? selection : newPO();
        crudForm.setBean(bean);
    }

}
