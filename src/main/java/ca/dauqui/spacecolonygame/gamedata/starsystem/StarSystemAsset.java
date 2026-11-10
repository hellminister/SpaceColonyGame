package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.util.EnumLike;
import ca.dauqui.spacecolonygame.util.TypesafeConstants;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Point3D;

import java.util.*;
import java.util.logging.Logger;


/**
 * Immutable structural record engineered to capture, sanitize, and expose stellar system core parameters.
 * Manages parent astronomical characteristics and maps attached orbiting planet assets.
 *
 * @param ordinal      The unique zero-based sequential index allocated during parsing for array indexing
 * @param id           The unique alphanumeric database lookup key matching the primary star name
 * @param data         The internal optimized enum map storing sanitized string values per column indices
 * @param coordinate   The three-dimensional space mapping vectors evaluated in light-years
 * @param name         The reactive localized primary title designation bound to the database keys
 * @param description  The reactive localized background description bound to the database keys
 * @param planetAssets The internal tracking list storing mapped orbital planet assets
 *
 * @author Jean-François Quintal
 */
public record StarSystemAsset(
        int ordinal,
        String id,
        Map<StarDataColumn, String> data,
        Point3D coordinate,
        ReadOnlyStringProperty name,
        ReadOnlyStringProperty description,
        List<PlanetAsset> planetAssets
) implements EnumLike {
    private static final Logger LOG = Logger.getLogger(StarSystemAsset.class.getName());
    private static final String STAR_DATA_PATH = "/ca/dauqui/spacecolonygame/data/starsystems/StarData.txt";
    private static final String STAR_SYSTEM_ASSET_LOCALIZE_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.data.star_data";

    private static final StarSystemAssetConstants STAR_SYSTEMS = new StarSystemAssetConstants(STAR_DATA_PATH);

    /**
     * Gives the StarSystemAsset corresponding to the given id
     * @param id the StarSystemAsset id
     * @return the corresponding StarSystemAsset or null if none with that id
     */
    public static StarSystemAsset getStarSystemAsset(String id) {
        return STAR_SYSTEMS.get(id);
    }

    /**
     * returns all the existing StarSystemAssets
     * @return An immutable collection containing all available StarSystemAssets
     */
    public static Collection<StarSystemAsset> getStarSystemAssets() {
        return STAR_SYSTEMS.getAll();
    }

    /**
     * @return A sorted list of StarSystemAsset in distance order
     */
    public static List<StarSystemAsset> getSortedStarSystemAssets() {
        return STAR_SYSTEMS.getSortedStarSystemAssets();
    }
    /**
     * Canonical constructor overload designed to accept raw row strings.
     * Delegates complex parsing out of the pre-construction phase to bypass java:S8444.
     *
     * @param ordinal  The unique sequential array index assigned to this specific stellar entry
     * @param dataStr The raw tokens sliced from a single data row entry
     */
    private StarSystemAsset(int ordinal, String[] dataStr) {
        this(ordinal,
                dataStr[StarDataColumn.NAME.ordinal()].trim(),
                Collections.unmodifiableMap(StarDataColumn.parseRow(dataStr)),
                new Point3D(
                        Double.parseDouble(dataStr[StarDataColumn.X.ordinal()].trim()),
                        Double.parseDouble(dataStr[StarDataColumn.Y.ordinal()].trim()),
                        Double.parseDouble(dataStr[StarDataColumn.Z.ordinal()].trim())
                ),
                Localize.getProperty(STAR_SYSTEM_ASSET_LOCALIZE_BUNDLE_PATH, dataStr[StarDataColumn.NAME.ordinal()].trim() + ".NAME"),
                Localize.getProperty(STAR_SYSTEM_ASSET_LOCALIZE_BUNDLE_PATH, dataStr[StarDataColumn.NAME.ordinal()].trim() + ".DESCRIPTION"),
                new ArrayList<>() // Initialized as a mutable list array to allow structural addPlanetAsset calls
        );
    }



    /**
     * Safe fallback reader utility engineered to prevent NullPointerException errors inside the UI thread.
     *
     * @param column The targeted metadata database column layout index key
     * @return The registered parameter string, or an empty string configuration if absent
     */
    public String getSafe(StarDataColumn column) {
        return data.getOrDefault(column, "");
    }


    /**
     * Computes the linear distance parameters relative to the galactic centre origin baseline (0,0,0).
     *
     * @return The absolute spatial distance scale evaluated in light-years
     */
    public double distance() {
        return coordinate.distance(0.0, 0.0, 0.0);
    }

    /**
     * Secure injection gateway layout designed to add planets internally while preserving encapsulation.
     *
     * @param planet The target compiled planet asset configuration layer to append
     */
    private void addPlanetAsset(PlanetAsset planet) {
        this.planetAssets.add(planet); // Accesses the underlying field pointer directly within the class envelope
    }

    /**
     * @return The minimum number of planets this star system can have
     */
    public int minNumberOfPlanets() {
        String rawValue = getSafe(StarDataColumn.MINPLN);
        return rawValue.isEmpty() ? -1 : Integer.parseInt(rawValue);
    }

    /**
     * @return The maximum number of planets this star system can have
     */
    public int maxNumberOfPlanets() {
        String rawValue = getSafe(StarDataColumn.MAXPLN);
        return rawValue.isEmpty() ? -1 : Integer.parseInt(rawValue);
    }

    /**
     * @return the spectral type of the star
     */
    public String spectralType() {
        return getSafe(StarDataColumn.SPEC);
    }

    /**
     * @return The mass of the star
     */
    public double mass() {
        String rawValue = getSafe(StarDataColumn.MASS);
        return rawValue.isEmpty() ? -1.0 : Double.parseDouble(rawValue.replace("?", ""));
    }

    /**
     * @return the radius of the star
     */
    public double radius() {
        String rawValue = getSafe(StarDataColumn.RADIUS);
        return rawValue.isEmpty() ? -1.0 : Double.parseDouble(rawValue.replace("?", ""));
    }

    /**
     * @return the effective temperature of the star
     */
    public double teff() {
        String rawValue = getSafe(StarDataColumn.TEFF);
        return rawValue.isEmpty() ? -1.0 : Double.parseDouble(rawValue.replace("?", ""));
    }

    /**
     * @return the visual luminosity relative to Sun (visible light output only
     */
    public double visualLum() {
        String rawValue = getSafe(StarDataColumn.VIS_LUM);
        return rawValue.isEmpty() ? -1.0 : Double.parseDouble(rawValue.replace("?", ""));
    }

    /**
     * @return the bolometric luminosity relative to Sun (total energy output)
     */
    public double bolometricLum() {
        String rawValue = getSafe(StarDataColumn.BOL_LUM);
        return rawValue.isEmpty() ? -1.0 : Double.parseDouble(rawValue.replace("?", ""));
    }

    /**
     * OVERRIDDEN ACCESSOR METHOD: Intercepts the native record accessor pipeline
     * to serve a read-only architectural shield layer to external callers.
     *
     * @return An unmodifiable view wrapper guarding the tracking collection layout against external mutations
     */
    public List<PlanetAsset> getKnownPlanets() {
        return Collections.unmodifiableList(planetAssets);
    }

    /**
     * A TypesafeConstant implementation that also keeps a list of the star systems in distance order
     */
    private static class StarSystemAssetConstants extends TypesafeConstants<StarSystemAsset> {
        private static final double MAX_DISTANCE = 25.0;
        private final List<StarSystemAsset> sortedStarSystemAssets;

        /**
         * Core constants factory initializer configuring data source resolution paths.
         *
         * @param starDataPath The internal absolute resource file path mapping raw catalogue parameters
         */
        public StarSystemAssetConstants(String starDataPath) {
            sortedStarSystemAssets = new ArrayList<>();
            super(starDataPath);
        }

        @Override
        protected void parseProperties(Properties props, Map<String, StarSystemAsset> targetMap) {
            List<String[]> rawRows = new ArrayList<>();

            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                if (value != null && !value.isBlank()) {
                    // Reconstruct the full line tokens array safely
                    String[] tokens = (key + value).concat(" ").split(",");
                    rawRows.add(tokens);
                }
            }

            // Sort rows by astronomical distance to preserve near-to-far catalogue progression
            rawRows.sort((r1, r2) -> {
                // First sort dimension: Absolute distance from sol system
                double dist1 = Double.parseDouble(r1[StarDataColumn.DIST.ordinal()].trim());
                double dist2 = Double.parseDouble(r2[StarDataColumn.DIST.ordinal()].trim());
                int distanceCompare = Double.compare(dist1, dist2);

                if (distanceCompare != 0) {
                    return distanceCompare;
                }

                // Second sort dimension: Taxonomy profiling layout (Stars 'S' must process before planetary bodies 'P')
                String type1 = r1[StarDataColumn.TY.ordinal()].trim();
                String type2 = r2[StarDataColumn.TY.ordinal()].trim();

                if (type1.equals("S") && !type2.equals("S")) return -1;
                if (!type1.equals("S") && type2.equals("S")) return 1;

                // Random or hash-native fallback sequence accepted for remaining sub-planetary components
                return 0;
            });

            StarSystemAsset last = null;
            for (String[] text : rawRows) {
                if (Double.parseDouble(text[StarDataColumn.DIST.ordinal()].trim()) > MAX_DISTANCE) {
                    break;
                }
                last = treatStellarBody(text, last, targetMap);
            }
        }

        /**
         * Dispatches structural token configurations to compile raw stellar entities or attach orbiting sub-bodies.
         *
         * @param text      The current parsed tokens array block reflecting a singular row entry
         * @param last      The immediate prior successfully parsed parent StarSystemAsset context
         * @param targetMap The active parsing dictionary accumulating completed global system records
         * @return The updated or verified tracking reference representing the current active master system context
         */
        private StarSystemAsset treatStellarBody(String[] text, StarSystemAsset last, Map<String, StarSystemAsset> targetMap) {
            String type = text[StarDataColumn.TY.ordinal()].trim().replace("?", "");

            switch (type) {
                case "S" -> {
                    var newSystem = new StarSystemAsset(nextOrdinal(), text);
                    targetMap.put(newSystem.id(), newSystem);
                    sortedStarSystemAssets.add(newSystem); // since they are read in sorted order
                    return newSystem;
                }
                case "PJ", "PN", "PT" -> {
                    var planet = new PlanetAsset(text);
                    if (last != null) {
                        last.addPlanetAsset(planet);
                    } else {
                        LOG.severe(() -> "No Star is yet Created or previous non-planet object is not a main sequence star -> " + Arrays.toString(text));
                    }
                    return last;
                }
                default -> {
                    LOG.info(() -> "Skipping " + text[StarDataColumn.NAME.ordinal()].trim() + " of type " + type);
                    return last;
                }
            }
        }

        /**
         * @return An unmodifiable view tracking star systems sequentially sorted near-to-far
         */
        private List<StarSystemAsset> getSortedStarSystemAssets() {
            return Collections.unmodifiableList(sortedStarSystemAssets);
        }
    }
}