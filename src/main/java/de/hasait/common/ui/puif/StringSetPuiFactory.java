package de.hasait.common.ui.puif;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.ui.widget.StringSetWidget;
import de.hasait.common.util.ReflectionUtil;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.util.Set;

@Service
public class StringSetPuiFactory extends AbstractPuiFactory<Set<String>, Set<String>, StringSetWidget, StringSetPuiConfig> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public StringSetPuiFactory() {
        super((Class<Set<String>>) ((Class) Set.class), 0, StringSetPuiConfig::new);
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, StringSetPuiConfig context) {
        if (super.canHandle(beanClass, propertyDescriptor, context)) {
            StringSetPui annotation = ReflectionUtil.findAnnotation(beanClass, propertyDescriptor, StringSetPui.class);
            if (annotation != null) {
                context.height = annotation.height();
                return true;
            }
        }
        return false;
    }

    @Override
    protected StringSetWidget createAndAddField(FormLayout formLayout, String caption, StringSetPuiConfig context) {
        StringSetWidget field = new StringSetWidget();
        field.setLabel(caption);
        field.setHeight(context.height);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <B> Binder.BindingBuilder<B, Set<String>> customizeBinding(Binder.BindingBuilder<B, Set<String>> bindingBuilder, StringSetPuiConfig context) {
        return bindingBuilder;
    }

}
