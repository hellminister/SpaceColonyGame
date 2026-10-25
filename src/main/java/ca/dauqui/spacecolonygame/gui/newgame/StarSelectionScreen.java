package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.mainwindow.MainWindow;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarSelectionScreen extends GridPane {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";

    private static final int MAX_SELECTED_STARS = 4;

    private final BooleanProperty preselection;
    private final Map<StarSystemAsset, StarSystem> probedSystems;
    private final StarList stars;

    public StarSelectionScreen(MainWindow mainWindow, Launch launchProbeAction, Launch launchShipAction) {
        probedSystems = new HashMap<>();

        preselection = new SimpleBooleanProperty(false);
        setStyle("-fx-background-color: black;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: lime");
        setGridLinesVisible(true);
        var colConst1 = new ColumnConstraints();
        colConst1.setPercentWidth(50);
        var colConst2 = new ColumnConstraints();
        colConst2.setPercentWidth(50);

        getColumnConstraints().addAll(colConst1, colConst2);

        setMaxWidth(1500);
        maxHeightProperty().bind(maxWidthProperty().multiply(0.66));

        var rowConst1 = new RowConstraints();
        rowConst1.setPercentHeight(75);
        var rowConst2 = new RowConstraints();
        rowConst2.setPercentHeight(25);
        getRowConstraints().addAll(rowConst1, rowConst2);

        setHgap(10);
        setVgap(10);

        List<StarNode> starNodes = new ArrayList<>();

        VBox starListBox = new VBox();
        starListBox.setAlignment(Pos.CENTER);
        Label starListLabel = Utility.createLabel(localizedProperty("StarSelectionScreen.STAR_LIST_LABEL"));

        stars = new StarList(mainWindow.getAssets().getStarSystems(), MAX_SELECTED_STARS, starNodes, preselection);

        Label probes = Utility.createLabel(localizedProperty("StarSelectionScreen.PROBES_LEFT"));
        probes.textProperty().unbind();
        probes.textProperty().bind(Bindings.concat(localizedProperty("StarSelectionScreen.PROBES_LEFT"), " ", Bindings.subtract(MAX_SELECTED_STARS, Bindings.size(stars.getSelectedStarsForProbes())) ));
        probes.visibleProperty().bind(preselection);

        StackPane launchButtons = new StackPane();

        Button launchProbeButton = new Button();
        launchProbeButton.textProperty().bind(localizedProperty("StarSelectionScreen.LAUNCH_PROBE_BUTTON"));
        Utility.setButtonStyle(launchProbeButton);
        launchProbeButton.setOnAction(_ -> launchProbeAction.launch());
        launchProbeButton.visibleProperty().bind(preselection);

        Button launchShipButton = new Button();
        launchShipButton.textProperty().bind(localizedProperty("StarSelectionScreen.LAUNCH_SHIP_BUTTON"));
        Utility.setButtonStyle(launchShipButton);
        launchShipButton.setOnAction(_ -> launchShipAction.launch());
        launchShipButton.visibleProperty().bind(preselection.not());
        launchShipButton.disableProperty().bind(stars.getSelection().isNull());

        launchButtons.setAlignment(Pos.CENTER);
        launchButtons.getChildren().addAll(launchProbeButton,  launchShipButton);

        starListBox.getChildren().addAll(starListLabel, stars, probes, launchButtons);
        add(starListBox, 1, 0);

        VBox descriptionBox = new VBox();
        descriptionBox.setSpacing(10);
        Label descriptionTitle = Utility.createLabel(localizedProperty("StarSelectionScreen.DESCRIPTION"));

        Label description = Utility.createLabelUnbound();
        description.setWrapText(true);

        descriptionBox.getChildren().addAll(descriptionTitle, description);

        add(descriptionBox, 1, 1);

        VBox starInfoBox = new VBox();
        starInfoBox.setSpacing(10);
        starInfoBox.setAlignment(Pos.TOP_CENTER);

        Label starNameLabel = new Label();
        starNameLabel.setFont(new Font("Arial", 20));
        starNameLabel.setTextFill(Color.DARKORANGE);

        InfoGrid<StarSystemAsset> starInfosGrid = new InfoGrid<>(2);

        starInfoBox.getChildren().addAll(starNameLabel, starInfosGrid);

        Tooltip solarUnit = new Tooltip();
        solarUnit.textProperty().bind(localizedProperty("StarSelectionScreen.TOOLTIP_SOLAR_UNIT"));


        starInfosGrid.addInfo(0, 0, localizedProperty("StarSelectionScreen.SPECTRAL_LABEL"), stars.getSelection(), StarSystemAsset::getSpectralType);

        Label mass = starInfosGrid.addInfo(1, 0, localizedProperty("StarSelectionScreen.MASS_LABEL"), stars.getSelection(), star -> String.format("%.2f",
                star.getMass()));
        mass.setTooltip(solarUnit);

        starInfosGrid.addInfo(0,1, localizedProperty("StarSelectionScreen.DISTANCE_LABEL"), stars.getSelection(),
                star -> String.format("%.4f", star.getDistance()));

        starInfosGrid.addInfo(1, 1, localizedProperty("StarSelectionScreen.MAIN_SHIP_TRAVEL_LABEL"), stars.getSelection(),
                star -> String.format("%.2f", star.getDistance()/0.30));

        starInfosGrid.addInfo(0, 2, localizedProperty("StarSelectionScreen.HUMANITY_TRAVEL_LABEL"), stars.getSelection(),
                star -> String.format("%.2f", star.getDistance()/0.005));

        starInfosGrid.addInfo(1, 2, localizedProperty("StarSelectionScreen.HUMANITY_TRANSIT_SURVIVAL_LABEL"), stars.getSelection(),
                star -> String.format("%.4f %%", (1/((star.getDistance()-40)*0.00061) + 110)));

        Label visualLum = starInfosGrid.addInfo(0, 3, localizedProperty("StarSelectionScreen.VISUAL_LUM_LABEL"), stars.getSelection(),
                star -> String.format("%.4f", star.getVisualLum()));
        visualLum.setTooltip(solarUnit);

        Label bolometricLum = starInfosGrid.addInfo(1, 3, localizedProperty("StarSelectionScreen.BOLOMETRIC_LUM_LABEL"), stars.getSelection(),
                star -> String.format("%.4f", star.getBolometricLum()));
        bolometricLum.setTooltip(solarUnit);

        starInfosGrid.addInfo(0, 4, localizedProperty("StarSelectionScreen.TEFF_LABEL"), stars.getSelection(),
                star -> String.format("%.0f K", star.getTeff()));

        Label radius = starInfosGrid.addInfo(1, 4, localizedProperty("StarSelectionScreen.RADIUS_LABEL"), stars.getSelection(),
                star -> String.format("%.3f", star.getRadius()));
        radius.setTooltip(solarUnit);

        starInfosGrid.addInfo(0, 5, localizedProperty("StarSelectionScreen.NUM_KNOWN_PLANETS"), stars.getSelection(),
                star -> String.format("%d", star.getKnownPlanets().size()));

        starInfosGrid.addInfo(1, 5, localizedProperty("StarSelectionScreen.NUM_FOUND_PLANETS"), stars.getSelection(),
                star -> {
                 if (star == null) {
                     return "";
                 }
                 StarSystem star2 = probedSystems.get(star);
                 if (star2 == null) {
                     return "?";
                 } else {
                     return String.format("%d", star2.getPlanets().size());
                 }
                });

        Label habitableLabel = starInfosGrid.addInfo(0, 6, localizedProperty("StarSelectionScreen.HABITABLE_PROBABILITY_LABEL"));
        GridPane.setHalignment(habitableLabel, HPos.RIGHT);

        starInfosGrid.addInfo(1, 6, new StringBinding() {

            {
                bind(stars.getSelection());
            }

            @Override
            protected String computeValue() {
                if (stars.getSelection().get() == null) {
                    return "";
                }
                StarSystem star2 = probedSystems.get(stars.getSelection().get());
                if (star2 == null) {
                    return "  ?";
                } else {
                    return String.format("  %d", star2.getPlanets().size());
                }
            }
        });

        stars.getSelection().addListener((_, _, newValue) -> {
            if (newValue != null) {
                description.textProperty().bind(newValue.descriptionProperty());
                starNameLabel.textProperty().bind(newValue.nameProperty());
            } else {
                description.setText("");
                starNameLabel.setText("");
            }
        });

        add(starInfoBox, 0, 1);

        StarField starField = new StarField(starNodes);

        add(starField, 0, 0);

    }

    public void setPreselection(boolean b) {
        preselection.set(b);
    }

    public void reset(){
        probedSystems.clear();
        stars.reset();
    }

    public void generateProbedStarSystems() {
        probedSystems.clear();

        for (StarSystemAsset probed : stars.getSelectedStarsForProbes().values()){
            probedSystems.put(probed, new StarSystem(probed));
        }

    }

    public StarSystem getGeneratedChosenStarSystem() {
        StarSystem system = probedSystems.get(stars.getSelection().get());
        if (system == null) {
            system = new StarSystem(stars.getSelection().get());
        }

        return system;
    }

    public static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, name);
    }

    @FunctionalInterface
    public interface Launch {
        void launch();
    }

}