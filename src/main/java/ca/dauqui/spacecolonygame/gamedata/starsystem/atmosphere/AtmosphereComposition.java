package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;

import java.util.List;

/**
 * This class contains the gases composing the planet's atmosphere as well as its pressure
 * It also calculates the atmosphere Toxicity, Corrosiveness, Greenhouse factor and Air pressure and surface temperature
 *
 * @author Jean-François Quintal
 */
public class AtmosphereComposition {
    private final PhaseReservoir vaporPhase;



    private final PhaseReservoir condensedPhase;
    private final DoubleBinding pressure;
    private final DoubleBinding toxicity;
    private final DoubleBinding corrosiveness;
    private final DoubleBinding greenhouseFactor;
    private final DoubleBinding surfaceTemperature;

    /**
     * Initialize the class and prepares the variables bindings
     * Try using the gravity and radius object contained within the planet of this atmosphere
     * @param gravity The gravity of the planet
     * @param radius the radius of the planet
     * @param radiusUnit the radius unit used
     */
    public AtmosphereComposition(double gravity, double radius, String radiusUnit, double tempEquilibrium, boolean isTidallyLocked) {
        this.vaporPhase = new PhaseReservoir();
        this.condensedPhase = new PhaseReservoir();

        double conversionValue = ("Re".equals(radiusUnit)) ? 6371000 : 69911000; // meters
        double surfaceArea = 4 * Math.PI * Math.pow(radius * conversionValue, 2);

        this.pressure = new DoubleBinding() {
            {
                bind(vaporPhase.getTotalMass());
            }

            @Override
            protected double computeValue() {
                return (vaporPhase.getTotalMass().get() * gravity) / (surfaceArea * 100000);
            }
        };

        toxicity = new AtmosphericWeightedAverageBinding(vaporPhase, AtmosphereGasType::toxicity);
        corrosiveness = new AtmosphericWeightedAverageBinding(vaporPhase, AtmosphereGasType::corrosiveness);
        greenhouseFactor = new AtmosphericWeightedAverageBinding(vaporPhase, AtmosphereGasType::greenhouseFactor);

        surfaceTemperature = new DoubleBinding() {
            {
                bind(greenhouseFactor, pressure);
            }

            @Override
            protected double computeValue() {
                double greenhouseMultiplier = 1.0 + (Math.log1p(pressure.get()) * (greenhouseFactor.get() / 100.0));
                double computedTemp = tempEquilibrium * greenhouseMultiplier;

                // If the world is tidally locked, apply the permanent background radiation cooling offset natively
                if (isTidallyLocked) {
                    computedTemp -= 15.0;
                }

                return computedTemp;
            }
        };

        this.surfaceTemperature.addListener(new InvalidationListener() {
            private boolean isUpdating = false;

            @Override
            public void invalidated(javafx.beans.Observable observable) {
                if (isUpdating) return;

                Platform.runLater(() -> {
                    isUpdating = true;
                    try {
                        updatePhaseTransitions();
                    } finally {
                        isUpdating = false;
                    }
                });
            }
        });
    }

    /**
     * @return the air pressure in Bars
     */
    public double getPressure() {
        return pressure.get();
    }

    /**
     * @return the air pressure in Bars as a binding
     */
    public DoubleBinding pressureProperty() {
        return pressure;
    }

    /**
     * @return The toxicity level of the atmosphere (0.0 to 1.0) where 1.0 is most toxic
     */
    public double getToxicity() {
        return toxicity.get();
    }

    /**
     * @return The toxicity level of the atmosphere as a binding (0.0 to 1.0) where 1.0 is most toxic
     */
    public DoubleBinding toxicityProperty() {
        return toxicity;
    }

    /**
     * @return The corrosiveness level of the atmosphere (0.0 to 1.0) where 1.0 is most corrosive
     */
    public double getCorrosiveness() {
        return corrosiveness.get();
    }

    /**
     * @return The corrosiveness level of the atmosphere as a binding (0.0 to 1.0) where 1.0 is most corrosive
     */
    public DoubleBinding corrosivenessProperty() {
        return corrosiveness;
    }


    /**
     * @return The greenhouse factor of the atmosphere
     */
    public double getGreenhouseFactor() {
        return greenhouseFactor.get();
    }

    /**
     * @return The greenhouse factor of the atmosphere as a binding
     */
    public DoubleBinding greenhouseFactorProperty() {
        return greenhouseFactor;
    }

