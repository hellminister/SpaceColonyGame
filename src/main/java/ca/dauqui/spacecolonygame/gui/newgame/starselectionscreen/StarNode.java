package ca.dauqui.spacecolonygame.gui.newgame.starselectionscreen;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * 3D spatial node container tracking stellar spatial coordinates,
 * surface temperatures, and volumetric scaling factors inside the viewport.
 *
 * @author Jean-François Quintal
 */
class StarNode extends Group {
    private static final int RADIUS_MULT = 15;
    private static final int LIGHT_YEAR_SIZE = 12;
    private static final int SELECTED_CIRCLE_SIZE = 10;
    private static final int PROBE_CIRCLE_SIZE = 5;

    private final StarSystemAsset star;
    private final Group starCircleSelected;
    private final Group starCircleProbeSelected;

    private final BooleanProperty selected;
    private final BooleanProperty markedForProbe;

    /**
     * Instantiates a spatial mesh entity, applying contextual stellar gas shaders
     * and baking orthogonal focus selection ring frameworks.
     *
     * @param star The immutable data profile tracking structural astronomical data points
     */
    public StarNode(StarSystemAsset star) {
        this.star = star;

        selected = new SimpleBooleanProperty(false);
        markedForProbe = new SimpleBooleanProperty(false);

        Sphere sphere = new Sphere(star.radius() * RADIUS_MULT);
        getChildren().add(sphere);

        double temp = star.teff();

        PhongMaterial material = new PhongMaterial();

        material.setDiffuseColor(Utility.getStarColor(temp));

        sphere.setMaterial(material);

        // Convert astronomical light year data vectors directly into 3D world space coordinates
        setTranslateX(star.coordinate().getX() * LIGHT_YEAR_SIZE);
        setTranslateY(star.coordinate().getY() * LIGHT_YEAR_SIZE);
        setTranslateZ(star.coordinate().getZ() * LIGHT_YEAR_SIZE);

        starCircleSelected = new Group();

        // Build a 3D tactical wireframe grid using 3 perpendicular intersecting 2D halos
        Circle starCircleSelectedX = new Circle(star.radius() * RADIUS_MULT + SELECTED_CIRCLE_SIZE);
        starCircleSelectedX.setFill(Color.TRANSPARENT);
        starCircleSelectedX.setStroke(Color.LIME);

        Circle starCircleSelectedY = new Circle(star.radius() * RADIUS_MULT + SELECTED_CIRCLE_SIZE);
        starCircleSelectedY.setFill(Color.TRANSPARENT);
        starCircleSelectedY.setStroke(Color.LIME);
        starCircleSelectedY.setRotationAxis(Rotate.X_AXIS);
        starCircleSelectedY.setRotate(90);

        Circle starCircleSelectedZ = new Circle(star.radius() * RADIUS_MULT + SELECTED_CIRCLE_SIZE);
        starCircleSelectedZ.setFill(Color.TRANSPARENT);
        starCircleSelectedZ.setStroke(Color.LIME);
        starCircleSelectedZ.setRotationAxis(Rotate.Y_AXIS);
        starCircleSelectedZ.setRotate(90);

        starCircleSelected.getChildren().addAll(starCircleSelectedX, starCircleSelectedY, starCircleSelectedZ);
        starCircleSelected.visibleProperty().bind(selected);

        starCircleProbeSelected = new Group();

        // Build a matching perpendicular structural halo layout to track active probe deployments
        Circle starCircleProbeSelectedX = new Circle(star.radius()*RADIUS_MULT + PROBE_CIRCLE_SIZE);
        starCircleProbeSelectedX.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedX.setStroke(Color.BLUE);

        Circle starCircleProbeSelectedY = new Circle(star.radius()*RADIUS_MULT + PROBE_CIRCLE_SIZE);
        starCircleProbeSelectedY.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedY.setStroke(Color.BLUE);
        starCircleProbeSelectedY.setRotationAxis(Rotate.X_AXIS);
        starCircleProbeSelectedY.setRotate(90);

        Circle starCircleProbeSelectedZ = new Circle(star.radius()*RADIUS_MULT + PROBE_CIRCLE_SIZE);
        starCircleProbeSelectedZ.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedZ.setStroke(Color.BLUE);
        starCircleProbeSelectedZ.setRotationAxis(Rotate.Y_AXIS);
        starCircleProbeSelectedZ.setRotate(90);

        starCircleProbeSelected.getChildren().addAll(starCircleProbeSelectedX, starCircleProbeSelectedY, starCircleProbeSelectedZ);
        starCircleProbeSelected.visibleProperty().bind(markedForProbe);

        getChildren().addAll(starCircleSelected, starCircleProbeSelected);

    }

    /**
     * @return The property indicating if this is selected
     */
    public BooleanProperty selectedProperty(){
        return selected;
    }

    /**
     * @return The property indicating if this has been marked for a probe
     */
    public BooleanProperty markedForProbeProperty(){
        return markedForProbe;
    }

}