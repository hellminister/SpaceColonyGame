package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;

public enum MagneticFieldStrength implements Localizable {
    NONE,
    WEAK,
    MODERATE,
    INTENSE;

    private static final Localize<MagneticFieldStrength> local = new Localize<>(MagneticFieldStrength.class, "data/magneticTypes.txt");
    @Override public ReadOnlyStringProperty localizedProperty() { return local.getString(this); }
}