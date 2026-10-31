package ca.dauqui.spacecolonygame.gamedata.starsystem;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;
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

        double radiusD = ps.getRadius();
        if (radiusD == -1) {
            double roll = rand.nextDouble();
            if (roll < 0.6) {
                type = "PT";
                radiusUnit = "Re";
                radiusD = rand.nextDouble(0.4, 2.0);
            } else if (roll < 0.9) {
                type = "PN";
                radiusUnit = "Rj";
                radiusD = rand.nextDouble(3.0, 5.0);
            } else {
                type = "PJ";
                radiusUnit = "Rj";
                radiusD = rand.nextDouble(6.0, 11.0);
            }
        }

        if ("-".equals(type)) {
            type = radiusD < 2.2 ? "PT" : (radiusD < 6.0 ? "PN" : "PJ");
            radiusUnit = "PT".equals(type) ? "Re" : "Rj";
        }

        double density = type.equals("PT") ? rand.nextDouble(4.0, 5.8) : rand.nextDouble(0.7, 1.8);
        double mass = type.equals("PT") ? Math.pow(radiusD, 3) * (density / 5.51) : Math.pow(radiusD, 3) * density * 317.8;
        double radiusInEarthUnits = type.equals("PT") ? radiusD : radiusD * 11.209;
        double gravityD = mass / Math.pow(radiusInEarthUnits, 2);

        // 3. Activité Volcanique continue (0.0 = Éteint, 1.0 = Io / Enfer magmatique)
        double volcanism = generateVolcanism(type, radiusInEarthUnits);
        TectonicType tectonic = generateTectonics(type, radiusInEarthUnits, volcanism);

        double albedo = type.equals("PT") ? rand.nextDouble(0.1, 0.4) : rand.nextDouble(0.3, 0.6);
        if (distance > 3.0 && type.equals("PT")) albedo += 0.25;

        double tempEquilibrium = 278.0 * Math.pow(((1.0 - albedo) * starLum) / Math.pow(distance, 2), 0.25);

        DoubleProperty radius = new SimpleDoubleProperty(radiusD);
        DoubleProperty gravity = new SimpleDoubleProperty(gravityD);
        AtmosphereComposition atmosphereComposition = new AtmosphereComposition(gravity, radius, radiusUnit);


        // L'atmosphère prend désormais le double continu pour son calcul de dégazage
        double temperature = generateAtmosphere(type, atmosphereComposition, gravityD, radiusD, radiusUnit, tempEquilibrium, volcanism);

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

        double finalTemperature = temperature;
        if (isTidallyLocked) finalTemperature -= 15.0;

        // Calcul final des sites basé sur les nouvelles propriétés réactives globales
        int calculatedSites = calculateAvailableSites(type, radiusD, radiusUnit, gravityD, finalTemperature, volcanism, receivedLight, tectonic, atmosphereComposition, magneticField);

        // Ré-injection propre des sites calculés dans l'instance
        return new Planet(type, radius, radiusUnit, mass, gravity,
                finalTemperature, distance,
                albedo, orbitalPeriodDays, rotationPeriodHours, axisTilt, isTidallyLocked,
                atmosphereComposition, tectonic, volcanism, magneticField, receivedLight, calculatedSites);
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

    private static double generateAtmosphere(String type, AtmosphereComposition composition, double gravity, double radius, String radiusUnit, double tempEquilibrium,
                                             double volcanism) {
        // Crée l'instance de composition réactive (on passe deux fois la gravité pour le constructeur)

        if (!"PT".equals(type)) {
            // GÉANTES GAZEUSES : Injection du cocktail primordial d'Hydrogène et d'Hélium
            List<AtmosphereGazType> nativeGases = AtmosphereGazType.findGasGiantAtmospheres();
            double gasGiantBaseMass = rand.nextDouble(50000.0, 500000.0);

            for (AtmosphereGazType gas : nativeGases) {
                if ("HYDROGEN".equals(gas.getId())) {
                    composition.addGaz(gas, gasGiantBaseMass * rand.nextDouble(0.70, 0.85));
                } else if ("HELIUM".equals(gas.getId())) {
                    composition.addGaz(gas, gasGiantBaseMass * rand.nextDouble(0.15, 0.25));
                }
            }
            return tempEquilibrium;
        }

        // PLANÈTES TELLURIQUES : Rétention selon la température d'équilibre
        if (gravity > 0.20 && tempEquilibrium < 500) {
            List<AtmosphereGazType> compList = new ArrayList<>(AtmosphereGazType.findTelluricAtmosphere(tempEquilibrium));

            if (compList.isEmpty()) {
                return tempEquilibrium;
            }

            // Tri par poids moléculaire avec un léger bruit géologique (+/- 5 g/mol)
            compList.sort((g1, g2) -> {
                double weight1 = g1.getMolecularWeight() + rand.nextDouble(-5.0, 5.0);
                double weight2 = g2.getMolecularWeight() + rand.nextDouble(-5.0, 5.0);
                return Double.compare(weight2, weight1); // Décroissant
            });

            // Définition de la cible de pression physique
            double maxPressure = Math.pow(gravity, 2) * 4.0;
            double baselinePressure = rand.nextDouble(0.01, maxPressure);

            if (volcanism > 1.0) {
                baselinePressure += rand.nextDouble(2.0, 5.0); // Runaway volcanique (Type Vénus)
            } else if (volcanism > 0.15) {
                baselinePressure += rand.nextDouble(0.1, 1.5);
            }

            double conversionValue = "Re".equals(radiusUnit) ? 6371000.0 : 69911000.0;
            double estimatedSurfaceArea = 4.0 * Math.PI * Math.pow(radius * conversionValue, 2);

            // Masse requise = (Pression cible * Surface * 100000) / Gravité
            double targetedRawMass = (baselinePressure * estimatedSurfaceArea * 100000.0) / gravity;

            double remainingPercentage = 1.0;

            for (int i = 0; i < compList.size() - 1 && remainingPercentage > 0.001; i++) {
                double share = rand.nextDouble(remainingPercentage * 0.40, remainingPercentage * 0.75);
                share = Math.round(share * 100.0) / 100.0;

                if (share > 0.0) {
                    composition.addGaz(compList.get(i), targetedRawMass * share);
                    remainingPercentage -= share;
                }
            }

            // Le gaz le plus léger restant prend le résidu mathématique
            double finalShare = Math.round(remainingPercentage * 100.0) / 100.0;
            if (finalShare > 0.0) {
                composition.addGaz(compList.getLast(), targetedRawMass * finalShare);
            }

            // Application de l'effet de serre global calculé en temps réel par ton DoubleBinding
            double greenhouseMultiplier = 1.0 + (Math.log1p(composition.getPressure()) * (composition.getGreenhouseFactor() / 100.0));
            return (tempEquilibrium * greenhouseMultiplier);
        }

        // Monde mort ou sans air
        return tempEquilibrium;
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
        if (atmosphere != null && atmosphere.getPressure() > 0.0) {
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