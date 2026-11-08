package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 * This enum represents the tectonic activity of a planet
 *
 * @author Jean-François Quintal
 */
@Localized
public enum TectonicType implements Localizable {
    /**
     * Indicates no tectonic activity
     */
    NONE,
    /**
     * Indicates normal tectonic activity (like on Earth
     */
    ACTIVE,
    /**
     * Indicate high tectonic activity
     */
    EXTREME;

    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.enums";

    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "TectonicType." + name());
    }
    @Override
    public String toString() {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "TectonicType." + name()).get();
    }
}