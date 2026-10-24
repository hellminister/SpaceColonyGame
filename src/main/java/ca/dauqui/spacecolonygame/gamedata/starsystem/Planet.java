package ca.dauqui.spacecolonygame.gamedata.starsystem;

public class Planet {

    private final String type;
    private final double radius;
    private final String radiusUnit;
    private final double mass;
    private final double gravity;
    private final double temperature;
    private final double atmosphericPressure; // En bars
    private final double distance; // En AU

    // Caractéristiques physiques localisables fortement typées
    private final AtmosphereComposition atmosphereComposition;
    private final TectonicType tectonic;
    private final VolcanismLevel volcanism;
    private final MagneticFieldStrength magneticField;

    @SuppressWarnings("java:S107")
    public Planet(String type, double radius, String radiusUnit, double mass, double gravity,
                  double temperature, double atmosphericPressure, double distance,
                  AtmosphereComposition atmosphereComposition, TectonicType tectonic,
                  VolcanismLevel volcanism, MagneticFieldStrength magneticField) {
        this.type = type;
        this.radius = radius;
        this.radiusUnit = radiusUnit;
        this.mass = mass;
        this.gravity = gravity;
        this.temperature = temperature;
        this.atmosphericPressure = atmosphericPressure;
        this.distance = distance;
        this.atmosphereComposition = atmosphereComposition;
        this.tectonic = tectonic;
        this.volcanism = volcanism;
        this.magneticField = magneticField;
    }


    public String getType() {
        return type;
    }

    public double getRadius() {
        return radius;
    }

    public String getRadiusUnit() {
        return radiusUnit;
    }

    public double getMass() {
        return mass;
    }

    public double getGravity() {
        return gravity;
    }

    public double getTemperature() {
        return temperature;
    }

    public AtmosphereComposition getMainAtmosphereGaz() {
        return atmosphereComposition;
    }

    public TectonicType getTectonic() {
        return tectonic;
    }

    public VolcanismLevel getVolcanism() {
        return volcanism;
    }

    public MagneticFieldStrength getMagneticField() {
        return magneticField;
    }

    public double getDistance() {
        return distance;
    }
}