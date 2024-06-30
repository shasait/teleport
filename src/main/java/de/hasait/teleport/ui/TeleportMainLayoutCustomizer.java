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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.hasait.common.ui.MainLayoutCustomizer;
import de.hasait.common.ui.VaadinUtil;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.LocationPO;
import de.hasait.teleport.domain.NetworkPO;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.VolumePO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TeleportMainLayoutCustomizer implements MainLayoutCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(TeleportMainLayoutCustomizer.class);

    @Override
    public void populateDrawer(VerticalLayout verticalLayout) {
        VaadinUtil.addDataViewRouterLink(verticalLayout, LocationPO.class, "grid", LocationGridView.class);
        VaadinUtil.addDataViewRouterLink(verticalLayout, NetworkPO.class, "grid", NetworkGridView.class);
        VaadinUtil.addDataViewRouterLink(verticalLayout, HypervisorPO.class, "grid", HypervisorGridView.class);
        VaadinUtil.addDataViewRouterLink(verticalLayout, StoragePO.class, "grid", StorageGridView.class);
        VaadinUtil.addDataViewRouterLink(verticalLayout, VolumePO.class, "grid", VolumeGridView.class);
    }

}
