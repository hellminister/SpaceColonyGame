package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;


public class StarNode extends Group {
    private static final int RADIUS_MULT = 15;
    private static final int LIGHT_YEAR_SIZE = 12;
    private static final int SELECTED_CIRCLE_SIZE = 10;
    private static final int PROBE_CIRCLE_SIZE = 5;

    private final StarSystemAsset star;
    private final Group starCircleSelected;
    private final Group starCircleProbeSelected;

    public StarNode(StarSystemAsset star) {
        this.star = star;

        Sphere sphere = new Sphere(star.getRadius() * RADIUS_MULT);
        getChildren().add(sphere);

        double temp = star.getTeff();

        PhongMaterial material = new PhongMaterial();

        material.setDiffuseColor(Utility.getStarColor(temp));

        sphere.setMaterial(material);

        setTranslateX(star.getCoordinate().getX() * LIGHT_YEAR_SIZE);
        setTranslateY(star.getCoordinate().getY() * LIGHT_YEAR_SIZE);
        setTranslateZ(star.getCoordinate().getZ() * LIGHT_YEAR_SIZE);

        starCircleSelected = new Group();

        Circle starCircleSelectedX = new Circle(star.getRadius() * RADIUS_MULT + SELECTED_CIRCLE_SIZE);
        starCircleSelectedX.setFill(Color.TRANSPARENT);
        starCircleSelectedX.setStroke(Color.LIME);

        Circle starCircleSelectedY = new Circle(star.getRadius() * RADIUS_MULT + SELECTED_CIRCLE_SIZE);
        starCircleSelectedY.setFill(Color.TRANSPARENT);
        starCircleSelectedY.setStroke(Color.LIME);
        starCircleSelectedY.setRotationAxis(Rotate.X_AXIS);
        starCircleSelectedY.setRotate(90);

        Circle starCircleSelectedZ = new Circle(star.getRadius() * RADIUS_MULT + SELECTED_CIRCLE_SIZE);
        starCircleSelectedZ.setFill(Color.TRANSPARENT);
        starCircleSelectedZ.setStroke(Color.LIME);
        starCircleSelectedZ.setRotationAxis(Rotate.Y_AXIS);
        starCircleSelectedZ.setRotate(90);

        starCircleSelected.getChildren().addAll(starCircleSelectedX, starCircleSelectedY, starCircleSelectedZ);

        starCircleProbeSelected = new Group();

        Circle starCircleProbeSelectedX = new Circle(star.getRadius()*RADIUS_MULT + PROBE_CIRCLE_SIZE);
        starCircleProbeSelectedX.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedX.setStroke(Color.BLUE);

        Circle starCircleProbeSelectedY = new Circle(star.getRadius()*RADIUS_MULT + PROBE_CIRCLE_SIZE);
        starCircleProbeSelectedY.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedY.setStroke(Color.BLUE);
        starCircleProbeSelectedY.setRotationAxis(Rotate.X_AXIS);
        starCircleProbeSelectedY.setRotate(90);

        Circle starCircleProbeSelectedZ = new Circle(star.getRadius()*RADIUS_MULT + PROBE_CIRCLE_SIZE);
        starCircleProbeSelectedZ.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedZ.setStroke(Color.BLUE);
        starCircleProbeSelectedZ.setRotationAxis(Rotate.Y_AXIS);
        starCircleProbeSelectedZ.setRotate(90);


        starCircleProbeSelected.getChildren().addAll(starCircleProbeSelectedX, starCircleProbeSelectedY, starCircleProbeSelectedZ);
        getChildren().addAll(starCircleSelected, starCircleProbeSelected);
        unselect();
        unchosenForProbe();

    }

    public void select() {
        starCircleSelected.setVisible(true);
    }

    public void unselect() {
        starCircleSelected.setVisible(false);
    }

    public void unchosenForProbe() {
        starCircleProbeSelected.setVisible(false);
    }

    public void chosenForProbe() {
        starCircleProbeSelected.setVisible(true);
    }

}