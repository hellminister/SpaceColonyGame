package ca.dauqui.spacecolonygame.localization;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public final class Localize {
    private static final Map<String, Map<String, StringProperty>> localizedStrings = new HashMap<>();

    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    private Localize() {}

    public static void updateLanguage(String languageCode) {
        Locale.setDefault(Locale.forLanguageTag(languageCode));

        for (Map.Entry<String, Map<String, StringProperty>> bundleEntry : localizedStrings.entrySet()) {
            String bundleName = bundleEntry.getKey();
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName);

            for (Map.Entry<String, StringProperty> propertyEntry : bundleEntry.getValue().entrySet()) {
                String key = propertyEntry.getKey();
                if (bundle.containsKey(key)) {
                    propertyEntry.getValue().set(bundle.getString(key));
                }
            }
        }
    }


    public static ReadOnlyStringProperty getProperty(String bundleName, String key) {
        Map<String, StringProperty> fileMap = localizedStrings.computeIfAbsent(bundleName, k -> new HashMap<>());

        return fileMap.computeIfAbsent(key, k -> {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
            String initialValue = bundle.containsKey(k) ? bundle.getString(k) : k + " not defined";
            return new SimpleStringProperty(initialValue);
        });
    }

}