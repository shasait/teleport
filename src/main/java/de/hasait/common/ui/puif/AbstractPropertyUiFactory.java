package de.hasait.common.ui.puif;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.ui.PropertyColumn;
import de.hasait.common.ui.PropertyField;
import de.hasait.common.ui.PropertyUiFactory;
import de.hasait.common.util.ReflectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.Objects;

public abstract class AbstractPropertyUiFactory<P, V, F extends Component & HasValue<?, V>> implements PropertyUiFactory<P> {

    private final Class<P> propertyClass;
    private final int priority;

    private MessageSource messageSource;

    protected AbstractPropertyUiFactory(Class<P> propertyClass, int priority) {
        this.propertyClass = propertyClass;
        this.priority = priority;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public final Class<P> getPropertyClass() {
        return propertyClass;
    }

    @Override
    public final int getPriority() {
        return priority;
    }

    @Override
    public final <B> PropertyField<B> createField(Class<B> beanClass, PropertyDescriptor propertyDescriptor) {
        if (!canHandle(propertyDescriptor)) {
            return null;
        }

        String propertyName = propertyDescriptor.getName();
        int layoutPriority = determineLayoutPriority(propertyName);
        boolean required = ReflectionUtil.isRequired(beanClass, propertyDescriptor);

        return new PropertyField<>(propertyName, layoutPriority) {
            @Override
            public void addFieldAndBind(FormLayout formLayout, Binder<B> binder) {
                String label = determineLabel(propertyName);
                F field = createAndAddField(formLayout, label);
                Binder.BindingBuilder<B, V> bindingBuilder = binder.forField(field);
                if (required) {
                    bindingBuilder = bindingBuilder.asRequired();
                }
                customizeBinding(bindingBuilder).bind(propertyName);
            }
        };
    }

    @Override
    public final <B> PropertyColumn<B> createColumn(Class<B> beanClass, PropertyDescriptor propertyDescriptor) {
        if (!canHandle(propertyDescriptor)) {
            return null;
        }

        String propertyName = propertyDescriptor.getName();
        int layoutPriority = determineLayoutPriority(propertyName);

        return new PropertyColumn<>(propertyName, layoutPriority) {
            @Override
            public Grid.Column<B> addColumn(Grid<B> grid) {
                return AbstractPropertyUiFactory.this.addColumn(propertyName, determineLabel(propertyName), grid);
            }
        };
    }

    protected abstract F createAndAddField(FormLayout formLayout, String caption);

    protected abstract <B> Binder.BindingBuilder<B, P> customizeBinding(Binder.BindingBuilder<B, V> bindingBuilder);

    protected boolean canHandle(PropertyDescriptor propertyDescriptor) {
        return propertyClass.isAssignableFrom(propertyDescriptor.getPropertyType());
    }

    protected <B> Grid.Column<B> addColumn(String propertyName, String label, Grid<B> grid) {
        Grid.Column<B> column = grid.addColumn(propertyName);
        column.setHeader(label);
        return column;
    }

    private String determineLabel(String propertyName) {
        return messageSource.getMessage(propertyName + ".label", null, propertyName, Locale.getDefault());
    }

    private int determineLayoutPriority(String propertyName) {
        String layoutPriorityString = messageSource.getMessage(propertyName + ".layoutPriority", null, "1000", Locale.getDefault());
        return Integer.parseInt(Objects.requireNonNull(layoutPriorityString));
    }

}
