package de.hasait.common.ui.puif;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.springframework.stereotype.Service;

@Service
public class StringPropertyUiFactory extends AbstractPropertyUiFactory<String, String, TextField> {

    public StringPropertyUiFactory() {
        super(String.class, 1000);
    }

    @Override
    protected TextField createAndAddField(FormLayout formLayout, String caption) {
        TextField field = new TextField(caption);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <T> Binder.BindingBuilder<T, String> customizeBinding(Binder.BindingBuilder<T, String> bindingBuilder) {
        return bindingBuilder;
    }

}
