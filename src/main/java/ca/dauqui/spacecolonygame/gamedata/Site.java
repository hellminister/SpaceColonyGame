package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.tiledata.TileData;

import java.util.HashMap;
import java.util.Map;

public class Site {
    public static final int MAP_HEIGHT = 150;
    public static final int MAP_WIDTH = 300;

    public final int mapDepth; // 0 for the surface and 1-4 for underground

  //  private final TileData[][][] siteArea;

    private final int[][][] siteArea;
    private final Map<Integer, TileData> tileMap;

    public Site(int mapDepth, Map<Integer, TileData> tileDataGeneric) {
        this.mapDepth = mapDepth;
  //      siteArea = new TileData[MAP_WIDTH][MAP_HEIGHT][mapDepth];
        siteArea = new int[MAP_WIDTH][MAP_HEIGHT][mapDepth];
        tileMap = new HashMap<>();
        tileMap.putAll(tileDataGeneric);
    }

    public int[][][] getTileData() {
        return siteArea;
    }

    public TileData getTileDataFor(int x, int y, int depth) {
        return tileMap.get(siteArea[x][y][depth]);
    }

    public void insertTileDataIn(int tileIndex, int x, int y, int depth) {
        siteArea[x][y][depth] = tileIndex;
    }

    public static int generateKeyForPosition(int x, int y, int depth) {
        return x + y * MAP_WIDTH + depth * MAP_WIDTH * MAP_HEIGHT;
    }

    public int getDepth() {
        return mapDepth;
    }

    public void insertTileDataIn(TileData terrain, int x, int y, int depth) {
        int key = generateKeyForPosition(x, y, depth);
        siteArea[x][y][depth] = key;
        tileMap.put(key, terrain);
    }
}