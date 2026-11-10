package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableMap;

class TotalMass extends AtmosphereMapTrackingBinding {

    protected TotalMass(ObservableMap<AtmosphereGasType, DoubleProperty> gazMap) {
        super(gazMap);
    }

    @Override
    protected double computeValue() {
        // Sum up all DoubleProperties currently in the map
        double sum = 0.0;
        for (var prop : bound.values()) {
            sum += prop.get();
        }
        return sum;
    }
}