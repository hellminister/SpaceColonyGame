package ca.dauqui.spacecolonygame.util;

import javafx.scene.media.Media;

import java.util.logging.Logger;

/**
 * This class serves as an image library so an image is loaded only once
 */
public enum VideoLibrary {
    VIDEO_LIBRARY;

    private static final Logger LOG = Logger.getLogger(VideoLibrary.class.getName());

    private static final String VIDEO_LIBRARY_FILE_PATH = "src/main/resources/ca/dauqui/spacecolonygame/video/";


    // stores the Videos from the common resources folder
    private final SoftMapCache<String, Media> theLibrary;

    VideoLibrary() {
        theLibrary = new SoftMapCache<>();
    }

    /**
     * Returns a Video from a given name first looking in the story resources folder
     * and if absent looking in the common resources folder
     *
     * @param imagePath the Video path and name following the Video folder
     * @return the asked Video (might return null if the file doesn't exist)
     */
    public Media getVideo(String imagePath) {
        return theLibrary.computeIfAbsent(imagePath, key -> new Media(VIDEO_LIBRARY_FILE_PATH + key));
    }

}