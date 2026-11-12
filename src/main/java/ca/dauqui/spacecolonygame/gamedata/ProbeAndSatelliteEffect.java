package ca.dauqui.spacecolonygame.gamedata;

import java.util.*;

/**
 * Defines the scanning and telemetry metadata effects unlocked by orbital probes and satellites.
 * The first two sections dictate what elements of the planetary fog of war are lifted on the HUD interfaces.
 */
public enum ProbeAndSatelliteEffect {

    // --- PLANETARY SCALE METRICS ---

    /** Unlocks the core thermal radiation and temperature variance readings. */
    SHOW_TEMPERATURE(EffectCategories.PLANETARY),

    /** Unlocks the atmospheric chemical composition and gas ratio breakdown. */
    SHOW_ATMOSPHERE_COMPOSITION(EffectCategories.PLANETARY),

    /** Unlocks the barometric surface pressure telemetry mapping. */
    SHOW_ATMOSPHERE_PRESSURE(EffectCategories.PLANETARY),

    /** Unlocks the total astronomical mass configuration registry. */
    SHOW_MASS(EffectCategories.PLANETARY),

    /** Unlocks the calculated surface gravity scale acceleration parameters. */
    SHOW_GRAVITY(EffectCategories.PLANETARY),

    /** Unlocks the lithospheric plate tectonics regime structural profiling. */
    SHOW_TECTONIC(EffectCategories.PLANETARY),

    /** Unlocks the active volcanic outgassing and eruptive site detection. */
    SHOW_VOLCANISM(EffectCategories.PLANETARY),

    /** Unlocks the magnetosphere strength and radiation shield index calculations. */
    SHOW_MAGNETIC_STRENGTH(EffectCategories.PLANETARY),

    /** Unlocks the global bolometric surface albedo reflection coefficient. */
    SHOW_ALBEDO(EffectCategories.PLANETARY),

    /** Unlocks the solar orbital revolution period tracking metrics. */
    SHOW_ORBITAL_PERIOD(EffectCategories.PLANETARY),

    /** Unlocks the sidereal planetary rotation period and day-night cycle trackers. */
    SHOW_ROTATION_PERIOD(EffectCategories.PLANETARY),

    /** Unlocks the axial tilt obliquity angle parameters relative to the ecliptic. */
    SHOW_AXIS_TILT(EffectCategories.PLANETARY),

    /** Unlocks the primary estimation mapping for maximum buildable outpost sites. */
    SHOW_SITE_COUNT(EffectCategories.PLANETARY),

    // --- LOCAL SITE SCALE METRICS ---

    /** Lifts the orbital shroud over the topographical landing canvas (reveals terrain difficulty hazards). */
    REVEAL_SITE_TOPOGRAPHY(EffectCategories.SITE),

    /** Generates the localized scanning report locating raw mineral deposits and geysers. */
    REVEAL_SITE_RESOURCES(EffectCategories.SITE),

    /** Penetrates the planet's mantle to map subterranean caves, tunnels, and bedrock resource pockets. */
    REVEAL_UNDERGROUND(EffectCategories.SITE),

    /** Forces atmospheric reactions even if he temperature is unsufficient */
    FORCE_ATMOSPHERIC_REACTIONS(EffectCategories.ATMOSPHERIC),
    ;

    private final Set<EffectCategories> categories;

    ProbeAndSatelliteEffect(EffectCategories... categories) {
        this.categories = (categories.length == 0)
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(categories)));

    }

    /**
     * Defines the functional operational scopes used to group orbital probe and satellite capabilities.
     */
    public enum EffectCategories {
        /** Global celestial metrics and planetary fog-of-war telemetry unlocks. */
        PLANETARY,

        /** Localized surface topography, mineral deposit, and subterranean site scanning unlocks. */
        SITE,

        /** Direct orbital interventions capable of triggering or altering atmospheric chemistry. */
        ATMOSPHERIC,
        ;
    }

    private static final Map<EffectCategories, Set<ProbeAndSatelliteEffect>> EFFECT_SETS;

    static {
        EnumMap<EffectCategories, Set<ProbeAndSatelliteEffect>> effects = new EnumMap<>(EffectCategories.class);
        Arrays.stream(EffectCategories.values()).forEach(k -> effects.put(k,  EnumSet.noneOf(ProbeAndSatelliteEffect.class)));
        Arrays.stream(ProbeAndSatelliteEffect.values()).forEach(v -> v.categories.forEach(c -> effects.get(c).add(v)));
        effects.replaceAll((k, v) -> Collections.unmodifiableSet(v));
        EFFECT_SETS = Collections.unmodifiableMap(effects);
    }


    /**
     * @param category the wanted category
     * @return A cached, unmodifiable view wrapper of the list of effects in the asked category
     */
    public static Set<ProbeAndSatelliteEffect> getEffectsFor(EffectCategories category) {
        return EFFECT_SETS.get(category);
    }

    /**
     * Evaluates if the collection payload contains at least one effect of the asked category
     *
     * @param effects The scanned collection layer provided by active telemetry assets
     * @param category the wanted category
     * @return True if a planetary attribute scanning baseline is detected
     */
    public static boolean containsAnyFromCategory(EffectCategories category, Collection<ProbeAndSatelliteEffect> effects){
        return !Collections.disjoint(effects, EFFECT_SETS.get(category));
    }

    /**
     * @param effect The effect to test
     * @param category the wanted category
     * @return True if the constant is of the asked type
     */
    public static boolean isEffectType(EffectCategories category, ProbeAndSatelliteEffect effect) {
        return EFFECT_SETS.get(category).contains(effect);
    }
}