package ca.dauqui.spacecolonygame.gamedata.tiledata;

public class Terrain extends TileData {
    private TerrainType terrainType;

    public Terrain(TerrainType terrainType) {
        this.terrainType = terrainType;
    }


    public TerrainType getTerrainType() {
        return terrainType;
    }
}