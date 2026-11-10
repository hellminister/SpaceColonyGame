package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.util.EnumLike;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.*;
import java.util.logging.Logger;

/**
 * Immutable data configuration record managing global telemetry hardware
 * and scanning probe profiles loaded from configuration registries.
 * <p>
 * Implements Localizable to dynamically link localized string properties
 * matching tracking identification keys.
 * </p>
 *
 * @param ordinal The unique zero-based sequential index allocated during parsing for array indexing
 * @param id      Unique alphanumeric token key identifying the satellite template type
 * @param effects Unmodifiable verification set containing all functional probe traits unlocked by this hardware
 * @param name    Reactive read-only string property tracking localized layout translation labels
 *
 * @author Jean-François Quintal
 */
@Localized
public record SatelliteType(
        int ordinal,
        String id,
        Set<ProbeAndSatelliteEffect> effects,
        ReadOnlyStringProperty name
    ) implements Localizable, EnumLike {
    private static final Logger LOG = Logger.getLogger(SatelliteType.class.getName());
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/satelliteType.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    /**
     * Typesafe central registry loading and isolating structured configuration parameters from disk.
     */
    private static final TypesafeConstants<SatelliteType> registry = new TypesafeConstants<>(CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, SatelliteType> targetMap) {
            Set<String> processedPrefixes = new HashSet<>();
            for (String key : props.stringPropertyNames()) {
                if (key.contains(".")) {
                    String prefix = key.substring(0, key.indexOf('.'));

                    if (processedPrefixes.add(prefix)) {
                        Set<ProbeAndSatelliteEffect> effects2 = new HashSet<>();
                        String effectsStr = props.getProperty(prefix + ".effects");

                        if (effectsStr != null && !effectsStr.isBlank()) {
                            for (String token : effectsStr.split(",")) {
                                try {
                                    effects2.add(ProbeAndSatelliteEffect.valueOf(token.trim().toUpperCase()));
                                } catch (IllegalArgumentException _) {
                                    LOG.severe(() -> "Unknown ProbeAndSatelliteEffect: " + token);
                                }
                            }
                        }

                        targetMap.put(prefix, new SatelliteType(nextOrdinal(), prefix, effects2));
                    }
                }
            }
        }

    };

    /**
     * Resolves a unique hardware template specification entry from the centralized data registry.
     *
     * @param id The target lookup alphanumeric identification key token string
     * @return The matched matching immutable SatelliteType registration profile instance (null if missing)
     */
    public static SatelliteType get(String id) {
        return registry.get(id.toUpperCase().trim());
    }

    /**
     * @return Complete unmodifiable collection snapshot holding all loaded hardware template definitions
     */
    public static Collection<SatelliteType> getAll() {
        return registry.getAll();
    }

    /**
     * Internal convenience builder constructor initializing transient structural properties
     * and auto-wiring localized string layout bundle properties.
     *
     * @param id      The target alphanumeric identification key token
     * @param effects The raw collection of data modifiers assigned to this profile
     */
    private SatelliteType(int ordinal, String id, Set<ProbeAndSatelliteEffect> effects) {
        this(ordinal, id, Collections.unmodifiableSet(effects), Localize.getProperty(ENUMS_BUNDLE, "SatelliteType." + id));
    }

    /**
     * Verifies if this hardware profile triggers specific scanning capability traits.
     *
     * @param effect The targeted scanning technology condition parameter to verify
     * @return True if the effect parameter resides inside the tracking capability set
     */
    public boolean hasEffect(ProbeAndSatelliteEffect effect) {
        return effects.contains(effect);
    }

    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return name;
    }

    @Override
    public String toString() {
        return id;
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