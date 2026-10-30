package ca.dauqui.spacecolonygame.gamedata.starsystem;

import java.util.List;
import java.util.Random;

public final class PlanetGenerator {
    private static double distanceGenerated = 0;
    private static final Random rand = new Random();

    private static final PlanetAsset DEFAULT_PLANET;
    static {
        String[] fakeData = new String[StarDataColumn.values().length];
        java.util.Arrays.fill(fakeData, "");
        fakeData[StarDataColumn.TY.ordinal()] = "-";
        fakeData[StarDataColumn.RADIUS.ordinal()] = "-1";
        DEFAULT_PLANET = new PlanetAsset(fakeData);
    }

    private PlanetGenerator() {}

    public static Planet generate(StarSystemAsset probed, PlanetAsset ps) {
        double starLum = probed.getBolometricLum() == -1 ? 1.0 : probed.getBolometricLum();
        double starMass = probed.getMass() == -1 ? 1.0 : probed.getMass();
        double distance = nextDistance(starLum);
        double receivedLight = starLum / Math.pow(distance, 2);

        String type = ps.getType().replace("?", "");
        String radiusUnit = ps.getRadiusUnit().replace("?", "");

        double radius = ps.getRadius();
        if (radius == -1) {
            double roll = rand.nextDouble();
            if (roll < 0.6) {
                type = "PT";
                radiusUnit = "Re";
                radius = rand.nextDouble(0.4, 2.0);
            } else if (roll < 0.9) {
                type = "PN";
                radiusUnit = "Rj";
                radius = rand.nextDouble(3.0, 5.0);
            } else {
                type = "PJ";
                radiusUnit = "Rj";
                radius = rand.nextDouble(6.0, 11.0);
            }
        }

        if ("-".equals(type)) {
            type = radius < 2.2 ? "PT" : (radius < 6.0 ? "PN" : "PJ");
            radiusUnit = "PT".equals(type) ? "Re" : "Rj";
        }

        double density = type.equals("PT") ? rand.nextDouble(4.0, 5.8) : rand.nextDouble(0.7, 1.8);
        double mass = type.equals("PT") ? Math.pow(radius, 3) * (density / 5.51) : Math.pow(radius, 3) * density * 317.8;
        double radiusInEarthUnits = type.equals("PT") ? radius : radius * 11.209;
        double gravity = mass / Math.pow(radiusInEarthUnits, 2);

        // 3. Activité Volcanique continue (0.0 = Éteint, 1.0 = Io / Enfer magmatique)
        double volcanism = generateVolcanism(type, radiusInEarthUnits);
        TectonicType tectonic = generateTectonics(type, radiusInEarthUnits, volcanism);

        double albedo = type.equals("PT") ? rand.nextDouble(0.1, 0.4) : rand.nextDouble(0.3, 0.6);
        if (distance > 3.0 && type.equals("PT")) albedo += 0.25;

        double tempEquilibrium = 278.0 * Math.pow(((1.0 - albedo) * starLum) / Math.pow(distance, 2), 0.25);

        // L'atmosphère prend désormais le double continu pour son calcul de dégazage
        AtmosphereResult atmosphere = generateAtmosphere(type, gravity, tempEquilibrium, volcanism);

        // 4. Intensité Magnétique continue (Proportionnelle à la masse et rotation/chaleur interne)
        double magneticField = generateMagnetosphere(mass, volcanism, type);

        double orbitalPeriodDays = Math.sqrt(Math.pow(distance, 3) / starMass) * 365.25;
        double axisTilt = rand.nextDouble() < 0.85 ? rand.nextDouble(0.0, 30.0) : rand.nextDouble(30.0, 90.0);

        double rotationPeriodHours;
        boolean isTidallyLocked = false;

        if (distance < (0.2 * Math.pow(starMass, 0.33))) {
            rotationPeriodHours = orbitalPeriodDays * 24.0;
            isTidallyLocked = true;
        } else {
            rotationPeriodHours = type.equals("PT") ? rand.nextDouble(10.0, 40.0) : rand.nextDouble(8.0, 18.0);
            if (rand.nextDouble() > 0.96 && type.equals("PT")) rotationPeriodHours += rand.nextDouble(100.0, 4000.0);
        }

        double finalTemperature = atmosphere.finalTemperature;
        if (isTidallyLocked) finalTemperature -= 15.0;

        int calculatedSites = calculateAvailableSites(type, radius, radiusUnit, gravity, finalTemperature, volcanism, receivedLight, tectonic, atmosphere.composition, magneticField);

        return new Planet(type, radius, radiusUnit, mass, gravity,
                finalTemperature, atmosphere.pressure, distance,
                albedo, orbitalPeriodDays, rotationPeriodHours, axisTilt, isTidallyLocked,
                atmosphere.composition, tectonic, volcanism, magneticField, receivedLight, calculatedSites);
    }

