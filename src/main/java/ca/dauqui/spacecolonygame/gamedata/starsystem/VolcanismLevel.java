package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;

public enum VolcanismLevel implements Localizable {
    NONE,
    LOW,
    ACTIVE,
    INTENSE;

    private static final Localize<VolcanismLevel> local = new Localize<>(VolcanismLevel.class, "data/volcanismTypes.txt");
    @Override public ReadOnlyStringProperty localizedProperty() { return local.getString(this); }
}