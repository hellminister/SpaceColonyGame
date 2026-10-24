package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;

public enum TectonicType implements Localizable {
    NONE,
    ACTIVE,
    EXTREME;

    private static final Localize<TectonicType> local = new Localize<>(TectonicType.class, "data/tectonicTypes.txt");
    @Override public ReadOnlyStringProperty localizedProperty() { return local.getString(this); }
}