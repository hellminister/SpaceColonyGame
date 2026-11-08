package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.TectonicType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.Terrain;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TerrainType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TileData;

import java.util.*;

/**
 * Procedural generation engine factory synthesizing geographic grid landing sites.
 * Leverages multifactor planetary metrics (gravity, atmospheric pressure, volcanism)
 * to deterministically carve topography structures and distribute subsurface mineral deposits.
 *
 * @author Jean-François Quintal
 */
public final class SiteGenerator {
    private static final Random random = new Random();
    private static final int MAX_SURFACE_SITES = 5;

    private static final int IMPASSABLE = -1;
    private static final int HARD = -2;
    private static final int NORMAL = -3;
    private static final int EASY = -4;
    private static final int CLEARED = -5;
    private static final int IMPASSABLE_U = -6;
    private static final int HARD_U = -7;
    private static final int NORMAL_U = -8;
    private static final int EASY_U = -9;
    private static final int CLEARED_U = -10;

    private static final Map<Integer, TileData> tileDataGeneric;

    static {
        tileDataGeneric = Map.of(IMPASSABLE, new Terrain(TerrainType.IMPASSABLE, false),
                HARD, new Terrain(TerrainType.HARD, false),
                NORMAL, new Terrain(TerrainType.NORMAL, false),
                EASY, new Terrain(TerrainType.EASY, false),
                CLEARED, new Terrain(TerrainType.CLEARED, false),
                IMPASSABLE_U, new Terrain(TerrainType.IMPASSABLE, true),
                HARD_U, new Terrain(TerrainType.HARD, true),
                NORMAL_U, new Terrain(TerrainType.NORMAL, true),
                EASY_U, new Terrain(TerrainType.EASY, true),
                CLEARED_U, new Terrain(TerrainType.CLEARED, true));
    }

    private SiteGenerator() {
        /* Static utility constructor stub */
    }

    /**
     * Determines maximum explorable subterranean vertical depth limits allowed on a planet
     * based on structural core dimensions and internal volcanic thermal flux conditions.
     *
     * @param planet Core planetary specifications tracking global astronomical parameters
     * @return Calculated depth index value specifying total allowed underground layers
     */
    private static int calculateExplorableDepth(Planet planet) {
        double radius = planet.radius();
        double heatFlux = planet.volcanism(); // Flux en W/m²
        int baseDepth;

        // Détermination selon la taille de la planète (chaleur résiduelle de base)
        if (radius < 0.8) {
            baseDepth = 4; // Petit corps froid (Lune/Mercure-like) -> jusqu'à 4 sous-sols
        } else if (radius < 1.5) {
            baseDepth = 3; // Taille moyenne (Terre/Vénus-like) -> 3 sous-sols
        } else {
            baseDepth = 2; // Super-Terre -> Croûte épaisse ou instable sous pression
        }

        // Réduction dynamique de la profondeur si le flux thermique interne est trop élevé (magma proche)
        if (heatFlux > 1.0) {
            baseDepth = Math.max(1, baseDepth - 2); // Monde hyper-volcanique (Io-like) : sous-sol très restreint
        } else if (heatFlux > 0.20) {
            baseDepth = Math.max(1, baseDepth - 1); // Activité soutenue
        }

        return baseDepth; // 1 (surface) + couches souterraines
    }

    /**
     * Synthesizes the master surface site array registry catalogue available for final choice selection.
     * Maps flat grid values before applying organic relief deformations and structural mineral distributions.
     *
     * @param planet Core planetary specifications tracking target destination environment variables
     * @return Unmodifiable list collection holding fully generated independent Site layout entries
     */
    public static List<Site> generateSurfaceSites(Planet planet) {
        List<Site> surfaceSites = new ArrayList<>();
        int calculatedDepth = calculateExplorableDepth(planet);

        double heatFlux = planet.volcanism();

        for (int i = 0; i < MAX_SURFACE_SITES; i++) {
            Site site = new Site(calculatedDepth, tileDataGeneric);
            int[][][] data = site.getTileData();

            for (int x = 0; x < Site.MAP_WIDTH; x++) {
                for (int y = 0; y < Site.MAP_HEIGHT; y++) {
                    int type = random.nextDouble() < 0.75 ? EASY : NORMAL;
                    data[x][y][0] = type;
                }
            }

            generateMountainRanges(data, heatFlux, planet.tectonic());
            generateCraters(data, planet.atmosphericPressure());

            applyResources(site, planet);

            surfaceSites.add(site);
        }

        return surfaceSites;
    }

