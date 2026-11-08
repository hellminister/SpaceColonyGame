package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.Colony;
import ca.dauqui.spacecolonygame.gamedata.ProbeAndSatelliteEffect;
import ca.dauqui.spacecolonygame.gamedata.SatelliteType;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.util.VideoLibrary;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Screen layout container for the pre-flight check and spacecraft launch sequence.
 * Orchestrates sequenced engine checklists and registers activated exploration probe data modifiers.
 *
 * @author Jean-François Quintal
 */
public class ShipLaunchScreen extends StackPane {
    private static final Logger LOG = Logger.getLogger(ShipLaunchScreen.class.getName());
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";
    private static final double ESTIMATE_BUTTON_LINE_HEIGHT = 36.0;

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

    /**
     * Constructs the primary launch control centre deck frame, binding responsive layout heights,
     * building transparent layout viewports, and generating sequential launch checklists.
     *
     * @param action Logic handler executed when confirming final screen exit procedures
     */
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

        // Sequence control routing chain linking sequential pre-flight triggers
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

        // Dynamically toggle vertical scrollbar necessity based on physical button entries threshold
        probeLaunchScroll.vbarPolicyProperty().bind(Bindings.createObjectBinding(() -> {
            if (probeLaunch.getChildren().size() > 5) {
                return ScrollPane.ScrollBarPolicy.AS_NEEDED;
            } else {
                return ScrollPane.ScrollBarPolicy.NEVER;
            }
        }, probeLaunch.getChildren()));

