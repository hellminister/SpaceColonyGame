package ca.dauqui.spacecolonygame.gamedata.tiledata;

public class Terrain extends TileData {
    private final TerrainType terrainType;
    private final ResourceType resource;
    private final int amount;

    public Terrain(TerrainType terrainType, ResourceType resource, int amount) {
        this.terrainType = terrainType;
        this.resource = resource;
        this.amount = amount;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public ResourceType getResource() {
        return resource;
    }

    public int getAmount() {
        return amount;
    }
}