    /**
     * Generates a dependent multi-tier structural subsurface map attached to a chosen site coordinate stack.
     * Leverages an elevation cascade algorithm derived from the structural layer directly overhead.
     *
     * @param chosenSite The operational target site container undergoing structural deep digging generation
     * @param planet     Core planetary specifications profile tracking global geological constants
     */
    public static void generateUnderground(Site chosenSite, Planet planet) {
        int[][][] data = chosenSite.getTileData();
        int totalDepth = chosenSite.getDepth();

        for (int depth = 1; depth < totalDepth; depth++) {
            for (int x = 0; x < Site.MAP_WIDTH; x++) {
                for (int y = 0; y < Site.MAP_HEIGHT; y++) {
                    int upperType =  data[x][y][depth - 1];

                    Terrain surfaceTerrain = (Terrain) chosenSite.getTileDataFor(x, y, 0);
                    Map<ResourceType, Double> permanentResource = surfaceTerrain.getResources();

                    double impassibleChance;
                    double hardChance;
                    double normalChance;

                    switch (upperType) {
                        case IMPASSABLE, IMPASSABLE_U:
                            impassibleChance = 0.60; hardChance = 0.30; normalChance = 0.08; break;
                        case HARD, HARD_U:
                            impassibleChance = 0.30; hardChance = 0.50; normalChance = 0.15; break;
                        case NORMAL, NORMAL_U:
                            impassibleChance = 0.15; hardChance = 0.35; normalChance = 0.40; break;
                        case EASY, EASY_U:
                        default:
                            impassibleChance = 0.05; hardChance = 0.20; normalChance = 0.55; break;
                    }

                    double depthFactor = (depth - 1) * 0.08;
                    impassibleChance += depthFactor;
                    hardChance = Math.max(0, hardChance - (depthFactor / 2));

                    double roll = random.nextDouble();
                    TerrainType currentType;

                    currentType = getTerrainType(roll, impassibleChance, hardChance, normalChance);

                    chosenSite.insertTileDataIn(new Terrain(currentType, permanentResource, true), x, y, depth);
                }
            }
        }
    }

    /**
     * Resolves the material classification assignment index matching a dynamic probability index check.
     *
     * @param roll             Random normalized probability verification token parameter
     * @param impassibleChance Absolute calculated density probability threshold for impassable rocks
     * @param hardChance       Absolute calculated density probability threshold for hard stone
     * @param normalChance     Absolute calculated density probability threshold for standard loose earth
     * @return Selected concrete terrain structure classification descriptor type
     */
    private static TerrainType getTerrainType(double roll, double impassibleChance, double hardChance, double normalChance) {
        TerrainType currentType;
        if (roll < impassibleChance) {
            currentType = TerrainType.IMPASSABLE;
        } else if (roll < (impassibleChance + hardChance)) {
            currentType = TerrainType.HARD;
        } else if (roll < (impassibleChance + hardChance + normalChance)) {
            currentType = TerrainType.NORMAL;
        } else {
            currentType = TerrainType.EASY;
        }
        return currentType;
    }

    /**
     * Scans the external natural resource registry matrix and distributes concentrated material veins
     * across the map layer based on mathematical environmental multifactor compatibility evaluation scores.
     *
     * @param site   The target layout grid data master structure receiving raw material node coordinates
     * @param planet Core planetary configuration profile supplying physical baseline environmental attributes
     */
    private static void applyResources(Site site, Planet planet) {
        Map<PlanetaryAttribute, Double> planetMetrics = new EnumMap<>(PlanetaryAttribute.class);
        planetMetrics.put(PlanetaryAttribute.VOLCANISM, planet.volcanism());
        planetMetrics.put(PlanetaryAttribute.PRESSURE, planet.atmosphericPressure());
        planetMetrics.put(PlanetaryAttribute.GRAVITY, planet.gravity());

        double tectonicValue = planet.tectonic().ordinal();
        planetMetrics.put(PlanetaryAttribute.TECTONIC, tectonicValue);

        for (ResourceType res : ResourceType.getNaturallyOccurringResources()) {

            double finalScore = 1.0;
            for (PlanetaryAttribute attributeName : res.influencingAttributes()) {
                    double metricValue = planetMetrics.getOrDefault(attributeName, 0.0);
                    finalScore += res.influenceScoreFor(attributeName, metricValue);
            }

            if (finalScore >= 1.0) {

                int baseVeins = random.nextInt(1, 4);
                int totalVeins = (int) (baseVeins * finalScore);
                totalVeins = Math.max(res.minPerSite(), totalVeins);

                int spawnedVeins = 0;
                int attempts = 0;
                while (spawnedVeins < totalVeins && attempts < 1000) {
                    attempts++;
                    int rx = random.nextInt(Site.MAP_WIDTH);
                    int ry = random.nextInt(Site.MAP_HEIGHT);

                    Terrain current = (Terrain) site.getTileDataFor(rx, ry, 0);

                    if (res.allowedOnTerrain(current.getTerrainType())) {

                        double minAmount = 500.0 * finalScore;
                        double maxAmount = 2500.0 * finalScore;
                        double finalAmount = random.nextDouble(minAmount, maxAmount);

                        Map<ResourceType, Double> resMap = Map.of(res, finalAmount);

                        site.insertTileDataIn(new Terrain(current.getTerrainType(), resMap, false), rx, ry, 0);
                        spawnedVeins++;
                    }
                }
            }
        }
    }

