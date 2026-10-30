package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.SatelliteType;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;

public final class SatelliteItem extends CargoItem {

    private final SatelliteType satelliteType;
    private final ReadOnlyStringProperty description;


    public SatelliteItem(SatelliteType satelliteType, int weight, int cost, int startingCount) {
        super(weight, cost, startingCount);
        this.satelliteType = satelliteType;
        description = Localize.getProperty("ca.dauqui.spacecolonygame.localization.enums", "SatelliteType." + satelliteType.getId() + ".desc");

    }

    public SatelliteType getSatelliteType() {
        return satelliteType;
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return satelliteType.nameProperty();
    }

    @Override
    public ReadOnlyStringProperty descriptionProperty() {
        return description;
    }
}