package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.TectonicType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.ResourceType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.Terrain;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TerrainType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TileData;

import java.util.*;

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

    private SiteGenerator() {}

    private static int calculateExplorableDepth(Planet planet) {
        double radius = planet.getRadius();
        double heatFlux = planet.getVolcanism(); // Flux en W/m²
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

        return 1 + baseDepth; // 1 (surface) + couches souterraines
    }

    public static List<Site> generateSurfaceSites(Planet planet) {
        List<Site> surfaceSites = new ArrayList<>();
        int calculatedDepth = calculateExplorableDepth(planet);

        // L'autorisation et la quantité de fumeroles dépendent directement du flux thermique continu
        double heatFlux = planet.getVolcanism();

        for (int i = 0; i < MAX_SURFACE_SITES; i++) {
            Site site = new Site(calculatedDepth, tileDataGeneric);
            int[][][] data = site.getTileData();

            for (int x = 0; x < Site.MAP_WIDTH; x++) {
                for (int y = 0; y < Site.MAP_HEIGHT; y++) {
                    int type = random.nextDouble() < 0.75 ? EASY : NORMAL;
                    data[x][y][0] = type;
                }
            }

            // Transmission du flux thermique brut à la génération de montagnes (volcans)
            generateMountainRanges(data, heatFlux, planet.getTectonic());
            generateCraters(data, planet.getAtmosphericPressure());

            // Application des ressources influencée par la physique de la planète
            applyResources(site, data, planet);

            surfaceSites.add(site);
        }

        return surfaceSites;
    }

    /**
     * Génère les sous-sols (Inchangé, conserve sa Passe 2 structurelle propre)
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

                    if (roll < impassibleChance) {
                        currentType = TerrainType.IMPASSABLE;
                    } else if (roll < (impassibleChance + hardChance)) {
                        currentType = TerrainType.HARD;
                    } else if (roll < (impassibleChance + hardChance + normalChance)) {
                        currentType = TerrainType.NORMAL;
                    } else {
                        currentType = TerrainType.EASY;
                    }

                    chosenSite.insertTileDataIn(new Terrain(currentType, permanentResource, true), x, y, depth);
                }
            }
        }
    }

    private static void applyResources(Site site, int[][][] data, Planet planet) {
        // 1. Cartographie sécurisée des attributs physiques à l'aide de ton Enum PlanetaryAttribute
        Map<PlanetaryAttribute, Double> planetMetrics = new HashMap<>();
        planetMetrics.put(PlanetaryAttribute.VOLCANISM, planet.getVolcanism());
        planetMetrics.put(PlanetaryAttribute.PRESSURE, planet.getAtmosphericPressure());
        planetMetrics.put(PlanetaryAttribute.GRAVITY, planet.getGravity());

        double tectonicValue = planet.getTectonic() == TectonicType.EXTREME ? 2.0 :
                (planet.getTectonic() == TectonicType.ACTIVE ? 1.0 : 0.0);
        planetMetrics.put(PlanetaryAttribute.TECTONIC, tectonicValue);

        // 2. Boucle universelle sur toutes les ressources du catalogue externe
        for (ResourceType res : ResourceType.getNaturallyOccurringResources()) {

            // Évaluation mathématique multi-factorielle du score global de la planète
            double finalScore = 1.0;
            for (PlanetaryAttribute attributeName : res.getInfluencingAttributes()) {                 // On convertit la chaîne du fichier en instance de ton Enum pour lire la Map
                    double metricValue = planetMetrics.getOrDefault(attributeName, 0.0);
                    finalScore += res.getInfluenceScoreFor(attributeName, metricValue);
            }

            // Si la note globale franchit le seuil de viabilité géologique
            if (finalScore >= 1.0) {

                // Calcul du nombre de veines (pondéré par le score et sécurisé par ton minPerSite)
                int baseVeins = random.nextInt(1, 4);
                int totalVeins = (int) (baseVeins * finalScore);
                totalVeins = Math.max(res.getMinPerSite(), totalVeins);

                int spawnedVeins = 0;
                int attempts = 0;
                // Boucle de sécurité pour éviter une boucle infinie si le terrain requis est rare
                while (spawnedVeins < totalVeins && attempts < 1000) {
                    attempts++;
                    int rx = random.nextInt(Site.MAP_WIDTH);
                    int ry = random.nextInt(Site.MAP_HEIGHT);

                    Terrain current = (Terrain) site.getTileDataFor(rx, ry, 0);

                    // Validation stricte du type de relief requis (appearsOn)
                    if (res.allowedOnTerrain(current.getTerrainType())) {

                        // Calcul du volume de minerai par filon indexé sur le score final
                        double minAmount = 500.0 * finalScore;
                        double maxAmount = 2500.0 * finalScore;
                        double finalAmount = random.nextDouble(minAmount, maxAmount);

                        Map<ResourceType, Double> resMap = Map.of(res, finalAmount);

                        // Insertion géographique standardisée selon ton architecture
                        site.insertTileDataIn(new Terrain(current.getTerrainType(), resMap, false), rx, ry, 0);
                        spawnedVeins++;
                    }
                }
            }
        }
    }

    private static void generateMountainRanges(int[][][] data, double heatFlux, TectonicType tec) {
        int ranges = tec == TectonicType.ACTIVE ? random.nextInt(2, 4) : (tec == TectonicType.EXTREME ? random.nextInt(4, 7) : 0);

        // Plus le flux magmatique est élevé, plus le risque d'alignements volcaniques (chaînes de montagnes) augmente
        if (heatFlux > 0.20) ranges += random.nextInt(1, 3);
        if (heatFlux > 1.2) ranges += random.nextInt(2, 4);

        for (int m = 0; m < ranges; m++) {
            boolean horizontal = random.nextBoolean();
            int currentX = random.nextInt(Site.MAP_WIDTH);
            int currentY = random.nextInt(Site.MAP_HEIGHT);
            int length = horizontal ? Site.MAP_WIDTH : Site.MAP_HEIGHT;

            // La largeur des chaînes dépend de la violence géothermique
            int width = heatFlux > 1.2 ? random.nextInt(12, 22) : random.nextInt(6, 14);

            for (int i = 0; i < length; i++) {
                if (horizontal) { currentX = i; if (random.nextDouble() < 0.3) currentY += random.nextBoolean() ? 1 : -1; }
                else { currentY = i; if (random.nextDouble() < 0.3) currentX += random.nextBoolean() ? 1 : -1; }

                int tx = Math.max(0, Math.min(Site.MAP_WIDTH - 1, currentX));
                int ty = Math.max(0, Math.min(Site.MAP_HEIGHT - 1, currentY));

                for (int w = -width; w <= width; w++) {
                    int nx = horizontal ? tx : tx + w;
                    int ny = horizontal ? ty + w : ty;

                    if (nx >= 0 && nx < Site.MAP_WIDTH && ny >= 0 && ny < Site.MAP_HEIGHT) {
                        double normDist = Math.abs(w) / (double) width;
                        double roll = random.nextDouble();
                        int t = normDist < 0.3 && roll < 0.85 ? IMPASSABLE : (normDist < 0.7 && roll < 0.70 ? HARD : NORMAL);
                        data[nx][ny][0] = t;
                    }
                }
            }
        }
    }

    private static void generateCraters(int[][][] data, double pressure) {
        int craters = pressure < 0.1 ? random.nextInt(15, 30) : (pressure < 1.0 ? random.nextInt(4, 10) : (pressure < 5.0 ? random.nextInt(1, 3) : 0));
        for (int c = 0; c < craters; c++) {
            int cx = random.nextInt(Site.MAP_WIDTH);
            int cy = random.nextInt(Site.MAP_HEIGHT);
            int r = random.nextInt(5, 20);
            for (int x = Math.max(0, cx - r - 3); x <= Math.min(Site.MAP_WIDTH - 1, cx + r + 3); x++) {
                for (int y = Math.max(0, cy - r - 3); y <= Math.min(Site.MAP_HEIGHT - 1, cy + r + 3); y++) {
                    double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
                    if (dist <= r + 2) {
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
            }
        }
    }
}