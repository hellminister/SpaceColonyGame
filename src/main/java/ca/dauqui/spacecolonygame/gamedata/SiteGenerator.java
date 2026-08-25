package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gamedata.tiledata.Terrain;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TerrainType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TileData;

import java.util.Random;

public final class SiteGenerator {
    private static final Random random = new Random();
    private SiteGenerator() {}

    //TODO make a real site generator using planet data and topography, for now just a random generator
    public static Site generateSite(Planet planet, StarSystem starSystem) {
        Site site = new Site();
        TileData[][][] data = site.getTileData();

        TerrainType[] types = TerrainType.values();
        for (int depth = 0; depth < Site.MAP_DEPTH; depth++) {
            for (int x = 0; x < Site.MAP_WIDTH; x++) {
                for (int y = 0; y < Site.MAP_HEIGHT; y++) {
                    data[x][y][depth] = new Terrain(types[random.nextInt(3)]);
                    }
            }
        }


        return site;
    }
}