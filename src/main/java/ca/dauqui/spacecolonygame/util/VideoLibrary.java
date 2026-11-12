package ca.dauqui.spacecolonygame.util;

import ca.dauqui.spacecolonygame.settings.GameParameters;
import javafx.scene.media.Media;

import java.io.File;
import java.net.URL;

/**
 * Micro-engineered asset library tracking heavy streamable cinematic video and audio media files.
 * Leverages URI external forms to satisfy rigorous JavaFX Media streaming constraints.
 */
public enum VideoLibrary implements AssetLibrary<Media> {
    VIDEO_LIBRARY;

    private final ModdableResourceLoader<Media> loader = new ModdableResourceLoader<>(GameParameters.StringParam.VIDEO_FOLDER_ROOT.get()) {
        @Override
        protected Media loadFromFile(File file) {
            try {
                // Media requires a properly structured file:// protocol URL format for external disk reading
                return new Media(file.toURI().toString());
            } catch (Exception _) {
                return null;
            }
        }

        @Override
        protected Media loadFromJar(URL jarUrl, String fullJarPath) {
            try {
                return new Media(jarUrl.toExternalForm()); // Safe protocol mapping inside the compressed JAR
            } catch (Exception _) {
                return null;
            }
        }
    };

    /**
     * Resolves a streamable video or background audio track component using custom plugin overrides.
     *
     * @param videoPath The relative filename target path (e.g., "cinematics/intro.mp4")
     * @return The cached JavaFX Media instance, or null if lookups or streaming connections fail
     */
    @Override
    public Media getAsset(String videoPath) {
        return loader.get(videoPath);
    }
}