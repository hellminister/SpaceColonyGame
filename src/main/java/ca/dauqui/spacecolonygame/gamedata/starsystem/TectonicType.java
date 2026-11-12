package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.settings.GameParameters;
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


    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return Localize.getProperty(GameParameters.StringParam.LOCALIZATION_ENUMS_BUNDLE.get(), "TectonicType." + name());
    }
    @Override
    public String toString() {
        return Localize.getProperty(GameParameters.StringParam.LOCALIZATION_ENUMS_BUNDLE.get(), "TectonicType." + name()).get();
    }
}