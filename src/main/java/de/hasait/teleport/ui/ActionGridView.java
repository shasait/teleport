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

package de.hasait.teleport.ui;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.hasait.common.ui.AbstractGridView;
import de.hasait.common.ui.MainLayout;
import de.hasait.teleport.service.Action;
import de.hasait.teleport.service.ActionService;
import jakarta.annotation.security.PermitAll;

/**
 *
 */
@PermitAll
@Route(value = "actions", layout = MainLayout.class)
@SpringComponent
@UIScope
public class ActionGridView extends AbstractGridView<Action<?>> {

    private final ActionService actionService;

    public ActionGridView(ActionService actionService) {
        super((Class) Action.class, 2);

        this.actionService = actionService;

        Grid.Column<Action<?>> descriptionColumn = beanGrid.addColumn(Action::getDescription);
        descriptionColumn.setHeader("Description");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        add(buttonLayout);

        Button refreshButton = new Button("Refresh");
        buttonLayout.add(refreshButton);

        refreshButton.addClickListener(event -> updateGrid());
    }

    @Override
    protected void updateGrid() {
        super.updateGrid();

        beanGrid.setItems(actionService.determineActions());
    }

}