    private static double generateVolcanism(String type, double radiusInEarthUnits) {
        if (!"PT".equals(type)) return 0.0; // Pas de surface rocheuse pour les géantes gazeuses
        if (radiusInEarthUnits < 0.5) return rand.nextDouble(0.01, 0.04); // Corps froids (Lune/Mars à ~0.03 W/m²)

        // Planètes actives : de 0.05 à 0.30 W/m² en moyenne, avec une chance de super-volcanisme (type Io) jusqu'à 2.5 W/m²
        double baseFlux = 0.05 + (radiusInEarthUnits * 0.1);
        if (rand.nextDouble() > 0.92) {
            return baseFlux + rand.nextDouble(1.0, 2.5); // Anomalie gravitationnelle / Effet de marée intense
        }
        return baseFlux + rand.nextDouble(0.0, 0.15);
    }

    private static TectonicType generateTectonics(String type, double radiusInEarthUnits, double volcanism) {
        if (!"PT".equals(type)) return TectonicType.NONE;
        // La tectonique dépend du flux thermique (volcanism)
        if (radiusInEarthUnits > 0.6 && volcanism > 0.12) {
            return volcanism > 1.0 ? TectonicType.EXTREME : TectonicType.ACTIVE;
        }
        return TectonicType.NONE;
    }

    private static double generateMagnetosphere(double mass, double volcanism, String type) {
        if (!"PT".equals(type)){
            return mass * 15.0; // Les géantes gazeuses ont des magnétosphères massives (ex: Jupiter)
        }
        if (mass < 0.1 || volcanism < 0.04){
            return 0.0; // Noyau solidifié ou trop petit
        }

        // Dynamo planétaire : dépend de la masse (noyau de fer) et de l'énergie thermique résiduelle
        return mass * (volcanism / 0.1) * rand.nextDouble(0.7, 1.3);
    }

    private static AtmosphereResult generateAtmosphere(String type, double gravity, double tempEquilibrium, double volcanism) {
        if (!"PT".equals(type)) {
            // Sélection dynamique basée sur la propriété native des géantes
            return new AtmosphereResult(AtmosphereComposition.findGasGiantAtmosphere(), rand.nextDouble(50.0, 500.0), tempEquilibrium);
        }

        if (gravity > 0.20 && tempEquilibrium < 500) {
            // Sélection dynamique basée sur la température d'équilibre de la planète
            List<AtmosphereComposition> compList = AtmosphereComposition.findTelluricAtmosphere(tempEquilibrium);

            AtmosphereComposition comp = compList.isEmpty() ? AtmosphereComposition.getAtmosphereComposition("NONE") : compList.get(rand.nextInt(compList.size()));

            if (comp.getId().equals("NONE")) {
                return new AtmosphereResult(comp, 0.0, tempEquilibrium);
            }

            double maxPressure = Math.pow(gravity, 2) * 4.0;
            double pressure = rand.nextDouble(0.01, maxPressure);

            if (volcanism > 1.0) {
                pressure += rand.nextDouble(2.0, 5.0); // Mondes hyper-volcaniques (type Vénus)
            } else if (volcanism > 0.15) {
                pressure += rand.nextDouble(0.1, 1.5); // Activité standard
            }

            // Application de la formule physique, totalement découplée
            double greenhouseMultiplier = 1.0 + (Math.log1p(pressure) * (comp.getGreenhouseFactor() / 100.0));
            return new AtmosphereResult(comp, pressure, tempEquilibrium * greenhouseMultiplier);
        }

        return new AtmosphereResult(AtmosphereComposition.getAtmosphereComposition("NONE"), 0.0, tempEquilibrium);
    }

