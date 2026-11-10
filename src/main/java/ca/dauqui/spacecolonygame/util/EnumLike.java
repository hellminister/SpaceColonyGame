package ca.dauqui.spacecolonygame.util;

/**
 * Defines a structural contract for dynamic, file-driven configuration constants
 * mimicking native language enums with unique zero-based array indexing capabilities.
 *
 * @author Jean-François Quintal
 */
public interface EnumLike {

    /**
     * @return The zero-based unique sequential index assigned during bootstrap parsing
     */
    int ordinal();

    /**
     * @return The unique text identifier key matching configuration maps
     */
    String id();
}