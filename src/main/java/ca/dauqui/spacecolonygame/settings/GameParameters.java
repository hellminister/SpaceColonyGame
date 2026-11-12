package ca.dauqui.spacecolonygame.settings;

import ca.dauqui.spacecolonygame.util.ModdablePropertiesLoader;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Strongly-typed, immutable configuration registry exposing primitive game constants and asset paths.
 * Loads external overrides from {@code game_parameters.properties} at class initialization while guaranteeing
 * safe fallback defaults if keys are missing or malformed.
 *
 * @author Jean-François Quintal
 */
public final class GameParameters {

    private static final Logger LOG = Logger.getLogger(GameParameters.class.getName());

    /**
     * Private utility constructor preventing instantiation.
     */
    private GameParameters() {}

    /**
     * Defines integer balancing constants and structural grid dimensions.
     */
    public enum IntParam {
        /** Maximum stoichiometric reaction iterations triggered per orbital ignition probe deployment. */
        IGNITION_PROBE_MAX_CYCLES(5),

        /** Maximum number of reconnaissance probes players can deploy during star selection. */
        MAX_INITIAL_PROBES(4),

        /** Horizontal tile width of a planetary landing site grid. */
        SITE_MAP_WIDTH(300),

        /** Vertical tile height of a planetary landing site grid. */
        SITE_MAP_HEIGHT(150),

        /** Minimum number of exploitable landing sites generated on a rocky terrestrial planet. */
        MIN_PLANET_SITES(1),

        /** Maximum number of exploitable landing sites generated on a rocky terrestrial planet. */
        MAX_PLANET_SITES(20);

        private int value;

        /**
         * Initializes an integer parameter with its built-in default value.
         *
         * @param defaultValue The fallback integer value applied if absent from the external file
         */
        IntParam(int defaultValue) {
            this.value = defaultValue;
        }

        /**
         * @return The resolved integer value of this parameter
         */
        public int get() {
            return value;
        }
    }

    /**
     * Defines floating-point physics constants, astronomical thresholds, and simulation scaling parameters.
     */
    public enum DoubleParam {
        /** Surface temperature cooling offset applied to tidally locked planets (Kelvin). */
        TIDALLY_LOCKED_COOLING_OFFSET(15.0),

        /** Upper finite safety cap on stoichiometric reaction cycles to prevent floating-point overflow. */
        MAX_REACTION_CYCLES_CAP(1e30),

        /** Maximum distance from Sol (in light-years) for stars included in the navigable catalogue. */
        MAX_STAR_DISTANCE_LY(25.0),

        /** Cruising velocity of the initial seed colony vessel expressed as a fraction of light speed (c). */
        SEED_SHIP_SPEED_C(0.30),

        /** Cruising velocity of follow-up sleeper colonist ships expressed as a fraction of light speed (c). */
        HUMANITY_SHIP_SPEED_C(0.005),

        /** Minimum surface gravity (in Earth g) required for a terrestrial planet to retain an atmosphere. */
        MIN_TELLURIC_ATMO_GRAVITY(0.20),

        /** Maximum equilibrium temperature (in Kelvin) below which a terrestrial planet retains its atmosphere. */
        MAX_TELLURIC_ATMO_TEMP(500.0);

        private double value;

        /**
         * Initializes a double-precision parameter with its built-in default value.
         *
         * @param defaultValue The fallback floating-point value applied if absent from the external file
         */
        DoubleParam(double defaultValue) {
            this.value = defaultValue;
        }

        /**
         * @return The resolved double-precision value of this parameter
         */
        public double get() {
            return value;
        }
    }

    /**
     * Defines classpath resource locations and localization bundle identifiers.
     */
    public enum StringParam {
        /** Classpath location of the core game parameters override file. */
        GAME_PARAMETERS_PATH("/ca/dauqui/spacecolonygame/data/game_parameters.properties"),

        /** Classpath location of the astronomical star and exoplanet catalogue dataset. */
        STAR_DATA_PATH("/ca/dauqui/spacecolonygame/data/starsystems/StarData.txt"),

        /** Classpath location of the atmospheric gas thermodynamic and hazard properties file. */
        ATMOSPHERES_DATA_PATH("/ca/dauqui/spacecolonygame/data/atmospheres.properties"),

        /** Classpath location of the stoichiometric atmospheric chemical reaction matrix file. */
        REACTIONS_DATA_PATH("/ca/dauqui/spacecolonygame/data/reactions.properties"),

        /** Classpath location of the demographic population tag compatibility matrix file. */
        POPULATION_TAG_DATA_PATH("/ca/dauqui/spacecolonygame/data/populationtag.properties"),

        /** Classpath location of the natural mineral and energy resource generation file. */
        RESOURCE_TYPE_DATA_PATH("/ca/dauqui/spacecolonygame/data/resourceType.properties"),

        /** Classpath location of the orbital probe and satellite capability configuration file. */
        SATELLITE_TYPE_DATA_PATH("/ca/dauqui/spacecolonygame/data/satellitetype.properties"),

