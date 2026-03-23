package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;


public class StarNode extends Group {

    private final StarSystemAsset star;
    private final Group starCircleSelected;
    private final Group starCircleProbeSelected;

    public StarNode(StarSystemAsset star) {
        this.star = star;

        Sphere sphere = new Sphere(star.getRadius()*10);
        getChildren().add(sphere);

        double temp = star.getTeff();

        PhongMaterial material = new PhongMaterial();

        if (temp <= 3000){
            material.setDiffuseColor(Color.rgb((int)(255.0 * (temp / 3000)), 0, 0));
        } else if (temp <= 4000) {
            material.setDiffuseColor(Color.rgb(255, (int)(127.0 * ((4000-temp)/1000)), 0));
        } else if (temp <= 6000) {
            material.setDiffuseColor(Color.rgb(255, 127 + (int)(128.0 * ((6000-temp)/2000)), 0));
        } else if (temp <= 10000) {
            material.setDiffuseColor(Color.rgb(255, 255, (int)(255.0 * ((10000-temp) / 4000))));
        } else if (temp <= 25000) {
            material.setDiffuseColor(Color.rgb(255 - (int)(255.0 * ((25000-temp) / 15000)), 255 - (int)(255.0 * ((25000-temp) / 15000)), 255));
        } else {
            material.setDiffuseColor(Color.rgb(0, 0, 255));
        }

        sphere.setMaterial(material);

        setTranslateX(star.getCoordinate().getX()*15);
        setTranslateY(star.getCoordinate().getY()*15);
        setTranslateZ(star.getCoordinate().getZ()*15);

        starCircleSelected = new Group();

        Circle starCircleSelectedX = new Circle(star.getRadius()*10 + 10);
        starCircleSelectedX.setFill(Color.TRANSPARENT);
        starCircleSelectedX.setStroke(Color.LIME);

        Circle starCircleSelectedY = new Circle(star.getRadius()*10 + 10);
        starCircleSelectedY.setFill(Color.TRANSPARENT);
        starCircleSelectedY.setStroke(Color.LIME);
        starCircleSelectedY.setRotationAxis(Rotate.X_AXIS);
        starCircleSelectedY.setRotate(90);

        Circle starCircleSelectedZ = new Circle(star.getRadius()*10 + 10);
        starCircleSelectedZ.setFill(Color.TRANSPARENT);
        starCircleSelectedZ.setStroke(Color.LIME);
        starCircleSelectedZ.setRotationAxis(Rotate.Y_AXIS);
        starCircleSelectedZ.setRotate(90);

        starCircleSelected.getChildren().addAll(starCircleSelectedX, starCircleSelectedY, starCircleSelectedZ);

        starCircleProbeSelected = new Group();

        Circle starCircleProbeSelectedX = new Circle(star.getRadius()*10 + 5);
        starCircleProbeSelectedX.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedX.setStroke(Color.BLUE);

        Circle starCircleProbeSelectedY = new Circle(star.getRadius()*10 + 5);
        starCircleProbeSelectedY.setFill(Color.TRANSPARENT);
        starCircleProbeSelectedY.setStroke(Color.BLUE);
        starCircleProbeSelectedY.setRotationAxis(Rotate.X_AXIS);
        starCircleProbeSelectedY.setRotate(90);

        Circle starCircleProbeSelectedZ = new Circle(star.getRadius()*10 + 5);
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