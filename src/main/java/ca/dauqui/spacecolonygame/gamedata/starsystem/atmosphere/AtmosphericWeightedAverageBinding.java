package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.beans.binding.DoubleBinding;

/**
 * Specialized child binding that computes a weighted average score
 * for a given gas property using a functional extractor lambda.
 */
final class AtmosphericWeightedAverageBinding extends AtmosphereMapTrackingBinding {
    private final java.util.function.ToDoubleFunction<AtmosphereGasType> coefficientExtractor;
    private final DoubleBinding totalMassBinding;

    public AtmosphericWeightedAverageBinding(PhaseReservoir reservoir,
                                             java.util.function.ToDoubleFunction<AtmosphereGasType> extractor) {
        super(reservoir.getGases());
        totalMassBinding = reservoir.getTotalMass();
        this.coefficientExtractor = extractor;
        bind(totalMassBinding); // Automatically recomputes if global air density changes
    }

    @Override
    protected double computeValue() {
        double totalMass = totalMassBinding.get();
        if (totalMass <= 0.0 || bound.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        for (var entry : bound.entrySet()) {
            AtmosphereGasType gas = entry.getKey();
            if (gas != null) {
                totalScore += entry.getValue().get() * coefficientExtractor.applyAsDouble(gas);
            }
        }

        return totalScore / totalMass;
    }
}