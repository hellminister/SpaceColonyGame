package ca.dauqui.spacecolonygame.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Technical I/O operations service engineered to load and merge configuration assets.
 * Implements a cascading strategy (JAR -> External Disk -> Active Plugins)
 * explicitly signalling to developers that the target section supports external modding.
 *
 * <p><strong>CRITICAL ARCHITECTURAL REQUIREMENT:</strong> All underlying translation bundles and configuration
 *  * files (.properties) MUST be explicitly encoded using <strong>UTF-8</strong> parameters to guarantee perfect rendering
 *  * of physics symbols (e.g., '²') and accented glyphs on the retro tactical HUD overlay.</p>
 */
public final class ModdablePropertiesLoader {
    private static final Logger LOG = Logger.getLogger(ModdablePropertiesLoader.class.getName());

    private static final String DATA_DIR = "data";
    private static final String PLUGINS_DIR = "plugins";

    private ModdablePropertiesLoader() {
        // Private constructor prevents instantiation of this stateless I/O service
    }

    /**
     * Resolves and consolidates property entries from multiple game layers.
     * Priority order (Highest replaces lowest): Plugins -> External Disk -> Core JAR
     *
     * @param relativeResourcePath The foundational internal resource path (e.g., "/ca/dauqui/spacecolonygame/data/atmospheres.properties")
     * @return A consolidated Properties instance reflecting hot-swapped values
     */
    public static Properties load(String relativeResourcePath) {
        Properties consolidatedProperties = new Properties();

        // LAYER 1: Extract immutable default game assets embedded inside the core compiler context
        try (InputStream jarStream = ModdablePropertiesLoader.class.getResourceAsStream(relativeResourcePath)) {
            if (jarStream != null) {
                consolidatedProperties.load(new InputStreamReader(jarStream, StandardCharsets.UTF_8));
                LOG.info(() -> "Successfully parsed core JAR bundle: " + relativeResourcePath);
            } else {
                LOG.warning(() -> "Core asset resource mapping not found: " + relativeResourcePath);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e, () -> "Fatal crash during core JAR asset deserialization: " + relativeResourcePath);
        }

        // Isolate filename from the resource URI path to mirror matching structures on the physical filesystem
        String fileName = relativeResourcePath.substring(relativeResourcePath.lastIndexOf('/') + 1);

        // LAYER 2: Intercept global localized overrides located on the disk data root
        File externalFile = new File(DATA_DIR, fileName);
        mergeFileIfExists(externalFile, consolidatedProperties);

        // LAYER 3: Traverse the active plugins subdirectories to override data parameters or inject custom mods
        File pluginsFolder = new File(PLUGINS_DIR);
        if (pluginsFolder.exists() && pluginsFolder.isDirectory()) {
            File[] modifications = pluginsFolder.listFiles(File::isDirectory);
            if (modifications != null) {
                for (File modDirectory : modifications) {
                    File pluginOverride = new File(modDirectory, fileName);
                    mergeFileIfExists(pluginOverride, consolidatedProperties);
                }
            }
        }

        return consolidatedProperties;
    }

    /**
     * Safe file system scanner designed to parse operational parameters without interrupting the render thread.
     */
    private static void mergeFileIfExists(File targetFile, Properties baseProperties) {
        if (targetFile.exists() && targetFile.isFile()) {
            try (InputStream fileStream = new FileInputStream(targetFile);
                 Reader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
                baseProperties.load(reader);
                LOG.info(() -> "Moddable data override successfully applied from: " + targetFile.getAbsolutePath());
            } catch (IOException e) {
                LOG.log(Level.WARNING, e, () -> "Failed to read moddable data layout file: " + targetFile.getAbsolutePath());
            }
        }
    }
}