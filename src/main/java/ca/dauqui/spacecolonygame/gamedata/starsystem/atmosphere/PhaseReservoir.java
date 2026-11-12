package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * High-performance, contiguous memory repository managing chemical mass distributions for a single thermodynamic phase.
 * Replaces dynamic map overheads with dense primitive property arrays indexed natively via gas type ordinals.
 * Fully integrates with JavaFX invalidation graphs to propagate mass fluctuations in O(1) constant time.
 *
 * @author Jean-François Quintal
 */
class PhaseReservoir {
    private final DoubleProperty[] gases;
    private final DoubleBinding[] gasesPercentage;
    private final DoubleBinding totalMass;

    // Transactional state buffers
    private final double[] pendingDeltas;
    private boolean inTransaction = false;
    private boolean suppressInvalidation = false;

    /**
     * Constructs an isolated phase reservoir, pre-allocating contiguous property cells
     * for every registered atmospheric gas type initialized at zero kilograms.
     */
    public PhaseReservoir() {
        int size = AtmosphereGasType.getAtmosphereGazTypes().size();
        gases = new DoubleProperty[size];
        gasesPercentage = new DoubleBinding[size];
        pendingDeltas = new double[size];

        for (AtmosphereGasType gasType : AtmosphereGasType.getAtmosphereGazTypes()) {
            this.gases[gasType.ordinal()] = new SimpleDoubleProperty(0.0);
        }

        totalMass = new DoubleBinding() {
            {
                bind(gases);
            }

            @Override
            protected void onInvalidating() {
                if (suppressInvalidation) {
                    return;
                }
                super.onInvalidating();
            }

            @Override
            protected double computeValue() {
                double sum = 0.0;
                for (DoubleProperty prop : gases) {
                    sum += prop.get();
                }
                return sum;
            }
        };

    }

    /**
     * Computes or retrieves a cached reactive binding evaluating the mass ratio of a specific gas (0.0 to 1.0).
     * Employs safe lazy-instantiation patterns to shield the runtime against redundant layout allocations.
     * Note: Returns the last committed mass; uncommitted transactional deltas are isolated until commit.
     *
     * @param gasType The target chemical gas profile to evaluate
     * @return A lazily evaluated DoubleBinding tracking the percentage of the selected gas
     */
    public DoubleBinding getGasPercentage(AtmosphereGasType gasType) {
        int index = gasType.ordinal();
        if (gasesPercentage[index] == null) {
            gasesPercentage[index] = new DoubleBinding() {
                private final ReadOnlyDoubleProperty rawValue = gases[index];
                {
                    bind(rawValue, totalMass);
                }

                @Override
                protected double computeValue() {
                    double total = totalMass.get();
                    return (total <= 0.0) ? 0.0 : rawValue.get() / total;
                }
            };
        }
        return gasesPercentage[index];
    }

    /**
     * Adds a certain amount of this gas to the planets atmosphere
     * @param gasType the gas added
     * @param amount the amount of gas added in kg (must be positive)
     * @throws IllegalArgumentException if {@code amount} is strictly negative
     */
    public void addGas(AtmosphereGasType gasType, double amount) {
        if (!Double.isFinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException("Invalid gas mass (must be finite and >= 0): " + amount);
        }

        if (amount == 0.0) {
            return;
        }

        int index = gasType.ordinal();
        if (inTransaction) {
            pendingDeltas[index] += amount;
        } else {
            DoubleProperty prop = gases[index];
            prop.set(prop.get() + amount);
        }
    }

