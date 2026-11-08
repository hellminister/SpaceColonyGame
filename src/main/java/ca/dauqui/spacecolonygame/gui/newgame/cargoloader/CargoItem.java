package ca.dauqui.spacecolonygame.gui.newgame.cargoloader;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 * Sealed operational interface defining a payload element eligible for transport
 * aboard the orbital colony supply ship.
 * <p>
 * Governs mass boundaries, purchasing cost, and inventory counters mapped directly
 * to JavaFX reactive UI layouts.
 * </p>
 *
 * @author Jean-François Quintal
 */
sealed interface CargoItem permits ResourceItem, BuildingItem, SatelliteItem  {

    /**
     * @return The financial purchase cost credit value for a single unit of this cargo type
     */
    int cost();

    /**
     * @return The structural weight footprint in kilograms/metric units per item allocation
     */
    int weight();

    /**
     * @return The baseline allocation quantity available before any player market adjustments
     */
    int startingCount();

    /**
     * @return The reactive, observable wrapper tracking current player manifest selections
     */
    IntegerProperty currentCount();


    /**
     * @return The localized display title string property bound to UI grid components
     */
    ReadOnlyStringProperty nameProperty();

    /**
     * @return The descriptive analytical tooltip text property explaining item attributes
     */
    ReadOnlyStringProperty descriptionProperty();

    /**
     * Resets the active operational purchase manifest back to original initialization parameters.
     */
    default void reset(){
        currentCount().setValue(startingCount());
    }
}