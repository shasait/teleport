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


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.StatusChangeEvent;
import de.hasait.common.domain.IdAndVersion;
import de.hasait.common.domain.SearchableRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public final class CrudForm<PO extends IdAndVersion, R extends SearchableRepository<PO, Long>> extends FormLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CrudForm.class);

    private final Class<PO> poClass;

    private final R repository;

    private final Binder<PO> binder;

    private final HorizontalLayout formButtonLayout;

    private final List<Runnable> changeListeners;

    private final Button addOrSaveButton;
    private final Button deleteButton;

    private final Span statusSpan;

    public CrudForm(Class<PO> poClass, R repository, BeanUiPopulator populator) {
        super();

        this.poClass = poClass;
        this.repository = repository;
        this.binder = new Binder<>(poClass);

        formButtonLayout = new HorizontalLayout();
        statusSpan = new Span();

        addOrSaveButton = new Button();
        addButtonToForm(addOrSaveButton);

        deleteButton = new Button("Delete");
        deleteButton.setIcon(VaadinIcon.TRASH.create());
        addButtonToForm(deleteButton);

        populator.populateForm(poClass,binder,this);

        add(formButtonLayout);
        add(statusSpan);

        addOrSaveButton.addClickListener(this::onAddOrSaveButtonClicked);
        deleteButton.addClickListener(this::onDeleteButtonClicked);

        binder.addStatusChangeListener(this::onBinderStatusChanged);
        binder.setStatusLabel(statusSpan);
        changeListeners = new CopyOnWriteArrayList<>();
    }

    public void setBean(PO bean) {
        binder.setBean(bean);
        binder.validate();

        boolean existing = bean.getId() != null;
        addOrSaveButton.setText(existing ? "Save" : "Add");
        addOrSaveButton.setIcon(existing ? VaadinIcon.ARCHIVE.create() : VaadinIcon.PLUS.create());
        deleteButton.setEnabled(existing);
    }

    public  void addListener(@Nonnull Runnable listener) {
        changeListeners.add(listener);
    }

    public  void removeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                LOG.warn("ignored listener failure", e);
            }
        }
    }

    private void addSpacer() {
        add(new Span(StringUtils.EMPTY));
    }

    private void addHeader(String text, int colspan) {
        H4 heading = new H4(text);
        heading.getStyle().set("margin-top", "1em");
        add(heading, colspan);
    }

    private void addButtonToForm(Component component) {
        formButtonLayout.add(component);
    }

    private void onBinderStatusChanged(StatusChangeEvent event) {
        boolean valid = !event.hasValidationErrors();
        addOrSaveButton.setEnabled(valid);
    }

    private void onAddOrSaveButtonClicked(ClickEvent<?> event) {
        BinderValidationStatus<PO> status = binder.validate();
        if (!status.isOk()) {
            String message = "Validation failed";
            if (status.hasErrors()) {
                message += ": " + status.getValidationErrors().get(0).getErrorMessage();
            }
            Notification.show(message);
            return;
        }

        PO bean = binder.getBean();
        if (bean != null) {
            repository.saveAndFlush(bean);
            notifyListeners();
        }
    }

    private void onDeleteButtonClicked(ClickEvent<?> clickEvent) {
        PO bean = binder.getBean();
        if (bean != null) {
            repository.delete(bean);
            notifyListeners();
        }
    }

}
