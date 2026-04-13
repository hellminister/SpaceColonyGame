package ca.dauqui.spacecolonygame.gamedata.starsystem;

import java.util.Random;

// TODO make this to create relatively accurate planet data based on given information
public final class PlanetGenerator {
    private static double distanceGenerated = 0;
    private static Random rand = new Random();

    private PlanetGenerator() {}

    public static Planet generate(StarSystemAsset probed, PlanetAsset ps) {
        double distance = getDistanceGenerated();
        String tectonic = "";
        String mainAtmosphereGaz = "";
        double temperature = ps.getTeff();
        double gravity = 0;
        double mass = 0;
        double radius = 0;
        String type = ps.getType();
        return new Planet(type, radius, mass, gravity, temperature, mainAtmosphereGaz, tectonic, distance);
    }

    public static Planet generate(StarSystemAsset probed) {
        double distance = getDistanceGenerated();
        String tectonic = "";
        String mainAtmosphereGaz = "";
        double temperature = 0;
        double gravity = 0;
        double mass = 0;
        double radius = 0;
        String type = "";
        return new Planet(type, radius, mass, gravity, temperature, mainAtmosphereGaz, tectonic, distance);
    }

    public static void start() {
        distanceGenerated = 0;
    }


    private static double getDistanceGenerated(){
        distanceGenerated = distanceGenerated + rand.nextDouble(0.2,20.0);
        return distanceGenerated;
    }
}