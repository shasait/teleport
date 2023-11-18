package de.hasait.common.ui.puif;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToLongConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;

@Service
public class LongPropertyUiFactory extends AbstractPropertyUiFactory<Long, String, TextField> {

    public LongPropertyUiFactory() {
        super(Long.class, 1002);
    }

    @Override
    protected TextField createAndAddField(FormLayout formLayout, String caption) {
        TextField field = new TextField(caption);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <B> Binder.BindingBuilder<B, Long> customizeBinding(Binder.BindingBuilder<B, String> bindingBuilder) {
        return bindingBuilder //
                .withNullRepresentation(StringUtils.EMPTY) //
                .withConverter(new StringToLongConverter("Invalid")) //
                ;
    }

    @Override
    protected boolean canHandle(PropertyDescriptor propertyDescriptor) {
        return super.canHandle(propertyDescriptor) || Long.TYPE.equals(propertyDescriptor.getPropertyType());
    }

}
