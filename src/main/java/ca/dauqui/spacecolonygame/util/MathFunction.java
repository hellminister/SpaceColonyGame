package ca.dauqui.spacecolonygame.util;

public enum MathFunction {
    /** y = ax + b -> params = [a, b] */
    LINEAR((x, p) -> {
        double a = p.length > 0 ? p[0] : 1.0;
        double b = p.length > 1 ? p[1] : 0.0;
        return (a * x) + b;
    }),

    /** y = ax² + bx + c -> params = [a, b, c] */
    PARABOLIC((x, p) -> {
        double a = p.length > 0 ? p[0] : 1.0;
        double b = p.length > 1 ? p[1] : 0.0;
        double c = p.length > 2 ? p[2] : 0.0;
        return (a * x * x) + (b * x) + c;
    }),

    /** Sigmoïde (Saturante) : y = L / (1 + e^(-k * (x - x0)))
     *  params = [L (max), k (pente), x0 (inflexion)] */
    SIGMOID((x, p) -> {
        double max = p.length > 0 ? p[0] : 1.0;
        double k = p.length > 1 ? p[1] : 1.0;
        double x0 = p.length > 2 ? p[2] : 0.0;
        return max / (1.0 + Math.exp(-k * (x - x0)));
    }),

    /** Polynomial d'ordre N : y = c0 + c1*x + c2*x² + c3*x³ + ... */
    POLYNOMIAL((x, p) -> {
        double result = 0.0;
        for (int i = 0; i < p.length; i++) {
            result += p[i] * Math.pow(x, i);
        }
        return result;
    });

    // L'interface fonctionnelle qui porte la formule mathématique
    @FunctionalInterface
    public interface Evaluator {
        double evaluate(double x, double[] params);
    }

    private final Evaluator evaluator;

    MathFunction(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Calcule le résultat de la fonction pour une valeur et des paramètres donnés.
     */
    public double calculate(double x, double[] params) {
        return evaluator.evaluate(x, params != null ? params : new double[0]);
    }
}