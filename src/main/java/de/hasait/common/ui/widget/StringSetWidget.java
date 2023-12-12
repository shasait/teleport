package de.hasait.common.ui.widget;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StringSetWidget extends CustomField<Set<String>> {

    private final Map<String, Boolean> data;
    private final ListDataProvider<String> dataProvider;
    private final ListBox<String> listBox;

    private final Dialog addDialog;
    private final Button deleteButton;

    public StringSetWidget() {
        super();

        data = new ConcurrentHashMap<>();
        dataProvider = new ListDataProvider<>(data.keySet());
        listBox = new ListBox<>();
        listBox.setItems(dataProvider);
        listBox.setSizeFull();

        addDialog = new Dialog();
        addDialog.setModal(true);
        addDialog.setCloseOnOutsideClick(false);
        buildAddDialogLayout();
        add(addDialog);

        Button addButton = new Button("Add...");
        addButton.addClickListener(event -> addDialog.open());

        deleteButton = new Button("Delete");
        deleteButton.addClickListener(event -> {
            listBox.getOptionalValue().ifPresent(value -> {
                data.remove(value);
                dataProvider.refreshAll();
                updateValue();
            });
        });

        VerticalLayout buttonLayout = new VerticalLayout(addButton, deleteButton);
        HorizontalLayout mainLayout = new HorizontalLayout(listBox, buttonLayout);
        mainLayout.setSizeFull();
        mainLayout.setFlexGrow(1, listBox);
        add(mainLayout);
        setSizeFull();

        listBox.addValueChangeListener(event -> updateDeleteButton());
        updateDeleteButton();
    }

    private void updateDeleteButton() {
        deleteButton.setEnabled(!listBox.isEmpty());
    }

    private void buildAddDialogLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        H2 title = new H2("Add Item");
        TextField textField = new TextField();

        Button addButton = new Button("Add");
        addButton.addClickListener(event -> {
            data.putIfAbsent(textField.getValue(), true);
            dataProvider.refreshAll();
            addDialog.close();
            updateValue();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> addDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(addButton, cancelButton);
        mainLayout.add(title, textField, buttonLayout);
        addDialog.add(mainLayout);

        addDialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                textField.clear();
                textField.focus();
            }
        });
    }

    @Override
    protected Set<String> generateModelValue() {
        return new HashSet<>(data.keySet());
    }

    @Override
    protected void setPresentationValue(Set<String> strings) {
        data.clear();
        strings.forEach(value -> data.putIfAbsent(value, true));
        dataProvider.refreshAll();
    }

}
