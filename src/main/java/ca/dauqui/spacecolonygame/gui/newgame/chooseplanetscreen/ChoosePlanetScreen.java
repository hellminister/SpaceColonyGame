package ca.dauqui.spacecolonygame.gui.newgame.chooseplanetscreen;

import ca.dauqui.spacecolonygame.gamedata.ProbeAndSatelliteEffect;
import ca.dauqui.spacecolonygame.gamedata.starsystem.atmosphere.AtmosphereComposition;
import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.newgame.InfoGrid;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Screen layout container for the planetary selection phase.
 * Orchestrates layout scrolling and manages reactive focus states.
 *
 * @author Jean-François Quintal
 */
public class ChoosePlanetScreen extends VBox {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";
    private static final double PLANET_VIEW_HEIGHT_MULT = 0.66;
    private static final double INFO_GRID_HEIGHT_MULT = 0.25;
    private static final double BUTTON_BAR_HEIGHT_MULT = 0.08;

    private final ObjectProperty<StarSystem> starSystem;
    private final ObjectProperty<PlanetNode> selectedPlanet;

    private final List<PlanetNode> planets;

    private final Set<ProbeAndSatelliteEffect> probeAndSatelliteEffects;

    private final HBox planetSpace;

    /**
     * Constructs the primary planet selection layout, defining proportional resizing limits,
     * clipping regional viewport masks, and wiring localized action controls.
     *
     * @param nextButtonAction Logic handler executed when confirming the active planet selection
     */
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

        StackPane planetView = new StackPane();
        planetSpace = new HBox();

        Rectangle clip = new Rectangle();

