package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class represents a possible gas for an atmosphere
 * The data for this class is found in the file specified in the CONFIG_PATH
 *
 * @author Jean-François Quintal
 */
@Localized
public record AtmosphereGasType(
        String id,
        double greenhouseFactor,
        double toxicity,
        double corrosiveness,
        double minTempEquilibrium,
        double maxTempEquilibrium,
        boolean isGasGiantNative,
        double molecularWeight,
        double antoineA,
        double antoineB,
        double antoineC,
        ReadOnlyStringProperty name
) implements Localizable {
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/atmospheres.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    private static final TypesafeConstants<AtmosphereGasType> GAS_LIST = new TypesafeConstants<>(CONFIG_PATH) {

        public static final String HELIUM = "HELIUM";
        public static final String HYDROGEN = "HYDROGEN";

        @Override
        protected void parseProperties(Properties props, Map<String, AtmosphereGasType> targetMap) {
            var processedPrefixes = new HashSet<String>();
            for (String key : props.stringPropertyNames()) {
                if (key.contains(".")) {
                    String prefix = key.substring(0, key.indexOf('.'));

                    if (processedPrefixes.add(prefix)) {
                        double greenhouse = Double.parseDouble(props.getProperty(prefix + ".greenhouseFactor", "0.0"));
                        double toxicity2 = Double.parseDouble(props.getProperty(prefix + ".toxicity", "0.0"));
                        double corrosive = Double.parseDouble(props.getProperty(prefix + ".corrosiveness", "0.0"));
                        double minTemp = Double.parseDouble(props.getProperty(prefix + ".minTemp", "0.0"));
                        double maxTemp = Double.parseDouble(props.getProperty(prefix + ".maxTemp", "9999.0"));
                        boolean isGiant = Boolean.parseBoolean(props.getProperty(prefix + ".isGasGiantNative", "false"));
                        double molecularWeight2 = Double.parseDouble(props.getProperty(prefix + ".molecularWeight ", "0.0"));
                        double antA = Double.parseDouble(props.getProperty(prefix + ".antoineA", "4.0"));
                        double antB = Double.parseDouble(props.getProperty(prefix + ".antoineB", "500.0"));
                        double antC = Double.parseDouble(props.getProperty(prefix + ".antoineC", "0.0"));


                        targetMap.put(prefix, new AtmosphereGasType(prefix, greenhouse, toxicity2, corrosive, minTemp, maxTemp, isGiant, molecularWeight2, antA, antB, antC));
                    }
                }
            }
        }

        @Override
        protected void fallback(Map<String, AtmosphereGasType> targetMap) {
            // ENFORCING CRITICAL COSMOLOGICAL CONSTANTS (Failsafe fallback injection)

            // Re-injecting foundational Hydrogen baseline parameters if missing from disk
            targetMap.computeIfAbsent(HYDROGEN, _ -> new AtmosphereGasType(HYDROGEN, 0.0, 0.0, 0.0, 0.0, 9999.0, true, 2.016, 3.543, 99.3, 2.5));

            // Re-injecting foundational Helium baseline parameters if missing from disk
            targetMap.computeIfAbsent(HELIUM, _ -> new AtmosphereGasType(HELIUM, 0.0, 0.0, 0.0, 0.0, 9999.0, true, 4.0026, 2.19, 12.2, 0.0));
        }
    };

    /**
     * Calculates the saturation vapour pressure for this gas at a specific temperature using Antoine's equation.
     * @param temperatureKelvin The current ambient surface temperature
     * @return Saturation pressure in Bars
     */
    public double calculateSaturationPressure(double temperatureKelvin) {
        if (temperatureKelvin <= 0 || (temperatureKelvin + antoineC) <= 0) return 0.0;
        double logP = antoineA - (antoineB / (temperatureKelvin + antoineC));
        return Math.pow(10, logP);
    }

    /**
     * Returns a list of all available gazes for gas giants
     * @return a list of all available gases for a gas giant
     */
    public static List<AtmosphereGasType> findGasGiantAtmospheres() {
        return GAS_LIST.getAll().stream()
                .filter(AtmosphereGasType::isGasGiantNative)
                .toList();
    }

    /**
     * returns a list of all available gases that can exists in an atmosphere of a given temperature
     * @param tempEquilibrium the planets temperature
     * @return the list of acceptable gases for the given temperature
     */
    public static List<AtmosphereGasType> findTelluricAtmosphere(double tempEquilibrium) {
        return GAS_LIST.getAll().stream()
                .filter(g -> !g.id().equals("NONE"))
                .filter(g -> tempEquilibrium >= g.minTempEquilibrium() && tempEquilibrium < g.maxTempEquilibrium())
                .toList();
    }

    /**
     * Gives the gaz corresponding to the given id
     * @param id the gaz id
     * @return the corresponding gaz or null if none with that id
     */
    public static AtmosphereGasType getAtmosphereGazType(String id) {
        return GAS_LIST.get(id);
    }

    /**
     * returns all the existing gazes
     * @return An immutable collection containing all available gazes
     */
    public static Collection<AtmosphereGasType> getAtmosphereGazTypes() {
        return GAS_LIST.getAll();
    }

    private AtmosphereGasType(String id, double greenhouseFactor, double toxicity, double corrosiveness,
                              double minTemp, double maxTemp, boolean isGasGiantNative, double molecularWeight, double antoineA, double antoineB, double antoineC) {
        this(id, greenhouseFactor, toxicity, corrosiveness, minTemp, maxTemp, isGasGiantNative, molecularWeight, antoineA, antoineB, antoineC,
                Localize.getProperty(ENUMS_BUNDLE, "AtmosphereGasType." + id));
    }

    /**
     * this will result in the same thing as calling name()
     * @return the ReadOnlyStringProperty containing a localized name for this gas
     */
    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return name;
    }

}