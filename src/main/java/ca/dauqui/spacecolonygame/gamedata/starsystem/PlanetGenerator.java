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
        fakeData[StarDataColumn.TY.ordinal()] = "-";
        fakeData[StarDataColumn.RADIUS.ordinal()] = "-1";

        DEFAULT_PLANET = new PlanetAsset(fakeData);
    }

    private PlanetGenerator() {}

    public static Planet generate(StarSystemAsset probed, PlanetAsset ps) {
        double starLum = probed.getBolometricLum() == -1 ? 1.0 : probed.getBolometricLum();
        double starMass = probed.getMass() == -1 ? 1.0 : probed.getMass(); // Utilisé pour Kepler
        double distance = nextDistance(starLum);

        String type = ps.getType().replace("?", "");
        String radiusUnit = ps.getRadiusUnit().replace("?", "");

        // 1. Détermination cohérente du rayon et du type
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

        // 2. Calcul de la Masse et de la Gravité
        double density = type.equals("PT") ? rand.nextDouble(4.0, 5.8) : rand.nextDouble(0.7, 1.8);
        double mass = type.equals("PT") ? Math.pow(radius, 3) * (density / 5.51) : Math.pow(radius, 3) * density * 317.8;
        double radiusInEarthUnits = type.equals("PT") ? radius : radius * 11.209;
        double gravity = mass / Math.pow(radiusInEarthUnits, 2);

        // 3. Extraction de la Géologie (Volcanisme)
        TectonicResult geology = generateGeology(type, radiusInEarthUnits);

        // 4. ALBÉDO (Réflectivité de la surface et de l'atmosphère)
        // Les géantes (nuages de gaz) et planètes glacées réfléchissent beaucoup plus la lumière que la roche nue
        double albedo = type.equals("PT") ? rand.nextDouble(0.1, 0.4) : rand.nextDouble(0.3, 0.6);
        if (distance > 3.0 && type.equals("PT")) albedo += 0.25; // Calotte glaciaire / Ligne des glaces

        // 5. THERMODYNAMIQUE : Corps Noir avec Albédo (Température d'équilibre en Kelvin)
        // Formule complète : Te = 278 * ((1 - albedo) * Lum / d²)^0.25
        double tempEquilibrium = 278.0 * Math.pow(((1.0 - albedo) * starLum) / Math.pow(distance, 2), 0.25);

        // 6. ATMOSPHÈRE ET EFFET DE SERRE
        AtmosphereResult atmosphere = generateAtmosphere(type, gravity, tempEquilibrium, geology.volcanism);

        // 7. MAGNÉTOSPHÈRE
        MagneticFieldStrength magneticField = generateMagnetosphere(mass, geology.volcanism);

        // =================================================================
        // --- ORBITE ET ROTATION ---
        // =================================================================

        // Révolution (3e loi de Kepler : T = sqrt(d³ / M_étoile) en années)
        double orbitalPeriodDays = Math.sqrt(Math.pow(distance, 3) / starMass) * 365.25;

        // Inclinaison de l'axe (Obliquité en degrés)
        double axisTilt = rand.nextDouble() < 0.85 ? rand.nextDouble(0.0, 30.0) : rand.nextDouble(30.0, 90.0);

        // Rotation (Durée du jour) & Verrouillage gravitationnel (Tidal Locking)
        double rotationPeriodHours;
        boolean isTidallyLocked = false;

        // Seuil d'effet de marée fort (proximité de l'étoile)
        if (distance < (0.2 * Math.pow(starMass, 0.33))) {
            rotationPeriodHours = orbitalPeriodDays * 24.0; // Journée = Année
            isTidallyLocked = true;
        } else {
            rotationPeriodHours = type.equals("PT") ? rand.nextDouble(10.0, 40.0) : rand.nextDouble(8.0, 18.0);
            if (rand.nextDouble() > 0.96 && type.equals("PT")) rotationPeriodHours += rand.nextDouble(100.0, 4000.0); // Type Vénus
        }

        // Ajustement cosmétique de la température si verrouillée (la face nuit éternelle refroidit la moyenne)
        double finalTemperature = atmosphere.finalTemperature;
        if (isTidallyLocked) finalTemperature -= 15.0;

        return new Planet(type, radius, radiusUnit, mass, gravity,
                finalTemperature, atmosphere.pressure, distance,
                albedo, orbitalPeriodDays, rotationPeriodHours, axisTilt, isTidallyLocked,
                atmosphere.composition, geology.tectonic, geology.volcanism, magneticField);
    }

    private static AtmosphereResult generateAtmosphere(String type, double gravity, double tempEquilibrium, VolcanismLevel volcanism) {
        if (!"PT".equals(type)) {
            return new AtmosphereResult(AtmosphereComposition.HYDROGEN_HELIUM, rand.nextDouble(50.0, 500.0), tempEquilibrium);
        }

        // Planète rocheuse
        if (gravity > 0.20 && tempEquilibrium < 500) {
            AtmosphereComposition comp = tempEquilibrium > 320 ? AtmosphereComposition.CARBON_DIOXIDE : AtmosphereComposition.NITROGEN;

            double maxPressure = Math.pow(gravity, 2) * 12.0;
            double pressure = rand.nextDouble(0.01, maxPressure);

            if (volcanism == VolcanismLevel.INTENSE) pressure += rand.nextDouble(2.0, 5.0); // Dégazage

            // EFFET DE SERRE (Augmentation logarithmique selon la pression et la nature des gaz)
            double greenhouseFactor = comp == AtmosphereComposition.CARBON_DIOXIDE ? 35.0 : 10.0;
            double greenhouseBonus = Math.log1p(pressure) * greenhouseFactor;

            return new AtmosphereResult(comp, pressure, tempEquilibrium + greenhouseBonus);
        }

        return new AtmosphereResult(AtmosphereComposition.NONE, 0.0, tempEquilibrium);
    }

    private record AtmosphereResult(AtmosphereComposition composition, double pressure, double finalTemperature) {}

    private static TectonicResult generateGeology(String type, double radiusInEarthUnits) {
        if (!"PT".equals(type)) {
            return new TectonicResult(TectonicType.NONE, VolcanismLevel.INTENSE);
        }

        if (radiusInEarthUnits > 0.6) {
            TectonicType tectonic = rand.nextDouble() > 0.4 ? TectonicType.ACTIVE : TectonicType.NONE;
            VolcanismLevel volcanism = tectonic == TectonicType.ACTIVE ? VolcanismLevel.ACTIVE : VolcanismLevel.LOW;
            return new TectonicResult(tectonic, volcanism);
        }

        return new TectonicResult(TectonicType.NONE, VolcanismLevel.NONE);
    }

    private record TectonicResult(TectonicType tectonic, VolcanismLevel volcanism) {}

    private static MagneticFieldStrength generateMagnetosphere(double mass, VolcanismLevel volcanism) {
        if (mass > 0.4 && volcanism != VolcanismLevel.NONE) {
            return mass > 10.0 ? MagneticFieldStrength.INTENSE : MagneticFieldStrength.MODERATE;
        }
        return mass > 0.1 ? MagneticFieldStrength.WEAK : MagneticFieldStrength.NONE;
    }

    public static Planet generate(StarSystemAsset probed) { return generate(probed, DEFAULT_PLANET); }
    public static void start() { distanceGenerated = 0; }

    private static double nextDistance(double starLuminosity) {
        double step = starLuminosity < 0.1 ? rand.nextDouble(0.05, 0.4) : rand.nextDouble(0.3, 4.0);
        distanceGenerated += step;
        return distanceGenerated;
    }
}