package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.Colony;
import ca.dauqui.spacecolonygame.gamedata.ProbeAndSatelliteEffect;
import ca.dauqui.spacecolonygame.gamedata.SatelliteType;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class ShipLaunchScreen extends StackPane {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";
    private static final double ESTIMATE_BUTTONLINE_HEIGHT = 36.0;



    private final Set<ProbeAndSatelliteEffect> activatedEffects;
    private final MediaView mediaView;
    private MediaPlayer currentMediaPlayer;
    private final VBox launcher;
    private final VBox shipLaunch;
    private final BooleanProperty doShipLaunch;
    private final VBox probeLaunch;

    private final ButtonLine next;

    private final ButtonLine pressurizeFuel;
    private final ButtonLine launchShip;
    private final ButtonLine energizeSatelliteLaserBeams;
    private final ButtonLine deploySolarSail;
    private final ButtonLine reactorToFullPower;

    public ShipLaunchScreen(EventHandler<ActionEvent> action) {
        super();
        activatedEffects = EnumSet.noneOf(ProbeAndSatelliteEffect.class);
        setStyle("-fx-background-color: transparent;");

        mediaView = new MediaView();
        mediaView.preserveRatioProperty().set(true);
        mediaView.fitWidthProperty().bind(this.widthProperty());
        mediaView.fitHeightProperty().bind(this.heightProperty());
        mediaView.setVisible(false);
        this.getChildren().add(mediaView);
        doShipLaunch = new SimpleBooleanProperty(true);
        shipLaunch = new VBox();
        shipLaunch.visibleProperty().bind(doShipLaunch);
        shipLaunch.setStyle("-fx-background-color: transparent;");

        next = makeButtonLine(localizedProperty("ShipLaunchScreen.NEXT"));
        next.setButtonAction(action);

        launchShip= new ButtonLine(localizedProperty("ShipLaunchScreen.LAUNCH_SHIP"));
        launchShip.setButtonAction(_ -> {
            playVideo("");
            launchShip.disableButton(true);
            next.disableButton(false);
            setProbeListButtonDisable(false);
        });

        energizeSatelliteLaserBeams= new ButtonLine(localizedProperty("ShipLaunchScreen.ENERGIZE_SATELLITE_LASER_BEAM"));
        energizeSatelliteLaserBeams.setButtonAction(_ -> {
            playVideo("");
            energizeSatelliteLaserBeams.disableButton(true);
            launchShip.disableButton(false);
        });

        deploySolarSail= new ButtonLine(localizedProperty("ShipLaunchScreen.DEPLOY_SOLAR_SAIL"));
        deploySolarSail.setButtonAction(_ -> {
            playVideo("");
            deploySolarSail.disableButton(true);
            energizeSatelliteLaserBeams.disableButton(false);
        });

        reactorToFullPower= new ButtonLine(localizedProperty("ShipLaunchScreen.REACTOR_TO_FULL_POWER"));
        reactorToFullPower.setButtonAction(_ -> {
            playVideo("");
            reactorToFullPower.disableButton(true);
            deploySolarSail.disableButton(false);
        });

        pressurizeFuel = new ButtonLine(localizedProperty("ShipLaunchScreen.PRESSURIZE_FUEL"));
        pressurizeFuel.setButtonAction(_ -> {
            playVideo("");
            pressurizeFuel.disableButton(true);
            reactorToFullPower.disableButton(false);
        });

        Label shipLaunchSectionName = Utility.createLabel(localizedProperty("ShipLaunchScreen.LAUNCH_SHIP_SECTION_TITLE"));
        setTitleSectionStyle(shipLaunchSectionName);

        shipLaunch.setSpacing(5.0);

        shipLaunch.getChildren().addAll(shipLaunchSectionName, pressurizeFuel, reactorToFullPower, deploySolarSail, energizeSatelliteLaserBeams, launchShip);

        VBox probeLaunchSection = new VBox();

        ScrollPane probeLaunchScroll = new ScrollPane();

        probeLaunch = new VBox();
        probeLaunch.setStyle("-fx-background-color: transparent;");
        probeLaunch.setSpacing(5.0);
        probeLaunchScroll.setStyle("-fx-background-color: transparent; " +
                "-fx-background: transparent; " +
                "-fx-padding: 0; " +
                "-fx-background-insets: 0;");
        probeLaunchScroll.getStylesheets().add(
                "data:text/css,.scroll-pane .viewport { -fx-background-color: transparent; }"
        );
        probeLaunchScroll.setContent(probeLaunch);
        probeLaunchScroll.setFitToWidth(true);
        probeLaunchScroll.setFitToHeight(true);

        probeLaunchScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        probeLaunchScroll.vbarPolicyProperty().bind(Bindings.createObjectBinding(() -> {
            if (probeLaunch.getChildren().size() > 5) {
                return ScrollPane.ScrollBarPolicy.AS_NEEDED;
            } else {
                return ScrollPane.ScrollBarPolicy.NEVER;
            }
        }, probeLaunch.getChildren()));


        probeLaunchScroll.minHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            if (probeLaunch.getChildren().isEmpty()) return 0.0;
            // Calcule la hauteur exacte requise sans jamais dépasser la taille de 5 boutons
            return Math.min(ESTIMATE_BUTTONLINE_HEIGHT * 5, probeLaunch.getChildren().size() * ESTIMATE_BUTTONLINE_HEIGHT);
        }, probeLaunch.getChildren()));

        probeLaunchScroll.maxHeightProperty().bind(probeLaunchScroll.minHeightProperty());

        // --- CONFIGURATION DE LA SECTION DES SONDES ---
        Label probeLaunchSectionName = Utility.createLabel(localizedProperty("ShipLaunchScreen.PROBE_SECTION_TITLE"));
        setTitleSectionStyle(probeLaunchSectionName);

        probeLaunchSection.getChildren().addAll(probeLaunchSectionName, probeLaunchScroll);
        probeLaunchSection.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));

        // La section se masque et libère sa place verticalement si aucune sonde n'est disponible
        probeLaunchSection.visibleProperty().bind(Bindings.isNotEmpty(probeLaunch.getChildren()));
        probeLaunchSection.managedProperty().bind(probeLaunchSection.visibleProperty());

        Label nextSectionName = Utility.createLabel(localizedProperty("ShipLaunchScreen.NEXT_SECTION_TITLE"));
        setTitleSectionStyle(nextSectionName);

        // --- CONFIGURATION DU CONTENEUR LAUNCHER ---
        launcher = new VBox();
        launcher.setPadding(new javafx.geometry.Insets(15));
        launcher.setStyle("-fx-background-color: black;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: lime");

        launcher.setMaxWidth(400);

        // Calcul déterministe de la hauteur globale basé sur les listes pour éviter le décalage asynchrone
        launcher.maxHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            double totalHeight = 30.0;
            double estimatedLabelHeight = 42.0; // Hauteur approximative des titres de section

            // 1. Prise en compte de la section Vaisseau
            if (doShipLaunch.get()) {
                totalHeight += (shipLaunch.getHeight() > 0) ? shipLaunch.getHeight() : (ESTIMATE_BUTTONLINE_HEIGHT * 6);
            }

            // 2. Prise en compte de la section Sondes (seulement si elle contient des éléments)
            if (!probeLaunch.getChildren().isEmpty()) {
                totalHeight += estimatedLabelHeight;
                totalHeight += Math.min(ESTIMATE_BUTTONLINE_HEIGHT * 5, probeLaunch.getChildren().size() * ESTIMATE_BUTTONLINE_HEIGHT);
                totalHeight += 20.0;
            }

            // 3. Prise en compte de la section Suivant
            totalHeight += estimatedLabelHeight + next.getHeight();
            if (next.getHeight() == 0) {
                totalHeight += ESTIMATE_BUTTONLINE_HEIGHT; // Valeur refuge si pas encore dessiné
            }

            return totalHeight + 15.0; // Petit tampon pour les bordures et espacements internes
        }, doShipLaunch, shipLaunch.heightProperty(), probeLaunch.getChildren(), next.heightProperty()));

        launcher.setMinHeight(0);

        launcher.getChildren().addAll(shipLaunch, probeLaunchSection, nextSectionName, next);
        this.getChildren().add(launcher);
        this.setAlignment(Pos.CENTER);
    }

    private static void setTitleSectionStyle(Label title) {
        title.setStyle("-fx-text-fill: BLUE;");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
    }

    private void playVideo(String s) {
    }

    public void reset(Colony colony) {
        activatedEffects.clear();
        generateProbeList(colony);
        if (doShipLaunch.get()) {
            next.disableButton(true);
            setProbeListButtonDisable(true);
            pressurizeFuel.disableButton(false);
            launchShip.disableButton(true);
            energizeSatelliteLaserBeams.disableButton(true);
            deploySolarSail.disableButton(true);
            reactorToFullPower.disableButton(true);
        } else {
            next.disableButton(false);
            setProbeListButtonDisable(false);
        }
        launcher.requestLayout();
    }

    private void generateProbeList(Colony colony) {
        probeLaunch.getChildren().clear();

        for (SatelliteType satelliteType : colony.getSatelliteTypesInInventory()){
            if (ProbeAndSatelliteEffect.containsPlanetaryEffect(satelliteType.getEffects())){
                ButtonLine probe = new ButtonLine(satelliteType.localizedProperty());
                probe.setButtonAction(_ -> {
                    int missing = colony.removeFromSatelliteInventory(satelliteType, 1);
                    if (missing <= 0){
                        activatedEffects.addAll(satelliteType.getEffects());
                    }
                    if (colony.getAmountInSatelliteInventory(satelliteType) == 0){
                        probe.disableButton(true);
                    }
                });
                probeLaunch.getChildren().add(probe);
            }
        }
    }

    private void setProbeListButtonDisable(boolean disable){
        for (Node node : probeLaunch.getChildren()) {
            if (node instanceof ButtonLine bl) {
                bl.setDisable(disable);
            }
        }
    }

    public Set<ProbeAndSatelliteEffect> getActiveProbeAndSatelliteEffects() {
        return Collections.unmodifiableSet(activatedEffects);
    }

    private ButtonLine makeButtonLine(ReadOnlyStringProperty text){
        return new ButtonLine(text);
    }

    private static ReadOnlyStringProperty localizedProperty(String key){
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, key);
    }

    private static class ButtonLine extends HBox{
        private final Button button;

        public ButtonLine(ReadOnlyStringProperty text){
            button = new Button();
            Utility.applyOutpostSquareButtonStyle(button);

            Label label = Utility.createLabel(text);
            setAlignment(Pos.CENTER_LEFT);
            getChildren().addAll(button, label);
            maxHeightProperty().bind(Bindings.add(Bindings.max(button.heightProperty(), label.heightProperty()),2));
            setStyle("-fx-background-color: black;");
        }
        public void setButtonAction(EventHandler<ActionEvent> action){
            button.setOnAction(action);
        }

        public void disableButton(boolean enable){
            button.setDisable(enable);
        }
    }
}