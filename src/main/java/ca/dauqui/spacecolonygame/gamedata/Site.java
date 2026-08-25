package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.tiledata.TileData;

public class Site {
    public static final int MAP_HEIGHT = 150;
    public static final int MAP_WIDTH = 300;
    public static final int MAP_DEPTH = 5; // 0 for the surface and 1-4 for underground

    private final TileData[][][] siteArea = new TileData[MAP_WIDTH][MAP_HEIGHT][MAP_DEPTH];

    public TileData[][][] getTileData() {
        return siteArea;
    }
}