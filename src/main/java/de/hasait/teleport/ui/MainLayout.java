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

package de.hasait.teleport.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.hasait.common.security.SecurityService;
import de.hasait.teleport.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SpringComponent
@UIScope
public class MainLayout extends AppLayout {

    private static final Logger LOG = LoggerFactory.getLogger(MainLayout.class);

    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logoH1 = new H1(Application.TITLE);
        logoH1.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        String loggedInUsername = securityService.getAuthenticatedUser().getUsername();
        Button logoutButton = new Button("Log out " + loggedInUsername, e -> securityService.logout());

        var header = new HorizontalLayout(new DrawerToggle(), logoH1, logoutButton);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logoH1);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        addToDrawer(new VerticalLayout( //
                new RouterLink(LocationGridView.TITLE, LocationGridView.class), //
                new RouterLink(NetworkGridView.TITLE, NetworkGridView.class), //
                new RouterLink(HypervisorGridView.TITLE, HypervisorGridView.class), //
                new RouterLink(StorageGridView.TITLE, StorageGridView.class), //
                new RouterLink(VolumeGroupGridView.TITLE, VolumeGroupGridView.class) //
        ));
    }

}
