package ca.dauqui.spacecolonygame.gui.newgame.cargoloader;

import ca.dauqui.spacecolonygame.gamedata.SatelliteType;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Immutable data record tracking orbital array telemetry hardware configurations inside
 * the launch market interface.
 *
 * @param weight        The payload weight limit footprint allocated in the cargo bay
 * @param cost          The procurement cost value evaluated against launch funds
 * @param startingCount The foundational unit balance loaded on layout generation
 * @param currentCount  The reactive observer checking active spinner UI state values
 * @param satelliteType The technical telemetry specification profile from core game data
 * @param description   The localized descriptive bundle property for tooltips
 *
 * @author Jean-François Quintal
 */
record SatelliteItem(
        int weight,
        int cost,
        int startingCount,
        IntegerProperty currentCount,
        SatelliteType satelliteType,
        ReadOnlyStringProperty description) implements CargoItem {

    /**
     * Convenience initialization block wiring structural tracking components
     * and resolving asset translation records.
     *
     * @param satelliteType The core orbital machine blueprint type
     * @param weight        The single unit physical mass footprint
     * @param cost          The capital trade budget cost
     * @param startingCount The starting stock balance parameter
     */
    public SatelliteItem(SatelliteType satelliteType, int weight, int cost, int startingCount) {
        this(weight, cost, startingCount, new SimpleIntegerProperty(startingCount), satelliteType,
                Localize.getProperty("ca.dauqui.spacecolonygame.localization.enums", "SatelliteType." + satelliteType.id() + ".desc"));
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return satelliteType.name();
    }

    @Override
    public ReadOnlyStringProperty descriptionProperty() {
        return description;
    }
}