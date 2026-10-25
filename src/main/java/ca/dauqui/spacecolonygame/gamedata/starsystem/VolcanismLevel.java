package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import javafx.beans.property.ReadOnlyStringProperty;

@Localized
public enum VolcanismLevel implements Localizable {
    NONE,
    LOW,
    ACTIVE,
    INTENSE;

    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.enums";

    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "VolcanismLevel." + name());
    }
    @Override
    public String toString() {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "VolcanismLevel." + name()).get();
    }
}