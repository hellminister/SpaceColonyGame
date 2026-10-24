package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;

public enum AtmosphereComposition implements Localizable {
    NONE,
    CARBON_DIOXIDE,
    NITROGEN,
    HYDROGEN_HELIUM,
    METHANE_AMMONIA;

    private static final Localize<AtmosphereComposition> local =
            new Localize<>(AtmosphereComposition.class, "data/atmosphereTypes.txt");

    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return local.getString(this);
    }
}