    /**
     * removes a certain amount of gas from the atmosphere
     * if the amount to be removed is greater than the amount contained, the method will return the amount that couldn't have been removed
     * ex.: wanting to remove 100 from 50 will result in the gas amount stored to be 0 and the return value to be 50
     * Evaluates effective available supply (including pending transactional deltas)
     *
     * @param gasType the gas type to be removed
     * @param amount the amount of gas to remove in kg (must be positive)
     * @return the amount that couldn't be removed in kg
     * @throws IllegalArgumentException if {@code amount} is strictly negative
     */
    public double removeGas(AtmosphereGasType gasType, double amount) {
        if (!Double.isFinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException("Invalid gas mass (must be finite and >= 0): " + amount);
        }

        if (amount == 0.0) {
            return 0.0;
        }

        int index = gasType.ordinal();
        if (inTransaction) {
            double effectiveContained = Math.max(0.0, gases[index].get() + pendingDeltas[index]);

            if (effectiveContained >= amount) {
                pendingDeltas[index] -= amount;
                return 0.0;
            } else {
                pendingDeltas[index] -= effectiveContained;
                return amount - effectiveContained;
            }
        } else {
            DoubleProperty prop = gases[index];
            double contained = prop.get();

            if (contained >= amount) {
                prop.set(contained - amount);
                return 0.0;
            } else {
                prop.set(0.0);
                return amount - contained;
            }
        }
    }

    /**
     * Returns a list of all gases currently present in the atmosphere,
     * sorted in descending order based on their current mass concentration.
     * Gases with a higher mass or percentage will appear first.
     *
     * @return a sorted list of AtmosphereGasType from highest to lowest concentration
     */
    public List<AtmosphereGasType> getGasesSortedByConcentration() {
        List<AtmosphereGasType> activeGases = new ArrayList<>();
        for (AtmosphereGasType gas : AtmosphereGasType.getAtmosphereGazTypes()) {
            if (gases[gas.ordinal()].get() > 0.0) {
                activeGases.add(gas);
            }
        }

        activeGases.sort((g1, g2) -> Double.compare(gases[g2.ordinal()].get(), gases[g1.ordinal()].get()));
        return activeGases;
    }

    /**
     * @return whether there gases in this phase
     */
    public boolean hasPhase(){
        return totalMass.get() > 0.0;
    }

    /**
     * @return The master continuous mass total tracking layout binding
     */
    public DoubleBinding getTotalMass() {
        return totalMass;
    }

    /**
     * Extracts the committed real-time raw mass value assigned to a specific gas tracking slot.
     * Note: Returns the last committed mass; uncommitted transactional deltas are isolated until commit.
     *
     * @param gas The targeting chemical asset reference
     * @return Current committed raw mass allocation expressed in kilograms
     */
    public double getMassFor(AtmosphereGasType gas) {
        return gases[gas.ordinal()].get();
    }

    /**
     * Opens a new transactional batching window. Subsequent additions and removals
     * are buffered inside pending delta arrays without triggering JavaFX property invalidations.
     */
    public void startTransaction() {
        if (inTransaction) {
            throw new IllegalStateException("A transaction is already active on this PhaseReservoir.");
        }
        Arrays.fill(pendingDeltas, 0.0);
        inTransaction = true;
    }

    /**
     * Commits all buffered mass changes onto the underlying reactive properties simultaneously,
     * triggering a single unified JavaFX binding invalidation wave.
     */
    public void commitTransaction() {
        if (!inTransaction) {
            throw new IllegalStateException("No active transaction to commit on this PhaseReservoir.");
        }

        suppressInvalidation = true;
        boolean hasChanged = false;
        try {
            for (int i = 0; i < gases.length; i++) {
                double delta = pendingDeltas[i];
                if (delta != 0.0) {
                    DoubleProperty prop = gases[i];
                    double newMass = Math.max(0.0, prop.get() + delta);
                    prop.set(newMass);
                    hasChanged = true;
                }
            }
        } finally {
            Arrays.fill(pendingDeltas, 0.0);
            inTransaction = false;
            suppressInvalidation = false;
        }

        if (hasChanged) {
            totalMass.invalidate();
        }
    }

    /**
     * Package-private accessor granting internal processing bindings direct lookups
     * over the internal array reference structure without exposure leaks.
     *
     * @return The raw foundational backing property array block
     */
    ReadOnlyDoubleProperty[] gasesValues() {
        return gases;
    }
}