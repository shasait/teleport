package de.hasait.common.ui.puif;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.Converter;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.function.Supplier;

public abstract class AbstractTextFieldForNumberPuiFactory<N extends Number, C> extends AbstractPuiFactory<N, String, TextField, C> {


    protected final Class<?> primitiveClass;
    protected final Converter<String, N> converter;

    public AbstractTextFieldForNumberPuiFactory(Class<N> propertyClass, Class<?> primitiveClass, Supplier<C> contextFactory, Converter<String, N> converter) {
        super(propertyClass, 2000, contextFactory);

        this.primitiveClass = primitiveClass;
        this.converter = converter;
    }

    @Override
    protected TextField createAndAddField(FormLayout formLayout, String caption, C context) {
        TextField field = new TextField(caption);
        formLayout.add(field, 1);
        return field;
    }

    @Override
    protected <B> Binder.BindingBuilder<B, N> customizeBinding(Binder.BindingBuilder<B, String> bindingBuilder, C context) {
        return bindingBuilder //
                .withNullRepresentation(StringUtils.EMPTY) //
                .withConverter(converter) //
                ;
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, C context) {
        return super.canHandle(beanClass, propertyDescriptor, context) || primitiveClass.equals(propertyDescriptor.getPropertyType());
    }

}
