package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.tiledata.ResourceType;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;

public final class ResourceItem extends CargoItem {

    private final ResourceType resourceType;
    private final int quantityPerUnit;
    private final ReadOnlyStringProperty description;

    public ResourceItem(ResourceType resourceType, int weight, int cost, int startingCount, int quantityPerUnit) {
        super(weight, cost, startingCount);
        this.resourceType = resourceType;
        this.quantityPerUnit = quantityPerUnit;
        description = Localize.getProperty("ca.dauqui.spacecolonygame.localization.enums", "ResourceType." + resourceType.getId() + ".desc");
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public int getQuantityPerUnit() {
        return quantityPerUnit;
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return resourceType.nameProperty();
    }

    @Override
    public ReadOnlyStringProperty descriptionProperty() {
        return description;
    }
}