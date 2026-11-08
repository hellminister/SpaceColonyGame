package ca.dauqui.spacecolonygame.util;

/**
 * Common operational blueprint for structural asset registries.
 * Allows transparent swapping of asset sourcing engines without breaking downstream UI pipelines.
 *
 * @param <V> The concrete asset component type (e.g., Image, Media)
 */
public interface AssetLibrary<V> {

    /**
     * Resolves, loads, and caches a target asset resource.
     *
     * @param assetPath The relative filename pathway within the asset package structure
     * @return The fully compiled asset, or null if lookups fail across all channels
     */
    V getAsset(String assetPath);
}