        /** Classpath location of the pre-launch cargo market pricing and mass table file. */
        CARGO_MARKET_DATA_PATH("/ca/dauqui/spacecolonygame/data/cargoMarket.properties"),

        /** Base bundle path for localized user interface labels and screen prompts. */
        LOCALIZATION_GUI_BUNDLE("ca.dauqui.spacecolonygame.localization.gui.gui"),

        /** Base bundle path for localized enum and data-driven entity display names. */
        LOCALIZATION_ENUMS_BUNDLE("ca.dauqui.spacecolonygame.localization.enums"),

        /** Base bundle path for localized star system names and lore descriptions. */
        LOCALIZATION_STAR_DATA_BUNDLE("ca.dauqui.spacecolonygame.localization.data.star_data"),

        /** Classpath root directory containing UI textures and background illustrations. */
        IMAGES_FOLDER_ROOT("/ca/dauqui/spacecolonygame/images/"),

        /** Classpath root directory containing launch sequence and event video cutscenes. */
        VIDEO_FOLDER_ROOT("/ca/dauqui/spacecolonygame/video/"),

        /** Classpath location of the supported UI localization languages registry file. */
        LANGUAGES_DATA_PATH("/ca/dauqui/spacecolonygame/data/languages.properties"),
        ;

        private String value;

        /**
         * Initializes a string parameter with its built-in default value.
         *
         * @param defaultValue The fallback string path or identifier applied if absent from the external file
         */
        StringParam(String defaultValue) {
            this.value = defaultValue;
        }

        /**
         * @return The resolved string value of this parameter
         */
        public String get() {
            return value;
        }
    }

    /**
     * Defines boolean engine flags and developer diagnostic switches.
     */
    public enum BooleanParam {
        /** Toggles visible layout grid lines on the star selection screen for UI debugging. */
        DEBUG_SHOW_GRID_LINES(false);

        private boolean value;

        /**
         * Initializes a boolean parameter with its built-in default value.
         *
         * @param defaultValue The fallback boolean flag applied if absent from the external file
         */
        BooleanParam(boolean defaultValue) {
            this.value = defaultValue;
        }

        /**
         * @return The resolved boolean value of this parameter
         */
        public boolean get() {
            return value;
        }
    }


    // --- STATIC INITIALIZATION & PARSING ---
    static {
        String configPath = StringParam.GAME_PARAMETERS_PATH.get();
        Properties props = ModdablePropertiesLoader.load(configPath);
        Set<String> unusedKeys = new HashSet<>(props.stringPropertyNames());

        for (IntParam param : IntParam.values()) {
            if (unusedKeys.remove(param.name())) {
                String raw = props.getProperty(param.name());
                if (raw != null && !raw.isBlank()) {
                    try {
                        param.value = Integer.parseInt(raw.trim());
                    } catch (NumberFormatException _) {
                        LOG.warning(() -> "Invalid integer format for parameter '" + param.name() + "': " + raw + ". Using default: " + param.value);
                    }
                }
            } else {
                logMissingParameter(param.name(), configPath, param.value);
            }
        }

        for (DoubleParam param : DoubleParam.values()) {
            if (unusedKeys.remove(param.name())) {
                String raw = props.getProperty(param.name());
                if (raw != null && !raw.isBlank()) {
                    try {
                        param.value = Double.parseDouble(raw.trim());
                    } catch (NumberFormatException _) {
                        LOG.warning(() -> "Invalid double format for parameter '" + param.name() + "': " + raw + ". Using default: " + param.value);
                    }
                }
            } else {
                logMissingParameter(param.name(), configPath, param.value);
            }
        }

        for (StringParam param : StringParam.values()) {
            if (unusedKeys.remove(param.name())) {
                String raw = props.getProperty(param.name());
                if (raw != null && !raw.isBlank()) {
                    param.value = raw.trim();
                }
            } else if (param != StringParam.GAME_PARAMETERS_PATH) {
                logMissingParameter(param.name(), configPath, param.value);
            }
        }

        for (BooleanParam param : BooleanParam.values()) {
            if (unusedKeys.remove(param.name())) {
                String raw = props.getProperty(param.name());
                if (raw != null && !raw.isBlank()) {
                    param.value = Boolean.parseBoolean(raw.trim());
                }
            } else {
                logMissingParameter(param.name(), configPath, param.value);
            }
        }

        for (String unknownKey : unusedKeys) {
            LOG.warning(() -> "Unrecognized parameter key in '" + configPath + "' (possible typo): " + unknownKey);
        }
    }

    /**
     * Logs an informational notice when a registered configuration constant is absent from the external file,
     * indicating that its built-in fallback default will remain active.
     *
     * @param paramName    The enumeration identifier of the missing parameter
     * @param configPath   The resource path of the parsed properties file
     * @param defaultValue The active fallback value retained by the simulation
     */
    private static void logMissingParameter(String paramName, String configPath, Object defaultValue) {
        LOG.info(() -> "Parameter '" + paramName + "' missing from '" + configPath + "'. Using default: " + defaultValue);
    }
}