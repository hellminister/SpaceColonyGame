package ca.dauqui.spacecolonygame.gui.newgame;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;

public final class RessourceItem extends CargoItem {
    public RessourceItem(int weight, int cost, int startingCount) {
        super(weight, cost, startingCount);
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return new SimpleStringProperty("name");
    }

    @Override
    public ReadOnlyStringProperty descriptionProperty() {
        return new SimpleStringProperty("description");
    }
}