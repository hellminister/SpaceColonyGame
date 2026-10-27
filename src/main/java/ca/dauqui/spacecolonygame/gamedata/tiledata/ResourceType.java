package ca.dauqui.spacecolonygame.gamedata.tiledata;

import ca.dauqui.spacecolonygame.gamedata.PlanetaryAttribute;
import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.util.MathFunction;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Localized
public final class ResourceType implements Localizable {
    private static final Logger LOG = Logger.getLogger(ResourceType.class.getName());
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/resourceType.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    private static final TypesafeConstants<ResourceType> tags = new TypesafeConstants<>(ResourceType.class, CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, ResourceType> targetMap) {
            for (String key : props.stringPropertyNames()) {
                if (key.endsWith(".id")) {
                    String prefix = key.substring(0, key.indexOf('.'));
                    String id2 = props.getProperty(prefix + ".id").toUpperCase().trim();

                    GenerationParameters parameters2 = null;
                    String naturallyOccurring = props.getProperty(prefix + ".naturallyOccurring");

                    if ("true".equals(naturallyOccurring)) {
                        parameters2 = parseGenerationParameters(props, prefix);
                    }

                    targetMap.put(id2, new ResourceType(id2, parameters2));
                }
            }
        }

        @Override
        protected void fallback(Map<String, ResourceType> targetMap) {
            // no resource, empty map
        }
    };

    /**
     * Analyse et extrait les règles mathématiques et géologiques pour une ressource naturelle.
     */
    private static GenerationParameters parseGenerationParameters(Properties props, String prefix) {
        int minPerSite = Integer.parseInt(props.getProperty(prefix + ".minPerSite", "0"));

        // Extraction de la liste des terrains admissibles (EASY, HARD...)
        Set<TerrainType> allowedTerrains = EnumSet.noneOf(TerrainType.class);
        String appearsOnStr = props.getProperty(prefix + ".appearsOn");
        if (appearsOnStr != null) {
            for (String s : appearsOnStr.split(",")) {
                allowedTerrains.add(TerrainType.valueOf(s.trim().toUpperCase()));
            }
        } else {
            allowedTerrains.add(TerrainType.HARD); // Valeur par défaut
        }

        String baseMinAmountSt = props.getProperty(prefix + ".baseMinAmount");
        double baseMinAmount = baseMinAmountSt != null ? Double.parseDouble(baseMinAmountSt.trim()) : 500;

        String baseMaxAmountSt = props.getProperty(prefix + ".baseMaxAmount");
        double baseMaxAmount = baseMinAmountSt != null ? Double.parseDouble(baseMaxAmountSt.trim()) : 500;


        // Extraction dynamique de la liste des influences mathématiques
        Map<PlanetaryAttribute, InfluenceModel> influences = new EnumMap<>(PlanetaryAttribute.class);
        String influenceMatchPrefix = prefix + ".influences.";

        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(influenceMatchPrefix) && key.endsWith(".type")) {
                // Découpage pour isoler le nom de l'attribut (ex: "VOLCANISM")
                PlanetaryAttribute attrName = PlanetaryAttribute.valueOf(key.substring(influenceMatchPrefix.length(), key.indexOf(".type")).toUpperCase());

                String typeStr = props.getProperty(key).toUpperCase().trim();
                MathFunction func = MathFunction.valueOf(typeStr);

                // Parsing de la liste de double, ex: "-4.0, 4.0, 0.0"
                String paramsStr = props.getProperty(prefix + ".influences." + attrName.name() + ".params", "");
                double[] params = new double[0];
                if (!paramsStr.isBlank()) {
                    String[] tokens = paramsStr.split(",");
                    params = new double[tokens.length];
                    for (int i = 0; i < tokens.length; i++) {
                        params[i] = Double.parseDouble(tokens[i].trim());
                    }
                }

                influences.put(attrName, new InfluenceModel(func, params));
            }
        }

        return new GenerationParameters(minPerSite, baseMinAmount, baseMaxAmount, allowedTerrains, influences);
    }

    private static final Set<ResourceType> naturallyOccurringResources;
    static {
        var set = tags.getAll().stream().filter(ResourceType::isNaturallyOccurring).collect(Collectors.toSet());
        naturallyOccurringResources = Set.copyOf(set);
    }

    public static Set<ResourceType> getNaturallyOccurringResources() {
        return naturallyOccurringResources;
    }

    // --- API PUBLIQUE DU REGISTRE ---
    public static ResourceType get(String id) {
        return tags.get(id);
    }

    public static Collection<ResourceType> getAllResources() {
        return tags.getAll();
    }

    private final String id;
    private final GenerationParameters parameters;
    private final ReadOnlyStringProperty name;

    private ResourceType(String id, GenerationParameters parameters) {
        this.id = id;
        this.parameters = parameters;
        this.name = Localize.getProperty(ENUMS_BUNDLE, "ResourceType." + id);
    }

    public String getId() { return id; }
    public boolean isNaturallyOccurring() { return parameters != null; }

    public int getMinPerSite() {
        return parameters != null ? parameters.minPerSite : 0;
    }

    public boolean allowedOnTerrain(TerrainType terrainTypeName) {
        return parameters != null && parameters.appearsOn.contains(terrainTypeName);
    }

    /**
     * Évalue l'impact mathématique d'un attribut planétaire sur cette ressource.
     */
    public double getInfluenceScoreFor(PlanetaryAttribute attributeName, double planetAttributeValue) {
        if (parameters == null) return 0.0;
        InfluenceModel model = parameters.influences.get(attributeName);
        return model != null ? model.function().calculate(planetAttributeValue, model.parameters()) : 0.0;
    }

    public Set<PlanetaryAttribute> getInfluencingAttributes() {
        return parameters != null ? parameters.influences.keySet() : Collections.emptySet();
    }

    @Override public ReadOnlyStringProperty localizedProperty() { return name; }
    public ReadOnlyStringProperty nameProperty() { return name; }
    @Override public String toString() { return name.get(); }
    @Override public boolean equals(Object obj) { return obj instanceof ResourceType && id.equals(((ResourceType) obj).id); }
    @Override public int hashCode() { return id.hashCode(); }

    // --- CLASSES INTERNES EMBARQUÉES ---
    private record GenerationParameters(
            int minPerSite,
            double baseMinAmount,
            double baseMaxAmount,
            Set<TerrainType> appearsOn,
            Map<PlanetaryAttribute, InfluenceModel> influences
    ) {}

    private record InfluenceModel(
            MathFunction function,
            double[] parameters
    ) {}
}