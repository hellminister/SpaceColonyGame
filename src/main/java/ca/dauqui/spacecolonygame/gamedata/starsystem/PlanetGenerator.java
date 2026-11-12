package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere.AtmosphereComposition;
import ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere.AtmosphereGasType;
import ca.dauqui.spacecolonygame.settings.GameParameters;
import ca.dauqui.spacecolonygame.util.GameRandom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * This utility class is used to generate planets for a star system
 * During the generation of the planets for a single star system,
 * use 1 instance of this per star system to populate
 */
public final class PlanetGenerator {
    private static final Random RAND = GameRandom.RANDOM;

    /**
     * an empty default planets for when no planet exists in the data
     */
    private static final PlanetAsset DEFAULT_PLANET;
    static {
        String[] fakeData = new String[StarDataColumn.values().length];
        java.util.Arrays.fill(fakeData, "");
        fakeData[StarDataColumn.TY.ordinal()] = "-";
        fakeData[StarDataColumn.RADIUS.ordinal()] = "-1";
        DEFAULT_PLANET = new PlanetAsset(fakeData);
    }

    /**
     * the distance calculated from the star for the current planet
     */
    private double distanceGenerated;

    public PlanetGenerator() {
        distanceGenerated = -1;
    }

    /**
     * The heart of the generator, this method will generate a planet
     * @param probed The star system in which the planet will exist
     * @param ps the existing planet data to be used for the planet (usually when once was detected in the star system irl)
     * @return a fully generated planet
     */
    @SuppressWarnings("java:S3776") // trying to reduce cognitive complexity of this comprehensible method would raise overall complexity of the class
    public Planet generate(StarSystemAsset probed, PlanetAsset ps) {
        double starLum = probed.bolometricLum() <= 0.0 ? 1.0 : probed.bolometricLum();
        double starMass = probed.mass() <= 0.0 ? 1.0 : probed.mass();
        double distance = nextDistance(starLum);
        double receivedLight = starLum / Math.pow(distance, 2);

        String type = ps.getType().replace("?", "");
        String radiusUnit = ps.getRadiusUnit().replace("?", "");

        double radius = ps.getRadius();
        if (radius <= 0.0) {
            double roll = RAND.nextDouble();
            if (roll < 0.6) {
                type = "PT";
                radiusUnit = "Re";
                radius = RAND.nextDouble(0.4, 2.0);
            } else if (roll < 0.9) {
                type = "PN";
                radiusUnit = "Rj";
                radius = RAND.nextDouble(3.0, 5.0);
            } else {
                type = "PJ";
                radiusUnit = "Rj";
                radius = RAND.nextDouble(6.0, 11.0);
            }
        }

        // if the type is undefined, choose the planet type based on the planets radius
        if ("-".equals(type)) {
            if (radius < 2.2) {
                type = "PT";
            } else if (radius < 6.0) {
                type = "PN";
            } else{
                type = "PJ";
            }
            radiusUnit = "PT".equals(type) ? "Re" : "Rj";
        }

        double density = type.equals("PT") ? RAND.nextDouble(4.0, 5.8) : RAND.nextDouble(0.7, 1.8);

        double mass = type.equals("PT") ? Math.pow(radius, 3) * (density / 5.51) : Math.pow(radius, 3) * density * 317.8;

        double radiusInEarthUnits = type.equals("PT") ? radius : radius * 11.209;

        double gravity = mass / Math.pow(radiusInEarthUnits, 2);

        double volcanism = generateVolcanism(type, radiusInEarthUnits);

        TectonicType tectonic = generateTectonics(type, radiusInEarthUnits, volcanism);

        double albedo = generateAlbedo(type, distance);

        double tempEquilibrium = 278.0 * Math.pow(((1.0 - albedo) * starLum) / Math.pow(distance, 2), 0.25);

        double orbitalPeriodDays = Math.sqrt(Math.pow(distance, 3) / starMass) * 365.25;

        double rotationPeriodHours;
        boolean isTidallyLocked = false;

        if (distance < (0.2 * Math.pow(starMass, 0.33))) {
            rotationPeriodHours = orbitalPeriodDays * 24.0;
            isTidallyLocked = true;
        } else {
            rotationPeriodHours = type.equals("PT") ? RAND.nextDouble(10.0, 40.0) : RAND.nextDouble(8.0, 18.0);
            if (RAND.nextDouble() > 0.96 && type.equals("PT")){
                rotationPeriodHours += RAND.nextDouble(100.0, 4000.0);
            }
        }

        AtmosphereComposition atmosphereComposition = new AtmosphereComposition(gravity, radius, radiusUnit, tempEquilibrium, isTidallyLocked);

        generateAtmosphere(type, atmosphereComposition, gravity, radius, radiusUnit, tempEquilibrium, volcanism);

        double magneticField = generateMagnetosphere(mass, volcanism, type);

        double axisTilt = RAND.nextDouble() < 0.85 ? RAND.nextDouble(0.0, 30.0) : RAND.nextDouble(30.0, 90.0);


        int calculatedSites = calculateAvailableSites(type, radius, volcanism, tectonic, atmosphereComposition, magneticField);

        return new Planet(type, radius, radiusUnit, mass, gravity,
                tempEquilibrium, distance,
                albedo, orbitalPeriodDays, rotationPeriodHours, axisTilt, isTidallyLocked,
                receivedLight, calculatedSites, atmosphereComposition, tectonic, volcanism, magneticField);
    }

