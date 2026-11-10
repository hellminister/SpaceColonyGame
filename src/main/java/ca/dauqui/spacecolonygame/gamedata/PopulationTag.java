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
 * Represents an immutable operational demographic profiling tag applied to colony populations.
 * Governs ideological, physiological, or occupational compatibility matrices, restricting or
 * enabling shared habitation and deployment rules within settlement structures.
 *
 * @param ordinal          The unique zero-based sequential index allocated during parsing for array indexing
 * @param id               The unique string identifier token tracking this demographic tag
 * @param incompatibleTags An unmodifiable set of tags that cannot coexist with this profile
 * @param name             The reactive localized text property layer bound to UI rendering pipelines
 *
 * @author Jean-François Quintal
 */
@Localized
public record PopulationTag(
        int ordinal,
        String id,
        Set<PopulationTag> incompatibleTags,
        ReadOnlyStringProperty name
) implements Localizable, EnumLike {
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/populationtag.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    /**
     * Specialized constants loader parsing demographic profiles from disk properties.
     * Reconstructs a bidirectional unmodifiable graph layout mapping social incompatibilities.
     */
    private static final TypesafeConstants<PopulationTag> tags = new TypesafeConstants<>(CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, PopulationTag> targetMap) {
            // Transient memory layer holding onto mutable hash sets during the bidirectional wiring phase
            Map<PopulationTag, Set<PopulationTag>> incompatibleTagSets = new HashMap<>();

            for (String key : props.stringPropertyNames()) {
                Result tag = computeIfAbsent(key, targetMap, incompatibleTagSets);

                String property = props.getProperty(key);
                String[] split = property.split(",");
                for (String s : split) {
                    String trimmed = s.trim();
                    Result incompatible = computeIfAbsent(trimmed, targetMap, incompatibleTagSets);
                    tag.incompatibleTags().add(incompatible.tag());
                    incompatible.incompatibleTags().add(tag.tag());
                }
            }
        }

        /**
         * Resolves or dynamically instantiates a demographic tag profile during the parsing phase.
         * Leverages a transient mutable hash set layer to allow bidirectional linkage
         * prior to sealing the collection context layout permanently.
         *
         * @param id                  The target unique registration demographic identifier string
         * @param targetMap           The global registry map recording final PopulationTag instances
         * @param incompatibleTagSets Transient tracking matrix capturing mutable references for graph wiring
         * @return A processing lookup transfer record containing the tag and its underlying mutable set
         */
        private Result computeIfAbsent(String id, Map<String, PopulationTag> targetMap, Map<PopulationTag, Set<PopulationTag>> incompatibleTagSets){
            PopulationTag tag = targetMap.get(id);
            Set<PopulationTag> incompatibleTags = null;
            if (tag == null) {
                // Initializes a transient mutable set that will act beneath the unmodifiable view wrapper
                incompatibleTags = new HashSet<>();
                tag = new PopulationTag(nextOrdinal(), id, incompatibleTags);
                targetMap.put(id, tag);
                incompatibleTagSets.put(tag, incompatibleTags);
            } else {
                // Re-extracts the active mutable reference pointer to safely continue the bidirectional layout wiring
                incompatibleTags = incompatibleTagSets.get(tag);
            }
            return new Result(tag, incompatibleTags);
        }

        /**
         * Transient structural carrier record deployed exclusively during the data-parsing phase
         * to transport linked mutable collections across initialization gates.
         *
         * @param tag              The active demographic tag model compiled inside this processing block
         * @param incompatibleTags The underlying mutable collection storing bidirectional social restrictions
         */
        private record Result(PopulationTag tag, Set<PopulationTag> incompatibleTags) {}

    };

    /**
     * returns all the existing population tags
     * @return An immutable collection containing all available population tags
     */
    public static Collection<PopulationTag> getTags() {
        return Collections.unmodifiableCollection(tags.getAll());
    }


    /**
     * returns a PopulationTag corresponding to the given id
     * @param id the PopulationTag id
     * @return the corresponding PopulationTag
     */
    public static PopulationTag getTag(String id) {
        return tags.get(id);
    }

    /**
     * Package-private secondary constructor designed to dynamically bind unmodifiable view
     * boundaries over mutable graph nodes during the initial properties bootup sweep.
     *
     * @param ordinal          The unique sequential array index assigned to this demographic profile
     * @param id               The unique string registration parameter tracking database entries
     * @param incompatibleTags The mutable social data set containing tracking restrictions
     */
    private PopulationTag(int ordinal, String id, Set<PopulationTag> incompatibleTags) {
        this(ordinal, id, Collections.unmodifiableSet(incompatibleTags), Localize.getProperty(ENUMS_BUNDLE, "PopulationTag." + id));
    }

    /**
     * Determines whether the given PopulationTag is compatible with this Tag
     * @param tag The Tag to evaluate
     * @return Whether they are compatible or not
     */
    public boolean isCompatible(PopulationTag tag) {
        return !incompatibleTags.contains(tag);
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
        if (!(obj instanceof PopulationTag other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}