package ca.dauqui.spacecolonygame.localization;

import ca.dauqui.spacecolonygame.util.ModdablePropertiesLoader;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Centralized localization service engine engineered to bind UI components to reactive strings.
 * Built to bypass Java Module System (JPMS) ResourceBundle.Control constraints by channelling
 * I/O operations directly through ModdablePropertiesLoader layout maps.
 *
 * <p><strong>CRITICAL ARCHITECTURAL REQUIREMENT:</strong> All underlying translation bundles and configuration
 *  * files (.properties) MUST be explicitly encoded using <strong>UTF-8</strong> parameters to guarantee perfect rendering
 *  * of physics symbols (e.g., '²') and accented glyphs on the retro tactical HUD overlay.</p>
 */
public final class Localize {
    private static final String CLASSPATH_SEPARATOR = "/";

    private static final Map<String, Map<String, StringProperty>> localizedStrings = new HashMap<>();
    private static final Map<String, Properties> cachedBundles = new HashMap<>();
    public static final String PROPERTIES_FILE_EXTENSION = ".properties";

    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    private Localize() {}

    /**
     * Swaps the system locale configuration settings and hot-swaps all registered properties text data.
     *
     * @param languageCode The target BCP 47 language tag (e.g., "en", "fr-CA")
     */
    public static void updateLanguage(String languageCode) {
        Locale.setDefault(Locale.forLanguageTag(languageCode));
        cachedBundles.clear();

        for (Map.Entry<String, Map<String, StringProperty>> bundleEntry : localizedStrings.entrySet()) {
            String bundleName = bundleEntry.getKey();
            Properties bundle = loadCascadingBundle(bundleName, Locale.getDefault());

            for (Map.Entry<String, StringProperty> propertyEntry : bundleEntry.getValue().entrySet()) {
                String key = propertyEntry.getKey();
                if (bundle.containsKey(key)) {
                    propertyEntry.getValue().set(bundle.getProperty(key));
                }
            }
        }
    }

    /**
     * Resolves or initializes a binding property linked to a localized file entry.
     *
     * @param bundleName The fully qualified path identifier of the targeted translation file
     * @param key        The structural key string pointing to the text line
     * @return A reactive ReadOnlyStringProperty that automatically updates when switching languages
     */
    public static ReadOnlyStringProperty getProperty(String bundleName, String key) {
        Map<String, StringProperty> fileMap = localizedStrings.computeIfAbsent(bundleName, _ -> new HashMap<>());

        return fileMap.computeIfAbsent(key, k -> {
            Properties bundle = cachedBundles.computeIfAbsent(bundleName, b -> loadCascadingBundle(b, Locale.getDefault()));
            String initialValue = bundle.containsKey(k) ? bundle.getProperty(k) : k + " not defined";
            return new SimpleStringProperty(initialValue);
        });
    }

    /**
     * Emulates Java's standard locale fallback resolution strategy manually to support named modules.
     * Searches order: 1. language_country (fr_CA) -> 2. language (fr) -> 3. baseline default (.properties)
     */
    private static Properties loadCascadingBundle(String baseName, Locale locale) {
        Properties mergedLayout = new Properties();
        String cleanBase = baseName.replace(".", CLASSPATH_SEPARATOR);

        // Resolution step 1: Base properties mapping fallback layer (e.g., enums.properties)
        String basePath = String.join("", CLASSPATH_SEPARATOR, cleanBase, PROPERTIES_FILE_EXTENSION);
        mergedLayout.putAll(ModdablePropertiesLoader.load(basePath));

        // Resolution step 2: Language code overlay layer (e.g., enums_fr.properties)
        if (!locale.getLanguage().isEmpty()) {
            String langPath = String.join("", CLASSPATH_SEPARATOR, cleanBase, "_", locale.getLanguage(), PROPERTIES_FILE_EXTENSION);
            mergedLayout.putAll(ModdablePropertiesLoader.load(langPath));
        }

        // Resolution step 3: Full variant country overlay layer (e.g., enums_fr_CA.properties)
        if (!locale.getCountry().isEmpty()) {
            String countryPath = String.join("", CLASSPATH_SEPARATOR, cleanBase, "_", locale.getLanguage(), "_", locale.getCountry(), PROPERTIES_FILE_EXTENSION);
            mergedLayout.putAll(ModdablePropertiesLoader.load(countryPath));
        }

        return mergedLayout;
    }

}