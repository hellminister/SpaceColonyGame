package ca.dauqui.spacecolonygame.gamedata;

/**
 * Structural enum registering fundamental physical planetary properties.
 * Serves as analytical key constants to map resource dependency evaluations
 * inside generation algorithms.
 *
 * @author Jean-François Quintal
 */
public enum PlanetaryAttribute {
    /**
     * Internal geothermal thermal flux index governing volcanic and mountain range activities.
     */
    VOLCANISM,

    /**
     * Surface atmospheric weight metrics governing crater density boundaries.
     */
    PRESSURE,

    /**
     * Structural crust stability classification mapping tectonic plate movements.
     */
    TECTONIC,

    /**
     * Volumetric gravitational threshold acceleration scalar.
     */
    GRAVITY,
    ;
}