    private static int calculateAvailableSites(String type, double radius, String radiusUnit,
                                               double gravity, double temperature,
                                               double volcanism, double receivedLight,
                                               TectonicType tectonic, AtmosphereComposition atmosphere,
                                               double magneticField) {
        // 1. FILTRE RADICAL : Les géantes n'ont pas de sol solide
        if (!"PT".equalsIgnoreCase(type)) {
            return 0;
        }

        // 2. SURFACE DE BASE SELON LE RAYON (Radius)
        int baseSites = 3;

        if (radius > 1.4) {
            baseSites = 6;
        } else if (radius < 0.4) {
            baseSites = 1;
        }

        int sites = baseSites + rand.nextInt(1, 4); // Jet aléatoire de base (+1 à +3)

        // 3. FACTEUR TECTONIQUE : Stabilité structurelle du sol
        if (tectonic == TectonicType.EXTREME) {
            sites = Math.max(1, sites - rand.nextInt(3, 5));
        } else if (tectonic == TectonicType.ACTIVE) {
            sites = Math.max(1, sites - rand.nextInt(1, 3));
        }

        // 4. FACTEUR CHAMP MAGNÉTIQUE : Bouclier anti-radiations (Nouveauté)
        if (magneticField < 0.2) {
            // Champ magnétique faible ou inexistant (ex: Mars/Lune) -> Bombardement de radiations intenses.
            // Les dômes ouverts sont exclus, forçant à chercher uniquement des sites abrités (canyons, grottes).
            sites = Math.max(1, sites - rand.nextInt(2, 4));
        } else if (magneticField >= 1.0 && magneticField <= 2.5) {
            // Bouclier magnétique idéal similaire à la Terre -> Protection maximale de la surface.
            sites += rand.nextInt(1, 4); // Bonus de sites exploitables en plaine dégagée
        } else if (magneticField > 5.0) {
            // Champ magnétique hyper-puissant (Mondes métalliques ou résidus magnétiques intenses) ->
            // Crée des tempêtes magnétiques qui grillent les systèmes de guidage des navettes d'atterrissage.
            sites = Math.max(1, sites - rand.nextInt(1, 3));
        }

        // 5. FACTEUR ATMOSPHÉRIQUE : Toxicité et Corrosion du Gamedata
        if (atmosphere != null && !"NONE".equalsIgnoreCase(atmosphere.getId())) {
            double corrosiveness = atmosphere.getCorrosiveness();
            double toxicity = atmosphere.getToxicity();

            if (corrosiveness > 0.5) {
                sites = Math.max(1, sites - rand.nextInt(2, 4));
            } else if (corrosiveness > 0.0) {
                sites = Math.max(1, sites - 1);
            }

            if (toxicity > 0.7) {
                sites = Math.max(1, sites - rand.nextInt(1, 3));
            }
        }

        // 6. TEMPÉRATURE & VOLCANISME
        if (temperature > 500 || temperature < -180) {
            sites = Math.max(1, sites - rand.nextInt(2, 4));
        } else if (temperature >= -20 && temperature <= 40) {
            sites += rand.nextInt(2, 5);
        }

        if (volcanism > 0.75) {
            sites = Math.max(1, sites - rand.nextInt(2, 4));
        } else if (volcanism > 0.10 && volcanism < 0.35) {
            sites += rand.nextInt(1, 3);
        }

        // 7. GARDE-FOU PHYSIQUE STRICT POUR L'UI (Toujours entre 1 et 20 pour les telluriques)
        return Math.clamp(sites, 1, 20);
    }

    private record AtmosphereResult(AtmosphereComposition composition, double pressure, double finalTemperature) {}
    public static Planet generate(StarSystemAsset probed) { return generate(probed, DEFAULT_PLANET); }

    public static void start() {
        // to detect if it is the first planet being placed
        distanceGenerated = -1.0;
    }

    private static double nextDistance(double starLuminosity) {
        // Calcule la limite interne de la zone d'accrétion (la zone minimale viable autour de l'étoile)
        double innerBoundary = 0.04 * Math.sqrt(starLuminosity);
        innerBoundary = Math.max(0.05, innerBoundary); // Garde-fou physique global

        if (distanceGenerated < 0) {
            // PREMIÈRE PLANÈTE : Positionnée très proche du bord interne pour peupler la zone < 0.4 AU
            double max = 0.15 * Math.sqrt(starLuminosity);
            double origin = Math.min(0.02, max);
            double bound = Math.max(0.02, max);
            if (Double.compare(origin, bound) == 0) {
                bound += 0.1; // Assure un intervalle valide si origin == bound
            }

            distanceGenerated = innerBoundary + rand.nextDouble(origin, bound);
            return distanceGenerated;
        }

        // PLANÈTES SUIVANTES : Écartement géométrique (multiplicateur)
        // Les systèmes compacts (étoiles faibles) s'écartent moins vite que les étoiles massives
        double minMultiplier = starLuminosity < 0.1 ? 1.2 : 1.3;
        double maxMultiplier = starLuminosity < 0.1 ? 1.5 : 2.1;

        double multiplier = rand.nextDouble(minMultiplier, maxMultiplier);

        // Ajout d'un léger décalage aléatoire linéaire pour casser la rigidité parfaite de la loi
        double jitter = rand.nextDouble(0.01, 0.1) * Math.sqrt(starLuminosity);

        // Nouvelle distance = (Ancienne distance * Multiplicateur géométrique) + Jitter
        distanceGenerated = (distanceGenerated * multiplier) + jitter;

        return distanceGenerated;
    }
}