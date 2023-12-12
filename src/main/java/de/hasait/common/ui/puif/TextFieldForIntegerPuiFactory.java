package de.hasait.common.ui.puif;

import com.vaadin.flow.data.converter.StringToIntegerConverter;
import org.springframework.stereotype.Service;

@Service
public class TextFieldForIntegerPuiFactory extends AbstractTextFieldForNumberPuiFactory<Integer, Void> {

    public TextFieldForIntegerPuiFactory() {
        super(Integer.class, Integer.TYPE, () -> null, new StringToIntegerConverter("Invalid"));
    }

}
