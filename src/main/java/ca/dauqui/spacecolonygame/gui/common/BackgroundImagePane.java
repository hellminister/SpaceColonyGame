package ca.dauqui.spacecolonygame.gui.common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import static ca.dauqui.spacecolonygame.util.ImageLibrary.IMAGE_LIBRARY;

/**
 * Responsive utility backdrop pane wrapping high-resolution images.
 * Dynamically binds image dimensions to follow target layout scaling masks smoothly.
 *
 * @author Jean-François Quintal
 */
public class BackgroundImagePane extends StackPane {

    /**
     * Constructs a background illustration pane, resolving asset instances from registries
     * and forcing dimensions to track structural workspace boundaries.
     *
     * @param root      The layout container reference used as the resizing coordinate master template
     * @param imageName Target relative file classification key inside the ImageLibrary registry
     */
    public BackgroundImagePane(Pane root, String imageName) {
        // Retrieve backdrop textures from the centralized modular asset library registry
        Image image = IMAGE_LIBRARY.getAsset(imageName);

        // Create background Pane
        var backgroundPlate = new ImageView(image);
        backgroundPlate.setPreserveRatio(true);

        // this pane is to permit resizing of the window
        getChildren().add(backgroundPlate);

        // Bootstrap layout limits matching current image source dimension shapes
        root.setPrefSize(backgroundPlate.getLayoutBounds().getWidth(), backgroundPlate.getLayoutBounds().getHeight());

        // Attach direct binding expressions to force images to stretch or shrink dynamically
        backgroundPlate.fitHeightProperty().bind(root.heightProperty());
        backgroundPlate.fitWidthProperty().bind(root.widthProperty());
    }
}