    /**
     * Determines planetary reflectivity parameters based on surface taxonomy and stellar proximity.
     * @param type The type of the planet
     * @param distance the distance between the planet and the star
     * @return the calculated albedo
     */
    private static double generateAlbedo(String type, double distance) {
        double albedo;
        if ("PT".equals(type)){
            albedo = RAND.nextDouble(0.1, 0.4);
            // Frost line adjustment: telluric worlds covered in volatile ices reflect more light
            if (distance > 3.0){
                albedo += 0.25;
            }
        } else if ("PN".equals(type)) {
            // Ice giant have naturally high and stable albedo
            albedo = RAND.nextDouble(0.35, 0.55);
        }else {
            albedo = distance < 0.5 ? RAND.nextDouble(0.05, 0.2) : RAND.nextDouble(0.3, 0.6);
        }

        // clamping the value to be sure it stays within possible values
        return Math.clamp(albedo, 0.0, 1.0);
    }

    /**
     * Generates the volcanism level of a planet ranging from 0.0 to more than 2.5 W/m²
     * @param type the type of the planet
     * @param radiusInEarthUnits the radius of the planet
     * @return the volcanism level in W/m²
     */
    private static double generateVolcanism(String type, double radiusInEarthUnits) {
        if (!"PT".equals(type)) {
            return 0.0; // no rocky surface for gas giants
        }
        if (radiusInEarthUnits < 0.5) {
            return RAND.nextDouble(0.01, 0.04); // cold bodies (Moon/Mars at ~0.03 W/m²)
        }

        // Active planets : from 0.05 to 0.30 W/m² in average, with a chance of super volcanism up to 2.5 W/m²
        double baseFlux = 0.05 + (radiusInEarthUnits * 0.1);
        if (RAND.nextDouble() > 0.92) {
            return baseFlux + RAND.nextDouble(1.0, 2.5); // gravitational anomaly
        }
        return baseFlux + RAND.nextDouble(0.0, 0.15);
    }

    /**
     *  generate the type of tectonic activity
     * @param type the type of the planet
     * @param radiusInEarthUnits the radius of the planet
     * @param volcanism the volcanism level of the planet in W/m²
     * @return the type of tectonic activity for the planet
     */
    private static TectonicType generateTectonics(String type, double radiusInEarthUnits, double volcanism) {
        if (!"PT".equals(type)) {
            return TectonicType.NONE;
        }

        if (radiusInEarthUnits > 0.6 && volcanism > 0.12) {
            return volcanism > 1.0 ? TectonicType.EXTREME : TectonicType.ACTIVE;
        }
        return TectonicType.NONE;
    }

