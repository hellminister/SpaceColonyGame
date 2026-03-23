package ca.dauqui.spacecolonygame.gamedata.starsystem;

/**
 * Enum representing the columns in the StarData.txt file
 * VERY IMPORTANT : the order of the enums MUST follow the order of the columns in the file
 */
public enum StarDataColumn {
    /** Object name */
    NAME,
    /** right ascension (J2000) in degrees */
    RA,
    /** declination (J2000) in degrees */
    DEC,
    /** apparent visual magnitude */
    VMAG,
    /** apparent J-band magnitude */
    JMAG,
    /** distance in light years */
    DIST,
    /** error on distance in percent (error also applies to X,Y,Z) */
    ERR,
    /** space coordinates in light years: +X = towards galactic center */
    X,
    /** space coordinates in light years: +Y = in direction of galactic rotation */
    Y,
    /** space coordinates in light years: +Z = towards galactic north */
    Z,
    /** type of object: S = main sequence star, WD = white dwarf, BD = brown dwarf, PJ = jovian planet, PN = neptunian planet, PT = terrestrial planet, ? = unconfirmed */
    TY,
    /** spectral type */
    SPEC,
    /** absolute visual magnitude */
    MV,
    /** absolute J-band magnitude */
    MJ,
    /** visual luminosity relative to Sun (visible light output only) */
    VIS_LUM,
    /** bolometric luminosity relative to Sun (total energy output) */
    BOL_LUM,
    /** mass */
    MASS,
    /** in terms of the mass of either the Sun (Ms), Jupiter (Mj), or the Earth (Me); ? indicates estimate */
    MASS_UNIT,
    /** radius */
    RADIUS,
    /** in terms of the radius of either the Sun (Rs), Jupiter (Rj), or the Earth (Re); ? indicates order of magnitude estimate */
    RADIUS_UNIT,
    /** effective temperature in Kelvin (for extrasolar planets, estimated average assuming an albedo of 0.5) */
    TEFF,
    /** for components of binary/multiple systems, indicates which component this object orbits */
    M,
    /** semimajor axis of component orbit in astronomical units (* indicates projected separation only where orbit is undetermined) */
    A,
    /** period of component orbit in years */
    PER,
    /** eccentricity of component orbit */
    ECC,
    /** minimum of planets to generate above the ones already marked */
    MINPLN,
    /** maximum of planets to generate above the ones already marked */
    MAXPLN

}