package ca.dauqui.spacecolonygame.gamedata.starsystem;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable structural record engineered to capture and expose raw astronomical parameters
 * extracted from the stellar database layouts, preserving uncertainty indicators for future gameplay layers.
 *
 * @param data The internal optimized enum map storing sanitized string values per column indices
 *
 * @author Jean-François Quintal
 */
public record PlanetAsset(Map<StarDataColumn, String> data) {

    /**
     * Private canonical constructor. Mandated to match the visibility scope restriction architecture.
     * Fully blocks external direct instantiation to enforce sanitized object construction.
     */
    public PlanetAsset(String[] dataStr) {
        this(Collections.unmodifiableMap(StarDataColumn.parseRow(dataStr)));
    }

    /**
     * Safe fallback utility engineered to prevent null pointer crashes within downstream UI rendering pipelines.
     *
     * @param column The targeted metadata database column layout index key
     * @return The registered parameter string, or an empty string configuration if absent
     */
    public String getSafe(StarDataColumn column) {
        return data.getOrDefault(column, "");
    }

    /**
     * @return The raw astronomical classification category token, preserving confirmation tokens (e.g., "PT", "PN?")
     */
    public String getType() {
        return getSafe(StarDataColumn.TY);
    }

    /**
     * Strips uncertainty tokens locally to parse the numeric dimension value safely.
     *
     * @return The radius value, or -1.0 if the metric remains undefined or unrecorded
     */
    public double getRadius() {
        String radiusValue = data.get(StarDataColumn.RADIUS);
        if (radiusValue == null || radiusValue.isEmpty() || "-".equals(radiusValue)) {
            return -1.0;
        }
        try {
            String cleanRadius = radiusValue.replace("?", "").trim();
            return Double.parseDouble(cleanRadius);
        } catch (NumberFormatException _) {
            return -1.0;
        }
    }

    /**
     * @return The raw radius unit framework parameter (e.g., "Re", "Re?")
     */
    public String getRadiusUnit() {
        return getSafe(StarDataColumn.RADIUS_UNIT);
    }
}