    /**
     * Spawns tectonic mountain ridges across the visual grid dataset layer matrix,
     * evaluating geothermal activities to govern ridge width parameters.
     *
     * @param data     The multidimensional primitive array storing structural tile index signatures
     * @param heatFlux The internal structural geothermal energy index scalar of the planet
     * @param tec      The structural crust tectonic composition configuration profile reference
     */
    private static void generateMountainRanges(int[][][] data, double heatFlux, TectonicType tec) {
        int ranges = calculateNumberOfRanges(heatFlux, tec);

        for (int m = 0; m < ranges; m++) {
            boolean horizontal = random.nextBoolean();
            int startX = random.nextInt(Site.MAP_WIDTH);
            int startY = random.nextInt(Site.MAP_HEIGHT);

            // Compute width profile once outside loops based on geothermal activity thresholds
            int width = heatFlux > 1.2 ? random.nextInt(12, 22) : random.nextInt(6, 14);

            // Extract the heavy directional iteration logic to a dedicated layout method
            traceSingleMountainRange(data, startX, startY, width, horizontal);
        }
    }

    /**
     * Traces a single geographic mountain range line, applying continuous random path
     * deviations to simulate organic geological formations across coordinates.
     *
     * @param data       The multidimensional primitive array storing structural tile index signatures
     * @param currentX   Horizontal starting coordinate matrix coordinate index
     * @param currentY   Vertical starting coordinate matrix coordinate index
     * @param width      Thickness footprint metric applied onto mountain brush tools
     * @param horizontal True to trace across columns, false to trace across rows
     */
    private static void traceSingleMountainRange(int[][][] data, int currentX, int currentY, int width, boolean horizontal) {
        int length = horizontal ? Site.MAP_WIDTH : Site.MAP_HEIGHT;

        for (int i = 0; i < length; i++) {
            // Unify tracking increment positions based on directional alignment traits
            if (horizontal) {
                currentX = i;
            } else {
                currentY = i;
            }

            // Apply minor organic meandering bends to simulate realistic geological fracture lines
            if (random.nextDouble() < 0.3) {
                int shift = random.nextBoolean() ? 1 : -1;
                if (horizontal) {
                    currentY += shift;
                } else {
                    currentX += shift;
                }
            }

            makeMountains(data, currentX, currentY, width, horizontal);
        }
    }

    /**
     * Operates a geographic brush tool matrix centered on a specific trajectory node,
     * layering material indexes to draw out realistic volumetric mountain slope ranges.
     *
     * @param data       The multidimensional primitive array storing structural tile index signatures
     * @param currentX   Horizontal pivot centre layout index coordinate
     * @param currentY   Vertical pivot centre layout index coordinate
     * @param width      Total horizontal or vertical radius thickness footprint span applied onto the slope tool
     * @param horizontal True to orient the core path across column corridors, false across row corridors
     */
    private static void makeMountains(int[][][] data, int currentX, int currentY, int width, boolean horizontal) {
        int tx = Math.clamp(currentX, 0, Site.MAP_WIDTH - 1);
        int ty = Math.clamp(currentY, 0, Site.MAP_HEIGHT - 1);

        for (int w = -width; w <= width; w++) {
            int nx = horizontal ? tx : tx + w;
            int ny = horizontal ? ty + w : ty;

            if (nx >= 0 && nx < Site.MAP_WIDTH && ny >= 0 && ny < Site.MAP_HEIGHT) {
                double normDist = Math.abs(w) / (double) width;
                double roll = random.nextDouble();
                int t = chooseMountainTileType(normDist, roll);
                data[nx][ny][0] = t;
            }
        }
    }

