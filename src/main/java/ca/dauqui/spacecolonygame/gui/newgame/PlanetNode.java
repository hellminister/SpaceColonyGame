package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;


public class PlanetNode extends Group {
    private final Planet planet;
    private final Group selectedCircle;

    public PlanetNode(Planet planet) {
        this.planet = planet;
        selectedCircle = new Group();

        Line line = new Line();
        line.setStartX(0);
        line.setStartY(-500);
        line.setEndX(0);
        line.setEndY(500);
        line.setStroke(Color.TRANSPARENT);

        Label distance = new Label(String.format("%.4f AU", planet.getDistance()));
        distance.setStyle("-fx-font-size: 20px;" +
                " -fx-text-fill: lime");
        distance.setTranslateY(290);
        distance.translateXProperty().bind(distance.widthProperty().divide(-2));

        Line  planetLine = new Line();
        planetLine.setStartX(0);
        planetLine.setStartY(-500);
        planetLine.setEndX(0);
        planetLine.setEndY(290);
        planetLine.setStroke(Color.LIGHTSLATEGRAY);

        Circle planetImage = new Circle();
        double planetRadius = Math.min(80, planet.getRadius() * (planet.getRadiusUnit().contains("j") ? 30 : 10));

        planetImage.setRadius(planetRadius);
        Color planetColor = planet.getRadiusUnit().contains("j") ? Color.RED : Color.DARKORANGE;
        planetImage.setFill(planetColor);



        Circle selected = new Circle();
        selected.setRadius(planetRadius + 10);
        selected.setStroke(Color.LIME);
        selected.setFill(Color.TRANSPARENT);
        selectedCircle.getChildren().add(selected);
        setSelected(false);

        getChildren().addAll(line, planetLine, planetImage, distance, selectedCircle);

    }

    public void setSelected(boolean selected) {
        selectedCircle.setVisible(selected);
    }

    public Planet getPlanet() {
        return this.planet;
    }
}