package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.ProbeAndSatelliteEffect;
import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class ChoosePlanetScreen extends VBox {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";
    private static final double PLANET_VIEW_HEIGHT_MULT = 0.66;
    private static final double INFO_GRID_HEIGHT_MULT = 0.25;
    private static final double BUTTON_BAR_HEIGHT_MULT = 0.08;



    private final ObjectProperty<StarSystem> starSystem;
    private final ObjectProperty<PlanetNode> selectedPlanet;

    private final List<PlanetNode> planets;
    private final StackPane planetView;

    private final Set<ProbeAndSatelliteEffect> probeAndSatelliteEffects;

    public ChoosePlanetScreen(EventHandler<ActionEvent> nextButtonAction) {
        this.starSystem = new SimpleObjectProperty<>();
        this.selectedPlanet = new SimpleObjectProperty<>();
        probeAndSatelliteEffects = EnumSet.noneOf(ProbeAndSatelliteEffect.class);

        setStyle("-fx-background-color: black;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: lime");

        planets = new ArrayList<>();

        setMaxWidth(1500);
        setMinWidth(100);
        maxHeightProperty().bind(maxWidthProperty().multiply(0.66));

        planetView = new StackPane();

        Rectangle clip = new Rectangle();

        planetView.maxHeightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));
        planetView.minHeightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));
        planetView.prefHeightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));

        clip.heightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));
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
        nextButton.textProperty().bind(Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "ChoosePlanetScreen.NEXT_BUTTON"));
        nextButton.disableProperty().bind(selectedPlanet.isNull());
        nextButton.setOnAction(nextButtonAction);

        var buttonPane = new HBox();

        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.getChildren().addAll(nextButton);

        buttonPane.maxHeightProperty().bind(heightProperty().multiply(BUTTON_BAR_HEIGHT_MULT));
        buttonPane.minHeightProperty().bind(heightProperty().multiply(BUTTON_BAR_HEIGHT_MULT));
        buttonPane.prefHeightProperty().bind(heightProperty().multiply(BUTTON_BAR_HEIGHT_MULT));

        getChildren().addAll(planetView, infoGrid, buttonPane);
    }

    private InfoGrid<PlanetNode> createInfoGrid() {
        InfoGrid<PlanetNode> infoGrid = new InfoGrid<>(2);

        infoGrid.maxHeightProperty().bind(heightProperty().multiply(INFO_GRID_HEIGHT_MULT));
        infoGrid.maxWidthProperty().bind(widthProperty());
        infoGrid.minHeightProperty().bind(heightProperty().multiply(INFO_GRID_HEIGHT_MULT));
        infoGrid.minWidthProperty().bind(widthProperty());
        infoGrid.prefWidthProperty().bind(widthProperty());
        infoGrid.prefHeightProperty().bind(heightProperty().multiply(INFO_GRID_HEIGHT_MULT));


        infoGrid.addInfo(0, 0, localizedProperty("ChoosePlanetScreen.TYPE"), selectedPlanet, planet -> planet.getPlanet().getType());
        infoGrid.addInfo(1, 0, localizedProperty("ChoosePlanetScreen.DISTANCE"), selectedPlanet,
                planet -> String.format("%.4f %s",planet.getPlanet().getDistance(), localizedProperty("ChoosePlanetScreen.DISTANCE_UNIT").get()));
        infoGrid.addInfo(0, 1, localizedProperty("ChoosePlanetScreen.RADIUS"), selectedPlanet,
                planet -> String.format("%.3f %s",planet.getPlanet().getRadius(), planet.getPlanet().getRadiusUnit()));
        infoGrid.addInfo(1, 1, localizedProperty("ChoosePlanetScreen.MASS"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_MASS ,planet -> String.format("%.3f", planet.getPlanet().getMass())));
        infoGrid.addInfo(0, 2, localizedProperty("ChoosePlanetScreen.GRAVITY"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_GRAVITY ,planet -> String.format("%.3f", planet.getPlanet().getGravity())));
        infoGrid.addInfo(1, 2, localizedProperty("ChoosePlanetScreen.TEMPERATURE"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_TEMPERATURE,
                planet -> {
                    double tempKelvin = planet.getPlanet().getTemperature();
                    double tempCelsius = tempKelvin - 273.15;
                    return String.format(Locale.getDefault(), "%.1f °C", tempCelsius);
                }));
        infoGrid.addInfo(0, 3, localizedProperty("ChoosePlanetScreen.ATMOSPHERIC_PRESSURE"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ATMOSPHERE_PRESSURE,
                        planet -> String.format("%.3f %s",planet.getPlanet().getAtmosphericPressure(), localizedProperty("ChoosePlanetScreen.PRESSURE_UNIT").get())));
        infoGrid.addInfo(1, 3, localizedProperty("ChoosePlanetScreen.ATMOSPHERE_COMPOSITION"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ATMOSPHERE_COMPOSITION, planet -> planet.getPlanet().getMainAtmosphereGaz().toString()));
        infoGrid.addInfo(0, 4, localizedProperty("ChoosePlanetScreen.TECTONIC_TYPE"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_TECTONIC, planet -> planet.getPlanet().getTectonic().toString()));
        infoGrid.addInfo(1, 4, localizedProperty("ChoosePlanetScreen.VOLCANISM_LEVEL"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_VOLCANISM,
                        planet -> String.format("%.2f %s",planet.getPlanet().getVolcanism(), localizedProperty("ChoosePlanetScreen.VOLCANISM_UNIT").get())));
        infoGrid.addInfo(0, 5, localizedProperty("ChoosePlanetScreen.MAGNETIC_FIELD_STRENGTH"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_MAGNETIC_STRENGTH, planet -> String.format("%.2f %s",planet.getPlanet().getMagneticField(), localizedProperty("ChoosePlanetScreen.MAGNETIC_FIELD_UNIT").get())));
        infoGrid.addInfo(1, 5, localizedProperty("ChoosePlanetScreen.ALBEDO"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ALBEDO, planet -> String.format("%.0f %%",planet.getPlanet().getAlbedo() * 100)));
        infoGrid.addInfo(0, 6, localizedProperty("ChoosePlanetScreen.ORBITAL_PERIOD_DAYS"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ORBITAL_PERIOD,
                        planet -> String.format("%.2f %s", planet.getPlanet().getOrbitalPeriodDays(), localizedProperty("ChoosePlanetScreen.ORBITAL_PERIOD_DAYS_UNIT").get())));
        infoGrid.addInfo(1, 6, localizedProperty("ChoosePlanetScreen.ROTATION_PERIOD_HOURS"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ROTATION_PERIOD,
                        planet -> String.format("%.2f %s",planet.getPlanet().getRotationPeriodHours(), localizedProperty("ChoosePlanetScreen.ROTATION_PERIOD_HOURS_UNIT").get())));
        infoGrid.addInfo(0, 7, localizedProperty("ChoosePlanetScreen.AXIS_TILT"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_AXIS_TILT, planet -> String.format("%.1f°", planet.getPlanet().getAxisTilt())));
        infoGrid.addInfo(1, 7, localizedProperty("ChoosePlanetScreen.IS_TIDALLY_LOCKED"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ROTATION_PERIOD,
                        planet -> planet.getPlanet().isTidallyLocked() ? localizedProperty("ChoosePlanetScreen.YES").get() : localizedProperty("ChoosePlanetScreen.NO").get()));
        infoGrid.addInfo(0, 8, localizedProperty("ChoosePlanetScreen.RECEIVED_LIGHT"), selectedPlanet,
                planet -> String.format("%.3f %s",planet.getPlanet().getReceivedLight(), localizedProperty("ChoosePlanetScreen.RECEIVED_LIGHT_UNIT").get()));
        infoGrid.addInfo(1, 8, localizedProperty("ChoosePlanetScreen.NUMBER_OF_SITES"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_SITE_COUNT,
                        planet -> String.format("%d", planet.getPlanet().getNumberOfSites())));

        infoGrid.setAlignment(Pos.CENTER);

        return infoGrid;
    }

    private void clearPlanets() {
        selectedPlanet.set(null);
        planets.clear();
        probeAndSatelliteEffects.clear();
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

    public void setActiveProbeAndSatelliteEffects(Set<ProbeAndSatelliteEffect> activeProbeAndSatelliteEffects) {
        probeAndSatelliteEffects.addAll(activeProbeAndSatelliteEffects);
    }

    private class PlanetFieldExtraction implements InfoGrid.ExtractFieldString<PlanetNode> {
        private final ProbeAndSatelliteEffect neededEffect;
        private final InfoGrid.ExtractFieldString<PlanetNode> extractor;

        private PlanetFieldExtraction(ProbeAndSatelliteEffect neededEffect, InfoGrid.ExtractFieldString<PlanetNode> extractor) {
            this.neededEffect = neededEffect;
            this.extractor = extractor;
        }

        @Override
        public String extractString(PlanetNode star) {
            if (probeAndSatelliteEffects.contains(neededEffect)) {
                return extractor.extractString(star);
            }
            return "?";
        }
    }
}