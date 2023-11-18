package de.hasait.common.ui;

import com.vaadin.flow.component.grid.Grid;

public abstract class PropertyColumn<B> {

    private final String propertyName;
    private final int layoutPriority;

    public PropertyColumn(String propertyName, int layoutPriority) {
        this.propertyName = propertyName;
        this.layoutPriority = layoutPriority;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public int getLayoutPriority() {
        return layoutPriority;
    }

    public abstract Grid.Column<B> addColumn(Grid<B> grid);

}
