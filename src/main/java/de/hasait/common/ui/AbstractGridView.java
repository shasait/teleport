/*
 * Copyright (C) 2021 by Sebastian Hasait (sebastian at hasait dot de)
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


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 *
 */
public abstract class AbstractGridView<T> extends VerticalLayout implements HasDynamicTitle {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractGridView.class);

    protected final Class<T> beanClass;
    protected final Grid<T> beanGrid;

    public AbstractGridView(Class<T> beanClass, int columns) {
        this.beanClass = beanClass;

        addAttachListener(this::attach);
        addDetachListener(this::detach);

        setSizeFull();

        beanGrid = new Grid<>(beanClass, false);
        beanGrid.setSizeFull();
        add(beanGrid);

        beanGrid.addSelectionListener(this::onGridSelectionChanged);
    }

    @Override
    public final String getPageTitle() {
        return VaadinUtil.getApplicationAndPageTitle(beanClass, "grid");
    }

    private void attach(AttachEvent attachEvent) {
        LOG.debug("attach {}", getClass());

        updateGrid();
    }

    private void detach(DetachEvent detachEvent) {
        LOG.debug("detach {}", getClass());
    }

    protected final T getGridSelection() {
        Set<T> selectedItems = beanGrid.getSelectedItems();
        return selectedItems.isEmpty() ? null : selectedItems.iterator().next();
    }

    protected void onGridSelectionChanged(SelectionEvent<?, T> event) {
    }

    protected void updateGrid() {
        LOG.debug("updateGrid");
    }

}
