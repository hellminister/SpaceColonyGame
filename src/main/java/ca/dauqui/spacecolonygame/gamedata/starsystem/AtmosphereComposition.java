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
 * This class contains the gazes composing the planet's atmosphere as well as its pressure
 * It also calculates the atmosphere Toxicity, Corrosiveness, Greenhouse factor and Air pressure
 */
public class AtmosphereComposition {

    private final ObservableMap<AtmosphereGazType, DoubleProperty> atmosphereGaz;
    private final Map<AtmosphereGazType, DoubleBinding> atmosphereGazPercentage;
    private final DoubleBinding totalGazMass;
    private final DoubleBinding pressure;
    private final DoubleBinding surfaceArea;
    private final DoubleBinding toxicity;
    private final DoubleBinding corrosiveness;
    private final DoubleBinding greenhouseFactor;

    /**
     * Initialize the class and prepares the variables bindings
     * Try using the gravity and radius object contained within the planet of this atmosphere
     * @param gravity The gravity of the planet
     * @param radius the radius of the planet
     * @param radiusUnit the radius unit used
     */
    public AtmosphereComposition(ReadOnlyDoubleProperty gravity, ReadOnlyDoubleProperty radius, String radiusUnit) {
        this.atmosphereGaz = FXCollections.observableMap(new HashMap<>());
        this.atmosphereGazPercentage = new HashMap<>();

        this.surfaceArea = new DoubleBinding() {
            private final double conversionValue;
            {
                bind(radius);
                if ("Re".equals(radiusUnit)) {
                    conversionValue = 6371000; // meters
                } else {
                    conversionValue = 69911000; // meters
                }
            }
            @Override
            protected double computeValue() {
                return 4 * Math.PI * Math.pow(radius.get() * conversionValue, 2);
            }
        };

        this.totalGazMass = new AtmosphereMapTrackingBinding() {
            @Override
            protected double computeValue() {
                // Sum up all DoubleProperties currently in the map
                return atmosphereGaz.values().stream()
                        .mapToDouble(DoubleProperty::get)
                        .sum();
            }
        };

        this.pressure = new DoubleBinding() {
            {
                bind(totalGazMass, surfaceArea, gravity);
            }

            @Override
            protected double computeValue() {
                return (totalGazMass.get() * gravity.get()) / (surfaceArea.get() * 100000);
            }
        };

        toxicity = new AtmosphericWeightedAverageBinding(AtmosphereGazType::getToxicity);
        corrosiveness = new AtmosphericWeightedAverageBinding(AtmosphereGazType::getCorrosiveness);
        greenhouseFactor = new AtmosphericWeightedAverageBinding(AtmosphereGazType::getGreenhouseFactor);
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
     * returns a Binding that gives the relative amount of the asked gaz composing the atmosphere in a value from 0.0 to 1.0 (just multiply by 100 for percent)
     * will add a missing gaz with amount of 0 in the main table if the gaz is not present
     * @param atmosphereGazType the requested gaz
     * @return a binding with the relative amount of the asked gaz
     */
    public DoubleBinding getAtmosphereGazPercentage(AtmosphereGazType atmosphereGazType) {
        var prop = atmosphereGazPercentage.get(atmosphereGazType);
        if (prop == null) {
            addGaz(atmosphereGazType, 0);
        }

        return atmosphereGazPercentage.computeIfAbsent(atmosphereGazType, _ ->
                new DoubleBinding() {
                    private final ReadOnlyDoubleProperty rawValue;
                    {
                        rawValue = atmosphereGaz.get(atmosphereGazType);
                        bind(rawValue, totalGazMass);
                    }

                    @Override
                    protected double computeValue() {
                        return rawValue.get() / totalGazMass.get();
                    }
                });
    }

    /**
     * Adds a certain amount of this gaz to the planets atmosphere
     * @param atmosphereGazType the gaz added
     * @param amount the amount of gaz added in kg
     */
    public void addGaz(AtmosphereGazType atmosphereGazType, double amount) {
        DoubleProperty prop = atmosphereGaz.get(atmosphereGazType);
        if (prop == null){
            atmosphereGaz.put(atmosphereGazType, new SimpleDoubleProperty(amount));
        } else {
            prop.set(prop.get() + amount);
        }
    }

    /**
     * removes a certain amount of gaz from the atmosphere
     * if the amount to be removed is greater than the amount contained, the method will return the amount that couldn't have been removed
     * ex.: wanting to remove 100 from 50 will result in the gaz amount stored to be 0 and the return value to be 50
     * This method won't remove a gaz from the table, but will put a missing one with a value of 0
     * @param atmosphereGazType the gaz type to be removed
     * @param amount the amount of gaz to remove in kg
     * @return the amount that couldn't be removed in kg
     */
    public double removeGaz(AtmosphereGazType atmosphereGazType, double amount) {
        DoubleProperty prop = atmosphereGaz.get(atmosphereGazType);
        if (prop == null){
            addGaz(atmosphereGazType, 0.0);
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
     * @return a sorted list of AtmosphereGazType from highest to lowest concentration
     */
    public List<AtmosphereGazType> getGasesSortedByConcentration() {
        if (atmosphereGaz.isEmpty()) {
            return List.of();
        } else if ( atmosphereGaz.size() == 1) {
            return List.of(atmosphereGaz.keySet().iterator().next());
        }

        return atmosphereGaz.entrySet().stream()
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
        return totalGazMass.get() > 0.0;
    }

    /**
     * Base abstract binding that encapsulates dynamic tracking and listener recycling
     * over the atmospheric gases map entries. Slashes raw boilerplate.
     */
    private abstract class AtmosphereMapTrackingBinding extends DoubleBinding {
        protected final InvalidationListener entryListener = _ -> bind();

        protected AtmosphereMapTrackingBinding() {
            // Automatically registers listeners whenever a gas is added or removed from the simulation
            atmosphereGaz.addListener((MapChangeListener<? super AtmosphereGazType, ? super DoubleProperty>) change -> {
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
            atmosphereGaz.values().forEach(prop -> prop.removeListener(entryListener));
            super.dispose();
        }
    }

    /**
     * Specialized child binding that computes a weighted average score
     * for a given gas property using a functional extractor lambda.
     */
    private final class AtmosphericWeightedAverageBinding extends AtmosphereMapTrackingBinding {
        private final java.util.function.ToDoubleFunction<AtmosphereGazType> coefficientExtractor;

        public AtmosphericWeightedAverageBinding(java.util.function.ToDoubleFunction<AtmosphereGazType> extractor) {
            this.coefficientExtractor = extractor;
            bind(totalGazMass); // Automatically recomputes if global air density changes
        }

        @Override
        protected double computeValue() {
            double totalMass = totalGazMass.get();
            if (totalMass <= 0.0 || atmosphereGaz.isEmpty()) {
                return 0.0;
            }

            double totalScore = atmosphereGaz.entrySet().stream()
                    .filter(entry -> entry.getKey() != null)
                    .mapToDouble(entry -> entry.getValue().get() * coefficientExtractor.applyAsDouble(entry.getKey()))
                    .sum();

            return totalScore / totalMass;
        }
    }
}