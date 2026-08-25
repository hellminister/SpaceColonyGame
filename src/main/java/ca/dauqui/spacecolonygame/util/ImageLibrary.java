package ca.dauqui.spacecolonygame.util;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class serves as an image library so an image is loaded only once
 */
@SuppressWarnings("java:S6548")
public enum ImageLibrary {
    IMAGE_LIBRARY;

    private static final Logger LOG = Logger.getLogger(ImageLibrary.class.getName());

    private static final String IMAGE_LIBRARY_FILE_PATH = "src/main/resources/ca/dauqui/spacecolonygame/images/";


    // stores the Images from the common resources folder
    private final Map<String, Image> theLibrary;

    ImageLibrary() {
        theLibrary = new ConcurrentHashMap<>();
    }

    /**
     * Returns an Image from a given name first looking in the story resources folder
     * and if absent looking in the common resources folder
     *
     * @param imagePath the image path and name following the images folder
     * @return the asked Image (might return null if the file doesn't exist)
     */
    public Image getImage(String imagePath) {
        return theLibrary.computeIfAbsent(imagePath, key -> {
            try (InputStream is = Files.newInputStream(Paths.get(IMAGE_LIBRARY_FILE_PATH + key ))) {
                return new Image(is);
            } catch (IOException e) {
                LOG.warning(e.getMessage());
            }
            return null;
        });
    }

}