package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.*;
import java.util.logging.Logger;

@Localized
public final class PopulationTag implements Localizable {
    private static final Logger LOG = Logger.getLogger(PopulationTag.class.getName());
    private static final String CONFIG_PATH = "/ca/dauqui/spacecolonygame/data/populationtag.properties";
    private static final String ENUMS_BUNDLE = "ca.dauqui.spacecolonygame.localization.enums";

    private static final TypesafeConstants<PopulationTag> tags = new TypesafeConstants<>(PopulationTag.class, CONFIG_PATH) {
        @Override
        protected void parseProperties(Properties props, Map<String, PopulationTag> targetMap) {
            Map<PopulationTag, Set<PopulationTag>> incompatibleTagSets = new HashMap<>();

            for (String key : props.stringPropertyNames()) {
                PopulationTag tag = targetMap.computeIfAbsent(key, _ -> new PopulationTag(key));
                String property = props.getProperty(key);
                String[] split = property.split(",");
                Set<PopulationTag> set = incompatibleTagSets.computeIfAbsent(tag, _ -> new HashSet<>());
                for (String s : split) {
                    String trimmed = s.trim();
                    PopulationTag incompatible = targetMap.computeIfAbsent(trimmed, _ -> new PopulationTag(trimmed));
                    set.add(incompatible);
                    Set<PopulationTag> set2 = incompatibleTagSets.computeIfAbsent(incompatible, _ -> new HashSet<>());
                    set2.add(tag);
                }
            }

            for (  Map.Entry<PopulationTag, Set<PopulationTag>> row : incompatibleTagSets.entrySet()){
                row.getKey().setIncompatibleTags(row.getValue());
            }
        }

        @Override
        protected void fallback(Map<String, PopulationTag> targetMap) {
            // not used here
        }
    };

    public static Collection<PopulationTag> getTags() {
        return Collections.unmodifiableCollection(tags.getAll());
    }

    public static PopulationTag getTag(String id) {
        return tags.get(id);
    }

    private final String id;
    private Set<PopulationTag> incompatibleTags;
    private final ReadOnlyStringProperty name;

    private PopulationTag(String id) {
        this.id = id;
        name = Localize.getProperty(ENUMS_BUNDLE, "PopulationTag." + id);
    }

    private void setIncompatibleTags(Set<PopulationTag> incompatibleTags) {
        this.incompatibleTags = Collections.unmodifiableSet(incompatibleTags);
    }

    public String getId() {
        return id;
    }
    public Set<PopulationTag> getIncompatibleTags() {
        return incompatibleTags;
    }

    public boolean isCompatible(PopulationTag tag) {
        return !incompatibleTags.contains(tag);
    }

    @Override
    public ReadOnlyStringProperty localizedProperty() {
        return name;
    }

    @Override
    public String toString() {
        return name.get();
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