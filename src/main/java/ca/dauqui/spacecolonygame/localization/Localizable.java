package ca.dauqui.spacecolonygame.localization;

import javafx.beans.property.ReadOnlyStringProperty;

/**
 * Defines the contract for game engine entities that expose dynamic, localized textual representations.
 * Classes implementing this interface provide a JavaFX observable string property that automatically
 * binds and updates its value whenever the active application language envelope shifts.
 */
public interface Localizable {

    /**
     * Extracts the read-only observable string property bound to the active localization registry.
     *
     * @return A live JavaFX string property tracking the current language translation bundle
     */
    ReadOnlyStringProperty localizedProperty();
}