    /**
     * generate the magnetosphere strength for the planet
     * @param mass the mass of the planet
     * @param volcanism the volcanic activity of the planet
     * @param type the type of the planet
     * @return the Magnetosphere strength of the planet in M_E
     */
    private static double generateMagnetosphere(double mass, double volcanism, String type) {
        if (!"PT".equals(type)){
            return mass * 15.0; // gas giants have immense magnetosphere
        }
        if (mass < 0.1 || volcanism < 0.04){
            return 0.0; // Solidified or insufficient core size: planetary dynamo is inactive
        }

        // Planetary dynamo: depends on mass (iron core) and residual thermal energy
        return mass * (volcanism / 0.1) * RAND.nextDouble(0.7, 1.3);
    }

    /**
     * generate the Atmosphere by choosing each gas and amount of it
     * @param type the type of the planet
     * @param composition The resulting Atmospheric Composition
     * @param gravity the gravity of the planet
     * @param radius the radius of the planet
     * @param radiusUnit the units of the radius (in earth or jupiter ratio)
     * @param tempEquilibrium the equilibrium temperature of the planet
     * @param volcanism the volcanic activity of the planet
     */
    private static void generateAtmosphere(String type, AtmosphereComposition composition, double gravity, double radius, String radiusUnit, double tempEquilibrium,
                                             double volcanism) {

        composition.startTransaction();

        try {
            // Generate the atmosphere for a gas giant
            if (!"PT".equals(type)) {
                generateGasGiantAtmosphere(composition);
            } else if (gravity > GameParameters.DoubleParam.MIN_TELLURIC_ATMO_GRAVITY.get() && tempEquilibrium < GameParameters.DoubleParam.MAX_TELLURIC_ATMO_TEMP.get()) {
                generateTelluricPlanetAtmosphere(composition, gravity, radius, radiusUnit, tempEquilibrium, volcanism);
            }
        } finally {
            composition.commitTransaction();
        }
    }

    /**
     * generate the Atmosphere for a telluric planet by choosing each gas and amount of it
     * @param composition The resulting Atmospheric Composition
     * @param gravity the gravity of the planet
     * @param radius the radius of the planet
     * @param radiusUnit the units of the radius (in earth or jupiter ratio)
     * @param tempEquilibrium the equilibrium temperature of the planet
     * @param volcanism the volcanic activity of the planet
     */
    private static void generateTelluricPlanetAtmosphere(AtmosphereComposition composition, double gravity, double radius, String radiusUnit, double tempEquilibrium,
                                                      double volcanism) {
        List<AtmosphereGasType> compList = new ArrayList<>(AtmosphereGasType.findTelluricAtmosphere(tempEquilibrium));

        if (compList.isEmpty()) {
            return;
        }

        // Sorted by molecular weight with a slight geological noise (+/- 5 g/mol)
        compList.sort((g1, g2) -> {
            double weight1 = g1.molecularWeight() + RAND.nextDouble(-5.0, 5.0);
            double weight2 = g2.molecularWeight() + RAND.nextDouble(-5.0, 5.0);
            return Double.compare(weight2, weight1); // in reverse order
        });

        // Defining the physical target pressure parameters
        double maxPressure = Math.pow(gravity, 2) * 4.0;
        double baselinePressure = RAND.nextDouble(0.01, maxPressure);

        if (volcanism > 1.0) {
            baselinePressure += RAND.nextDouble(2.0, 5.0); // Runaway volcanism (Type Venus)
        } else if (volcanism > 0.15) {
            baselinePressure += RAND.nextDouble(0.1, 1.5);
        }

        // calculate the planet surface size
        double conversionValue = "Re".equals(radiusUnit) ? 6371000.0 : 69911000.0;
        double estimatedSurfaceArea = 4.0 * Math.PI * Math.pow(radius * conversionValue, 2);

        // needed gas mass for target pressure
        double targetedRawMass = (baselinePressure * estimatedSurfaceArea * 100000.0) / gravity;

        double remainingPercentage = 1.0;

        for (int i = 0; i < compList.size() - 1 && remainingPercentage > 0.001; i++) {
            double share = RAND.nextDouble(remainingPercentage * 0.40, remainingPercentage * 0.75);
            share = Math.round(share * 100.0) / 100.0;

            if (share > 0.0) {
                composition.addAtmosphereGas(compList.get(i), targetedRawMass * share);
                remainingPercentage -= share;
            }
        }

        // the lightest gas takes what is left
        double finalShare = Math.round(remainingPercentage * 100.0) / 100.0;
        if (finalShare > 0.0) {
            composition.addAtmosphereGas(compList.getLast(), targetedRawMass * finalShare);
        }
    }

