package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;

import java.util.List;

/**
 * Central atmospheric simulation controller orchestrating dynamic multiphase gas matrices.
 * Bridges kinetic vapour mechanics and frozen surface crust accumulations through dedicated {@link PhaseReservoir} stores.
 * Exposes core reactive JavaFX bindings for planetary pressure, cumulative toxicity, corrosiveness,
 * greenhouse factors, and stabilized thermodynamic surface temperatures.
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
     * Initializes the planetary composition matrix, binding structural planetary dimensions
     * against dynamic gas masses to evaluate macro-climatic criteria.
     *
     * @param gravity          Calculated surface gravitational acceleration vector scalar (m/s²)
     * @param radius           The geometric radius layout dimensions parameter of the host planet
     * @param radiusUnit       Structural configuration token asserting Earth "Re" or Jovian "Rj" scaling maps
     * @param tempEquilibrium  Foundational black-body raw thermal index tracking bounds prior to greenhouse adjustments (Kelvin)
     * @param isTidallyLocked  Flag asserting permanent sync rotations, introducing systemic radiation cooling offsets
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

        // Asynchronous structural listener holding a lock protecting feedback calculation loops
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
    private void updatePhaseTransitions() {
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
                condensingVapourGas(gas, satPressure, currentTotalPressure, totalMass, gasMass);
            } else if (partialPressure < satPressure) {
                // Unsaturated status: check if we have ground reserves to sublimate
                evaporatingCondensedGas(gas, satPressure, currentTotalPressure, totalMass, gasMass);
            }
        }
    }

    /**
     * Models chemical phase transitions by extracting unsaturated gas mass from surface ice sheets back into the atmosphere.
     * Evaluates local mass deficits based on current partial and saturation pressure ratios to safely drive sublimation loops.
     *
     * @param gas                  The chemical gas type signature processing sublimation
     * @param satPressure          The computed saturation vapour pressure of the target compound at current temperature (Bars)
     * @param currentTotalPressure The absolute macro barometric pressure currently exerted by the global vapour phase (Bars)
     * @param totalMass            The cumulative active mass aggregate currently present inside the vapour reservoir (kg)
     * @param gasMass              The current raw individual mass distribution of this gas element floating in the air (kg)
     */
    private void evaporatingCondensedGas(AtmosphereGasType gas, double satPressure, double currentTotalPressure, double totalMass, double gasMass) {
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

    /**
     * Models chemical phase transitions by condensing saturated atmospheric gas fractions down into solid or liquid surface blankets.
     * Calculates the strict mass overhead exceeding allowed physical limits to trigger automated precipitation fallout.
     *
     * @param gas                  The chemical gas type signature processing condensation
     * @param satPressure          The computed saturation vapour pressure of the target compound at current temperature (Bars)
     * @param currentTotalPressure The absolute macro barometric pressure currently exerted by the global vapour phase (Bars)
     * @param totalMass            The cumulative active mass aggregate currently present inside the vapour reservoir (kg)
     * @param gasMass              The current raw individual mass distribution of this gas element floating in the air (kg)
     */
    private void condensingVapourGas(AtmosphereGasType gas, double satPressure, double currentTotalPressure, double totalMass, double gasMass) {
        double maxAllowedMass = (satPressure / currentTotalPressure) * totalMass;
        double massToCondense = gasMass - maxAllowedMass;

        if (massToCondense > 0.0) {
            removeAtmosphereGas(gas, massToCondense);
            addCondensedGas(gas, massToCondense);
        }
    }

    /**
     * Extracts the real-time vapour mass value assigned to a specific gas tracking slot.
     *
     * @param gas The targeting chemical asset reference
     * @return Current raw vapour mass allocation expressed in kilograms
     */
    public double getVaporMassFor(AtmosphereGasType gas) {
        return vaporPhase.getMassFor(gas);
    }

    /**
     * @return The master continuous vapour mass total tracking layout binding
     */
    public DoubleBinding getVaporTotalMass() {
        return vaporPhase.getTotalMass();
    }

    /**
     * Evaluates if the vapour phase repository contains active mass aggregates.
     *
     * @return True if the internal absolute vapour mass registry strictly exceeds zero kilograms
     */
    public boolean hasAtmosphere() {
        return vaporPhase.hasPhase();
    }

    /**
     * Compiles a localized sequence of all chemical elements currently active within the vapour matrix,
     * filtered to exclude trace levels and sorted in strict descending concentration order.
     *
     * @return A sorted, snapshot list of non-empty gaseous AtmosphereGasType entities
     */
    public List<AtmosphereGasType> getAtmosphereGasesSortedByConcentration() {
        return vaporPhase.getGasesSortedByConcentration();
    }

    /**
     * Strips a specified mass allocation away from the target vapour array slot.
     *
     * @param gasType The target chemical compound structural reference to drain
     * @param amount            Target mass quantity to withdraw measured in kilograms
     * @return The remaining mass deficit quantity that could not be removed if supply was insufficient
     */
    public double removeAtmosphereGas(AtmosphereGasType gasType, double amount) {
        return vaporPhase.removeGas(gasType, amount);
    }

    /**
     * Injects a specified mass allocation into the targeting vapour array index slot.
     *
     * @param gasType The targeting chemical compound structural reference
     * @param amount            Mass quantity to inject measured in kilograms
     */
    public void addAtmosphereGas(AtmosphereGasType gasType, double amount) {
        vaporPhase.addGas(gasType, amount);
    }

    /**
     * Computes or retrieves a cached reactive binding evaluating the vapour mass ratio of a specific gas (0.0 to 1.0).
     *
     * @param gasType The target chemical gas profile to evaluate
     * @return A lazily evaluated DoubleBinding tracking the percentage of the selected gas in the air
     */
    public DoubleBinding getAtmosphereGasPercentage(AtmosphereGasType gasType) {
        return vaporPhase.getGasPercentage(gasType);
    }

    /**
     * Extracts the real-time condensed mass value assigned to a specific gas tracking slot.
     *
     * @param gas The targeting chemical asset reference
     * @return Current raw condensed mass allocation expressed in kilograms
     */
    public double getCondensedGasMassFor(AtmosphereGasType gas) {
        return condensedPhase.getMassFor(gas);
    }

    /**
     * @return The master continuous condensed mass total tracking layout binding
     */
    public DoubleBinding getCondensedGasTotalMass() {
        return condensedPhase.getTotalMass();
    }

    /**
     * Compiles a localized sequence of all chemical elements currently active within the condensed matrix,
     * filtered to exclude trace levels and sorted in strict descending concentration order.
     *
     * @return A sorted, snapshot list of non-empty frozen or liquid AtmosphereGasType entities
     */
    public List<AtmosphereGasType> getCondensedGasesSortedByConcentration() {
        return condensedPhase.getGasesSortedByConcentration();
    }

    /**
     * Strips a specified mass allocation away from the target condensed array slot.
     *
     * @param gasType The target chemical compound structural reference to drain
     * @param amount            Target mass quantity to withdraw measured in kilograms
     * @return The remaining mass deficit quantity that could not be removed if supply was insufficient
     */
    public double removeCondensedGas(AtmosphereGasType gasType, double amount) {
        return condensedPhase.removeGas(gasType, amount);
    }

    /**
     * Injects a specified mass allocation into the targeting condensed array index slot.
     *
     * @param gasType The targeting chemical compound structural reference
     * @param amount            Mass quantity to inject measured in kilograms
     */
    public void addCondensedGas(AtmosphereGasType gasType, double amount) {
        condensedPhase.addGas(gasType, amount);
    }

    /**
     * Computes or retrieves a cached reactive binding evaluating the condensed mass ratio of a specific gas (0.0 to 1.0).
     *
     * @param gasType The target chemical gas profile to evaluate
     * @return A lazily evaluated DoubleBinding tracking the percentage of the selected frozen asset on the crust
     */
    public DoubleBinding getCondensedGasPercentage(AtmosphereGasType gasType) {
        return condensedPhase.getGasPercentage(gasType);
    }

    /**
     * Evaluates if the condensed phase repository contains active mass aggregates.
     *
     * @return True if the internal absolute condensed mass registry strictly exceeds zero kilograms
     */
    public boolean hasCondensedGases() {
        return condensedPhase.hasPhase();
    }

}