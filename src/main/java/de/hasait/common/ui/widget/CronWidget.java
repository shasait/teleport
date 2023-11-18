package de.hasait.common.ui.widget;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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

    public CronWidget(MessageSource messageSource) {
        super();

        Locale locale = Locale.getDefault();

        nextFormatter = DateTimeFormatter.ofPattern(messageSource.getMessage("cronNext.formatter", null, locale));

        cronExpressionField = new TextField(messageSource.getMessage("cronExpression.label", null, locale));
        cronExpressionField.setTooltipText(messageSource.getMessage("cronExpression.tooltip", null, locale));

        next1PreviewLabel = new TextField(messageSource.getMessage("cronNext.label", null, locale));

        nextRelativePreviewLabel = new TextField(messageSource.getMessage("cronNextRelative.label", null, locale));

        next2PreviewLabel = new TextField(messageSource.getMessage("cronNextNext.label", null, locale));
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
