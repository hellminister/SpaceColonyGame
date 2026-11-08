package ca.dauqui.spacecolonygame.gui.newgame.starselectionscreen;

import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.List;

/**
 * 3D Viewport container rendering local stellar clusters and handling camera orientations.
 * Leverages geometric matrix rotation algorithms to manipulate simulated spatial viewpoints.
 *
 * @author Jean-François Quintal
 */
class StarField extends BorderPane {
    // 5 degrees converted to standard rotation degrees for native JavaFX Rotate blocks
    private static final double ANGLE_CHANGE_DEGREES = 5.0;
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";

    /**
     * Constructs the 3D projection viewport frame, populating the local cluster sphere models,
     * binding layout clipping masks, and wiring mechanical continuous button rotation timers.
     *
     * @param stars Target array listing generated reactive stellar mesh elements to render
     */
    public StarField(List<StarNode> stars) {
        setStyle("-fx-background-color: black;");

        Group content3D = new Group();

        // Baseline structural sphere representing the localized system core primary sun
        Sphere sun = new Sphere(10);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.YELLOW);
        sun.setMaterial(material);
        sun.setTranslateX(0.0);
        sun.setTranslateY(0.0);
        sun.setTranslateZ(0.0);

        content3D.getChildren().add(sun);

        for (StarNode star : stars) {
            content3D.getChildren().add(star);
        }


        // ISOLATION GATEWAY: Instantiate a SubScene to contain 3D depth rendering
        // Width and height properties will bind dynamically to the container layout box
        SubScene subScene3D = createSubScene3D(content3D);

        StackPane root = new StackPane(subScene3D);
        HBox hBox = new HBox();

        // Bind SubScene dimensions dynamically to follow window boundary updates
        subScene3D.widthProperty().bind(root.widthProperty());
        subScene3D.heightProperty().bind(root.heightProperty());


        setCenter(root);

        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);

        // Set up the three explicit spatial transformation axes
        Rotate xRotation = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotation = new Rotate(0, Rotate.Y_AXIS);
        Rotate zRotation = new Rotate(0, Rotate.Z_AXIS);

        // Append transformations to the render group pipeline for hardware-accelerated processing
        content3D.getTransforms().addAll(xRotation, yRotation, zRotation);

        // Bind interactive adjustment buttons to matrix calculation channels
        Button x1 = matrixRotateNodeButton( ()-> xRotation.setAngle(xRotation.getAngle() + ANGLE_CHANGE_DEGREES), localizedProperty("StarField.ROLL_PLUS"));
        Button x2 = matrixRotateNodeButton( () -> xRotation.setAngle(xRotation.getAngle() - ANGLE_CHANGE_DEGREES), localizedProperty("StarField.ROLL_MINUS"));
        Button y1 = matrixRotateNodeButton( () -> yRotation.setAngle(yRotation.getAngle() + ANGLE_CHANGE_DEGREES), localizedProperty("StarField.PITCH_PLUS"));
        Button y2 = matrixRotateNodeButton( () -> yRotation.setAngle(yRotation.getAngle() - ANGLE_CHANGE_DEGREES),  localizedProperty("StarField.PITCH_MINUS"));
        Button z1 = matrixRotateNodeButton( () -> zRotation.setAngle(zRotation.getAngle() + ANGLE_CHANGE_DEGREES),  localizedProperty("StarField.YAW_PLUS"));
        Button z2 = matrixRotateNodeButton( () -> zRotation.setAngle(zRotation.getAngle() - ANGLE_CHANGE_DEGREES),  localizedProperty("StarField.YAW_MINUS"));

        hBox.getChildren().addAll(x1, x2, y1, y2, z1, z2);

        setBottom(hBox);
    }

    /**
     * Creates a SubScene to present a 3D content
     * @param content3D the content to present
     * @return the resulting SubScene
     */
    private static SubScene createSubScene3D(Group content3D) {
        SubScene subScene3D = new SubScene(content3D, 400, 400, true, SceneAntialiasing.BALANCED);
        subScene3D.setFill(Color.BLACK);

        // Configure perspective optics depth tracking pipelines
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        // Step back camera on Z axis to look at world coordinate centre (0,0,0)
        camera.setTranslateZ(-1750);
        camera.setFieldOfView(20);

        subScene3D.setCamera(camera);
        return subScene3D;
    }

    /**
     * Builds an interaction button coupled to a structural animation timer
     * to support continuous linear rotation tracking while pressed down.
     *
     * @param action Functional behaviour layout to invoke on mechanical execution pulses
     * @param text   Localized textual label property driving button visuals
     * @return Fully configured and wired interaction button component
     */
    private Button matrixRotateNodeButton(ButtonAction action, ReadOnlyStringProperty text) {
        Button button = new Button();
        button.textProperty().bind(text);
        Utility.setButtonStyle(button);

        // Drive automated continuous repetition frames during sustained clicks
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            // Interval for updates in nanoseconds (e.g., 100ms = 100,000,000 ns)
            private static final long INTERVAL = 100_000_000L;

            @Override
            public void handle(long now) {
                // Check if enough time has passed since the last update
                if (now - lastUpdate >= INTERVAL) {
                    action.action();
                    lastUpdate = now;
                }
            }
        };

        // Anchor animation loops directly to physical click engagement cycles
        button.setOnMousePressed(_ ->{
             action.action();
             timer.start();
        });
        button.setOnMouseReleased(_ -> timer.stop());

        return button;
    }

    /**
     * Functional interface template deploying custom lambda launch executions across screens.
     */
    @FunctionalInterface
    private interface ButtonAction {
        /**
         * Fires custom contextual layout triggers.
         */
        void action();
    }

    /**
     * Fetches reactive localized resource bundle properties assigned to tracking string keys.
     *
     * @param name Target translation property identifier tag
     * @return Localized text expression binding node
     */
    public static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, name);
    }
}