package de.hasait.common.ui.puif;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.domain.SearchableRepository;
import de.hasait.common.ui.JpaRepositoryDataProvider;

public abstract class AbstractToOnePropertyUiFactory<PO, R extends SearchableRepository<PO, ?>> extends AbstractPropertyUiFactory<PO, PO, ComboBox<PO>> {

    private final R repository;

    public AbstractToOnePropertyUiFactory(Class<PO> poClass, int priority, R repository) {
        super(poClass, priority);

        this.repository = repository;
    }

    @Override
    protected ComboBox<PO> createAndAddField(FormLayout formLayout, String caption) {
        ComboBox<PO> field = new ComboBox<>(caption);
        field.setItems(new JpaRepositoryDataProvider<>(repository));
        field.setItemLabelGenerator(this::getPoLabel);
        formLayout.add(field, 1);
        return field;
    }

    protected abstract String getPoLabel(PO po);

    @Override
    protected <B> Binder.BindingBuilder<B, PO> customizeBinding(Binder.BindingBuilder<B, PO> bindingBuilder) {
        return bindingBuilder;
    }

    @Override
    protected <B> Grid.Column<B> addColumn(String propertyName, String label, Grid<B> grid) {
        return super.addColumn(propertyName + "." + getColumnLabelProperty(), label, grid);
    }

    protected abstract String getColumnLabelProperty();

}