        // Cap scroll region size safely to wrap exactly up to a max threshold limit of 5 lines
        probeLaunchScroll.minHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            if (probeLaunch.getChildren().isEmpty()) return 0.0;
            return Math.min(ESTIMATE_BUTTON_LINE_HEIGHT * 5, probeLaunch.getChildren().size() * ESTIMATE_BUTTON_LINE_HEIGHT);
        }, probeLaunch.getChildren()));

        probeLaunchScroll.maxHeightProperty().bind(probeLaunchScroll.minHeightProperty());

        // --- CONFIGURATION OF THE PROBE SECTION ---
        Label probeLaunchSectionName = Utility.createLabel(localizedProperty("ShipLaunchScreen.PROBE_SECTION_TITLE"));
        setTitleSectionStyle(probeLaunchSectionName);

        probeLaunchSection.getChildren().addAll(probeLaunchSectionName, probeLaunchScroll);
        probeLaunchSection.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));

        // Completely hide probe panel frame nodes if available inventory tracking indices stay empty
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

        // Deterministic responsive height calculation pipeline to prevent layout shifting
        launcher.maxHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            double totalHeight = 30.0;
            double estimatedLabelHeight = 42.0; // Hauteur approximative des titres de section

            // 1. Considering the launch ship section
            if (doShipLaunch.get()) {
                totalHeight += (shipLaunch.getHeight() > 0) ? shipLaunch.getHeight() : (ESTIMATE_BUTTON_LINE_HEIGHT * 6);
            }

            // 2. Considering the Probes section (if any)
            if (!probeLaunch.getChildren().isEmpty()) {
                totalHeight += estimatedLabelHeight;
                totalHeight += Math.min(ESTIMATE_BUTTON_LINE_HEIGHT * 5, probeLaunch.getChildren().size() * ESTIMATE_BUTTON_LINE_HEIGHT);
                totalHeight += 20.0;
            }

            // 3. Considering the do next section
            totalHeight += estimatedLabelHeight + next.getHeight();
            if (next.getHeight() == 0) {
                totalHeight += ESTIMATE_BUTTON_LINE_HEIGHT;
            }

            return totalHeight + 15.0; // Buffer for borders and interior spacing
        }, doShipLaunch, shipLaunch.heightProperty(), probeLaunch.getChildren(), next.heightProperty()));

        launcher.setMinHeight(0);

        launcher.getChildren().addAll(shipLaunch, probeLaunchSection, nextSectionName, next);
        this.getChildren().add(launcher);
        this.setAlignment(Pos.CENTER);
    }

    /**
     * Applies standard uniform typographic heading styles across section header nodes.
     *
     * @param title The UI label heading node to apply formatting rules on
     */
    private static void setTitleSectionStyle(Label title) {
        title.setStyle("-fx-text-fill: BLUE;");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
    }

    /**
     * Instantiates a localized cinematic media stream retrieved via the moddable asset pipeline,
     * safely clearing background playback registers, rendering the video viewport visible,
     * and isolating interface layout nodes during animation cycles.
     *
     * @param videoPath Target relative file pathway managed by the VideoLibrary (e.g., "launch/rocket.mp4")
     */
    private void playVideo(String videoPath) {
        if (videoPath == null || videoPath.isBlank()) {
            return;
        }

        try {
            // 1. Safely dispose of the previous media player to prevent severe native memory leaks
            if (currentMediaPlayer != null) {
                currentMediaPlayer.stop();
                currentMediaPlayer.dispose();
                currentMediaPlayer = null;
            }

            // 2. Fetch the streamable resource instance via the global cascading mod system registry
            Media videoAsset = VideoLibrary.VIDEO_LIBRARY.getAsset(videoPath);
            if (videoAsset == null) {
                LOG.warning(() -> "Launch cinematic asset could not be resolved by the loader: " + videoPath);
                return;
            }

            // 3. Initialize the core media player execution context
            currentMediaPlayer = new MediaPlayer(videoAsset);
            mediaView.setMediaPlayer(currentMediaPlayer);

            // 4. Interface state swaps: Obscure control buttons and isolate the video view during playback
            launcher.setVisible(false);
            mediaView.setVisible(true);

            // 5. Setup lifecycle callbacks to bring back the UI controls as soon as the video reaches its completion gate
            currentMediaPlayer.setOnEndOfMedia(() -> {
                mediaView.setVisible(false);
                launcher.setVisible(true);
                currentMediaPlayer.stop();
                currentMediaPlayer.dispose();
                currentMediaPlayer = null;
            });

            // 6. Fire the media execution thread engine
            currentMediaPlayer.play();

        } catch (Exception e) {
            LOG.warning(() -> "Failed to engage launch cutscene video processing pipeline: " + e.getMessage());
            // Safe fallback recovery restoring operational deck controls if hardware or format errors register
            mediaView.setVisible(false);
            launcher.setVisible(true);
        }
    }

    /**
     * Wipes historical data registers, evaluates current asset maps,
     * and resets system checklist paths to baseline positions.
     *
     * @param colony Core state logic entity describing player inventory states
     */
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

    /**
     * Iterates over available orbital assets maps to generate the active probe launching checklist.
     *
     * @param colony Master inventory structure supplying current launch module units balance records
     */
    private void generateProbeList(Colony colony) {
        probeLaunch.getChildren().clear();

        for (SatelliteType satelliteType : colony.getSatelliteTypesInInventory()){
            if (ProbeAndSatelliteEffect.containsPlanetaryEffect(satelliteType.effects())){
                ButtonLine probe = new ButtonLine(satelliteType.localizedProperty());
                probe.setButtonAction(_ -> {
                    int missing = colony.removeFromSatelliteInventory(satelliteType, 1);
                    if (missing <= 0){
                        activatedEffects.addAll(satelliteType.effects());
                    }
                    if (colony.getAmountInSatelliteInventory(satelliteType) == 0){
                        probe.disableButton(true);
                    }
                });
                probeLaunch.getChildren().add(probe);
            }
        }
    }

    /**
     * Loops across generated reconnaissance probe elements to toggle their operational selection availability.
     *
     * @param disable True to safely lock down the full interaction line button array
     */
    private void setProbeListButtonDisable(boolean disable){
        for (Node node : probeLaunch.getChildren()) {
            if (node instanceof ButtonLine bl) {
                bl.disableButton(disable);
            }
        }
    }

    /**
     * @return An unmodifiable reference wrapper enclosing all probe technology metrics unlocked for this star
     */
    public Set<ProbeAndSatelliteEffect> getActiveProbeAndSatelliteEffects() {
        return Collections.unmodifiableSet(activatedEffects);
    }

    /**
     * Factory utility helper allocating configured checklist element boundaries.
     *
     * @param text Persistent key mapping localized string text parameters
     * @return Formatted button line entity block
     */
    private ButtonLine makeButtonLine(ReadOnlyStringProperty text){
        return new ButtonLine(text);
    }

    /**
     * Fetches reactive localized resource bundle properties assigned to tracking string keys.
     *
     * @param key Target translation property identifier tag
     * @return Localized text expression binding node
     */
    private static ReadOnlyStringProperty localizedProperty(String key){
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, key);
    }

    /**
     * Internal layout row component grouping an action trigger box
     * and its associated tracking display description label.
     */
    private static class ButtonLine extends HBox{
        private final Button button;

        /**
         * Hooks tracking dependencies up to the evaluation pipeline engine.
         *
         * @param text Alphanumeric text wrapper assigning label values to rows
         */
        public ButtonLine(ReadOnlyStringProperty text){
            button = new Button();
            Utility.applyOutpostSquareButtonStyle(button);

            Label label = Utility.createLabel(text);
            setAlignment(Pos.CENTER_LEFT);
            getChildren().addAll(button, label);
            maxHeightProperty().bind(Bindings.add(Bindings.max(button.heightProperty(), label.heightProperty()),2));
            setStyle("-fx-background-color: black;");
        }

        /**
         * Binds execution handlers directly onto the click interface button triggers.
         *
         * @param action Custom lambda callback tracking operational checklist routines
         */
        public void setButtonAction(EventHandler<ActionEvent> action){
            button.setOnAction(action);
        }

        /**
         * Mutates selection parameters checking if interaction gates are disabled.
         *
         * @param enable True to lock down input operations on this row checkbox trigger
         */
        public void disableButton(boolean enable){
            button.setDisable(enable);
        }
    }
}