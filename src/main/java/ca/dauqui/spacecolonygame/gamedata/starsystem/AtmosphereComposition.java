package ca.dauqui.spacecolonygame.gamedata.starsystem;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the gases composing the planet's atmosphere as well as its pressure
 * It also calculates the atmosphere Toxicity, Corrosiveness, Greenhouse factor and Air pressure and surface temperature
 *
 * @author Jean-François Quintal
 */
public class AtmosphereComposition {

    private final ObservableMap<AtmosphereGasType, DoubleProperty> atmosphereGas;
    private final Map<AtmosphereGasType, DoubleBinding> atmosphereGasPercentage;
    private final DoubleBinding totalGasMass;
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
        this.atmosphereGas = FXCollections.observableMap(new HashMap<>());
        this.atmosphereGasPercentage = new HashMap<>();

        double conversionValue = ("Re".equals(radiusUnit)) ? 6371000 : 69911000; // meters
        double surfaceArea = 4 * Math.PI * Math.pow(radius * conversionValue, 2);

        this.totalGasMass = new AtmosphereMapTrackingBinding() {
            @Override
            protected double computeValue() {
                // Sum up all DoubleProperties currently in the map
                double sum = 0.0;
                for (var prop : atmosphereGas.values()) {
                    sum += prop.get();
                }
                return sum;
            }
        };

        this.pressure = new DoubleBinding() {
            {
                bind(totalGasMass);
            }

            @Override
            protected double computeValue() {
                return (totalGasMass.get() * gravity) / (surfaceArea * 100000);
            }
        };

        toxicity = new AtmosphericWeightedAverageBinding(AtmosphereGasType::toxicity);
        corrosiveness = new AtmosphericWeightedAverageBinding(AtmosphereGasType::corrosiveness);
        greenhouseFactor = new AtmosphericWeightedAverageBinding(AtmosphereGasType::greenhouseFactor);

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
     * returns a Binding that gives the relative amount of the asked gas composing the atmosphere in a value from 0.0 to 1.0 (just multiply by 100 for percent)
     * will add a missing gas with amount of 0 in the main table if the gas is not present
     * @param atmosphereGasType the requested gas
     * @return a binding with the relative amount of the asked gas
     */
    public DoubleBinding getAtmosphereGasPercentage(AtmosphereGasType atmosphereGasType) {
        var prop = atmosphereGasPercentage.get(atmosphereGasType);
        if (prop == null) {
            addGas(atmosphereGasType, 0);
        }

        return atmosphereGasPercentage.computeIfAbsent(atmosphereGasType, _ ->
                new DoubleBinding() {
                    private final ReadOnlyDoubleProperty rawValue;
                    {
                        rawValue = atmosphereGas.get(atmosphereGasType);
                        bind(rawValue, totalGasMass);
                    }

                    @Override
                    protected double computeValue() {
                        return rawValue.get() / totalGasMass.get();
                    }
                });
    }

    /**
     * Adds a certain amount of this gas to the planets atmosphere
     * @param atmosphereGasType the gas added
     * @param amount the amount of gas added in kg
     */
    public void addGas(AtmosphereGasType atmosphereGasType, double amount) {
        DoubleProperty prop = atmosphereGas.get(atmosphereGasType);
        if (prop == null){
            atmosphereGas.put(atmosphereGasType, new SimpleDoubleProperty(amount));
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
        DoubleProperty prop = atmosphereGas.get(atmosphereGasType);
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
        if (atmosphereGas.isEmpty()) {
            return List.of();
        } else if ( atmosphereGas.size() == 1) {
            return List.of(atmosphereGas.keySet().iterator().next());
        }

        return atmosphereGas.entrySet().stream()
                // Sort by value (DoubleProperty) in reverse order (descending)
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().get(), entry1.getValue().get()))
                // Map back to the keys (the gas types)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * @return whether there is an atmosphere or not
     */
    public boolean hasAtmosphere(){
        return totalGasMass.get() > 0.0;
    }

    /**
     * Base abstract binding that encapsulates dynamic tracking and listener recycling
     * over the atmospheric gases map entries. Slashes raw boilerplate.
     */
    private abstract class AtmosphereMapTrackingBinding extends DoubleBinding {
        protected final InvalidationListener entryListener = _ -> invalidate();

        protected AtmosphereMapTrackingBinding() {
            // Automatically registers listeners whenever a gas is added or removed from the simulation
            atmosphereGas.addListener((MapChangeListener<? super AtmosphereGasType, ? super DoubleProperty>) change -> {
                if (change.wasAdded()) {
                    change.getValueAdded().addListener(entryListener);
                }
                if (change.wasRemoved()) {
                    change.getValueRemoved().removeListener(entryListener);
                }
                invalidate();
            });
        }

        @Override
        public void dispose() {
            atmosphereGas.values().forEach(prop -> prop.removeListener(entryListener));
            super.dispose();
        }
    }

    /**
     * Specialized child binding that computes a weighted average score
     * for a given gas property using a functional extractor lambda.
     */
    private final class AtmosphericWeightedAverageBinding extends AtmosphereMapTrackingBinding {
        private final java.util.function.ToDoubleFunction<AtmosphereGasType> coefficientExtractor;

        public AtmosphericWeightedAverageBinding(java.util.function.ToDoubleFunction<AtmosphereGasType> extractor) {
            this.coefficientExtractor = extractor;
            bind(totalGasMass); // Automatically recomputes if global air density changes
        }

        @Override
        protected double computeValue() {
            double totalMass = totalGasMass.get();
            if (totalMass <= 0.0 || atmosphereGas.isEmpty()) {
                return 0.0;
            }

            double totalScore = 0.0;
            for (var entry : atmosphereGas.entrySet()) {
                AtmosphereGasType gas = entry.getKey();
                if (gas != null) {
                    totalScore += entry.getValue().get() * coefficientExtractor.applyAsDouble(gas);
                }
            }

            return totalScore / totalMass;
        }
    }
}