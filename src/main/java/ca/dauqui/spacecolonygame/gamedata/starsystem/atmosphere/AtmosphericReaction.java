package ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere;

import ca.dauqui.spacecolonygame.util.EnumLike;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;

import java.util.*;
import java.util.logging.Logger;

/**
 * Immutable configuration record defining a stoichiometric chemical reaction within planetary atmospheres.
 * Parses reactant ratios, product outputs, thermal activation thresholds, and minimum partial pressure limits.
 * Implements {@link EnumLike} to support contiguous array indexing during kinetic calculations.
 *
 * @param ordinal             The unique zero-based sequential index allocated during parsing
 * @param id                  The unique alphanumeric database lookup key identifying the reaction
 * @param reactants           Unmodifiable map associating source gases with their stoichiometric coefficients
 * @param products            Unmodifiable map associating generated gases with their stoichiometric coefficients
 * @param minTemperature      Minimum ambient surface temperature required to ignite/sustain the reaction (Kelvin)
 * @param minPartialPressures Unmodifiable map defining the minimum partial pressure required per reactant (Bars)
 * @param reactionRate        Maximum fraction of the limiting reactant converted per simulation turn (0.0 to 1.0)
 *
 * @author Jean-François Quintal
 */
public record AtmosphericReaction(
        int ordinal,
        String id,
        Map<AtmosphereGasType, Integer> reactants,
        Map<AtmosphereGasType, Integer> products,
        double minTemperature,
        Map<AtmosphereGasType, Double> minPartialPressures,
        double reactionRate
) implements EnumLike {

    private static final Logger LOG = Logger.getLogger(AtmosphericReaction.class.getName());
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/reactions.properties";

    private static final TypesafeConstants<AtmosphericReaction> REACTIONS = new TypesafeConstants<>(CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, AtmosphericReaction> targetMap) {
            Set<String> processedPrefixes = new HashSet<>();

            for (String key : props.stringPropertyNames()) {
                if (key.contains(".")) {
                    String prefix = key.substring(0, key.indexOf('.'));

                    if (processedPrefixes.add(prefix)) {
                        Map<AtmosphereGasType, Integer> reactantsMap = parseStoichiometry(props.getProperty(prefix + ".reactants", ""), prefix);
                        Map<AtmosphereGasType, Integer> productsMap = parseStoichiometry(props.getProperty(prefix + ".products", ""), prefix);
                        Map<AtmosphereGasType, Double> pressuresMap = parsePressures(props.getProperty(prefix + ".minPartialPressures", ""), prefix);

                        double minTemp = Double.parseDouble(props.getProperty(prefix + ".minTemp", "0.0"));
                        double rate = Math.clamp(Double.parseDouble(props.getProperty(prefix + ".reactionRate", "0.1")), 0.0, 1.0);

                        if (!reactantsMap.isEmpty() && !productsMap.isEmpty()) {
                            targetMap.put(prefix, new AtmosphericReaction(
                                    nextOrdinal(),
                                    prefix,
                                    Collections.unmodifiableMap(reactantsMap),
                                    Collections.unmodifiableMap(productsMap),
                                    minTemp,
                                    Collections.unmodifiableMap(pressuresMap),
                                    rate
                            ));
                        } else {
                            LOG.warning(() -> "Skipping invalid reaction configuration (missing valid reactants or products): " + prefix);
                        }
                    }
                }
            }
        }

        private Map<AtmosphereGasType, Integer> parseStoichiometry(String raw, String reactionId) {
            Map<AtmosphereGasType, Integer> result = new HashMap<>();
            if (raw == null || raw.isBlank()) return result;

            for (String token : raw.split(",")) {
                String[] parts = token.trim().split(":");
                if (parts.length == 2) {
                    AtmosphereGasType gas = AtmosphereGasType.getAtmosphereGazType(parts[0].trim());
                    if (gas != null) {
                        result.put(gas, Integer.parseInt(parts[1].trim()));
                    } else {
                        LOG.warning(() -> "Unknown gas '" + parts[0].trim() + "' in reaction " + reactionId);
                    }
                }
            }
            return result;
        }

        private Map<AtmosphereGasType, Double> parsePressures(String raw, String reactionId) {
            Map<AtmosphereGasType, Double> result = new HashMap<>();
            if (raw == null || raw.isBlank()) return result;

            for (String token : raw.split(",")) {
                String[] parts = token.trim().split(":");
                if (parts.length == 2) {
                    AtmosphereGasType gas = AtmosphereGasType.getAtmosphereGazType(parts[0].trim());
                    if (gas != null) {
                        result.put(gas, Double.parseDouble(parts[1].trim()));
                    } else {
                        LOG.warning(() -> "Unknown gas '" + parts[0].trim() + "' in pressure thresholds for " + reactionId);
                    }
                }
            }
            return result;
        }
    };

    /**
     * @param id The unique reaction identifier
     * @return The corresponding AtmosphericReaction, or null if not found
     */
    public static AtmosphericReaction getReaction(String id) {
        return REACTIONS.get(id);
    }

    /**
     * @return An immutable collection containing all registered atmospheric chemical reactions
     */
    public static Collection<AtmosphericReaction> getAllReactions() {
        return REACTIONS.getAll();
    }
}