package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import javafx.beans.binding.DoubleBinding;

/**
 * Specialized structural binding computing a real-time weighted average score
 * for localized planetary air property constants across active phase dimensions.
 * Direct dependency attachments bypass traditional structural iterations to yield ultra-low latency updates.
 *
 * @author Jean-François Quintal
 */
final class AtmosphericWeightedAverageBinding extends DoubleBinding{
    private final java.util.function.ToDoubleFunction<AtmosphereGasType> coefficientExtractor;
    private final PhaseReservoir reservoir;

    /**
     * Spans a reactive tracking connection mapping raw structural property values
     * against dynamic total mass denominator changes.
     *
     * @param reservoir The target PhaseReservoir composition source to track
     * @param extractor Functional criteria mapping coefficient extractions from chemical assets
     */
    public AtmosphericWeightedAverageBinding(PhaseReservoir reservoir,
                                             java.util.function.ToDoubleFunction<AtmosphereGasType> extractor) {
        this.reservoir = reservoir;
        this.coefficientExtractor = extractor;
        bind(reservoir.gasesValues());
    }

    @Override
    protected double computeValue() {
        double totalMass = reservoir.getTotalMass().get();
        if (totalMass <= 0.0) {
            return 0.0;
        }

        double totalScore = 0.0;
        for (var gas : AtmosphereGasType.getAtmosphereGazTypes()) {
                totalScore += reservoir.getMassFor(gas) * coefficientExtractor.applyAsDouble(gas);
        }

        return totalScore / totalMass;
    }
}