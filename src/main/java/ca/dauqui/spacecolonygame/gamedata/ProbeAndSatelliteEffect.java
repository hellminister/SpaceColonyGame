package ca.dauqui.spacecolonygame.gamedata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Defines the scanning and telemetry metadata effects unlocked by orbital probes and satellites.
 * The first two sections dictate what elements of the planetary fog of war are lifted on the HUD interfaces.
 */
public enum ProbeAndSatelliteEffect {

    // --- PLANETARY SCALE METRICS ---

    /** Unlocks the core thermal radiation and temperature variance readings. */
    SHOW_TEMPERATURE,

    /** Unlocks the atmospheric chemical composition and gas ratio breakdown. */
    SHOW_ATMOSPHERE_COMPOSITION,

    /** Unlocks the barometric surface pressure telemetry mapping. */
    SHOW_ATMOSPHERE_PRESSURE,

    /** Unlocks the total astronomical mass configuration registry. */
    SHOW_MASS,

    /** Unlocks the calculated surface gravity scale acceleration parameters. */
    SHOW_GRAVITY,

    /** Unlocks the lithospheric plate tectonics regime structural profiling. */
    SHOW_TECTONIC,

    /** Unlocks the active volcanic outgassing and eruptive site detection. */
    SHOW_VOLCANISM,

    /** Unlocks the magnetosphere strength and radiation shield index calculations. */
    SHOW_MAGNETIC_STRENGTH,

    /** Unlocks the global bolometric surface albedo reflection coefficient. */
    SHOW_ALBEDO,

    /** Unlocks the solar orbital revolution period tracking metrics. */
    SHOW_ORBITAL_PERIOD,

    /** Unlocks the sidereal planetary rotation period and day-night cycle trackers. */
    SHOW_ROTATION_PERIOD,

    /** Unlocks the axial tilt obliquity angle parameters relative to the ecliptic. */
    SHOW_AXIS_TILT,

    /** Unlocks the primary estimation mapping for maximum buildable outpost sites. */
    SHOW_SITE_COUNT,

    // --- LOCAL SITE SCALE METRICS ---

    /** Lifts the orbital shroud over the topographical landing canvas (reveals terrain difficulty hazards). */
    REVEAL_SITE_TOPOGRAPHY,

    /** Generates the localized scanning report locating raw mineral deposits and geysers. */
    REVEAL_SITE_RESOURCES,

    /** Penetrates the planet's mantle to map subterranean caves, tunnels, and bedrock resource pockets. */
    REVEAL_UNDERGROUND,

    /** Forces atmospheric reactions even if he temperature is unsufficient */
    FORCE_ATMOSPHERIC_REACTIONS,
    ;

    // Guarded unmodifiable sets constructed once at class loading phase
    private static final Set<ProbeAndSatelliteEffect> PLANETARY_EFFECTS = Collections.unmodifiableSet(EnumSet.of(
            SHOW_TEMPERATURE, SHOW_ATMOSPHERE_COMPOSITION, SHOW_ATMOSPHERE_PRESSURE,
            SHOW_MASS, SHOW_GRAVITY, SHOW_TECTONIC, SHOW_VOLCANISM,
            SHOW_MAGNETIC_STRENGTH, SHOW_ALBEDO, SHOW_ORBITAL_PERIOD,
            SHOW_ROTATION_PERIOD, SHOW_AXIS_TILT, SHOW_SITE_COUNT
    ));

    private static final Set<ProbeAndSatelliteEffect> SITE_EFFECTS = Collections.unmodifiableSet(EnumSet.of(
            REVEAL_SITE_TOPOGRAPHY, REVEAL_SITE_RESOURCES, REVEAL_UNDERGROUND
    ));

    /**
     * @return A cached, unmodifiable view wrapper guarding the global planetary telemetry effect constraints.
     */
    public static Set<ProbeAndSatelliteEffect> getPlanetaryEffects() {
        return PLANETARY_EFFECTS;
    }

    /**
     * @return A cached, unmodifiable view wrapper guarding the localized site telemetry effect constraints.
     */
    public static Set<ProbeAndSatelliteEffect> getSiteEffect() {
        return SITE_EFFECTS;
    }

    /**
     * Evaluates if the collection payload contains at least one active planetary metric unlock.
     *
     * @param effects The scanned collection layer provided by active telemetry assets
     * @return True if a planetary attribute scanning baseline is detected
     */
    public static boolean containsPlanetaryEffect(Collection<ProbeAndSatelliteEffect> effects){
        return !Collections.disjoint(effects, PLANETARY_EFFECTS);
    }

    /**
     * Evaluates if the collection payload contains at least one active localized site metric unlock.
     *
     * @param effects The scanned collection layer provided by active telemetry assets
     * @return True if a localized site scanning baseline is detected
     */
    public static boolean containsSiteEffect(Collection<ProbeAndSatelliteEffect> effects){
        return !Collections.disjoint(effects, SITE_EFFECTS);
    }

    /**
     * @param effect The targeted scanning modifier constant to test
     * @return True if the constant is of planetary effect type
     */
    public static boolean isPlanetaryEffect(ProbeAndSatelliteEffect effect) {
        return PLANETARY_EFFECTS.contains(effect);
    }

    /**
     * @param effect The targeted scanning modifier constant to test
     * @return True if the constant is of site effect type
     */
    public static boolean isSiteEffect(ProbeAndSatelliteEffect effect) {
        return SITE_EFFECTS.contains(effect);
    }
}