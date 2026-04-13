package ca.dauqui.spacecolonygame.gamedata.starsystem;

public class Planet {

    private String type;
    private double radius;
    private double mass;
    private double gravity;
    private double temperature;
    private String mainAtmosphereGaz;
    private String tectonic;
    private String volcanism;
    private String magneticField;
    private double distance; //in AU

    public Planet(String type, double radius, double mass, double gravity, double temperature, String mainAtmosphereGaz, String tectonic, double distance) {
        this.type = type;
        this.radius = radius;
        this.mass = mass;
        this.gravity = gravity;
        this.temperature = temperature;
        this.mainAtmosphereGaz = mainAtmosphereGaz;
        this.tectonic = tectonic;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }
}