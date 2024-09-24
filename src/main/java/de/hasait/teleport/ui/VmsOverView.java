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


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.hasait.common.ui.MainLayout;
import de.hasait.common.util.Util;
import de.hasait.teleport.api.VmState;
import de.hasait.teleport.api.VolumeState;
import de.hasait.teleport.domain.NetworkInterfacePO;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.service.action.Action;
import de.hasait.teleport.service.action.ActionService;
import jakarta.annotation.security.PermitAll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@PermitAll
@Route(value = "vms-overview", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@SpringComponent
@UIScope
public class VmsOverView extends VerticalLayout {

    private final ActionService actionService;

    public VmsOverView(VirtualMachineRepository virtualMachineRepository, ActionService actionService) {
        this.actionService = actionService;

        List<VirtualMachinePO> allVms = virtualMachineRepository.findAll();

        List<Action<?>> actions = actionService.determinePossibleActions();
        Map<String, List<Action<?>>> actionsByUiBinding = new HashMap<>();
        actions.forEach(action -> action.getUiBindings().forEach((key, title) -> Util.multiMapPut(actionsByUiBinding, key, action)));

        Div table = createDiv("overviewTable", this);
        Div thead = createDiv("overviewTableHead", table);
        Div headerRow = createDiv("overviewTr", thead);

        createDiv("overviewTh", headerRow, "Name"); // TODO I18N
        createDiv("overviewTh", headerRow, "State"); // TODO I18N

        createDiv("overviewTh", headerRow, "Cores"); // TODO I18N
        createDiv("overviewTh", headerRow, "MemMB"); // TODO I18N
        createDiv("overviewTh", headerRow, "HugeP"); // TODO I18N

        int maxVolumeAttachments = allVms.stream().mapToInt(vm -> vm.getVolumeAttachments().size()).max().orElse(1);
        for (int i = 0; i < maxVolumeAttachments; i++) {
            createDiv("overviewTh", headerRow, "Volume" + i); // TODO I18N
        }

        int maxNetworkInterfaces = allVms.stream().mapToInt(vm -> vm.getNetworkInterfaces().size()).max().orElse(1);
        for (int i = 0; i < maxNetworkInterfaces; i++) {
            createDiv("overviewTh", headerRow, "Net" + i); // TODO I18N
        }

        createDiv("overviewTh", headerRow, "Actions"); // TODO I18N

        Div tbody = createDiv("overviewTableBody", table);
        for (VirtualMachinePO virtualMachine : allVms) {
            Div bodyRow = createDiv("overviewTr", tbody);

            Div nameCell = createDiv("overviewTd", bodyRow, virtualMachine.toFqName());

            Div stateCell = createDiv("overviewTd", bodyRow);
            VmState state = virtualMachine.getState();
            createSpan("state-" + state.name(), stateCell, state.name());

            createDiv("overviewTd", bodyRow, Integer.toString(virtualMachine.getCores()));
            createDiv("overviewTd", bodyRow, Integer.toString(virtualMachine.getMemMb()));
            createDiv("overviewTd", bodyRow, virtualMachine.isMemHugePages() ? "X" : "O");

            int vaIndex = 0;
            for (VolumeAttachmentPO volumeAttachment : virtualMachine.getVolumeAttachments()) {
                Div volumeCell = createDiv("overviewTd", bodyRow);
                VolumeState volumeState = volumeAttachment.getVolume().getState();
                createSpan("volumeState-" + volumeState.name(), volumeCell, volumeState.name());
                createSpan("overviewValue", volumeCell, volumeAttachment.getDev() + "=" + volumeAttachment.getVolume().toFqName());
                vaIndex++;
            }
            while (vaIndex < maxVolumeAttachments) {
                createDiv("overviewTd", bodyRow);
                vaIndex++;
            }

            int niIndex = 0;
            for (NetworkInterfacePO networkInterface : virtualMachine.getNetworkInterfaces()) {
                Div networkInterfaceCell = createDiv("overviewTd", bodyRow);
                createSpan("overviewValue", networkInterfaceCell, networkInterface.getNetwork().getName());
                createSpan("overviewValue", networkInterfaceCell, networkInterface.getMac());
                createSpan("overviewValue", networkInterfaceCell, networkInterface.getIpv4());
                niIndex++;
            }
            while (niIndex < maxNetworkInterfaces) {
                createDiv("overviewTd", bodyRow);
                niIndex++;
            }

            Div actionsCell = createDiv("overviewTd", bodyRow);
            String uiBindingKey = virtualMachine.toFqName();
            List<Action<?>> vmActions = actionsByUiBinding.get(uiBindingKey);
            if (vmActions != null && !vmActions.isEmpty()) {
                for (Action<?> action : vmActions) {
                    String title = action.getUiBindings().get(uiBindingKey);
                    Button button = new Button(title);
                    button.setTooltipText(action.getDescription());
                    button.addSingleClickListener(event -> onActionClick(event, action));
                    actionsCell.add(button);
                }
            }
        }
    }

    private void onActionClick(ClickEvent<Button> buttonClickEvent, Action<?> action) {
        buttonClickEvent.getSource().setEnabled(false);
        actionService.submit(action);
    }

    private Div createDiv(String className, HasComponents parent) {
        return createDiv(className, parent, null);
    }

    private Div createDiv(String className, HasComponents parent, String text) {
        Div div = new Div();
        if (text != null) {
            div.setText(text);
        }
        div.addClassName(className);
        parent.add(div);
        return div;
    }

    private Span createSpan(String className, HasComponents parent, String text) {
        Span span = new Span();
        if (text != null) {
            span.setText(text);
        }
        span.addClassName(className);
        parent.add(span);
        return span;
    }

}