        // Enforce rigid viewport bounds matching the layout multipliers
        planetView.maxHeightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));
        planetView.minHeightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));
        planetView.prefHeightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));

        planetSpace.maxHeightProperty().bind(planetView.heightProperty());
        planetSpace.minHeightProperty().bind(planetView.heightProperty());
        planetSpace.prefHeightProperty().bind(planetView.heightProperty());

        planetSpace.maxWidthProperty().bind(widthProperty().subtract(200));
        planetSpace.minWidthProperty().bind(widthProperty().subtract(200));
        planetSpace.prefWidthProperty().bind(widthProperty().subtract(200));

        StackPane.setAlignment(planetSpace, Pos.CENTER_LEFT);
        StackPane.setMargin(planetSpace, new Insets(0, 0, 0, 150));

        planetSpace.setAlignment(Pos.CENTER);


        clip.heightProperty().bind(heightProperty().multiply(PLANET_VIEW_HEIGHT_MULT));
        clip.widthProperty().bind(widthProperty());

        planetView.setClip(clip);

        planetView.setAlignment(Pos.CENTER);

        // Background stellar hub representing the system primary star
        Circle starCircle = new Circle();
        starSystem.addListener((_, _, newValue) -> {
            if (newValue != null) {
                starCircle.setFill(Utility.getStarColor(newValue.getStaticData().teff()));
            } else {
                starCircle.setFill(Color.BLACK);
            }
        });

        starCircle.setRadius(1000);
        starCircle.setTranslateX(-1900); // Anchor star far to the left margin

        planetView.getChildren().addAll(starCircle, planetSpace);

        addStarSystemListener();

        GridPane infoGrid = createInfoGrid();

        Button nextButton = new Button();
        Utility.setButtonStyle(nextButton);
        nextButton.textProperty().bind(Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "ChoosePlanetScreen.NEXT_BUTTON"));
        // Disabled if no destination planet is explicitly highlighted
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

    /**
     * Attaches reactive structural updates when shifting active star system context maps.
     * Clears out old node layouts and re-calculates new orbital spacing intervals.
     */
    private void addStarSystemListener() {
        starSystem.addListener((_, oldValue, newValue) -> {
            if (oldValue != newValue) {
                clearPlanets();
                if (newValue != null) {
                    List<PlanetNode> nodes = newValue.getPlanets().stream()
                            .map(this::createPlanetNode)
                            .toList();
                    planets.addAll(nodes);

                    if (!planets.isEmpty()) {
                        for (int i = 0; i < planets.size(); i++) {
                            insertPlanet(i);
                        }
                    }
                }
            }
        });
    }

    /**
     * inserts a planet in the planet view and a spacer if it is not the last planet of the system
     * @param i index of the planet to add
     */
    private void insertPlanet(int i) {
        planetSpace.getChildren().add(planets.get(i));
        if (i < planets.size() -1){
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMaxWidth(500);
            planetSpace.getChildren().add(spacer);
        }
    }

    /**
     * Dynamically instantiates a planet component and registers click interactions.
     *
     * @param planet Core data profile reference
     * @return Initialized and reactive graphical component
     */
    private PlanetNode createPlanetNode(Planet planet) {
        PlanetNode planetNode = new PlanetNode(planet, selectedPlanet);

        // Target state updater utilizing reactive bindings inside the node
        planetNode.setOnMouseClicked(_ -> selectedPlanet.set(planetNode));

        return planetNode;
    }

    /**
     * Builds and configures the detailed target data matrix panel.
     * Integrates situational probe technology visibility filter masks.
     *
     * @return Fully operational informational display matrix pane
     */
    private InfoGrid<PlanetNode> createInfoGrid() {
        InfoGrid<PlanetNode> infoGrid = new InfoGrid<>(2);

        infoGrid.maxHeightProperty().bind(heightProperty().multiply(INFO_GRID_HEIGHT_MULT));
        infoGrid.maxWidthProperty().bind(widthProperty());
        infoGrid.minHeightProperty().bind(heightProperty().multiply(INFO_GRID_HEIGHT_MULT));
        infoGrid.minWidthProperty().bind(widthProperty());
        infoGrid.prefWidthProperty().bind(widthProperty());
        infoGrid.prefHeightProperty().bind(heightProperty().multiply(INFO_GRID_HEIGHT_MULT));


        infoGrid.addInfo(0, 0, localizedProperty("ChoosePlanetScreen.TYPE"), selectedPlanet, planet -> planet.getPlanet().type());
        infoGrid.addInfo(1, 0, localizedProperty("ChoosePlanetScreen.DISTANCE"), selectedPlanet,
                planet -> String.format("%.4f %s",planet.getPlanet().distance(), localizedProperty("ChoosePlanetScreen.DISTANCE_UNIT").get()));
        infoGrid.addInfo(0, 1, localizedProperty("ChoosePlanetScreen.RADIUS"), selectedPlanet,
                planet -> Utility.formatMetric(planet.getPlanet().radius(), 3, planet.getPlanet().radiusUnit()));
        infoGrid.addInfo(1, 1, localizedProperty("ChoosePlanetScreen.MASS"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_MASS ,planet -> String.format("%.3f", planet.getPlanet().mass())));
        infoGrid.addInfo(0, 2, localizedProperty("ChoosePlanetScreen.GRAVITY"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_GRAVITY ,planet -> String.format("%.3f", planet.getPlanet().gravity())));
        infoGrid.addInfo(1, 2, localizedProperty("ChoosePlanetScreen.TEMPERATURE"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_TEMPERATURE,
                planet -> {
                    double tempKelvin = planet.getPlanet().surfaceTemperature();
                    double tempCelsius = tempKelvin - 273.15;
                    return String.format(Locale.getDefault(), "%.1f °C", tempCelsius);
                }));
        infoGrid.addInfo(0, 3, localizedProperty("ChoosePlanetScreen.ATMOSPHERIC_PRESSURE"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ATMOSPHERE_PRESSURE, planet -> {
                    if (!"PT".equals(planet.getPlanet().type())){
                        return "-";
                    }
                    return Utility.formatMetric(planet.getPlanet().atmosphericPressure(), 3, localizedProperty("ChoosePlanetScreen.PRESSURE_UNIT").get());
                }));
        var atmosphere = infoGrid.addInfo(1, 3, localizedProperty("ChoosePlanetScreen.ATMOSPHERE_COMPOSITION"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ATMOSPHERE_COMPOSITION, planet ->{
                    AtmosphereComposition atmosphereComposition = planet.getPlanet().atmosphereComposition();
                    if (!atmosphereComposition.hasAtmosphere()){
                        return localizedProperty("ChoosePlanetScreen.NO_ATMOSPHERE").get();
                    }

                    return atmosphereComposition.getAtmosphereGasesSortedByConcentration().stream()
                            .limit(2)
                            .map(gasType -> String.format("%s %.1f%%",
                                    gasType.name().get(),
                                    atmosphereComposition.getAtmosphereGasPercentage(gasType).get() * 100.0))
                            .collect(java.util.stream.Collectors.joining(", "));

                }));
        var atmosphereToolTip = new Tooltip();
        atmosphereToolTip.setShowDelay(Duration.millis(50));
        atmosphereToolTip.setHideDelay(Duration.ONE);
        atmosphereToolTip.textProperty().bind(new StringBinding() {
            {
                bind(localizedProperty("ChoosePlanetScreen.ATMOSPHERE_COMPOSITION"), selectedPlanet);
            }

            @Override
            protected String computeValue() {
                if (selectedPlanet.get() == null){
                    return "";
                }

                if (!probeAndSatelliteEffects.contains(ProbeAndSatelliteEffect.SHOW_ATMOSPHERE_COMPOSITION)){
                    return "?";
                }

                StringBuilder text = new StringBuilder();

                AtmosphereComposition atmosphereComposition = selectedPlanet.get().getPlanet().atmosphereComposition();
                if (!atmosphereComposition.hasAtmosphere()) {
                    text.append(localizedProperty("ChoosePlanetScreen.NO_ATMOSPHERE").get());
                } else {
                    text.append(atmosphereComposition.getAtmosphereGasesSortedByConcentration().stream().filter(gasType -> atmosphereComposition.getAtmosphereGasPercentage(gasType).get() > 0.0)
                            .map(gasType -> String.format("%s %.1f%%",
                                    gasType.name().get(),
                                    atmosphereComposition.getAtmosphereGasPercentage(gasType).get() * 100.0))
                            .collect(java.util.stream.Collectors.joining("\n")));
                }

                if (atmosphereComposition.hasCondensedGases()){
                    text.append("\n\n").append("CONDENSED GASES").append("\n\n")
                            .append(atmosphereComposition.getCondensedGasesSortedByConcentration().stream().filter(gasType -> atmosphereComposition.getCondensedGasPercentage(gasType).get() > 0.0)
                                    .map(gasType -> String.format("%s %.1f%%",
                                            gasType.name().get(),
                                            atmosphereComposition.getCondensedGasPercentage(gasType).get() * 100.0))
                                    .collect(java.util.stream.Collectors.joining("\n")));
                }



                return text.toString();
            }
        });
        atmosphere.setTooltip(atmosphereToolTip);
        infoGrid.addInfo(0, 4, localizedProperty("ChoosePlanetScreen.TECTONIC_TYPE"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_TECTONIC, planet -> planet.getPlanet().tectonic().toString()));
        infoGrid.addInfo(1, 4, localizedProperty("ChoosePlanetScreen.VOLCANISM_LEVEL"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_VOLCANISM,
                        planet -> Utility.formatMetric(planet.getPlanet().volcanism(), 2, localizedProperty("ChoosePlanetScreen.VOLCANISM_UNIT").get())));
        infoGrid.addInfo(0, 5, localizedProperty("ChoosePlanetScreen.MAGNETIC_FIELD_STRENGTH"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_MAGNETIC_STRENGTH, planet -> String.format("%.2f %s",planet.getPlanet().magneticField(), localizedProperty("ChoosePlanetScreen.MAGNETIC_FIELD_UNIT").get())));
        infoGrid.addInfo(1, 5, localizedProperty("ChoosePlanetScreen.ALBEDO"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ALBEDO, planet -> String.format("%.0f %%",planet.getPlanet().albedo() * 100)));
        infoGrid.addInfo(0, 6, localizedProperty("ChoosePlanetScreen.ORBITAL_PERIOD_DAYS"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ORBITAL_PERIOD,
                        planet -> Utility.formatMetric(planet.getPlanet().orbitalPeriodDays(), 2, localizedProperty("ChoosePlanetScreen.ORBITAL_PERIOD_DAYS_UNIT").get())));
        infoGrid.addInfo(1, 6, localizedProperty("ChoosePlanetScreen.ROTATION_PERIOD_HOURS"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ROTATION_PERIOD,
                        planet -> Utility.formatMetric(planet.getPlanet().rotationPeriodHours(), 2, localizedProperty("ChoosePlanetScreen.ROTATION_PERIOD_HOURS_UNIT").get())));
        infoGrid.addInfo(0, 7, localizedProperty("ChoosePlanetScreen.AXIS_TILT"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_AXIS_TILT, planet -> String.format("%.1f°", planet.getPlanet().axisTilt())));
        infoGrid.addInfo(1, 7, localizedProperty("ChoosePlanetScreen.IS_TIDALLY_LOCKED"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_ROTATION_PERIOD,
                        planet -> planet.getPlanet().isTidallyLocked() ? localizedProperty("ChoosePlanetScreen.YES").get() : localizedProperty("ChoosePlanetScreen.NO").get()));
        infoGrid.addInfo(0, 8, localizedProperty("ChoosePlanetScreen.RECEIVED_LIGHT"), selectedPlanet,
                planet -> Utility.formatMetric(planet.getPlanet().receivedLight(), 3, localizedProperty("ChoosePlanetScreen.RECEIVED_LIGHT_UNIT").get()));
        infoGrid.addInfo(1, 8, localizedProperty("ChoosePlanetScreen.NUMBER_OF_SITES"), selectedPlanet,
                new PlanetFieldExtraction(ProbeAndSatelliteEffect.SHOW_SITE_COUNT,
                        planet -> String.format("%d", planet.getPlanet().numberOfSites())));

        infoGrid.setAlignment(Pos.CENTER);

        return infoGrid;
    }

    /**
     * Erases telemetry data structures and unlocks selection pointers upon system unloading.
     */
    private void clearPlanets() {
        selectedPlanet.set(null);
        planets.clear();
        planetSpace.getChildren().clear();
        probeAndSatelliteEffects.clear();
    }

    /**
     * Binds a target astronomical stellar matrix structure to drive current system layouts.
     */
    public void setSystem(StarSystem chosenStarSystem) {
        starSystem.set(chosenStarSystem);
    }

    /**
     * @return The underlying physical core planet profile linked to the highlighted item
     */
    public Planet getPlanet() {
        return this.selectedPlanet.get().getPlanet();
    }

    private static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, name);
    }

    /**
     * Updates structural scanning data modifiers uncovered via orbital scan technologies.
     */
    public void setActiveProbeAndSatelliteEffects(Set<ProbeAndSatelliteEffect> activeProbeAndSatelliteEffects) {
        probeAndSatelliteEffects.addAll(activeProbeAndSatelliteEffects);
    }

    /**
     * Internal extraction proxy masking telemetry fields unless specific technology
     * detection traits are registered.
     */
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