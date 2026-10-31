package ca.dauqui.spacecolonygame.gamedata.starsystem;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Planet {

    private final String type;
    private final DoubleProperty radius;
    private final String radiusUnit;
    private final double mass;
    private final DoubleProperty gravity;
    private final double temperature;
    private final double distance; // En AU

    private final double albedo;
    private final double orbitalPeriodDays;  // Révolution (Durée de l'année)
    private final double rotationPeriodHours; // Rotation (Durée du jour)
    private final double axisTilt;           // Inclinaison de l'axe
    private final boolean isTidallyLocked;   // Verrouillage gravitationnel
    private final double receivedLight;
    private final int numberOfSites;

    // Caractéristiques physiques localisables fortement typées
    private final AtmosphereComposition atmosphereComposition;
    private final TectonicType tectonic;
    private final double volcanism;
    private final double magneticField;

    @SuppressWarnings("java:S107")
    public Planet(String type, DoubleProperty radius, String radiusUnit, double mass, DoubleProperty gravity,
                  double temperature, double distance,
                  double albedo, double orbitalPeriodDays, double rotationPeriodHours,
                  double axisTilt, boolean isTidallyLocked,
                  AtmosphereComposition atmosphereComposition, TectonicType tectonic,
                  double volcanism, double magneticField, double receivedLight, int numberOfSites) {
        this.type = type;
        this.radius = radius;
        this.radiusUnit = radiusUnit;
        this.mass = mass;
        this.gravity = gravity;
        this.temperature = temperature;
        this.distance = distance;

        this.albedo = albedo;
        this.orbitalPeriodDays = orbitalPeriodDays;
        this.rotationPeriodHours = rotationPeriodHours;
        this.axisTilt = axisTilt;
        this.isTidallyLocked = isTidallyLocked;
        this.receivedLight = receivedLight;
        this.numberOfSites = numberOfSites;

        this.atmosphereComposition = atmosphereComposition;
        this.tectonic = tectonic;
        this.volcanism = volcanism;
        this.magneticField = magneticField;
    }


    public String getType() {
        return type;
    }

    public ReadOnlyDoubleProperty radiusProperty() {
        return radius;
    }

    public double getRadius() {
        return radius.get();
    }

    public String getRadiusUnit() {
        return radiusUnit;
    }

    public double getMass() {
        return mass;
    }

    public double getGravity() {
        return gravity.get();
    }

    public ReadOnlyDoubleProperty gravityProperty() {
        return gravity;
    }

    public double getTemperature() {
        return temperature;
    }

    public AtmosphereComposition getAtmosphereGazes() {
        return atmosphereComposition;
    }

    public TectonicType getTectonic() {
        return tectonic;
    }

    public double getVolcanism() {
        return volcanism;
    }

    public double getMagneticField() {
        return magneticField;
    }

    public double getDistance() {
        return distance;
    }

    public double getAtmosphericPressure() {
        return atmosphereComposition.getPressure();
    }

    public double getAlbedo() {
        return albedo;
    }

    public double getOrbitalPeriodDays() {
        return orbitalPeriodDays;
    }

    public double getRotationPeriodHours() {
        return rotationPeriodHours;
    }

    public double getAxisTilt() {
        return axisTilt;
    }

    public boolean isTidallyLocked() {
        return isTidallyLocked;
    }

    public double getReceivedLight() {
        return receivedLight;
    }

    public int getNumberOfSites() {
        return numberOfSites;
    }
}