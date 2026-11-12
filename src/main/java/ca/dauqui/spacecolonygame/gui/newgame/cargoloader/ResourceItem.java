package ca.dauqui.spacecolonygame.gui.newgame.cargoloader;

import ca.dauqui.spacecolonygame.gamedata.ResourceType;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.settings.GameParameters;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Immutable data record wrapping raw inventory material assets for colony deployment manifests.
 * Implements a reactive structural layer via property wrappers to drive live UI calculations.
 *
 * @param weight           The structural mass footprint per unit allocation
 * @param cost             The financial credit cost required per unit purchase
 * @param startingCount    The initial stock baseline setting before market interaction
 * @param currentCount     The live observable counter capturing selected manifest amounts
 * @param resourceType     The core raw material configuration reference from data registries
 * @param quantityPerUnit  The internal storage capacity conversion factor injected on delivery
 * @param description      The pre-rendered localized description property layer
 *
 * @author Jean-François Quintal
 */
record ResourceItem(
        int weight,
        int cost,
        int startingCount,
        IntegerProperty currentCount,
        ResourceType resourceType,
        int quantityPerUnit,
        ReadOnlyStringProperty description) implements CargoItem {

    /**
     * Primary convenience builder constructor initializing transient structural
     * properties from underlying resource system configurations.
     *
     * @param resourceType     The target raw material type definition
     * @param weight           The physical mass per cargo unit
     * @param cost             The trade valuation price tag
     * @param startingCount    The baseline setup count
     * @param quantityPerUnit  The real resource value multiplier packed per container
     */
    public ResourceItem(ResourceType resourceType, int weight, int cost, int startingCount, int quantityPerUnit) {
        this(weight, cost, startingCount, new SimpleIntegerProperty(startingCount), resourceType, quantityPerUnit,
                Localize.getProperty(GameParameters.StringParam.LOCALIZATION_ENUMS_BUNDLE.get(), "ResourceType." + resourceType.id() + ".desc"));
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return resourceType.name();
    }

    @Override
    public ReadOnlyStringProperty descriptionProperty() {
        return description;
    }
}