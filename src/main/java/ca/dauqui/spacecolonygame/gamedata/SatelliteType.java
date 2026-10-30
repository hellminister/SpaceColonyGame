package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.*;
import java.util.logging.Logger;

@Localized
public final class SatelliteType implements Localizable {
    private static final Logger LOG = Logger.getLogger(SatelliteType.class.getName());
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/satelliteType.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    private static final TypesafeConstants<SatelliteType> registry = new TypesafeConstants<>(SatelliteType.class, CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, SatelliteType> targetMap) {
            for (String key : props.stringPropertyNames()) {
                if (key.endsWith(".id")) {
                    String prefix = key.substring(0, key.indexOf('.'));
                    String id2 = props.getProperty(prefix + ".id").toUpperCase().trim();

                    Set<ProbeAndSatelliteEffect> effects2 = new HashSet<>();
                    String effectsStr = props.getProperty(prefix + ".effects");

                    if (effectsStr != null && !effectsStr.isBlank()) {
                        for (String token : effectsStr.split(",")) {
                            try {
                                effects2.add(ProbeAndSatelliteEffect.valueOf(token.trim().toUpperCase()));
                            } catch (IllegalArgumentException _) {
                                LOG.severe(() -> "Effet de sonde inconnu dans le fichier de config : " + token);
                            }
                        }
                    }

                    targetMap.put(id2, new SatelliteType(id2, effects2));
                }
            }
        }

        @Override
        protected void fallback(Map<String, SatelliteType> targetMap) {
            // Pas de fallback nécessaire, ou possibilité d'ajouter une sonde "vide"
        }
    };

    public static SatelliteType get(String id) {
        return registry.get(id.toUpperCase().trim());
    }

    public static Collection<SatelliteType> getAll() {
        return registry.getAll();
    }

    private final String id;
    private final Set<ProbeAndSatelliteEffect> effects;
    private final ReadOnlyStringProperty name;

    private SatelliteType(String id, Set<ProbeAndSatelliteEffect> effects) {
        this.id = id;
        this.effects = Collections.unmodifiableSet(effects);
        this.name = Localize.getProperty(ENUMS_BUNDLE, "SatelliteType." + id);
    }

    public String getId() {
        return id;
    }

    public Set<ProbeAndSatelliteEffect> getEffects() {
        return effects;
    }

    public boolean hasEffect(ProbeAndSatelliteEffect effect) {
        return effects.contains(effect);
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
        return name.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SatelliteType other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}