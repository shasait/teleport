package de.hasait.common.ui.puif;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;

@Service
public class IntegerPropertyUiFactory extends AbstractPropertyUiFactory<Integer, String, TextField> {

    public IntegerPropertyUiFactory() {
        super(Integer.class, 1001);
    }

    @Override
    protected TextField createAndAddField(FormLayout formLayout, String caption) {
        TextField field = new TextField(caption);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <B> Binder.BindingBuilder<B, Integer> customizeBinding(Binder.BindingBuilder<B, String> bindingBuilder) {
        return bindingBuilder //
                .withNullRepresentation(StringUtils.EMPTY) //
                .withConverter(new StringToIntegerConverter("Invalid")) //
                ;
    }

    @Override
    protected boolean canHandle(PropertyDescriptor propertyDescriptor) {
        return super.canHandle(propertyDescriptor) || Integer.TYPE.equals(propertyDescriptor.getPropertyType());
    }

}