    /**
     * generates the atmosphere of a gas giant
     * @param composition The resulting Atmospheric Composition
     */
    private static void generateGasGiantAtmosphere(AtmosphereComposition composition) {
        List<AtmosphereGasType> nativeGases = AtmosphereGasType.findGasGiantAtmospheres();
        double gasGiantBaseMass = RAND.nextDouble(50000.0, 500000.0);

        // FAILSAFE: If database configurations are completely empty, retrieve ANY available gas as fallback
        if (nativeGases.isEmpty()) {
            Collection<AtmosphereGasType> allGases = AtmosphereGasType.getAtmosphereGazTypes();
            if (!allGases.isEmpty()) {
                nativeGases = List.of(allGases.iterator().next());
            }
        }

        if (!nativeGases.isEmpty()) {
            double remainingPercentage = 1.0;

            // Loop through all but the last native gas to distribute mass dynamically
            for (int i = 0; i < nativeGases.size() - 1; i++) {
                AtmosphereGasType gas = nativeGases.get(i);
                double share;

                // Adaptive allocation: use explicit targets if core elements exist, fallback to dynamic slices
                if ("HYDROGEN".equals(gas.id())) {
                    share = RAND.nextDouble(0.70, 0.82);
                } else if ("HELIUM".equals(gas.id())) {
                    share = RAND.nextDouble(0.15, 0.25);
                } else {
                    // Dynamically scale unknown or modded gases based on the remaining atmospheric budget
                    share = RAND.nextDouble(remainingPercentage * 0.10, remainingPercentage * 0.40);
                }

                // Strict mathematical safeguard to never overshoot the remaining budget parameters
                share = Math.clamp(share, 0.0, remainingPercentage);
                composition.addAtmosphereGas(gas, gasGiantBaseMass * share);
                remainingPercentage -= share;
            }

            // The final gas in the tracked registry absorbs the absolute mathematical residual (guarantees exactly 100%)
            if (remainingPercentage > 0.0) {
                composition.addAtmosphereGas(nativeGases.getLast(), gasGiantBaseMass * remainingPercentage);
            }
        }
    }