    /**
     * @return the temperature of the planet affected by the greenhouse effect
     */
    public double getSurfaceTemperature() {
        return surfaceTemperature.get();
    }

    /**
     * @return the temperature of the planet affected by the greenhouse effect
     */
    public DoubleBinding surfaceTemperatureProperty() {
        return surfaceTemperature;
    }

    /**
     * Evaluates dynamic partial pressures against local saturation layers.
     * Condenses gas overheads into surface ice sheets using addCondensedGas,
     * or sublimates ground frost back into vapour using removeCondensedGas.
     */
    public void updatePhaseTransitions() {
        double currentTotalPressure = getPressure();
        double currentTemp = getSurfaceTemperature();
        double totalMass = getVaporTotalMass().get();

        if (totalMass <= 0.0) return;

        // Loop through a copy to prevent concurrent modification exceptions during map shifts
        for (AtmosphereGasType gas : AtmosphereGasType.getAtmosphereGazTypes()) {
            double gasMass = getVaporMassFor(gas);
            double partialPressure = (gasMass / totalMass) * currentTotalPressure;
            double satPressure = gas.calculateSaturationPressure(currentTemp);

            if (partialPressure > satPressure) {
                // Saturated status: calculate excess mass to condense out of the air
                double maxAllowedMass = (satPressure / currentTotalPressure) * totalMass;
                double massToCondense = gasMass - maxAllowedMass;

                if (massToCondense > 0.0) {
                    removeAtmosphereGas(gas, massToCondense);
                    addCondensedGas(gas, massToCondense);
                }
            } else if (partialPressure < satPressure) {
                // Unsaturated status: check if we have ground reserves to sublimate
                double groundReserve = getCondensedGasMassFor(gas);

                if (groundReserve > 0.0) {
                    double targetPartialMass = (satPressure / currentTotalPressure) * totalMass;
                    double massDeficit = targetPartialMass - gasMass;

                    if (massDeficit > 0.0) {
                        // Dynamically request removal
                        double massToSublimate = Math.min(massDeficit, groundReserve);
                        double unableToRemove = removeCondensedGas(gas, massToSublimate);

                        double actualSublimated = massToSublimate - unableToRemove;
                        if (actualSublimated > 0.0) {
                            addAtmosphereGas(gas, actualSublimated);
                        }
                    }
                }
            }
        }
    }

    public double getVaporMassFor(AtmosphereGasType gas) {
        return vaporPhase.getMassFor(gas);
    }

    public DoubleBinding getVaporTotalMass() {
        return vaporPhase.getTotalMass();
    }

    public boolean hasAtmosphere() {
        return vaporPhase.hasPhase();
    }

    public List<AtmosphereGasType> getAtmosphereGasesSortedByConcentration() {
        return vaporPhase.getGasesSortedByConcentration();
    }

    public double removeAtmosphereGas(AtmosphereGasType atmosphereGasType, double amount) {
        return vaporPhase.removeGas(atmosphereGasType, amount);
    }

    public void addAtmosphereGas(AtmosphereGasType atmosphereGasType, double amount) {
        vaporPhase.addGas(atmosphereGasType, amount);
    }

    public DoubleBinding getAtmosphereGasPercentage(AtmosphereGasType atmosphereGasType) {
        return vaporPhase.getGasPercentage(atmosphereGasType);
    }

    public double getCondensedGasMassFor(AtmosphereGasType gas) {
        return condensedPhase.getMassFor(gas);
    }

    public DoubleBinding getCondensedGasTotalMass() {
        return condensedPhase.getTotalMass();
    }

    public List<AtmosphereGasType> getCondensedGasesSortedByConcentration() {
        return condensedPhase.getGasesSortedByConcentration();
    }

    public double removeCondensedGas(AtmosphereGasType atmosphereGasType, double amount) {
        return condensedPhase.removeGas(atmosphereGasType, amount);
    }

    public void addCondensedGas(AtmosphereGasType atmosphereGasType, double amount) {
        condensedPhase.addGas(atmosphereGasType, amount);
    }

    public DoubleBinding getCondensedGasPercentage(AtmosphereGasType atmosphereGasType) {
        return condensedPhase.getGasPercentage(atmosphereGasType);
    }

    public boolean hasCondensedGases() {
        return condensedPhase.hasPhase();
    }

}