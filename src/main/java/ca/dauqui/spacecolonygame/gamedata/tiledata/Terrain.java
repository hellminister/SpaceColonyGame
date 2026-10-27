package ca.dauqui.spacecolonygame.gamedata.tiledata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Terrain extends TileData {
    private final TerrainType terrainType;
    private final Map<ResourceType, Double> resources;
    private final boolean underground;

    public Terrain(TerrainType terrainType, boolean underground) {
        this.terrainType = terrainType;
        resources = new HashMap<>();
        this.underground = underground;
    }

    public Terrain(TerrainType terrainType, Map<ResourceType, Double> toDuplicate, boolean underground) {
        this.terrainType = terrainType;
        resources = new HashMap<>();
        resources.putAll(toDuplicate);
        this.underground = underground;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public Collection<ResourceType> getResource() {
        return Collections.unmodifiableCollection(resources.keySet());
    }

    public double getAmountforResource(ResourceType resource) {
        return resources.getOrDefault(resource, 0.0);
    }

    public Map<ResourceType, Double> getResources() {
        return Collections.unmodifiableMap(resources);
    }
}