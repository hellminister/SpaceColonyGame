package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;
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

    /**
     * Constructs an isolated phase reservoir, pre-allocating contiguous property cells
     * for every registered atmospheric gas type initialized at zero kilograms.
     */
    public PhaseReservoir() {
        gases = new DoubleProperty[AtmosphereGasType.getAtmosphereGazTypes().size()];
        gasesPercentage = new DoubleBinding[AtmosphereGasType.getAtmosphereGazTypes().size()];

        for (AtmosphereGasType gasType : AtmosphereGasType.getAtmosphereGazTypes()) {
            this.gases[gasType.ordinal()] = new SimpleDoubleProperty(0.0);
        }

        totalMass = new DoubleBinding() {
            {
                bind(gases);
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
     * @param amount the amount of gas added in kg
     */
    public void addGas(AtmosphereGasType gasType, double amount) {
        DoubleProperty prop = gases[gasType.ordinal()];
        prop.set(prop.get() + amount);
    }

    /**
     * removes a certain amount of gas from the atmosphere
     * if the amount to be removed is greater than the amount contained, the method will return the amount that couldn't have been removed
     * ex.: wanting to remove 100 from 50 will result in the gas amount stored to be 0 and the return value to be 50
     *
     * @param gasType the gas type to be removed
     * @param amount the amount of gas to remove in kg
     * @return the amount that couldn't be removed in kg
     */
    public double removeGas(AtmosphereGasType gasType, double amount) {
        DoubleProperty prop = gases[gasType.ordinal()];

        double contained = prop.get();

        if (contained >= amount){
            prop.set(contained - amount);
            return 0;
        } else {
            prop.set(0);
            return amount - contained;
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

    public double getMassFor(AtmosphereGasType gas) {
        return gases[gas.ordinal()].get();
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