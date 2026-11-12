package ca.dauqui.spacecolonygame.settings;

import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.SupportedLanguage;
import javafx.beans.property.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reactive, persistent user preferences registry managing runtime-modifiable game settings.
 * Exposes writable JavaFX properties for configuration screens and read-only views for game logic.
 * Automatically persists changes to {@code user_settings.properties} on disk.
 *
 * @author Jean-François Quintal
 */
public final class GameSettings {

    private static final Logger LOG = Logger.getLogger(GameSettings.class.getName());
    private static final Path SETTINGS_FILE = Path.of("user_settings.properties");

    /**
     * Private utility constructor preventing instantiation.
     */
    private GameSettings() {}

    /**
     * Defines toggleable boolean user preferences persisted across game sessions.
     */
    public enum BooleanSetting {
        /** Enables or bypasses the interactive pre-flight spacecraft launch checklist sequence. */
        ENABLE_PREFLIGHT_CHECKLIST(true),

        /** Determines whether new campaigns generate a fresh random seed or use the custom user seed. */
        USE_RANDOM_SEED(true);

        private final BooleanProperty property;

        /**
         * Initializes a boolean setting with its baseline default value.
         *
         * @param defaultValue The initial boolean state applied if no saved override exists on disk
         */
        BooleanSetting(boolean defaultValue) {
            this.property = new SimpleBooleanProperty(defaultValue);
        }

        /**
         * @return The current boolean value of this setting
         */
        public boolean get() {
            return property.get();
        }

        /**
         * @return A read-only JavaFX property view safe for binding in gameplay and simulation logic
         */
        public ReadOnlyBooleanProperty readOnlyProperty() {
            return property;
        }

        /**
         * @return The writable JavaFX property intended exclusively for configuration UI controls
         */
        public BooleanProperty property() {
            return property;
        }
    }

    /**
     * Defines 64-bit integer user preferences persisted across game sessions.
     */
    public enum LongSetting {
        /** The 64-bit procedural generation seed for random generation */
        WORLD_SEED(System.currentTimeMillis());

        private final LongProperty property;

        /**
         * Initializes a long integer setting with its baseline default value.
         *
         * @param defaultValue The initial 64-bit integer value applied if no saved override exists on disk
         */
        LongSetting(long defaultValue) {
            this.property = new SimpleLongProperty(defaultValue);
        }

        /**
         * @return The current 64-bit integer value of this setting
         */
        public long get() {
            return property.get();
        }

        /**
         * @return A read-only JavaFX property view safe for observation across game systems
         */
        public ReadOnlyLongProperty readOnlyProperty() {
            return property;
        }

        /**
         * @return The writable JavaFX property intended exclusively for configuration UI controls
         */
        public LongProperty property() {
            return property;
        }
    }

    /**
     * Defines the active user interface localization language preference.
     */
    public enum LanguageSetting {
        /** Tracks the currently selected {@link SupportedLanguage} locale across all UI screens. */
        ACTIVE_LANGUAGE;

        private final ObjectProperty<SupportedLanguage> property = new SimpleObjectProperty<>();

        /**
         * @return The currently active {@link SupportedLanguage} instance
         */
        public SupportedLanguage get() {
            return property.get();
        }

        /**
         * @return A read-only JavaFX property view tracking the active language selection
         */
        public ReadOnlyObjectProperty<SupportedLanguage> readOnlyProperty() {
            return property;
        }

        /**
         * @return The writable JavaFX property intended for language selector UI controls
         */
        public ObjectProperty<SupportedLanguage> property() {
            return property;
        }
    }

    /**
     * Resolves the seed to use when starting a new campaign: generates a fresh random 64-bit seed
     * if {@link BooleanSetting#USE_RANDOM_SEED} is enabled, otherwise returns the user-defined seed.
     *
     * @return The active 64-bit seed for procedural star system and planet generation
     */
    public static long resolveNewGameSeed() {
        if (BooleanSetting.USE_RANDOM_SEED.get()) {
            long generated = ThreadLocalRandom.current().nextLong();
            LongSetting.WORLD_SEED.property().set(generated);
            return generated;
        }
        return LongSetting.WORLD_SEED.get();
    }

    /**
     * Loads persisted user preferences from disk, synchronizes the initial UI locale,
     * and attaches automatic persistence listeners to all setting properties.
     * Must be invoked early during application startup before UI scenes are constructed.
     */
    public static void init() {
        loadFromDisk();

        ObjectProperty<SupportedLanguage> langProp = LanguageSetting.ACTIVE_LANGUAGE.property();
        langProp.addListener((_, _, newLang) -> {
            if (newLang != null) {
                Localize.updateLanguage(newLang.id());
                saveToDisk();
            }
        });

        for (BooleanSetting setting : BooleanSetting.values()) {
            setting.property().addListener((_, _, _) -> saveToDisk());
        }
        for (LongSetting setting : LongSetting.values()) {
            setting.property().addListener((_, _, _) -> saveToDisk());
        }
    }

    /**
     * Reads saved user preferences from {@code user_settings.properties} on disk if present,
     * applying validated overrides onto the reactive setting properties.
     */
    private static void loadFromDisk() {
        Properties props = new Properties();
        if (Files.exists(SETTINGS_FILE)) {
            try (Reader reader = Files.newBufferedReader(SETTINGS_FILE, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                LOG.log(Level.WARNING, e, () -> "Failed to load user settings from " + SETTINGS_FILE);
            }
        }

        for (BooleanSetting setting : BooleanSetting.values()) {
            String raw = props.getProperty(setting.name());
            if (raw != null && !raw.isBlank()) {
                setting.property().set(Boolean.parseBoolean(raw.trim()));
            }
        }

        for (LongSetting setting : LongSetting.values()) {
            String raw = props.getProperty(setting.name());
            if (raw != null && !raw.isBlank()) {
                try {
                    setting.property().set(Long.parseLong(raw.trim()));
                } catch (NumberFormatException _) {
                    LOG.warning(() -> "Invalid seed format in user settings: " + raw);
                }
            }
        }

        String savedLangCode = props.getProperty(LanguageSetting.ACTIVE_LANGUAGE.name(), "en");
        SupportedLanguage resolved = SupportedLanguage.get(savedLangCode);
        LanguageSetting.ACTIVE_LANGUAGE.property().set(resolved);
        if (resolved != null) {
            Localize.updateLanguage(resolved.id());
        }
    }

    /**
     * Serializes all current user preferences to {@code user_settings.properties} on disk in UTF-8 encoding.
     */
    private static void saveToDisk() {
        Properties props = new Properties();
        for (BooleanSetting setting : BooleanSetting.values()) {
            props.setProperty(setting.name(), String.valueOf(setting.get()));
        }
        for (LongSetting setting : LongSetting.values()) {
            props.setProperty(setting.name(), String.valueOf(setting.get()));
        }
        SupportedLanguage currentLang = LanguageSetting.ACTIVE_LANGUAGE.get();
        if (currentLang != null) {
            props.setProperty(LanguageSetting.ACTIVE_LANGUAGE.name(), currentLang.id());
        }

        try (Writer writer = Files.newBufferedWriter(SETTINGS_FILE, StandardCharsets.UTF_8)) {
            props.store(writer, "Space Colony Game - User Settings");
        } catch (IOException e) {
            LOG.log(Level.WARNING, e, () -> "Failed to save user settings to " + SETTINGS_FILE);
        }
    }
}