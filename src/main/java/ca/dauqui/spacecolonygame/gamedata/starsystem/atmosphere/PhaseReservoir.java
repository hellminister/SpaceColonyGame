package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PhaseReservoir {
    private final ObservableMap<AtmosphereGasType, DoubleProperty> gases;
    private final Map<AtmosphereGasType, DoubleBinding> gasesPercentage;
    private final DoubleBinding totalMass;

    public PhaseReservoir() {
        gases = FXCollections.observableMap(new HashMap<>());
        gasesPercentage = new HashMap<>();
        totalMass = new TotalMass(gases);

    }

    /**
     * returns a Binding that gives the relative amount of the asked gas composing the atmosphere in a value from 0.0 to 1.0 (just multiply by 100 for percent)
     * will add a missing gas with amount of 0 in the main table if the gas is not present
     * @param atmosphereGasType the requested gas
     * @return a binding with the relative amount of the asked gas
     */
    public DoubleBinding getGasPercentage(AtmosphereGasType atmosphereGasType) {
        var prop = gasesPercentage.get(atmosphereGasType);
        if (prop == null) {
            addGas(atmosphereGasType, 0);
        }

        return gasesPercentage.computeIfAbsent(atmosphereGasType, _ ->
                new DoubleBinding() {
                    private final ReadOnlyDoubleProperty rawValue;
                    {
                        rawValue = gases.get(atmosphereGasType);
                        bind(rawValue, totalMass);
                    }

                    @Override
                    protected double computeValue() {
                        double total = totalMass.get();
                        return (total <= 0.0) ? 0.0 : rawValue.get() / total;
                    }
                });
    }

    /**
     * Adds a certain amount of this gas to the planets atmosphere
     * @param atmosphereGasType the gas added
     * @param amount the amount of gas added in kg
     */
    public void addGas(AtmosphereGasType atmosphereGasType, double amount) {
        DoubleProperty prop = gases.get(atmosphereGasType);
        if (prop == null){
            gases.put(atmosphereGasType, new SimpleDoubleProperty(amount));
        } else {
            prop.set(prop.get() + amount);
        }
    }

    /**
     * removes a certain amount of gas from the atmosphere
     * if the amount to be removed is greater than the amount contained, the method will return the amount that couldn't have been removed
     * ex.: wanting to remove 100 from 50 will result in the gas amount stored to be 0 and the return value to be 50
     * This method won't remove a gas from the table, but will put a missing one with a value of 0
     * @param atmosphereGasType the gas type to be removed
     * @param amount the amount of gas to remove in kg
     * @return the amount that couldn't be removed in kg
     */
    public double removeGas(AtmosphereGasType atmosphereGasType, double amount) {
        DoubleProperty prop = gases.get(atmosphereGasType);
        if (prop == null){
            addGas(atmosphereGasType, 0.0);
            return amount;
        }

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
        if (gases.isEmpty()) {
            return List.of();
        } else if ( gases.size() == 1) {
            return List.of(gases.keySet().iterator().next());
        }

        return gases.entrySet().stream()
                // Sort by value (DoubleProperty) in reverse order (descending)
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().get(), entry1.getValue().get()))
                // Map back to the keys (the gas types)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * @return whether there is an atmosphere or not
     */
    public boolean hasPhase(){
        return totalMass.get() > 0.0;
    }

    public DoubleBinding getTotalMass() {
        return totalMass;
    }

    public ObservableMap<AtmosphereGasType, DoubleProperty> getGases() {
        return gases;
    }

    public double getMassFor(AtmosphereGasType gas) {
        DoubleProperty mass = gases.get(gas);
        return (mass == null) ? 0.0 : mass.get();
    }
}