    /**
     * calculate the number of available sites for colonization on a given planet
     * @param type the type of planet
     * @param radius the radius of the planet
     * @param volcanism the volcanic activity of the planet
     * @param tectonic the tectonic activity of the planet
     * @param atmosphere the atmosphere composition of the planet
     * @param magneticField the magnetic field strength of the planet
     * @return the number of available site on the planet
     */
    @SuppressWarnings("java:S3776")
    private static int calculateAvailableSites(String type, double radius,
                                               double volcanism,
                                               TectonicType tectonic, AtmosphereComposition atmosphere,
                                               double magneticField) {

        // 1. Gas giant doesn't have any available site
        if (!"PT".equalsIgnoreCase(type)) {
            return 0;
        }

        double temperature = atmosphere.getSurfaceTemperature();

        // 2. PANET SIZE : calculate a starting number of available sites based on the radius
        int baseSites = 3;

        //unit is automatically Re since there are no gas giants here
        if (radius > 1.4) {
            baseSites = 6;
        } else if (radius < 0.4) {
            baseSites = 1;
        }

        int sites = baseSites + RAND.nextInt(1, 4);

        // 3. TECTONICS : stability of the ground
        if (tectonic == TectonicType.EXTREME) {
            sites = Math.max(1, sites - RAND.nextInt(3, 5));
        } else if (tectonic == TectonicType.ACTIVE) {
            sites = Math.max(1, sites - RAND.nextInt(1, 3));
        }

        // 4. MAGNETIC FIELD : Anti-radiation shield
        if (magneticField < 0.2) {
            // Weak or non-existent magnetic field (e.g., Mars/Moon) -> Intense radiation bombardment.
            // Open domes are excluded, forcing to look only for sheltered sites (canyons, caves).
            sites = Math.max(1, sites - RAND.nextInt(2, 4));
        } else if (magneticField >= 1.0 && magneticField <= 2.5) {
            // Ideal magnetic shield similar to Earth -> Maximum surface protection.
            sites += RAND.nextInt(1, 4);
        } else if (magneticField > 5.0) {
            // Hyper-intense magnetic field (metallic worlds or intense magnetic remnants) ->
            // Creates magnetic storms that fry landing shuttle guidance systems.
            sites = Math.max(1, sites - RAND.nextInt(1, 3));
        }

        // 5. ATMOSPHERIC COMPOSITION : toxicity and corrosiveness
        if (atmosphere.getPressure() > 0.0) {
            double corrosiveness = atmosphere.getCorrosiveness();
            double toxicity = atmosphere.getToxicity();

            if (corrosiveness > 0.5) {
                sites = Math.max(1, sites - RAND.nextInt(2, 4));
            } else if (corrosiveness > 0.0) {
                sites = Math.max(1, sites - 1);
            }

            if (toxicity > 0.7) {
                sites = Math.max(1, sites - RAND.nextInt(1, 3));
            }
        }

        // 6. TEMPERATURE & VOLCANISM
        if (temperature > 500 || temperature < -180) {
            sites = Math.max(1, sites - RAND.nextInt(2, 4));
        } else if (temperature >= -20 && temperature <= 40) {
            sites += RAND.nextInt(2, 5);
        }

        if (volcanism > 0.75) {
            sites = Math.max(1, sites - RAND.nextInt(2, 4));
        } else if (volcanism > 0.10 && volcanism < 0.35) {
            sites += RAND.nextInt(1, 3);
        }

        // 7. minimum of 1 site and maximum of 20 site on a rocky planet
        return Math.clamp(sites, GameParameters.IntParam.MIN_PLANET_SITES.get(), GameParameters.IntParam.MAX_PLANET_SITES.get());
    }

    /**
     * The heart of the generator, this method will generate a planet
     * @param probed The star system in which the planet will exist
     * @return a fully generated planet
     */
    public Planet generate(StarSystemAsset probed) { return generate(probed, DEFAULT_PLANET); }

    /**
     * calculate the distance of the planet to the star
     * @param starLuminosity the luminosity of the star
     * @return the distance between the planet and the star
     */
    private double nextDistance(double starLuminosity) {
        // Computes the inner boundary of the accretion disc (the minimum viable zone around the star)
        double innerBoundary = 0.04 * Math.sqrt(starLuminosity);
        innerBoundary = Math.max(0.05, innerBoundary); // Global physical safeguard

        if (distanceGenerated < 0) {
            // FIRST PLANET: Positioned very close to the inner edge to populate the < 0.4 AU zone
            double max = 0.15 * Math.sqrt(starLuminosity);
            double origin = Math.min(0.02, max);
            double bound = Math.max(0.02, max);
            if (Double.compare(origin, bound) == 0) {
                bound += 0.1; // Guarantees a valid interval if origin == bound
            }

            distanceGenerated = innerBoundary + RAND.nextDouble(origin, bound);
            return distanceGenerated;
        }

        // SUBSEQUENT PLANETS: Geometric spacing (multiplier)
        // Compact systems (faint stars) spread out more slowly than massive stars
        double minMultiplier = starLuminosity < 0.1 ? 1.2 : 1.3;
        double maxMultiplier = starLuminosity < 0.1 ? 1.5 : 2.1;

        double multiplier = RAND.nextDouble(minMultiplier, maxMultiplier);

        // Adds a slight randomized linear offset to break the perfect rigidity of the law
        double jitter = RAND.nextDouble(0.01, 0.1) * Math.sqrt(starLuminosity);

        // New distance = (Previous distance * Geometric multiplier) + Jitter
        distanceGenerated = (distanceGenerated * multiplier) + jitter;

        return distanceGenerated;
    }
}