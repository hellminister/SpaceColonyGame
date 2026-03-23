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
public final class ImageLibrary {
    private static final Logger LOG = Logger.getLogger(ImageLibrary.class.getName());

    private static final String IMAGE_LIBRARY_FILE_PATH = "src/main/resources/ca/dauqui/spacecolonygame/images/";

    private static final ImageLibrary library = new ImageLibrary();

    // stores the Images from the common resources folder
    private final Map<String, Image> theLibrary;

    private ImageLibrary() {
        theLibrary = new ConcurrentHashMap<>();
    }

    /**
     * Returns an Image from a given name first looking in the story resources folder
     * and if absent looking in the common resources folder
     *
     * @param imagePath the image path and name following the images folder
     * @return the asked Image (might return null if the file doesn't exist)
     */
    public static Image getImage(String imagePath) {
        return library.theLibrary.computeIfAbsent(imagePath, key -> {
            try (InputStream is = Files.newInputStream(Paths.get(IMAGE_LIBRARY_FILE_PATH + key ))) {
                return new Image(is);
            } catch (IOException e) {
                LOG.warning(e.getMessage());
            }
            return null;
        });
    }

}