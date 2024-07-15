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
import de.hasait.teleport.service.action.Action;
import de.hasait.teleport.service.action.ActionFutureTask;
import de.hasait.teleport.service.action.ActionService;
import jakarta.annotation.security.PermitAll;

/**
 *
 */
@PermitAll
@Route(value = "actions", layout = MainLayout.class)
@SpringComponent
@UIScope
public class ActionsGridView extends AbstractGridView<Action<?>> {

    private final ActionService actionService;
    private final Grid<ActionFutureTask<?>> queueGrid;

    public ActionsGridView(ActionService actionService) {
        super((Class) Action.class, 2);

        this.actionService = actionService;

        Grid.Column<Action<?>> beanGridDescriptionColumn = beanGrid.addColumn(Action::getDescription);
        beanGridDescriptionColumn.setHeader("Description");

        HorizontalLayout topButtonsLayout = new HorizontalLayout();
        addComponentAsFirst(topButtonsLayout);

        HorizontalLayout possibleActionsButtonLayout = new HorizontalLayout();
        add(possibleActionsButtonLayout);

        queueGrid = new Grid<>((Class) ActionFutureTask.class, false);
        queueGrid.setSizeFull();
        add(queueGrid);

        Grid.Column<ActionFutureTask<?>> queueGridRunningColumn = queueGrid.addColumn(ActionFutureTask::isRunning);
        beanGridDescriptionColumn.setHeader("Running");
        Grid.Column<ActionFutureTask<?>> queueGridDescriptionColumn = queueGrid.addColumn(t -> t.getAction().getDescription());
        queueGridDescriptionColumn.setHeader("Description");

        // Buttons

        Button refreshButton = new Button("Refresh");
        refreshButton.addClickListener(event -> update());
        topButtonsLayout.add(refreshButton);

        Button executeButton = new Button("Execute");
        executeButton.addClickListener(event -> executeSelectedPossibleActions());
        possibleActionsButtonLayout.add(executeButton);

        update();
    }

    @Override
    protected void updateGrid() {
        super.updateGrid();

        beanGrid.setItems(actionService.determinePossibleActions());
    }

    private void executeSelectedPossibleActions() {
        beanGrid.getSelectedItems().forEach(actionService::submit);

        update();
    }

    private void update() {
        updateGrid();

        queueGrid.setItems(actionService.determineQueuedAndExecutingActions());
    }

}
