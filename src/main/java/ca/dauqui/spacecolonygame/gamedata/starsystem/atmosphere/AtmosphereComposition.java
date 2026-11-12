package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    private final double massPerBar;

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

        this.massPerBar = (surfaceArea * 100000.0) / gravity;

        this.pressure = new DoubleBinding() {
            {
                bind(vaporPhase.getTotalMass());
            }

            @Override
            protected double computeValue() {
                double mass = vaporPhase.getTotalMass().get();
                return (mass <= 0.0 || massPerBar <= 0.0) ? 0.0 : mass / massPerBar;
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
                double computedTemp = tempEquilibrium;
                double currentPressure = pressure.get();
                double ghFactor = greenhouseFactor.get();

                if (currentPressure > 0.0 && !Double.isNaN(ghFactor)) {
                    double greenhouseMultiplier = 1.0 + (Math.log1p(currentPressure) * (ghFactor / 100.0));
                    computedTemp *= greenhouseMultiplier;
                }

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
            public void invalidated(Observable observable) {
                if (isUpdating) return;

                Platform.runLater(() -> {
                    isUpdating = true;
                    try {
                        processAtmosphericCycle();
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
        return pressureProperty().get();
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
        return toxicityProperty();
    }

    /**
     * @return The corrosiveness level of the atmosphere (0.0 to 1.0) where 1.0 is most corrosive
     */
    public double getCorrosiveness() {
        return corrosivenessProperty().get();
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
        return greenhouseFactorProperty().get();
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
        return surfaceTemperatureProperty().get();
    }

    /**
     * @return the temperature of the planet affected by the greenhouse effect
     */
    public DoubleBinding surfaceTemperatureProperty() {
        return surfaceTemperature;
    }

    /**
     * Executes a complete atmospheric simulation step in two sequential, atomic phases:
     * stoichiometric chemical reactions followed by thermodynamic phase transitions.
     * Remember : must be called at the end of the turn
     */
    public void processAtmosphericCycle(){
        updateChemicalReactions(false);
        updatePhaseTransitions();
    }

    /**
     * Forces all eligible chemical reactions to ignite and burn to completion regardless of ambient temperature,
     * followed by immediate thermodynamic condensation of the resulting products.
     *
     * @param maxCycle The maximum number of reaction iterations permitted to prevent infinite loops
     */
    public void forceAtmosphericReactions(int maxCycle) {
        int safetyCounter = 0;
        while (updateChemicalReactions(true) && safetyCounter < maxCycle) {
            safetyCounter++;
        }

        updatePhaseTransitions();
    }

    /**
     * Evaluates dynamic partial pressures against local saturation layers.
     * Condenses gas overheads into surface ice sheets using addCondensedGas,
     * or sublimates ground frost back into vapour using removeCondensedGas.
     */
    private void updatePhaseTransitions() {
        double currentTemp = getSurfaceTemperature();

        if (!hasAtmosphere() && !hasCondensedGases()){
            return;
        }

        startTransaction();
        try {
            // Loop through a copy to prevent concurrent modification exceptions during map shifts
            for (AtmosphereGasType gas : AtmosphereGasType.getAtmosphereGazTypes()) {
                double gasMass = getVaporMassFor(gas);
                double partialPressure = gasMass / massPerBar;
                double satPressure = gas.calculateSaturationPressure(currentTemp);

                if (partialPressure > satPressure) {
                    // Saturated status: calculate excess mass to condense out of the air
                    condensingVapourGas(gas, satPressure, gasMass);
                } else if (partialPressure < satPressure) {
                    // Unsaturated status: check if we have ground reserves to sublimate
                    evaporatingCondensedGas(gas, satPressure, gasMass);
                }
            }
        } finally {
            commitTransaction();
        }
    }

    /**
     * Models chemical phase transitions by extracting unsaturated gas mass from surface ice sheets back into the atmosphere.
     * Evaluates local mass deficits based on current partial and saturation pressure ratios to safely drive sublimation loops.
     *
     * @param gas                  The chemical gas type signature processing sublimation
     * @param satPressure          The computed saturation vapour pressure of the target compound at current temperature (Bars)
     * @param gasMass              The current raw individual mass distribution of this gas element floating in the air (kg)
     */
    private void evaporatingCondensedGas(AtmosphereGasType gas, double satPressure, double gasMass) {
        double groundReserve = getCondensedGasMassFor(gas);

        if (groundReserve > 0.0) {
            double targetPartialMass = satPressure * massPerBar;
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
     * @param gasMass              The current raw individual mass distribution of this gas element floating in the air (kg)
     */
    private void condensingVapourGas(AtmosphereGasType gas, double satPressure, double gasMass) {
        double maxAllowedMass = satPressure * massPerBar;
        double massToCondense = gasMass - maxAllowedMass;

        if (massToCondense > 0.0) {
            removeAtmosphereGas(gas, massToCondense);
            addCondensedGas(gas, massToCondense);
        }
    }

    /**
     * Evaluates all registered chemical reactions against ambient temperature and partial pressure thresholds.
     * Allocates shared reactants proportionally when concurrent reactions compete for the same gas.
     *
     * @param ignoreTemp True to bypass minimum activation temperature thresholds (e.g., during forced orbital ignition)
     * @return True if at least one chemical reaction executed and modified atmospheric masses
     */
    private boolean updateChemicalReactions(boolean ignoreTemp) {
        double currentTemp = getSurfaceTemperature();
        boolean hadReaction = false;
        if (hasAtmosphere()) {
            Collection<AtmosphericReaction> allReactions = AtmosphericReaction.getAllReactions();
            if (!allReactions.isEmpty()) {
                int gasCount = AtmosphereGasType.getAtmosphereGazTypes().size();
                double[] totalGasDemand = new double[gasCount];
                double[] reactionTheoreticalCycles = new double[allReactions.size()];

                // PASS 1: Filter eligible reactions and compute theoretical stoichiometric cycles
                var activeReactions = getEligibleReactions(allReactions, currentTemp, ignoreTemp, reactionTheoreticalCycles, totalGasDemand);
                if (!activeReactions.isEmpty()) {

                    // PASS 2: Compute proportional throttling ratios for over-demanded reactants
                    double[] gasThrottleFactors = computeThrottleFactors(gasCount, totalGasDemand);

                    startTransaction();
                    try {
                        // PASS 3: Apply throttled stoichiometric conversions via the active transaction
                        hadReaction = applyReactions(activeReactions, gasThrottleFactors, reactionTheoreticalCycles);
                    } finally {
                        commitTransaction();
                    }
                }
            }
        }
        return hadReaction;
    }

    /**
     * Computes proportional throttling factors (0.0 to 1.0) for each gas to prevent negative mass balances
     * when multiple concurrent reactions compete for the same limited reactant.
     *
     * @param gasCount       The total number of registered atmospheric gas types
     * @param totalGasDemand Array indexed by gas ordinal containing cumulative mass requested by active reactions (kg)
     * @return An array indexed by gas ordinal specifying the allowable execution ratio per gas (1.0 = unthrottled)
     */
    private double[] computeThrottleFactors(int gasCount, double[] totalGasDemand) {
        double[] gasThrottleFactors = new double[gasCount];
        Arrays.fill(gasThrottleFactors, 1.0);

        for (AtmosphereGasType gas : AtmosphereGasType.getAtmosphereGazTypes()) {
            int index = gas.ordinal();
            double available = getVaporMassFor(gas);
            double demand = totalGasDemand[index];
            if (demand > available && available > 0.0) {
                double factor = available / demand;
                gasThrottleFactors[index] = Double.isFinite(factor) ? factor : 0.0;
            }
        }
        return gasThrottleFactors;
    }

    /**
     * Executes throttled stoichiometric conversions for all active reactions within the current atomic transaction,
     * consuming reactants and generating reaction products into the vapour reservoir.
     *
     * @param activeReactions           The list of eligible chemical reactions scheduled for execution
     * @param gasThrottleFactors        Array indexed by gas ordinal containing proportional scaling factors (0.0 to 1.0)
     * @param reactionTheoreticalCycles Array indexed by reaction ordinal storing unthrottled reaction cycles
     * @return True if at least one reaction executed with a strictly positive cycle count
     */
    private boolean applyReactions(List<AtmosphericReaction> activeReactions, double[] gasThrottleFactors, double[] reactionTheoreticalCycles) {
        boolean hadReaction = false;
        for (AtmosphericReaction reaction : activeReactions) {
            double finalThrottle = 1.0;
            for (AtmosphereGasType gas : reaction.reactants().keySet()) {
                finalThrottle = Math.min(finalThrottle, gasThrottleFactors[gas.ordinal()]);
            }

            double finalizedCycles = reactionTheoreticalCycles[reaction.ordinal()] * finalThrottle;
            if (finalizedCycles > 0.0) {
                for (var reactantEntry : reaction.reactants().entrySet()) {
                    AtmosphereGasType gas = reactantEntry.getKey();
                    double massToConsume = finalizedCycles * reactantEntry.getValue() * gas.molecularWeight();
                    removeAtmosphereGas(gas, massToConsume);
                }

                for (var productEntry : reaction.products().entrySet()) {
                    AtmosphereGasType gas = productEntry.getKey();
                    double massToProduce = finalizedCycles * productEntry.getValue() * gas.molecularWeight();
                    addAtmosphereGas(gas, massToProduce);
                }
                hadReaction = true;
            }
        }
        return hadReaction;
    }

    /**
     * Evaluates all registered chemical reactions against current planetary conditions,
     * filtering eligible reactions and accumulating their theoretical stoichiometric demands.
     *
     * @param allReactions              The complete collection of configured chemical reactions in the simulation
     * @param currentTemp               Current planetary surface temperature expressed in Kelvin
     * @param ignoreTemp                True to bypass minimum temperature thresholds during forced ignition
     * @param reactionTheoreticalCycles Output array indexed by reaction ordinal storing maximum allowed cycles per turn
     * @param totalGasDemand            Output array indexed by gas ordinal accumulating total mass demanded across all active reactions (kg)
     * @return A filtered list containing only the chemical reactions eligible to execute during this turn
     */
    private List<AtmosphericReaction> getEligibleReactions(Collection<AtmosphericReaction> allReactions, double currentTemp, boolean ignoreTemp, double[] reactionTheoreticalCycles,
                                                           double[] totalGasDemand) {
        List<AtmosphericReaction> activeReactions = new ArrayList<>();
        for (AtmosphericReaction reaction : allReactions) {
            if (applicableReaction(reaction, currentTemp, ignoreTemp)) {

                // Find limiting reactant
                double maxCycles = calculateMaxCycles(reaction);
                if (maxCycles <= 0.0) {
                    continue;
                }

                double allowedCycles = maxCycles * reaction.reactionRate();
                if (allowedCycles > 0.0) {
                    activeReactions.add(reaction);
                    reactionTheoreticalCycles[reaction.ordinal()] = allowedCycles;

                    for (var reactantEntry : reaction.reactants().entrySet()) {
                        AtmosphereGasType gas = reactantEntry.getKey();
                        double massDemanded = allowedCycles * reactantEntry.getValue() * gas.molecularWeight();
                        totalGasDemand[gas.ordinal()] += massDemanded;
                    }
                }
            }
        }
        return activeReactions;
    }

    /**
     * Calculates the maximum number of stoichiometric reaction cycles supported by the limiting reactant.
     * Enforces an upper finite safety cap to prevent floating-point overflow on massive worlds.
     *
     * @param reaction The chemical reaction to evaluate
     * @return The maximum allowable reaction cycles capped at 1e30, or 0.0 if a reactant is depleted
     */
    private double calculateMaxCycles(AtmosphericReaction reaction) {
        double maxCycles = Double.MAX_VALUE;
        for (var reactantEntry : reaction.reactants().entrySet()) {
            AtmosphereGasType gas = reactantEntry.getKey();
            double availableMass = getVaporMassFor(gas);
            if (availableMass <= 0.0) {
                maxCycles = 0.0;
                break;
            }

            double potentialCycles = availableMass / (reactantEntry.getValue() * gas.molecularWeight());
            if (Double.isFinite(potentialCycles)) {
                maxCycles = Math.min(maxCycles, potentialCycles);
            }
        }

        return Math.min(maxCycles, 1e30);
    }

    /**
     * Evaluates whether ambient thermal and partial pressure conditions satisfy
     * the activation thresholds required to ignite a specific chemical reaction.
     *
     * @param reaction             The candidate chemical reaction to evaluate
     * @param currentTemp          Current planetary surface temperature in Kelvin
     * @param ignoreTemp  True to ignore minimum activation temperature requirements
     * @return True if temperature and all reactant partial pressure thresholds are met
     */
    private boolean applicableReaction(AtmosphericReaction reaction, double currentTemp, boolean ignoreTemp) {
        return (ignoreTemp || currentTemp >= reaction.minTemperature()) && arePartialPressuresMet(reaction);
    }

    /**
     * Evaluates whether the atmosphere contains a volatile mixture capable of igniting
     * during high-friction atmospheric entry, regardless of current ambient surface temperature.
     *
     * @return True if at least one registered chemical reaction satisfies all its minimum partial pressure thresholds
     */
    public boolean hasExplosiveAtmosphere() {
        if (!hasAtmosphere()) {
            return false;
        }

        for (AtmosphericReaction reaction : AtmosphericReaction.getAllReactions()) {
            if (arePartialPressuresMet(reaction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether all reactant partial pressures meet or exceed the minimum ignition thresholds
     * defined for a specific chemical reaction.
     *
     * @param reaction The candidate chemical reaction to evaluate
     * @return True if all required partial pressures are satisfied
     */
    private boolean arePartialPressuresMet(AtmosphericReaction reaction) {
        for (var thresholdEntry : reaction.minPartialPressures().entrySet()) {
            AtmosphereGasType gas = thresholdEntry.getKey();
            double minRequiredBar = thresholdEntry.getValue();
            double partialPressure = getVaporMassFor(gas) / massPerBar;

            if (partialPressure < minRequiredBar) {
                return false;
            }
        }
        return true;
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

    /**
     * Begins an atomic simulation turn across both vapour and condensed phase reservoirs.
     */
    public void startTransaction() {
        vaporPhase.startTransaction();
        condensedPhase.startTransaction();
    }

    /**
     * Commits all pending atmospheric and surface phase changes simultaneously.
     */
    public void commitTransaction() {
        vaporPhase.commitTransaction();
        condensedPhase.commitTransaction();
    }

}