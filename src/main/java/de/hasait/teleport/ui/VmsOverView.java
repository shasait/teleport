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


import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.hasait.common.ui.MainLayout;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import jakarta.annotation.security.PermitAll;

/**
 *
 */
@PermitAll
@Route(value = "vms-overview", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@SpringComponent
@UIScope
public class VmsOverView extends VerticalLayout {

    public VmsOverView(VirtualMachineRepository virtualMachineRepository) {
        Div table = createDiv("overviewTable", this);
        Div thead = createDiv("overviewTableHead", table);
        Div headerRow = createDiv("overviewTr", thead);

        Div nameHeader = createDiv("overviewTh", headerRow);
        nameHeader.setText("Name");

        Div stateHeader = createDiv("overviewTh", headerRow);
        stateHeader.setText("State");

        Div tbody = createDiv("overviewTableBody", table);
        int maxVas = 0;
        for (VirtualMachinePO virtualMachine : virtualMachineRepository.findAll()) {
            Div bodyRow = createDiv("overviewTr", tbody);

            Div nameCell = createDiv("overviewTd", bodyRow);
            nameCell.setText(virtualMachine.getName());

            Div stateCell = createDiv("overviewTd", bodyRow);
            stateCell.setText(virtualMachine.getState().name());

            int vaIndex = 0;
            for (VolumeAttachmentPO volumeAttachment : virtualMachine.getVolumeAttachments()) {
                Div vaCell = createDiv("overviewTd", bodyRow);
                vaCell.setText(volumeAttachment.getDev() + "=" + volumeAttachment.getVolume().toFqName());
                vaIndex++;
            }
            maxVas = Math.max(maxVas, vaIndex);
        }

        for (int va = 0; va < maxVas; va++) {
            Div vaHeader = createDiv("overviewTh", headerRow);
            vaHeader.setText("Volume" + va);
        }
    }

    private Div createDiv(String className, HasComponents parent) {
        Div div = new Div();
        div.addClassName(className);
        parent.add(div);
        return div;
    }

}
