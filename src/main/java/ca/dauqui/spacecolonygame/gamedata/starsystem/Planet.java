package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere.AtmosphereComposition;

/**
 * Immutable structural record defining the comprehensive physical, environmental,
 * and atmospheric characteristics of a generated planetary body.
 * Optimized using Canadian English development standards.
 *
 * @param type                   The astronomical classification category (e.g., "PT", "PN", "PJ")
 * @param radius                 The physical radius dimension metric
 * @param radiusUnit             The unit designator framework deployed ("Re" or "Rj")
 * @param mass                   The planetary mass relative parameters
 * @param gravity                The baseline static surface gravity parameters
 * @param temperatureEquilibrium The unmodified equilibrium temperature Kelvin
 * @param distance               The semi-major orbital axis distance tracked in Astronomical Units (AU)
 * @param albedo                 The localized surface reflectivity index configuration
 * @param orbitalPeriodDays      The complete stellar revolution duration tracked in Earth days
 * @param rotationPeriodHours    The sidereal rotation day duration tracked in hours
 * @param axisTilt               The axial obliquity tilt inclination value evaluated in degrees
 * @param isTidallyLocked        True if the planetary rotation is fully locked with its host star
 * @param receivedLight          The net stellar irradiance flux captured at orbital distance
 * @param numberOfSites          The total count of safe exploitable landing site grids available
 * @param atmosphereComposition  The mutable reactive biochemical atmospheric simulation layer
 * @param tectonic               The core geological crust plate structural behaviour index
 * @param volcanism              The continuous global geothermal heat flux metric (W/m²)
 * @param magneticField          The planetary magnetosphere dynamo protective shield index
 *
 * @author Jean-François Quintal
 */
public record Planet(String type,
                     double radius,
                     String radiusUnit,
                     double mass,
                     double gravity,
                     double temperatureEquilibrium,
                     double distance,
                     double albedo,
                     double orbitalPeriodDays,
                     double rotationPeriodHours,
                     double axisTilt,
                     boolean isTidallyLocked,
                     double receivedLight,
                     int numberOfSites,
                     AtmosphereComposition atmosphereComposition,
                     TectonicType tectonic,
                     double volcanism,
                     double magneticField) {

    /**
     * @return The atmospheric pressure for the planet in Bars
     */
    public double atmosphericPressure() {
        return atmosphereComposition.getPressure();
    }

    /**
     * @return the planet's temperature in Kelvin
     */
    public double surfaceTemperature() { return atmosphereComposition.getSurfaceTemperature(); }
}