package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ChoosePlanetScreen extends VBox {
    private final ObjectProperty<StarSystem> starSystem;
    private final ObjectProperty<PlanetNode> selectedPlanet;

    private final List<PlanetNode> planets;
    private final StackPane planetView;

    public ChoosePlanetScreen() {
        this.starSystem = new SimpleObjectProperty<>();
        this.selectedPlanet = new SimpleObjectProperty<>();
        setStyle("-fx-background-color: black;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: lime");

        planets = new ArrayList<>();

        setMaxWidth(1500);
        setMinWidth(100);
        maxHeightProperty().bind(maxWidthProperty().multiply(0.66));

        planetView = new StackPane();
        Rectangle clip = new Rectangle();

        planetView.maxHeightProperty().bind(heightProperty().multiply(0.66));
        planetView.minHeightProperty().bind(heightProperty().multiply(0.66));
        planetView.prefHeightProperty().bind(heightProperty().multiply(0.66));

        clip.heightProperty().bind(heightProperty().multiply(0.66));
        clip.widthProperty().bind(widthProperty());

        planetView.setClip(clip);

        planetView.setAlignment(Pos.CENTER);


        Circle starCircle = new Circle();
        starSystem.addListener((_, _, newValue) -> {
            if (newValue != null) {
                starCircle.setFill(Utility.getStarColor(newValue.getStaticData().getTeff()));
            } else {
                starCircle.setFill(Color.BLACK);
            }
        });

        starCircle.setRadius(1000);
        starCircle.setTranslateX(-1900);

        planetView.getChildren().add(starCircle);

        starSystem.addListener((_, oldValue, newValue) -> {
            if (oldValue != newValue) {
                if (newValue != null) {
                    clearPlanets();
                    for (Planet planet : newValue.getPlanets()) {
                        PlanetNode planetNode = new PlanetNode(planet);
                        planetNode.setOnMouseClicked(_ -> {
                            if (selectedPlanet.get() != null) {
                                selectedPlanet.get().setSelected(false);
                            }
                            selectedPlanet.set(planetNode);
                            planetNode.setSelected(true);
                        });


                        planets.add(planetNode);
                    }

                    double interval = 1200.0/planets.size();
                    int i = 0;

                    for (PlanetNode planetNode : planets) {
                        planetNode.setTranslateX(-700 + interval * i++);
                    }

                    planetView.getChildren().addAll(planets);
                } else {
                    clearPlanets();
                }
            }
        });


        GridPane infoGrid = createInfoGrid();

        getChildren().addAll(planetView, infoGrid);
    }

    private GridPane createInfoGrid() {
        GridPane infoGrid = new GridPane();

        infoGrid.maxHeightProperty().bind(heightProperty().multiply(0.33));
        infoGrid.maxWidthProperty().bind(widthProperty());
        infoGrid.minHeightProperty().bind(heightProperty().multiply(0.33));
        infoGrid.minWidthProperty().bind(widthProperty());
        infoGrid.prefWidthProperty().bind(widthProperty());
        infoGrid.prefHeightProperty().bind(heightProperty().multiply(0.33));

        infoGrid.setStyle("-fx-background-color: black;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: blue");






        return infoGrid;
    }

    private void clearPlanets() {
        planetView.getChildren().removeAll(planets);
        planets.clear();
        selectedPlanet.set(null);
    }

    public void setSystem(StarSystem chosenStarSystem) {
        starSystem.set(chosenStarSystem);
    }

    public Planet getPlanet() {
        return this.selectedPlanet.get().getPlanet();
    }
}