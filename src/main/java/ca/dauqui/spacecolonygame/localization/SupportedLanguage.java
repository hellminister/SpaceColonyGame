package ca.dauqui.spacecolonygame.localization;

import ca.dauqui.spacecolonygame.settings.GameParameters;
import ca.dauqui.spacecolonygame.util.EnumLike;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Immutable configuration record representing an available UI localization language.
 * Stores the native endonym display name directly so players can always identify their language
 * regardless of the currently active locale.
 *
 * @param ordinal     The unique zero-based sequential index allocated during parsing
 * @param id          The BCP 47 language tag (e.g., "en", "fr-CA")
 * @param displayName The native endonym label displayed in settings menus
 *
 * @author Jean-François Quintal
 */
public record SupportedLanguage(
        int ordinal,
        String id,
        String displayName
) implements EnumLike {

    private static final TypesafeConstants<SupportedLanguage> LANGUAGES = new TypesafeConstants<>(GameParameters.StringParam.LANGUAGES_DATA_PATH.get()) {
        @Override
        protected void parseProperties(Properties props, Map<String, SupportedLanguage> targetMap) {
            for (String code : props.stringPropertyNames()) {
                String label = props.getProperty(code);
                if (label != null && !label.isBlank()) {
                    String cleanCode = code.trim();
                    targetMap.put(cleanCode, new SupportedLanguage(nextOrdinal(), cleanCode, label.trim()));
                }
            }
        }

        @Override
        protected void fallback(Map<String, SupportedLanguage> targetMap) {
            targetMap.computeIfAbsent("en", code -> new SupportedLanguage(nextOrdinal(), code, "English"));
        }
    };

    /**
     * Resolves a supported language entry by its BCP 47 tag, falling back to English if unrecognized.
     *
     * @param code The BCP 47 language identifier tag
     * @return The matched {@link SupportedLanguage}, or the English default fallback
     */
    public static SupportedLanguage get(String code) {
        if (code == null || code.isBlank()) {
            return LANGUAGES.get("en");
        }
        SupportedLanguage lang = LANGUAGES.get(code.trim());
        return lang != null ? lang : LANGUAGES.get("en");
    }

    /**
     * @return An immutable collection containing all registered supported languages
     */
    public static Collection<SupportedLanguage> getAll() {
        return LANGUAGES.getAll();
    }

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SupportedLanguage other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}