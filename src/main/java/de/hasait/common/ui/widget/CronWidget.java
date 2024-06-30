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

package de.hasait.common.ui.widget;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.ui.VaadinUtil;
import de.hasait.common.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CronWidget {

    private final DateTimeFormatter nextFormatter;

    private final TextField cronExpressionField;
    private final TextField next1PreviewLabel;
    private final TextField nextRelativePreviewLabel;
    private final TextField next2PreviewLabel;

    public CronWidget() {
        super();

        Locale locale = Locale.getDefault();

        nextFormatter = DateTimeFormatter.ofPattern(VaadinUtil.getTranslation("cronNext.formatter"));

        cronExpressionField = new TextField(VaadinUtil.getTranslation("cronExpression.label"));
        cronExpressionField.setTooltipText(VaadinUtil.getTranslation("cronExpression.tooltip"));

        next1PreviewLabel = new TextField(VaadinUtil.getTranslation("cronNext.label"));

        nextRelativePreviewLabel = new TextField(VaadinUtil.getTranslation("cronNextRelative.label"));

        next2PreviewLabel = new TextField(VaadinUtil.getTranslation("cronNextNext.label"));
    }

    public TextField getCronExpressionField() {
        return cronExpressionField;
    }

    public TextField getNext1PreviewLabel() {
        return next1PreviewLabel;
    }

    public TextField getNextRelativePreviewLabel() {
        return nextRelativePreviewLabel;
    }

    public TextField getNext2PreviewLabel() {
        return next2PreviewLabel;
    }

    public void populateLayout(HasComponents hasComponents) {
        hasComponents.add(cronExpressionField);

        next1PreviewLabel.setReadOnly(true);
        hasComponents.add(next1PreviewLabel);

        nextRelativePreviewLabel.setReadOnly(true);
        hasComponents.add(nextRelativePreviewLabel);

        next2PreviewLabel.setReadOnly(true);
        hasComponents.add(next2PreviewLabel);
    }

    public void populateBinder(Binder<?> binder) {
        binder.forMemberField(cronExpressionField) //
                .withValidator(value -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime next1 = Util.determineNext(value, now);
                    next1PreviewLabel.setValue(formatNext(next1));
                    nextRelativePreviewLabel.setValue(Util.determineNextRelative(now, next1, Integer.MAX_VALUE));
                    LocalDateTime next2 = Util.determineNext(value, next1);
                    next2PreviewLabel.setValue(formatNext(next2));
                    return true;
                }, "Invalid");
    }

    private String formatNext(LocalDateTime next) {
        return next == null ? StringUtils.EMPTY : nextFormatter.format(next);
    }

}
