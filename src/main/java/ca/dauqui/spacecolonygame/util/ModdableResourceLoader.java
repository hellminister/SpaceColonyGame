package ca.dauqui.spacecolonygame.util;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Universal asset loading infrastructure engineered to manage multi-tier file resolution hooks.
 * Mirrors the exact structural modularity guidelines of ModdablePropertiesLoader.
 * Implements an override cascade hierarchy (Plugins -> External Data -> Core JAR Payload)
 * and utilizes a non-blocking SoftMapCache to protect high-overhead memory structures.
 *
 * @param <V> The concrete JavaFX or multimedia component type (e.g., Image, Media)
 */
public abstract class ModdableResourceLoader<V> {
    private static final Logger LOG = Logger.getLogger(ModdableResourceLoader.class.getName());

    private static final String DATA_DIR = "data";
    private static final String PLUGINS_DIR = "plugins";

    private final String internalJarFolderRoot;
    private final SoftMapCache<String, V> cache = new SoftMapCache<>();

    /**
     * Initializes the asset streaming layer with its foundational fallback internal route.
     *
     * @param internalJarFolderRoot The embedded resource path root (e.g., "/ca/dauqui/spacecolonygame/images/")
     */
    protected ModdableResourceLoader(String internalJarFolderRoot) {
        this.internalJarFolderRoot = internalJarFolderRoot;
    }

    /**
     * Resolves and caches a target asset component utilizing high-priority external mod overrides.
     *
     * @param relativeAssetPath The relative filename pathway (e.g., "stars/sol.png")
     * @return The fully compiled asset, or null if lookup operations fail across all sectors
     */
    public final V get(String relativeAssetPath) {
        return cache.computeIfAbsent(relativeAssetPath, this::resolveAssetFromCascade);
    }

    private V resolveAssetFromCascade(String fileName) {
        // LAYER 1: Intercept asset adjustments within the active plugins folders (Highest Priority)
        V asset = resolveAssetFromPluginFolders(fileName);
        if (asset != null){
            return asset;
        }

        // LAYER 2: Intercept global disk custom variations located inside the root data folder
        asset = resolveAssetFromExternalData(fileName);
        if (asset != null){
            return asset;
        }

        // LAYER 3: Core Baseline Fallback - Stream the default immutable assets inside the compiled JAR
        asset = resolveAssetFromJar(fileName);
        if (asset != null){
            return asset;
        }

        LOG.log(Level.SEVERE, () -> "Target multimedia resource completely vanished across all operational modding tiers: " + fileName);
        return null;
    }

    /**
     * Layer 3 Resolution: Resolves the baseline asset packaged within the compiled application JAR archive.
     * Acts as the ultimate fallback if no external mod overrides are discovered on the physical storage layers.
     *
     * @param fileName The naked filename pathway target to extract from the embedded package root
     * @return The instantiated internal asset, or null if the embedded resource configuration is missing
     */
    private V resolveAssetFromJar(String fileName) {
        V asset;
        String fullJarPath = internalJarFolderRoot + fileName;
        URL jarResourceUrl = getClass().getResource(fullJarPath);
        if (jarResourceUrl != null) {
            asset = loadFromJar(jarResourceUrl, fullJarPath);
            if (asset != null) {
                LOG.fine(() -> "Successfully parsed core JAR asset entry: " + fullJarPath);
                return asset;
            }
        }
        return null;
    }

    /**
     * Layer 2 Resolution: Scans the global external data directory for centralized local custom overrides.
     * Intercepts and blocks fallback JAR reading if a valid loose file variation is deployed directly on the disk.
     *
     * @param fileName The naked filename pathway target to verify within the root data folder
     * @return The instantiated local file asset, or null if no disk override is present or valid
     */
    private V resolveAssetFromExternalData(String fileName) {
        V asset;
        File externalDataFile = new File(DATA_DIR, fileName);
        if (externalDataFile.exists() && externalDataFile.isFile()) {
            asset = loadFromFile(externalDataFile);
            if (asset != null) {
                LOG.info(() -> "Moddable asset successfully overridden from local data disk: " + externalDataFile.getAbsolutePath());
                return asset;
            }
        }
        return null;
    }

    /**
     * Layer 1 Resolution: Evaluates active plugin subdirectories via functional streams for high-priority mod modifications.
     * Leverages lazy evaluation structures to implement an aggressive short-circuit strategy, returning the very
     * first successfully parsed hijacked asset variation while skipping remaining mod folders to maintain speed.
     *
     * @param fileName The naked filename pathway target to seek across the subdirectories of the plugins folder
     * @return The highly prioritized plugin custom asset, or null if no active mod overrides the asset
     */
    private V resolveAssetFromPluginFolders(String fileName) {
        File pluginsFolder = new File(PLUGINS_DIR);
        if (pluginsFolder.exists() && pluginsFolder.isDirectory()) {
            File[] pluginDirs = pluginsFolder.listFiles(File::isDirectory);
            if (pluginDirs != null) {
                return Arrays.stream(pluginDirs)
                        .map(modDirectory -> new File(modDirectory, fileName))
                        .filter(file -> file.exists() && file.isFile())
                        .map(this::loadFromFile)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
    }

    /**
     * Concrete conversion delegate processing physical external files found on the storage disk.
     * Allows sub-classes to choose between opening direct streams or generating file:// URI paths.
     *
     * @param file The validated target file entry discovered on the physical file system
     * @return The instantiated asset layer, or null if compilation drops out
     */
    protected abstract V loadFromFile(File file);

    /**
     * Concrete conversion delegate processing standard resource lookups inside the application archive packaging.
     *
     * @param jarUrl      The fully qualified embedded URL link form resolved by the ClassLoader
     * @param fullJarPath The original raw string configuration pathway within the archive
     * @return The instantiated asset layer, or null if initialization parameters fail
     */
    protected abstract V loadFromJar(URL jarUrl, String fullJarPath);
}