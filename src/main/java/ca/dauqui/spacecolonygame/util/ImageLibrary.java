package ca.dauqui.spacecolonygame.util;

import ca.dauqui.spacecolonygame.settings.GameParameters;
import javafx.scene.image.Image;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Micro-engineered asset library tracking loaded JavaFX image textures.
 * Plugs directly into the universal ModdableResourceLoader engine to support live custom graphics hot-swapping.
 */
public enum ImageLibrary implements AssetLibrary<Image> {
    IMAGE_LIBRARY;

    private final ModdableResourceLoader<Image> loader = new ModdableResourceLoader<>(GameParameters.StringParam.IMAGES_FOLDER_ROOT.get()) {
        @Override
        protected Image loadFromFile(File file) {
            try (InputStream fis = new FileInputStream(file);
                 InputStream bis = new BufferedInputStream(fis)) {
                return new Image(bis); // Direct high-speed physical stream ingestion
            } catch (Exception _) {
                return null;
            }
        }

        @Override
        protected Image loadFromJar(URL jarUrl, String fullJarPath) {
            try (InputStream is = getClass().getResourceAsStream(fullJarPath)) {
                return is != null ? new Image(is) : null;
            } catch (Exception _) {
                return null;
            }
        }
    };

    /**
     * Resolves a texture component using custom plugin overrides and soft reference caches.
     *
     * @param imagePath The relative filename target path (e.g., "planets/desert.png")
     * @return The cached JavaFX Image instance, or null if resolution fails
     */
    @Override
    public Image getAsset(String imagePath) {
        return loader.get(imagePath);
    }
}