package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.tiledata.TerrainType;
import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.settings.GameParameters;
import ca.dauqui.spacecolonygame.util.EnumLike;
import ca.dauqui.spacecolonygame.util.MathFunction;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a specific mineral or energy resource type available within the simulation framework.
 * This record encapsulates the data structures, multifactorial mathematical curves, and geological
 * parameters required to procedurally distribute raw deposits across colonial site grids.
 * <p>
 * Sourced dynamically from external files to allow comprehensive modifier configuration overrides,
 * mirroring advanced hard-science space simulation layouts.
 * </p>
 *
 * @param ordinal    The unique zero-based sequential index allocated during parsing for array indexing
 * @param id         The unique operational string token identifier tracking this resource type
 * @param parameters The underlying geological constraints and environmental behaviour parameters
 * @param name       The reactive localized string property layer bound to user interface frameworks
 *
 * @author Jean-François Quintal
 */
@Localized
public record ResourceType(
        int ordinal,
        String id,
        GenerationParameters parameters,
        ReadOnlyStringProperty name
) implements Localizable, EnumLike {

    /**
     * Decentralized registry map capturing all parsed resource configurations at application boot.
     * Implements a qualitative cascading strategy mapping tokens to matching record instances.
     */
    private static final TypesafeConstants<ResourceType> tags = new TypesafeConstants<>(GameParameters.StringParam.RESOURCE_TYPE_DATA_PATH.get()) {
        @Override
        protected void parseProperties(Properties props, Map<String, ResourceType> targetMap) {
            Set<String> processedPrefixes = new HashSet<>();

            // Iterates through property namespaces to group individual resource parameters cleanly
            for (String key : props.stringPropertyNames()) {
                if (key.contains(".")) {
                    String prefix = key.substring(0, key.indexOf('.'));

                    // Blocks redundant parsing loops over already processed resource configurations
                    if (processedPrefixes.add(prefix)) {
                        GenerationParameters parameters2 = null;
                        String naturallyOccurring = props.getProperty(prefix + ".naturallyOccurring");

                        if ("true".equals(naturallyOccurring)) {
                            parameters2 = parseGenerationParameters(props, prefix);
                        }

                        // Employs the private utility constructor to inject localized UI bindings natively
                        targetMap.put(prefix, new ResourceType(nextOrdinal(), prefix, parameters2));
                    }
                }
            }
        }
    };

    /**
     * Analyses, extracts, and compiles the complex continuous mathematical curves and
     * terrain constraints for a naturally occurring planetary resource.
     *
     * @param props  The unified properties configuration table loaded from disk
     * @param prefix The targeted resource namespace key token (e.g., "ORE_A")
     * @return A fully compiled, immutable sub-record encapsulating distribution constraints
     */
    private static GenerationParameters parseGenerationParameters(Properties props, String prefix) {
        int minPerSite = Integer.parseInt(props.getProperty(prefix + ".minPerSite", "0"));

        // Extracts the collection of compatible terrain profiles (e.g., EASY, HARD)
        Set<TerrainType> allowedTerrains = EnumSet.noneOf(TerrainType.class);
        String appearsOnStr = props.getProperty(prefix + ".appearsOn");
        if (appearsOnStr != null) {
            for (String s : appearsOnStr.split(",")) {
                allowedTerrains.add(TerrainType.valueOf(s.trim().toUpperCase()));
            }
        } else {
            allowedTerrains.add(TerrainType.HARD); // Robust engine default fallback
        }

        String baseMinAmountSt = props.getProperty(prefix + ".baseMinAmount");
        double baseMinAmount = baseMinAmountSt != null ? Double.parseDouble(baseMinAmountSt.trim()) : 500;

        String baseMaxAmountSt = props.getProperty(prefix + ".baseMaxAmount");
        double baseMaxAmount = baseMaxAmountSt != null ? Double.parseDouble(baseMaxAmountSt.trim()) : 500;


        // Dynamically builds the multifactorial planetary influence matrix map
        Map<PlanetaryAttribute, InfluenceModel> influences = new EnumMap<>(PlanetaryAttribute.class);
        String influenceMatchPrefix = prefix + ".influences.";

        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(influenceMatchPrefix) && key.endsWith(".type")) {
                instantiateInfluence(props, prefix, key, influenceMatchPrefix, influences);
            }
        }

        return new GenerationParameters(minPerSite, baseMinAmount, baseMaxAmount, allowedTerrains, influences);
    }

    /**
     * Dynamically instantiates a single mathematical curve profile and injects it
     * into the multifactorial planetary influence matrix map.
     * Parses continuous parameters directly from comma-separated property values.
     *
     * @param props                  The unified properties table loaded from configuration files
     * @param prefix                 The targeted resource namespace key token (e.g., "ORE_B")
     * @param key                    The fully qualified property key string marking the curve type definition
     * @param influenceMatchPrefix   The structural text prefix used to locate the specific attribute name
     * @param influences             The operational map registry tracking compiled planetary influence behaviours
     */
    private static void instantiateInfluence(Properties props, String prefix, String key, String influenceMatchPrefix, Map<PlanetaryAttribute, InfluenceModel> influences) {
        // Extracts the planetary attribute keyword enum entry (e.g., "TECTONIC") by isolating the token substring
        PlanetaryAttribute attrName = PlanetaryAttribute.valueOf(key.substring(influenceMatchPrefix.length(), key.indexOf(".type")).toUpperCase());

        String typeStr = props.getProperty(key).toUpperCase().trim();
        MathFunction func = MathFunction.valueOf(typeStr);

        // Parses the continuous array parameters (e.g., coefficients like "4.0, 2.0, 1.0")
        String paramsStr = props.getProperty(prefix + ".influences." + attrName.name() + ".params", "");
        double[] params = new double[0];
        if (!paramsStr.isBlank()) {
            String[] tokens = paramsStr.split(",");
            params = new double[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                params[i] = Double.parseDouble(tokens[i].trim());
            }
        }

        // Stores the fully validated mathematical model directly into the resource influence layout
        influences.put(attrName, new InfluenceModel(func, params));
    }

    private static final Set<ResourceType> naturallyOccurringResources;
    static {
        var set = tags.getAll().stream().filter(ResourceType::isNaturallyOccurring).collect(Collectors.toSet());
        naturallyOccurringResources = Set.copyOf(set);
    }

    /**
     * @return the set of naturally occurring ressources available
     */
    public static Set<ResourceType> getNaturallyOccurringResources() {
        return naturallyOccurringResources;
    }

    /**
     * Returns the RessourceType corresponding to the given id
     * @param id the id of the ressource type
     * @return the RessourceType or null if it doesn't exist
     */
    public static ResourceType get(String id) {
        return tags.get(id);
    }

    /**
     * @return A collection of all available ResourceType
     */
    public static Collection<ResourceType> getAllResources() {
        return tags.getAll();
    }

    /**
     * Package-private canonical constructor overload mapping the localized text property pipeline.
     *
     * @param ordinal    The unique zero-based sequential index allocated during properties bootup
     * @param id         The unique alphanumeric database lookup key matching the property prefix
     * @param parameters The compiled structural generation models assigned to this resource type
     */
    private ResourceType(int ordinal, String id, GenerationParameters parameters) {
        this(ordinal, id, parameters, Localize.getProperty(GameParameters.StringParam.LOCALIZATION_ENUMS_BUNDLE.get(), "ResourceType." + id));
    }

    /**
     * @return whether this ResourceType is naturally occurring
     */
    public boolean isNaturallyOccurring() { return parameters != null; }

    /**
     * @return the minimum amount to be generated by a site
     */
    public int minPerSite() {
        return parameters != null ? parameters.minPerSite : 0;
    }

    /**
     * @param terrainTypeName the TerrainType to be checked
     * @return whether the giving TerrainType allows for this ResourceType
     */
    public boolean allowedOnTerrain(TerrainType terrainTypeName) {
        return parameters != null && parameters.appearsOn.contains(terrainTypeName);
    }

    /**
     * Evaluates the continuous mathematical curve of a specified planetary environmental factor
     * to determine its final generation score modifier for this resource.
     * <p>
     * The resulting scalar output serves as a direct multiplier affecting both total vein
     * counts and local deposit richness parameters during procedural map instantiation.
     * </p>
     *
     * @param attributeName         The targeted environmental property metric keyword (e.g., VOLCANISM)
     * @param planetAttributeValue  The raw physical magnitude value measured on the host planet
     * @return The calculated continuous curve multiplier, or 0.0 if no influence is defined
     */
    public double influenceScoreFor(PlanetaryAttribute attributeName, double planetAttributeValue) {
        if (parameters == null){
            return 0.0;
        }
        InfluenceModel model = parameters.influences.get(attributeName);
        return model != null ? model.function().calculate(planetAttributeValue, model.parameters()) : 0.0;
    }

    /**
     * @return the set of influencing factors for the appearance of this ResourceType
     */
    public Set<PlanetaryAttribute> influencingAttributes() {
        return parameters != null ? parameters.influences.keySet() : Collections.emptySet();
    }

    @Override public ReadOnlyStringProperty localizedProperty() {
        return name;
    }


    @Override
    public String toString() {
        return id;
    }

    @Override public boolean equals(Object obj) { return obj instanceof ResourceType objI && id.equals((objI).id); }
    @Override public int hashCode() { return id.hashCode(); }

    /**
     * Internal metadata bundle capturing the complete geological and procedural layout
     * constraints for a naturally occurring planetary resource.
     * Guaranteed to preserve immutable configuration properties across all simulation threads.
     *
     * @param minPerSite  The minimum number of mandatory veins that must spawn on a valid landing grid
     * @param baseMinAmount The baseline lower boundary quantity for raw ore volume per vein pocket
     * @param baseMaxAmount The baseline upper boundary quantity for raw ore volume per vein pocket
     * @param appearsOn   The set of allowable surface terrain classification categories where deposits can manifest
     * @param influences  The structural map pairing planetary physical attributes with their specific curve models
     */
    private record GenerationParameters(
            int minPerSite,
            double baseMinAmount,
            double baseMaxAmount,
            Set<TerrainType> appearsOn,
            Map<PlanetaryAttribute, InfluenceModel> influences
    ) {}

    /**
     * Encapsulates a distinct mathematical curve function paired with its array coefficients.
     * Optimized to pass parameters directly to high-throughput evaluation routines.
     *
     * @param function   The operational curve behaviour algorithm to execute (LINEAR, SIGMOID, etc.)
     * @param parameters The continuous coefficient payload array driving the algebraic equations
     */
    private record InfluenceModel(
            MathFunction function,
            double[] parameters
    ) {

        /**
         * Compact constructor implementing defensive cloning over primitive arrays.
         * Explicitly breaks external reference tracking links to isolate internal physics configurations.
         *
         * @param function   The operational curve behaviour algorithm to execute
         * @param parameters The continuous coefficient payload array driving the algebraic equations
         */
        public InfluenceModel(MathFunction function, double[] parameters) {
            this.function = function;
            this.parameters = parameters.clone();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof InfluenceModel(MathFunction function1, double[] parameters1))) return false;
            return Objects.deepEquals(parameters, parameters1) && function == function1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(function, Arrays.hashCode(parameters));
        }

        @Override
        public String toString() {
            return "InfluenceModel{" +
                    "function=" + function +
                    ", parameters=" + Arrays.toString(parameters) +
                    '}';
        }
    }
}