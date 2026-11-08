package ca.dauqui.spacecolonygame.gui.newgame.cargoloader;

import ca.dauqui.spacecolonygame.gamedata.tiledata.Building;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Immutable data record holding prefabricated modular settlement units prepped
 * for planetary deployment.
 *
 * @param weight        The physical mass footprint required inside the ship hull
 * @param cost          The dynamic credit cost charged upon loading
 * @param startingCount The underlying minimum baseline loaded at setup
 * @param currentCount  The observable tracker capturing selected logistics values
 * @param building      The structural blueprint reference managing outworld tile properties
 *
 * @author Jean-François Quintal
 */
record BuildingItem(
        int weight,
        int cost,
        int startingCount,
        IntegerProperty currentCount,
        Building building) implements CargoItem {

    /**
     * Setup constructor configuring localized asset pipelines and generating
     * clean UI tracking variables.
     *
     * @param weight        The modular component mass requirements
     * @param cost          The building token investment requirements
     * @param startingCount The starting baseline inventory configuration
     * @param building      The core layout building data instance
     */
    public BuildingItem(int weight, int cost, int startingCount, Building building){
        this(weight, cost, startingCount, new SimpleIntegerProperty(startingCount), building);
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