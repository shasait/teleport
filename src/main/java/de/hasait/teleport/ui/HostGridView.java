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


import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.hasait.common.ui.AbstractCrudGrid;
import de.hasait.common.ui.BeanUiPopulator;
import de.hasait.common.ui.MainLayout;
import de.hasait.teleport.domain.HostPO;
import de.hasait.teleport.domain.HostRepository;
import jakarta.annotation.security.PermitAll;

/**
 *
 */
@PermitAll
@Route(value = "hosts", layout = MainLayout.class)
@SpringComponent
@UIScope
public class HostGridView extends AbstractCrudGrid<HostPO, HostRepository> {

    public HostGridView(HostRepository repository, BeanUiPopulator populator) {
        super(HostPO.class, repository, 2, populator);
    }

}
