package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.tiledata.TileData;
import ca.dauqui.spacecolonygame.settings.GameParameters;

import java.util.HashMap;
import java.util.Map;

/**
 * Core matrix grid tracking geographic layouts, material compositions,
 * and multi-level subterranean depth layers for a planetary landing zone.
 * <p>
 * Leverages a dual-key space partitioning framework: global generic templates use
 * pre-defined baseline signatures, while localized layout alterations convert geometric
 * coordinates dynamically into unique positional hash tokens.
 * </p>
 *
 * @author Jean-François Quintal
 */
public class Site {
    public static final int MAP_HEIGHT = GameParameters.IntParam.SITE_MAP_HEIGHT.get();
    public static final int MAP_WIDTH = GameParameters.IntParam.SITE_MAP_WIDTH.get();

    public final int mapDepth; // 0 for the surface and 1-4 for underground


    private final int[][][] siteArea;
    private final Map<Integer, TileData> tileMap;

    /**
     * Constructs a single deployment site, configuring dimensions arrays and packing
     * baseline generic data mapping profiles.
     *
     * @param mapDepth        Maximum subterranean depth capacity layout authorized (0 for surface only)
     * @param tileDataGeneric Foundational master dictionary registering generic terrain templates
     */
    public Site(int mapDepth, Map<Integer, TileData> tileDataGeneric) {
        // Dimension allocation includes mapDepth + 1 to properly house index zero surface layers
        this.mapDepth = mapDepth+1;
        // Dimension allocation includes mapDepth + 1 to properly house index zero surface layers
        siteArea = new int[MAP_WIDTH][MAP_HEIGHT][this.mapDepth];
        tileMap = new HashMap<>();
        tileMap.putAll(tileDataGeneric);
    }

    /**
     * @return Raw transactional multidimensional primitive array storing structural tile index signatures
     */
    public int[][][] getTileData() {
        return siteArea;
    }

    /**
     * Resolves the concrete terrain configuration template assigned to specific spatial coordinates.
     *
     * @param x     Horizontal coordinate parameter bound by MAP_WIDTH boundaries
     * @param y     Vertical coordinate parameter bound by MAP_HEIGHT boundaries
     * @param depth Targeted altitude tier depth layer index parameter
     * @return Resolved structural template details mapping terrain attributes
     */
    public TileData getTileDataFor(int x, int y, int depth) {
        return tileMap.get(siteArea[x][y][depth]);
    }

    /**
     * Registers a generic global tile template ID signature at specific coordinate vectors.
     *
     * @param tileIndex Registry reference key corresponding to a pre-defined tile map entry
     * @param x         Horizontal coordinate parameter bound by MAP_WIDTH boundaries
     * @param y         Vertical coordinate parameter bound by MAP_HEIGHT boundaries
     * @param depth     Targeted altitude tier depth layer index parameter
     */
    public void insertTileDataIn(int tileIndex, int x, int y, int depth) {
        siteArea[x][y][depth] = tileIndex;
    }

    /**
     * Analytical positioning hash engine calculating unique coordinate sequence keys
     * to prevent coordinate cross-contamination.
     *
     * @param x     Horizontal coordinate parameters index
     * @param y     Vertical coordinate parameters index
     * @param depth Targeted layer altitude index
     * @return Computed unique flat memory coordinate mapping ID
     */
    public static int generateKeyForPosition(int x, int y, int depth) {
        return x + y * MAP_WIDTH + depth * MAP_WIDTH * MAP_HEIGHT;
    }

    /**
     * @return Underlying structural maximum vertical depth parameter limit allowed (1 is surface only)
     */
    public int getDepth() {
        return mapDepth;
    }

    /**
     * Registers a unique custom localized custom file block override directly into coordinates,
     * appending custom reference variants into the local dictionary.
     *
     * @param terrain Custom localized unique variation template instance to inject on coordinates
     * @param x       Horizontal coordinate parameter bound by MAP_WIDTH boundaries
     * @param y       Vertical coordinate parameter bound by MAP_HEIGHT boundaries
     * @param depth   Targeted altitude tier depth layer index parameter
     */
    public void insertTileDataIn(TileData terrain, int x, int y, int depth) {
        int key = generateKeyForPosition(x, y, depth);
        siteArea[x][y][depth] = key;
        tileMap.put(key, terrain);
    }
}