package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.*;
import java.util.logging.Logger;

@Localized
public final class AtmosphereComposition implements Localizable {
    private static final Logger LOG = Logger.getLogger(AtmosphereComposition.class.getName());
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/atmospheres.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    private static final TypesafeConstants<AtmosphereComposition> GAZ_LIST = new TypesafeConstants<>(AtmosphereComposition.class, CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, AtmosphereComposition> targetMap) {
            for (String key : props.stringPropertyNames()) {
                if (key.endsWith(".id")) {
                    String prefix = key.substring(0, key.indexOf('.'));
                    String id = props.getProperty(prefix + ".id");
                    double greenhouse = Double.parseDouble(props.getProperty(prefix + ".greenhouseFactor", "0.0"));
                    double toxicity = Double.parseDouble(props.getProperty(prefix + ".toxicity", "0.0"));
                    double corrosive = Double.parseDouble(props.getProperty(prefix + ".corrosiveness", "0.0"));
                    double minTemp = Double.parseDouble(props.getProperty(prefix + ".minTemp", "0.0"));
                    double maxTemp = Double.parseDouble(props.getProperty(prefix + ".maxTemp", "9999.0"));
                    boolean isGiant = Boolean.parseBoolean(props.getProperty(prefix + ".isGasGiantNative", "false"));


                    targetMap.put(id, new AtmosphereComposition(id, greenhouse, toxicity, corrosive, minTemp, maxTemp, isGiant));
                }
            }
        }

        @Override
        protected void fallback(Map<String, AtmosphereComposition> targetMap) {
            if ( !(targetMap.containsKey("NONE"))){
                targetMap.put("NONE", new AtmosphereComposition("NONE", 0.0, 0.0, 0.0, 0.0, 9999.0, false));
            }

            if ( !(targetMap.containsKey("HYDROGEN_HELIUM"))){
                targetMap.put("HYDROGEN_HELIUM", new AtmosphereComposition("HYDROGEN_HELIUM", 5.0, 0.2, 0.0, 0.0, 9999.0, true));
            }
        }
    };

    /**
     * Recherche un gaz adapté pour une géante gazeuse.
     */
    public static AtmosphereComposition findGasGiantAtmosphere() {
        return GAZ_LIST.getAll().stream()
                .filter(AtmosphereComposition::isGasGiantNative)
                .findFirst()
                // Repli de secours si aucun gaz n'est coché dans le fichier
                .orElseGet(() -> GAZ_LIST.getInternalMap().getOrDefault("HYDROGEN_HELIUM", GAZ_LIST.get("NONE")));
    }

    /**
     * Recherche le gaz tellurique dont la plage de température correspond à la planète.
     */
    public static List<AtmosphereComposition> findTelluricAtmosphere(double tempEquilibrium) {
        return GAZ_LIST.getAll().stream()
                .filter(g -> !g.isGasGiantNative() && !g.getId().equals("NONE"))
                .filter(g -> tempEquilibrium >= g.getMinTempEquilibrium() && tempEquilibrium < g.getMaxTempEquilibrium())
                .toList();
    }

    public static AtmosphereComposition getAtmosphereComposition(String id) {
        return GAZ_LIST.get(id);
    }

    public static Collection<AtmosphereComposition> getAtmosphereCompositions() {
        return GAZ_LIST.getAll();
    }

    private final String id;
    private final double greenhouseFactor;
    private final double toxicity;
    private final double corrosiveness;
    private final double minTempEquilibrium;
    private final double maxTempEquilibrium;
    private final boolean isGasGiantNative;
    private final ReadOnlyStringProperty name;

    private AtmosphereComposition(String id, double greenhouseFactor, double toxicity, double corrosiveness,
                                  double minTemp, double maxTemp, boolean isGasGiantNative) {
        this.id = id;
        this.greenhouseFactor = greenhouseFactor;
        this.toxicity = toxicity;
        this.corrosiveness = corrosiveness;
        this.minTempEquilibrium = minTemp;
        this.maxTempEquilibrium = maxTemp;
        this.isGasGiantNative = isGasGiantNative;
        this.name = Localize.getProperty(ENUMS_BUNDLE, "AtmosphereComposition." + id);
    }

    public String getId() { return id; }
    public double getGreenhouseFactor() { return greenhouseFactor; }
    public double getToxicity() { return toxicity; }
    public double getCorrosiveness() { return corrosiveness; }

    public double getMinTempEquilibrium() { return minTempEquilibrium; }
    public double getMaxTempEquilibrium() { return maxTempEquilibrium; }
    public boolean isGasGiantNative() { return isGasGiantNative; }

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
        if (!(obj instanceof AtmosphereComposition other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


}