    /**
     * Determines the physical structural type designation for a mountain terrain tile
     * based on centre-point normalized distance scaling factors and probability evaluation checks.
     *
     * @param normDist The normalized distance scalar value relative to the mountain ridge centre point (0.0 to 1.0)
     * @param roll     The calculated probability threshold float check value mapped from the generation loops
     * @return The integer data template index mapping the concrete mountain material configuration
     */
    private static int chooseMountainTileType(double normDist, double roll) {
        if (normDist < 0.3 && roll < 0.85) {
            // Highly core rugged elevations forming completely blocked geographic chokepoints
            return IMPASSABLE;
        } else if (normDist < 0.7 && roll < 0.70) {
            // Denser rocky slopes providing significant defensive structural attributes
            return HARD;
        } else {
            // Soft peripheral foothills merging back into common standard valley floors
            return NORMAL;
        }
    }

    /**
     * Evaluates active crust tectonic profiles and checks continuous volcanic thermal heat
     * flux modifiers to calculate the total number of mountain ridge structures to deploy.
     *
     * @param heatFlux Core geothermal energy movement metric scalar registered on the planet
     * @param tec      Crust tectonic stability specification index configuration profile
     * @return Calculated integer threshold specifying total distinct mountain lines to draw
     */
    private static int calculateNumberOfRanges(double heatFlux, TectonicType tec) {
        int ranges = switch(tec){
            case ACTIVE -> random.nextInt(2, 4);
            case EXTREME -> random.nextInt(4, 7);
            case NONE -> 0;
        };

        if (heatFlux > 0.20){
            ranges += random.nextInt(1, 3);
        }

        if (heatFlux > 1.2){
            ranges += random.nextInt(2, 4);
        }
        return ranges;
    }

    /**
     * Deploys ballistic meteorite impact matrices scattered randomly across coordinate tracks,
     * sweeping radial circumference loops to carve out depression contours on the surface.
     *
     * @param data     The multidimensional primitive array storing structural tile index signatures
     * @param pressure Planetary surface atmospheric air weight thickness metrics
     */
    private static void generateCraters(int[][][] data, double pressure) {
        int craters = calculateNumberCraters(pressure);
        for (int c = 0; c < craters; c++) {
            int cx = random.nextInt(Site.MAP_WIDTH);
            int cy = random.nextInt(Site.MAP_HEIGHT);
            int r = random.nextInt(5, 20);
            for (int x = Math.max(0, cx - r - 3); x <= Math.min(Site.MAP_WIDTH - 1, cx + r + 3); x++) {
                for (int y = Math.max(0, cy - r - 3); y <= Math.min(Site.MAP_HEIGHT - 1, cy + r + 3); y++) {
                    double dist = Math.sqrt(Math.pow((double) x - cx, 2) + Math.pow((double) y - cy, 2));
                    if (dist <= r + 2) {
                        chooseTerrainForCrater(data, dist, r, x, y);
                    }
                }
            }
        }
    }


    /**
     * Calculates the structural density allocation thresholds for planetary craters
     * based on underlying atmospheric pressure boundaries.
     *
     * @param pressure The physical atmospheric surface pressure metric of the planet
     * @return The calculated number of impact craters to distribute on the map
     */
    private static int calculateNumberCraters(double pressure) {
        int nbCraters = 0;

        if (pressure < 0.1) {
            // High vulnerability impact profile for thin atmospheres or vacuum worlds
            nbCraters = random.nextInt(15, 30);
        } else if (pressure < 1.0) {
            // Moderate structural attrition filters for medium atmospheric contexts
            nbCraters = random.nextInt(4, 10);
        } else if (pressure < 5.0) {
            // Low collision allocation counts for dense atmospheric configurations
            nbCraters = random.nextInt(1, 3);
        }

        return nbCraters;
    }

    /**
     * Distributes specific structural rugged materials at targeted radius coordinates
     * from a crater epicenter to model geological lips and flat interior impact baselines.
     *
     * @param data Raw primitive grid data array tracking active material signatures
     * @param dist Geometric calculated pixel distance vector relative to the collision epicenter centre point
     * @param r    Foundation inner core radius boundary threshold assigned to this impact instance
     * @param x    Horizontal layout grid row coordination index parameter
     * @param y    Vertical layout grid row coordination index parameter
     */
    private static void chooseTerrainForCrater(int[][][] data, double dist, int r, int x, int y) {
        int t;
        if (dist > r - 1 && dist <= r + 1)
            t = random.nextDouble() < 0.80 ? IMPASSABLE : HARD;
        else if (dist > r + 1)
            t = random.nextDouble() < 0.50 ? HARD : data[x][y][0];
        else
            t = random.nextDouble() < 0.70 ? EASY : NORMAL;
        data[x][y][0] = t;
    }
}