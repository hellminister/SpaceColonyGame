package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChoosePlanetScreen extends VBox {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";

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

        Button nextButton = new Button();
        Utility.setButtonStyle(nextButton);
        nextButton.textProperty().bind(Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "ChoosePlanetScreen.nextButton"));
        nextButton.disableProperty().bind(selectedPlanet.isNull());


        getChildren().addAll(planetView, infoGrid, nextButton);
    }

    private InfoGrid<PlanetNode> createInfoGrid() {
        InfoGrid<PlanetNode> infoGrid = new InfoGrid<>(2);

        infoGrid.maxHeightProperty().bind(heightProperty().multiply(0.33));
        infoGrid.maxWidthProperty().bind(widthProperty());
        infoGrid.minHeightProperty().bind(heightProperty().multiply(0.33));
        infoGrid.minWidthProperty().bind(widthProperty());
        infoGrid.prefWidthProperty().bind(widthProperty());
        infoGrid.prefHeightProperty().bind(heightProperty().multiply(0.33));

        infoGrid.setStyle("-fx-background-color: black;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: blue");


        infoGrid.addInfo(0, 0, localizedProperty("ChoosePlanetScreen.TYPE"), selectedPlanet, planet -> planet.getPlanet().getType());
        infoGrid.addInfo(1, 0, localizedProperty("ChoosePlanetScreen.DISTANCE"), selectedPlanet, planet -> planet.getPlanet().getDistance() + " AU");
        infoGrid.addInfo(0, 1, localizedProperty("ChoosePlanetScreen.RADIUS"), selectedPlanet, planet -> planet.getPlanet().getRadius() + " " + planet.getPlanet().getRadiusUnit());
        infoGrid.addInfo(1, 1, localizedProperty("ChoosePlanetScreen.MASS"), selectedPlanet, planet -> String.valueOf(planet.getPlanet().getMass()));
        infoGrid.addInfo(0, 2, localizedProperty("ChoosePlanetScreen.GRAVITY"), selectedPlanet, planet -> String.valueOf(planet.getPlanet().getGravity()));
        infoGrid.addInfo(1, 2, localizedProperty("ChoosePlanetScreen.TEMPERATURE"), selectedPlanet,
                planet -> {
                                       double tempKelvin = planet.getPlanet().getTemperature();
                                       double tempCelsius = tempKelvin - 273.15;
                                       return String.format(Locale.getDefault(), "%.1f °C", tempCelsius);
        });
        infoGrid.addInfo(0, 3, localizedProperty("ChoosePlanetScreen.ATMOSPHERIC_PRESSURE"), selectedPlanet,
                planet -> planet.getPlanet().getAtmosphericPressure() + " " + localizedProperty("ChoosePlanetScreen.PRESSURE_UNIT").get());
        infoGrid.addInfo(1, 3, localizedProperty("ChoosePlanetScreen.ATMOSPHERE_COMPOSITION"), selectedPlanet, planet -> planet.getPlanet().getMainAtmosphereGaz().toString());
        infoGrid.addInfo(0, 4, localizedProperty("ChoosePlanetScreen.TECTONIC_TYPE"), selectedPlanet, planet -> planet.getPlanet().getTectonic().toString());
        infoGrid.addInfo(1, 4, localizedProperty("ChoosePlanetScreen.VOLCANISM_LEVEL"), selectedPlanet, planet -> planet.getPlanet().getVolcanism().toString());
        infoGrid.addInfo(0, 5, localizedProperty("ChoosePlanetScreen.MAGNETIC_FIELD_STRENGTH"), selectedPlanet, planet -> planet.getPlanet().getMagneticField().toString());
        infoGrid.addInfo(1, 5, localizedProperty("ChoosePlanetScreen.ALBEDO"), selectedPlanet, planet -> String.format(Locale.getDefault(), "%.0f %%",
                planet.getPlanet().getAlbedo() * 100));
        infoGrid.addInfo(0, 6, localizedProperty("ChoosePlanetScreen.ORBITAL_PERIOD_DAYS"), selectedPlanet,
                planet -> planet.getPlanet().getOrbitalPeriodDays() + " " + localizedProperty("ChoosePlanetScreen.ORBITAL_PERIOD_DAYS_UNIT").get());
        infoGrid.addInfo(1, 6, localizedProperty("ChoosePlanetScreen.ROTATION_PERIOD_HOURS"), selectedPlanet,
                planet -> planet.getPlanet().getRotationPeriodHours() + " " + localizedProperty(
                "ChoosePlanetScreen.ROTATION_PERIOD_HOURS_UNIT").get());
        infoGrid.addInfo(0, 7, localizedProperty("ChoosePlanetScreen.AXIS_TILT"), selectedPlanet, planet -> String.format(Locale.getDefault(), "%.1f°",
                planet.getPlanet().getAxisTilt()));
        infoGrid.addInfo(1, 7, localizedProperty("ChoosePlanetScreen.IS_TIDALLY_LOCKED"), selectedPlanet,
                planet -> planet.getPlanet().isTidallyLocked() ? localizedProperty("ChoosePlanetScreen.YES").get()
                : localizedProperty("ChoosePlanetScreen.NO").get());



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

    public static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, name);
    }
}