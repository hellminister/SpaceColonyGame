package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.tiledata.Building;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;

public final class BuildingItem extends CargoItem {
    private final Building building;

    public BuildingItem(int weight, int cost, int startingCount, Building building) {
        super(weight, cost, startingCount);
        this.building = building;
    }

    public Building getBuilding() {
        return building;
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