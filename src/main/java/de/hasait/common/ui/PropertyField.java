package de.hasait.common.ui;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;

public abstract class PropertyField<BEAN> {

    private final String propertyName;
    private final int layoutPriority;

    public PropertyField(String propertyName, int layoutPriority) {
        this.propertyName = propertyName;
        this.layoutPriority = layoutPriority;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public int getLayoutPriority() {
        return layoutPriority;
    }

    public abstract void addFieldAndBind(FormLayout formLayout, Binder<BEAN> binder);

}
