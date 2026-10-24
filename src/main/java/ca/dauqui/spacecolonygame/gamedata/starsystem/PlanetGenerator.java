package ca.dauqui.spacecolonygame.gamedata.starsystem;

import java.util.Random;

public final class PlanetGenerator {
    private static double distanceGenerated = 0;
    private static final Random rand = new Random();

    private static final PlanetAsset DEFAULT_PLANET;
    static {
        // Crée un asset factice avec des valeurs par défaut pour les planètes inconnues détectées par les sondes
        String[] fakeData = new String[StarDataColumn.values().length];
        java.util.Arrays.fill(fakeData, "");
        fakeData[StarDataColumn.TY.ordinal()] = "?";
        fakeData[StarDataColumn.RADIUS.ordinal()] = "-1";

        DEFAULT_PLANET = new PlanetAsset(fakeData);
    }

    private PlanetGenerator() {}

    public static Planet generate(StarSystemAsset probed, PlanetAsset ps) {
        // 1. Calcul de la distance UA selon la luminosité de l'étoile
        double starLum = probed.getBolometricLum() == -1 ? 1.0 : probed.getBolometricLum();
        double distance = nextDistance(starLum);

        String type = ps.getType();
        String radiusUnit = ps.getRadiusUnit();

        if ( "?".equals(type)) {
            type = switch (rand.nextInt(3)) {
                case 0 -> "PT";
                case 1 -> "PJ";
                case 2 -> "PN";
                default -> throw new IllegalStateException("Unexpected type index during planet generation");
            };
            radiusUnit = "PT".equals(type) ? "Re" : "Rj";
        }

        // 2. Détermination du rayon (en Rayons Terrestres 'Re')
        double radius = ps.getRadius();
        if (radius == -1) {
            radius = type.equals("PT") ? rand.nextDouble(0.4, 2.2) : rand.nextDouble(3.0, 11.0);
        }

        // 3. Densité et Masse (Pipeline physique)
        double density = type.equals("PT") ? rand.nextDouble(0.8, 1.3) : rand.nextDouble(0.15, 0.35);
        double mass = Math.pow(radius, 3) * density;

        // 4. Gravité de surface (g = M / R²)
        double gravity = mass / Math.pow(radius, 2);

        // 5. Température d'équilibre (Loi du corps noir)
        double tempEquilibrium = 278.0 * Math.pow(starLum / Math.pow(distance, 2), 0.25);

        // 3. Extraction des sous-systèmes (Pour réduire la complexité cognitive)
        AtmosphereResult atmosphere = generateAtmosphere(type, gravity, tempEquilibrium);
        TectonicResult geology = generateGeology(type, radius);
        MagneticFieldStrength magneticField = generateMagnetosphere(mass, geology.volcanism);

        @SuppressWarnings("java:S107")
        Planet planet = new Planet(type, radius, radiusUnit, mass, gravity,
                atmosphere.finalTemperature, atmosphere.pressure, distance,
                atmosphere.composition, geology.tectonic, geology.volcanism, magneticField);
        return planet;
    }

    private static AtmosphereResult generateAtmosphere(String type, double gravity, double tempEquilibrium) {
        if (!"PT".equals(type)) {
            return new AtmosphereResult(AtmosphereComposition.HYDROGEN_HELIUM, rand.nextDouble(50.0, 500.0), tempEquilibrium);
        }

        if (gravity > 0.3 && tempEquilibrium < 500) {
            AtmosphereComposition comp = tempEquilibrium > 320 ? AtmosphereComposition.CARBON_DIOXIDE : AtmosphereComposition.NITROGEN;
            double pressure = rand.nextDouble(0.01, 10.0);
            double finalTemp = tempEquilibrium + (pressure * 12.0);
            return new AtmosphereResult(comp, pressure, finalTemp);
        }

        return new AtmosphereResult(AtmosphereComposition.NONE, 0.0, tempEquilibrium);
    }

    private record AtmosphereResult(AtmosphereComposition composition, double pressure, double finalTemperature) {}


    private static TectonicResult generateGeology(String type, double radius) {
        if (!"PT".equals(type)) {
            return new TectonicResult(TectonicType.NONE, VolcanismLevel.INTENSE);
        }

        if (radius > 0.7) {
            TectonicType tectonic = rand.nextDouble() > 0.3 ? TectonicType.ACTIVE : TectonicType.NONE;
            VolcanismLevel volcanism = tectonic == TectonicType.ACTIVE ? VolcanismLevel.ACTIVE : VolcanismLevel.LOW;
            return new TectonicResult(tectonic, volcanism);
        }

        return new TectonicResult(TectonicType.NONE, VolcanismLevel.NONE);
    }

    private record TectonicResult(TectonicType tectonic, VolcanismLevel volcanism) {}

    private static MagneticFieldStrength generateMagnetosphere(double mass, VolcanismLevel volcanism) {
        if (mass > 0.4 && volcanism != VolcanismLevel.NONE) {
            return mass > 2.0 ? MagneticFieldStrength.INTENSE : MagneticFieldStrength.MODERATE;
        }
        return mass > 0.1 ? MagneticFieldStrength.WEAK : MagneticFieldStrength.NONE;
    }


    public static Planet generate(StarSystemAsset probed) {
        return generate(probed, DEFAULT_PLANET);
    }

    public static void start() {
        distanceGenerated = 0;
    }

    private static double nextDistance(double starLuminosity) {
        // Les orbites autour d'étoiles naines (faible luminosité) sont plus resserrées
        double step = starLuminosity < 0.1 ? rand.nextDouble(0.05, 0.4) : rand.nextDouble(0.3, 4.0);
        distanceGenerated += step;
        return distanceGenerated;
    }
}