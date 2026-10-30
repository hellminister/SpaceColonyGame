package ca.dauqui.spacecolonygame.gamedata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum ProbeAndSatelliteEffect {
    // Échelle Planétaire
    SHOW_TEMPERATURE,
    SHOW_ATMOSPHERE_COMPOSITION,
    SHOW_ATMOSPHERE_PRESSURE,
    SHOW_MASS,
    SHOW_GRAVITY,
    SHOW_TECTONIC,
    SHOW_VOLCANISM,
    SHOW_MAGNETIC_STRENGTH,
    SHOW_ALBEDO,
    SHOW_ORBITAL_PERIOD,
    SHOW_ROTATION_PERIOD,
    SHOW_AXIS_TILT,
    SHOW_SITE_COUNT,

    // Échelle Locale (Site)
    REVEAL_SITE_TOPOGRAPHY, // Révèle le Canvas de surface (EASY, HARD, Cratères, Montagnes)
    REVEAL_SITE_RESOURCES,  // Révèle les gisements de minerais et fumeroles (Rapport de Scan)
    REVEAL_UNDERGROUND,      // Révèle la topographie et ressources des sous-sols (Passe 2)
    ;

    private static final EnumSet<ProbeAndSatelliteEffect> planetaryEffects = EnumSet.of(SHOW_TEMPERATURE, SHOW_ATMOSPHERE_COMPOSITION, SHOW_ATMOSPHERE_PRESSURE, SHOW_MASS, SHOW_GRAVITY, SHOW_TECTONIC, SHOW_VOLCANISM, SHOW_MAGNETIC_STRENGTH, SHOW_ALBEDO, SHOW_ORBITAL_PERIOD, SHOW_ROTATION_PERIOD, SHOW_AXIS_TILT, SHOW_SITE_COUNT);

    private static final EnumSet<ProbeAndSatelliteEffect> siteEffects = EnumSet.of(REVEAL_SITE_TOPOGRAPHY, REVEAL_SITE_RESOURCES, REVEAL_UNDERGROUND);

    public static Set<ProbeAndSatelliteEffect> getPlanetaryEffects() {
        return Collections.unmodifiableSet(planetaryEffects);
    }

    public static Set<ProbeAndSatelliteEffect> getSiteEffect() {
        return Collections.unmodifiableSet(siteEffects);
    }

    public static boolean containsPlanetaryEffect(Collection<ProbeAndSatelliteEffect> effects){
        Set<ProbeAndSatelliteEffect> planetaryEffectsCopy = EnumSet.copyOf(planetaryEffects);
        planetaryEffectsCopy.retainAll(effects);
        return !planetaryEffectsCopy.isEmpty();
    }

    public static boolean containsSiteEffect(Collection<ProbeAndSatelliteEffect> effects){
        Set<ProbeAndSatelliteEffect> siteEffectsCopy = EnumSet.copyOf(siteEffects);
        siteEffectsCopy.retainAll(effects);
        return !siteEffectsCopy.isEmpty();
    }

    public static boolean isPlanetaryEffect(ProbeAndSatelliteEffect effect){
        return planetaryEffects.contains(effect);
    }

    public static boolean isSiteEffect(ProbeAndSatelliteEffect effect){
        return siteEffects.contains(effect);
    }
}