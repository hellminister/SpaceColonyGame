package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.*;
import java.util.logging.Logger;

@Localized
public final class AtmosphereGazType implements Localizable {
    private static final Logger LOG = Logger.getLogger(AtmosphereGazType.class.getName());
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/atmospheres.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    private static final TypesafeConstants<AtmosphereGazType> GAZ_LIST = new TypesafeConstants<>(AtmosphereGazType.class, CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, AtmosphereGazType> targetMap) {
            Set<String> processedPrefixes = new HashSet<>();
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
                        double molecularWeight = Double.parseDouble(props.getProperty(prefix + ".molecularWeight ", "0.0"));

                        targetMap.put(prefix, new AtmosphereGazType(prefix, greenhouse, toxicity2, corrosive, minTemp, maxTemp, isGiant, molecularWeight));
                    }
                }
            }
        }

        @Override
        protected void fallback(Map<String, AtmosphereGazType> targetMap) {
        }
    };

    /**
     * Recherche un gaz adapté pour une géante gazeuse.
     */
    public static List<AtmosphereGazType> findGasGiantAtmospheres() {
        return GAZ_LIST.getAll().stream()
                .filter(AtmosphereGazType::isGasGiantNative)
                .toList();
    }

    /**
     * Recherche le gaz tellurique dont la plage de température correspond à la planète.
     */
    public static List<AtmosphereGazType> findTelluricAtmosphere(double tempEquilibrium) {
        return GAZ_LIST.getAll().stream()
                .filter(g -> !g.getId().equals("NONE"))
                .filter(g -> tempEquilibrium >= g.getMinTempEquilibrium() && tempEquilibrium < g.getMaxTempEquilibrium())
                .toList();
    }

    public static AtmosphereGazType getAtmosphereGazType(String id) {
        return GAZ_LIST.get(id);
    }

    public static Collection<AtmosphereGazType> getAtmosphereGazTypes() {
        return GAZ_LIST.getAll();
    }

    private final String id;
    private final double greenhouseFactor;
    private final double toxicity;
    private final double corrosiveness;
    private final double minTempEquilibrium;
    private final double maxTempEquilibrium;
    private final boolean isGasGiantNative;
    private final double molecularWeight;
    private final ReadOnlyStringProperty name;

    private AtmosphereGazType(String id, double greenhouseFactor, double toxicity, double corrosiveness,
                              double minTemp, double maxTemp, boolean isGasGiantNative, double molecularWeight) {
        this.id = id;
        this.greenhouseFactor = greenhouseFactor;
        this.toxicity = toxicity;
        this.corrosiveness = corrosiveness;
        this.minTempEquilibrium = minTemp;
        this.maxTempEquilibrium = maxTemp;
        this.isGasGiantNative = isGasGiantNative;
        this.name = Localize.getProperty(ENUMS_BUNDLE, "AtmosphereGazType." + id);
        this.molecularWeight = molecularWeight;
    }

    public String getId() { return id; }
    public double getGreenhouseFactor() { return greenhouseFactor; }
    public double getToxicity() { return toxicity; }
    public double getCorrosiveness() { return corrosiveness; }

    public double getMinTempEquilibrium() {
        return minTempEquilibrium;
    }

    public double getMaxTempEquilibrium() {
        return maxTempEquilibrium;
    }

    public boolean isGasGiantNative() {
        return isGasGiantNative;
    }

    public double getMolecularWeight() {
        return molecularWeight;
    }

    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return name;
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AtmosphereGazType other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


}