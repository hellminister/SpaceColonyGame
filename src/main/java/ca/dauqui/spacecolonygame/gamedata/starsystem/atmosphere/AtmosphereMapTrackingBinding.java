package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

/**
 * Base abstract binding that encapsulates dynamic tracking and listener recycling
 * over the atmospheric gases map entries. Slashes raw boilerplate.
 */
abstract class AtmosphereMapTrackingBinding extends DoubleBinding {
    protected final InvalidationListener entryListener = _ -> invalidate();
    protected final ObservableMap<AtmosphereGasType, DoubleProperty> bound;

    protected AtmosphereMapTrackingBinding(ObservableMap<AtmosphereGasType, DoubleProperty> gazMap) {
        bound = gazMap;
        // Automatically registers listeners whenever a gas is added or removed from the simulation
        bound.addListener((MapChangeListener<? super AtmosphereGasType, ? super DoubleProperty>) change -> {
            if (change.wasAdded()) {
                change.getValueAdded().addListener(entryListener);
            }
            if (change.wasRemoved()) {
                change.getValueRemoved().removeListener(entryListener);
            }
            invalidate();
        });
    }

    @Override
    public void dispose() {
        bound.values().forEach(prop -> prop.removeListener(entryListener));
        super.dispose();
    }
}