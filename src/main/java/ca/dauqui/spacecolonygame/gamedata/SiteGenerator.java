package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.TectonicType;
import ca.dauqui.spacecolonygame.gamedata.starsystem.VolcanismLevel;
import ca.dauqui.spacecolonygame.gamedata.tiledata.ResourceType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.Terrain;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TerrainType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.TileData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SiteGenerator {
    private static final Random random = new Random();
    private static final int MAX_SURFACE_SITES = 5;

    private SiteGenerator() {}

    private static int calculateExplorableDepth(Planet planet) {
        double radius = planet.getRadius();
        int baseDepth;

        if (radius < 0.8) {
            baseDepth = 4; // Petit corps froid -> max 4 sous-sols
        } else if (radius < 1.5) {
            baseDepth = 3; // Taille moyenne
        } else {
            baseDepth = 2; // Écorce fine
        }

        if (planet.getVolcanism() == VolcanismLevel.INTENSE) {
            baseDepth = Math.max(1, baseDepth - 2);
        } else if (planet.getVolcanism() == VolcanismLevel.ACTIVE) {
            baseDepth = Math.max(1, baseDepth - 1);
        }

        return 1 + baseDepth;
    }

    /**
     * PASSE 1 : Génération de la surface (depth = 0) et de la cartographie des ressources.
     */
    public static List<Site> generateSurfaceSites(Planet planet) {
        List<Site> surfaceSites = new ArrayList<>();
        int calculatedDepth = calculateExplorableDepth(planet);

        // Détermination des autorisations de ressources selon la planète
        boolean allowFumeroles = planet.getVolcanism() == VolcanismLevel.ACTIVE
                || planet.getVolcanism() == VolcanismLevel.INTENSE;

        for (int i = 0; i < MAX_SURFACE_SITES; i++) {
            Site site = new Site(calculatedDepth);
            TileData[][][] data = site.getTileData();

            // Étape 1 : Remplissage initial de base (Terrain de base, pas de ressources)
            for (int x = 0; x < Site.MAP_WIDTH; x++) {
                for (int y = 0; y < Site.MAP_HEIGHT; y++) {
                    TerrainType type = random.nextDouble() < 0.75 ? TerrainType.EASY : TerrainType.NORMAL;
                    data[x][y][0] = new Terrain(type, ResourceType.NONE, 0);
                }
            }

            // Étape 2 : Sculptage géométrique des montagnes et cratères
            // Note : En réécrivant sur les coordonnées, on conserve temporairement ResourceType.NONE
            generateMountainRanges(data, planet.getVolcanism(), planet.getTectonic());
            generateCraters(data, planet.getAtmosphericPressure());

            // Étape 3 : Saupoudrage des filons de minerais et fumeroles sur la surface (depth = 0)
            // On reconstruit l'objet Terrain aux coordonnées choisies pour y injecter sa ressource unique
            applyResources(data, allowFumeroles);

            surfaceSites.add(site);
        }

        return surfaceSites;
    }

    /**
     * PASSE 2 : Génère les sous-sols en dupliquant verticalement les ressources de la surface.
     */
    public static void generateUnderground(Site chosenSite, Planet planet) {
        TileData[][][] data = chosenSite.getTileData();
        int totalDepth = chosenSite.getDepth();

        for (int depth = 1; depth < totalDepth; depth++) {
            for (int x = 0; x < Site.MAP_WIDTH; x++) {
                for (int y = 0; y < Site.MAP_HEIGHT; y++) {

                    // Récupération du terrain du dessus pour la topographie
                    Terrain upperTerrain = (Terrain) data[x][y][depth - 1];
                    TerrainType upperType = upperTerrain.getTerrainType();

                    // Récupération de la ressource originelle gravée à la surface (depth = 0)
                    Terrain surfaceTerrain = (Terrain) data[x][y][0];
                    ResourceType permanentResource = surfaceTerrain.getResource();
                    int permanentAmount = surfaceTerrain.getAmount();

                    double impassibleChance;
                    double hardChance;
                    double normalChance;

                    switch (upperType) {
                        case IMPASSIBLE:
                            impassibleChance = 0.60; hardChance = 0.30; normalChance = 0.08;
                            break;
                        case HARD:
                            impassibleChance = 0.30; hardChance = 0.50; normalChance = 0.15;
                            break;
                        case NORMAL:
                            impassibleChance = 0.15; hardChance = 0.35; normalChance = 0.40;
                            break;
                        case EASY:
                        default:
                            impassibleChance = 0.05; hardChance = 0.20; normalChance = 0.55;
                            break;
                    }

                    double depthFactor = (depth - 1) * 0.08;
                    impassibleChance += depthFactor;
                    hardChance = Math.max(0, hardChance - (depthFactor / 2));

                    double roll = random.nextDouble();
                    TerrainType currentType;

                    if (roll < impassibleChance) {
                        currentType = TerrainType.IMPASSIBLE;
                    } else if (roll < (impassibleChance + hardChance)) {
                        currentType = TerrainType.HARD;
                    } else if (roll < (impassibleChance + hardChance + normalChance)) {
                        currentType = TerrainType.NORMAL;
                    } else {
                        currentType = TerrainType.EASY;
                    }

                    // On instancie le terrain souterrain avec sa ressource persistante héritée du dessus
                    data[x][y][depth] = new Terrain(currentType, permanentResource, permanentAmount);
                }
            }
        }
    }

    private static void applyResources(TileData[][][] data, boolean allowFumeroles) {
        // Détermination du nombre de filons à implanter sur la carte
        int numVeins = random.nextInt(15, 30);

        for (int i = 0; i < numVeins; i++) {
            int rx = random.nextInt(Site.MAP_WIDTH);
            int ry = random.nextInt(Site.MAP_HEIGHT);

            Terrain current = (Terrain) data[rx][ry][0];

            // Choix du type de minerai
            double roll = random.nextDouble();
            ResourceType res = roll < 0.50 ? ResourceType.ORE_A : (roll < 0.85 ? ResourceType.ORE_B : ResourceType.ORE_C);
            int amount = random.nextInt(500, 2500);

            data[rx][ry][0] = new Terrain(current.getTerrainType(), res, amount);
        }

        // Implantation des fumeroles si la géodynamique de la planète le permet
        if (allowFumeroles) {
            int numFumeroles = random.nextInt(3, 8);
            for (int i = 0; i < numFumeroles; i++) {
                int fx = random.nextInt(Site.MAP_WIDTH);
                int fy = random.nextInt(Site.MAP_HEIGHT);

                Terrain current = (Terrain) data[fx][fy][0];
                int amount = random.nextInt(1000, 5000); // Réserves thermiques

                data[fx][fy][0] = new Terrain(current.getTerrainType(), ResourceType.FUMEROLE, amount);
            }
        }
    }

    private static void generateMountainRanges(TileData[][][] data, VolcanismLevel vol, TectonicType tec) {
        int ranges = tec == TectonicType.ACTIVE ? random.nextInt(2, 4) : (tec == TectonicType.EXTREME ? random.nextInt(4, 7) : 0);
        if (vol == VolcanismLevel.ACTIVE || vol == VolcanismLevel.INTENSE) ranges += random.nextInt(1, 3);

        for (int m = 0; m < ranges; m++) {
            boolean horizontal = random.nextBoolean();
            int currentX = random.nextInt(Site.MAP_WIDTH);
            int currentY = random.nextInt(Site.MAP_HEIGHT);
            int length = horizontal ? Site.MAP_WIDTH : Site.MAP_HEIGHT;
            int width = vol == VolcanismLevel.INTENSE ? random.nextInt(12, 22) : random.nextInt(6, 14);

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
                        TerrainType t = normDist < 0.3 && roll < 0.85 ? TerrainType.IMPASSIBLE : (normDist < 0.7 && roll < 0.70 ? TerrainType.HARD : TerrainType.NORMAL);
                        data[nx][ny][0] = new Terrain(t, ResourceType.NONE, 0);
                    }
                }
            }
        }
    }

    private static void generateCraters(TileData[][][] data, double pressure) {
        int craters = pressure < 0.1 ? random.nextInt(15, 30) : (pressure < 1.0 ? random.nextInt(4, 10) : (pressure < 5.0 ? random.nextInt(1, 3) : 0));
        for (int c = 0; c < craters; c++) {
            int cx = random.nextInt(Site.MAP_WIDTH);
            int cy = random.nextInt(Site.MAP_HEIGHT);
            int r = random.nextInt(5, 20);

            for (int x = Math.max(0, cx - r - 3); x <= Math.min(Site.MAP_WIDTH - 1, cx + r + 3); x++) {
                for (int y = Math.max(0, cy - r - 3); y <= Math.min(Site.MAP_HEIGHT - 1, cy + r + 3); y++) {
                    double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
                    if (dist <= r + 2) {
                        TerrainType t;
                        if (dist > r - 1 && dist <= r + 1) t = random.nextDouble() < 0.80 ? TerrainType.IMPASSIBLE : TerrainType.HARD;
                        else if (dist > r + 1) t = random.nextDouble() < 0.50 ? TerrainType.HARD : ((Terrain) data[x][y][0]).getTerrainType();
                        else t = random.nextDouble() < 0.70 ? TerrainType.EASY : TerrainType.NORMAL;
                        data[x][y][0] = new Terrain(t, ResourceType.NONE, 0);
                    }
                }
            }
        }
    }
}