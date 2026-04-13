package ca.dauqui.spacecolonygame.gui.newgame;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public abstract sealed class CargoItem permits RessourceItem, BuildingItem, SatelliteItem  {
    private int weight;
    private int cost;
    private IntegerProperty currentCount;
    private int startingCount;

    protected CargoItem(int weight, int cost, int startingCount) {
        this.weight = weight;
        this.cost = cost;
        this.currentCount = new SimpleIntegerProperty(startingCount);
        this.startingCount = startingCount;
    }

    public int getCost() {
        return cost;
    }

    public int getWeight() {
        return weight;
    }

    public int getStartingCount() {
        return startingCount;
    }

    public IntegerProperty getCurrentCount() {
        return currentCount;
    }

    public void reset(){
        currentCount.setValue(startingCount);
    }

    public abstract ReadOnlyStringProperty nameProperty();
    public abstract ReadOnlyStringProperty descriptionProperty();

}