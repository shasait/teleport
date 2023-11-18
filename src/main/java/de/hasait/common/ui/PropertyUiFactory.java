package de.hasait.common.ui;

import java.beans.PropertyDescriptor;

public interface PropertyUiFactory<P> {

    Class<P> getPropertyClass();

    int getPriority();

    <B> PropertyField<B> createField(Class<B> beanClass, PropertyDescriptor propertyDescriptor);

    <B> PropertyColumn<B> createColumn(Class<B> beanClass, PropertyDescriptor propertyDescriptor);

}
