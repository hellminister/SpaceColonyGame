package ca.dauqui.spacecolonygame.gui.common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import static ca.dauqui.spacecolonygame.util.ImageLibrary.IMAGE_LIBRARY;

public class BackgroundImagePane extends StackPane {


    public BackgroundImagePane(Pane root, String imageName) {
        // Background image (somewhat big, will probably have to look for a smaller one
        Image image = IMAGE_LIBRARY.getImage(imageName);

        // Create background Pane
        var backgroundPlate = new ImageView(image);
        backgroundPlate.setPreserveRatio(true);

        // this pane is to permit resizing of the window
        getChildren().add(backgroundPlate);

        root.setPrefSize(backgroundPlate.getLayoutBounds().getWidth(), backgroundPlate.getLayoutBounds().getHeight());

        backgroundPlate.fitHeightProperty().bind(root.heightProperty());
        backgroundPlate.fitWidthProperty().bind(root.widthProperty());
    }
}