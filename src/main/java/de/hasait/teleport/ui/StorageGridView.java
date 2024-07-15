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


import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.hasait.common.ui.AbstractCrudGrid;
import de.hasait.common.ui.BeanUiPopulator;
import de.hasait.common.ui.CrudForm;
import de.hasait.common.ui.MainLayout;
import de.hasait.teleport.domain.StoragePO;
import de.hasait.teleport.domain.StorageRepository;
import de.hasait.teleport.service.storage.StorageService;
import jakarta.annotation.security.PermitAll;

/**
 *
 */
@PermitAll
@Route(value = "storages", layout = MainLayout.class)
@SpringComponent
@UIScope
public class StorageGridView extends AbstractCrudGrid<StoragePO, StorageRepository> {

    private final StorageService storageService;

    public StorageGridView(StorageService storageService, StorageRepository repository, BeanUiPopulator populator) {
        super(StoragePO.class, repository, 2, populator);

        this.storageService = storageService;
    }

    @Override
    protected void customizeCrudForm(CrudForm<StoragePO, StorageRepository> crudForm) {
        super.customizeCrudForm(crudForm);

        crudForm.getBinder().withValidator((po, valueContext) -> {
            String failureReason = storageService.validateProviderConfig(po.getDriver(), po.getDriverConfig());
            if (failureReason != null) {
                return ValidationResult.error(failureReason);
            }
            return ValidationResult.ok();
        });

    }
}
