package de.hasait.common.ui.puif;

import com.vaadin.flow.data.converter.StringToLongConverter;
import org.springframework.stereotype.Service;

@Service
public class TextFieldForLongPuiFactory extends AbstractTextFieldForNumberPuiFactory<Long, Void> {

    public TextFieldForLongPuiFactory() {
        super(Long.class, Long.TYPE, () -> null, new StringToLongConverter("Invalid"));
    }

}
