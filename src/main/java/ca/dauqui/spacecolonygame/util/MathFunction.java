package ca.dauqui.spacecolonygame.util;

/**
 * High-performance mathematical modelling engine defining core exoplanetary generation curves.
 * Employs a fast-fail philosophy to expose structural configuration errors immediately.
 */
public enum MathFunction {

    /**
     * Linear Progression Curve: y = ax + b
     * <p>
     * Parameters:
     * p0: a - slope (Required)
     * p1: b - offset (Required)
     */
    LINEAR((x, p) -> (p[0] * x) + p[1]),

    /**
     * Parabolic Distribution Curve: y = ax² + bx + c
     * <p>
     * Parameters:
     * p0: a - curvature coefficient (Required)
     * p1: b - linear coefficient (Required)
     * p2: c - vertical shift offset (Required)
     */
    PARABOLIC((x, p) -> (p[0] * x * x) + (p[1] * x) + p[2]),

    /**
     * Saturating Sigmoid Curve: y = L / (1 + e^(-k * (x - x0)))
     * <p>
     * Parameters:
     * p0: L - maximum ceiling asymptote (Required)
     * p1: k - logistic growth rate slope (Required)
     * p2: x0 - sigmoid midpoint inflexion centre (Required)
     */
    SIGMOID((x, p) -> p[0] / (1.0 + Math.exp(-p[1] * (x - p[2])))),

    /**
     * Dynamic Polynomial Curve of Order N: y = c0 + c1*x + c2*x² + c3*x³ + ...
     * Processes raw primitive arrays directly to bypass iterative exponent strain.
     * <p>
     * Parameters:
     * p0...pN: coefficients for each successive power of x starting from 0 (At least one required)
     */
    POLYNOMIAL((x, p) -> {
        double result = 0.0;
        double currentPower = 1.0;
        for (double coefficient : p) {
            result += coefficient * currentPower;
            currentPower *= x;
        }
        return result;
    });

    /**
     * Functional layout bridge executing raw algebraic algorithms.
     */
    @FunctionalInterface
    public interface Evaluator {
        /**
         * Resolves the curve outcome utilizing dynamic variable parameters.
         *
         * @param x      The active environmental metric input scalar
         * @param params The configuration coefficient payload array
         * @return The calculated outcome scale value
         */
        double evaluate(double x, double[] params);
    }

    private final Evaluator evaluator;

    MathFunction(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Resolves the function result for a given value and configuration parameters.
     * Direct pointer passthrough optimized for maximum simulation throughput.
     *
     * @param x      The target configuration property input to test
     * @param params The evaluation parameters extracted from data loaders
     * @return The final parsed evaluation score
     */
    public double calculate(double x, double[] params) {
        return evaluator.evaluate(x, params);
    }
}