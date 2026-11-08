package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gui.common.BackgroundImagePane;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.mainwindow.MainWindow;
import ca.dauqui.spacecolonygame.gui.newgame.cargoloader.ShipCargoLoader;
import ca.dauqui.spacecolonygame.gui.newgame.chooseplanetscreen.ChoosePlanetScreen;
import ca.dauqui.spacecolonygame.gui.newgame.starselectionscreen.StarSelectionScreen;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

/**
 * Master state machine scene managing the chronological registration panels
 * and orchestration screens for starting a new campaign session.
 * Actively toggles visibility scopes to guide players across initialization checkpoints.
 *
 * @author Jean-François Quintal
 */
public class NewGameScene extends Scene {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";


    private final MainWindow mainWindow;

    private final Node readStory;
    private final Node commanderName;
    private final Node aiName;
    private final StarSelectionScreen starSelection;
    private final Node shipLoading;
    private final ShipLaunchScreen shipTakeOff;
    private ShipCargoLoader cargoLoader;
    private final ChoosePlanetScreen choosePlanet;
    private final Node siteChooser;

    /**
     * Constructs the root orchestration canvas scene, layering all setup panel instances
     * simultaneously and configuring baseline visibility masks.
     *
     * @param mainWindow Core primary application window frame supervising current scene swaps
     */
    public NewGameScene(MainWindow mainWindow) {
        super(new StackPane());
        var root = (StackPane) getRoot();
        this.mainWindow = mainWindow;



        root.setStyle("-fx-background-color: black");
        root.setAlignment(Pos.CENTER);

        // this pane is to permit resizing of the window
        var backgroundImagePane = new BackgroundImagePane(root, "milky-way-full-stars-space.jpg");
        root.getChildren().add(backgroundImagePane);

        readStory = createReadStory();
        commanderName = createCommanderName();
        aiName = createAiName();
        starSelection = createStarSelection();
        shipLoading = createShipLoading();
        shipTakeOff = createShipTakeoff();
        choosePlanet = createChoosePlanet();
        siteChooser = createSiteChooser();

        root.getChildren().addAll(readStory, commanderName, aiName, starSelection, shipLoading, shipTakeOff, choosePlanet, siteChooser);

        // Isolate the narrative window as the mandatory initial step entry gate
        readStory.setVisible(true);
        commanderName.setVisible(false);
        aiName.setVisible(false);
        shipLoading.setVisible(false);
        starSelection.setVisible(false);
        starSelection.setPreselection(true);
        shipTakeOff.setVisible(false);
        choosePlanet.setVisible(false);
        siteChooser.setVisible(false);

    }

    /**
     * Instantiates the prologue textual storytelling narrative overlay pane.
     *
     * @return Fully operational narrative UI card component node
     */
    private Node createReadStory() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: black;" +
                " -fx-border-color: lime;" +
                " -fx-border-width: 1px;");

        root.setMaxSize(400, 500);

        Label storyText = Utility.createLabel(localizedProperty("NewGameScene.STORY"));

        Button readStoryNextButton = new Button();
        readStoryNextButton.textProperty().bind(localizedProperty("NewGameScene.STORY_NEXT"));

        readStoryNextButton.setOnAction(_ -> {
            readStory.setVisible(false);
            commanderName.setVisible(true);
        });

        Utility.setButtonStyle(readStoryNextButton);

        root.setSpacing(30);

        root.getChildren().addAll(storyText, readStoryNextButton);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    /**
     * Instantiates the text tracking input interface mapping the player commander name.
     *
     * @return Fully initialized name config box node component
     */
    private Node createCommanderName() {
        return createSetNamesBox(localizedProperty("NewGameScene.COMMANDER_LABEL"),
                localizedProperty("NewGameScene.COMMANDER_DEFAULT_NAME"),
                        localizedProperty("NewGameScene.COMMANDER_NEXT"),
                name -> {
                    mainWindow.getColony().setCommanderName(name);

                    commanderName.setVisible(false);
                    aiName.setVisible(true);
                },
                                localizedProperty("NewGameScene.COMMANDER_BACK"),
                _ -> {
                    commanderName.setVisible(false);
                    readStory.setVisible(true);
                });
    }

    /**
     * Instantiates the text tracking input interface mapping the companion core AI designation.
     *
     * @return Fully initialized name config box node component
     */
    private Node createAiName() {
        return createSetNamesBox(localizedProperty("NewGameScene.AI_LABEL"),
                localizedProperty("NewGameScene.AI_DEFAULT_NAME"),
                        localizedProperty("NewGameScene.AI_NEXT"),
                name -> {
            mainWindow.getColony().setAIName(name);

            aiName.setVisible(false);
            starSelection.setVisible(true);
            starSelection.setPreselection(true);
        },
                                localizedProperty("NewGameScene.AI_BACK"),
                _ -> {
            aiName.setVisible(false);
            commanderName.setVisible(true);
        });
    }

    /**
     * Universal structural factory building uniform name configuration panel boxes.
     *
     * @param label1           Title text instruction banner property
     * @param defaultName      Initial default fallback name selection text property
     * @param nextButtonText   Localized label tag for the validation switch button
     * @param nextButtonAction Lambda callback execution mapping onward validation gates
     * @param backButtonText   Localized label tag for the cancellation step button
     * @param backButtonAction Lambda callback execution mapping backward navigation gates
     * @return Generated structural unified naming block node box
     */
    private Node createSetNamesBox(ReadOnlyStringProperty label1, ReadOnlyStringProperty defaultName, ReadOnlyStringProperty nextButtonText, Action nextButtonAction, ReadOnlyStringProperty backButtonText,
                                   Action backButtonAction){
        VBox root = new VBox();

        root.setStyle("-fx-background-color: black;" +
                " -fx-border-color: lime;" +
                " -fx-border-width: 1px;");

        root.setMaxSize(400, 100);
        root.setSpacing(10);

        Label nameText = Utility.createLabel(label1);

        TextField nameTextField = new TextField();
        nameTextField.setText(defaultName.get());
        defaultName.addListener((_, oldValue, newValue) -> {
            String previous = nameTextField.getText();
            if (previous.trim().equals(oldValue.trim())) {
                nameTextField.setText(newValue);
            }
        });

        nameTextField.setMaxWidth(300);

        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER);

        Button nextButton  = new Button();
        nextButton.textProperty().bind(nextButtonText);
        nextButton.setOnAction(_ -> nextButtonAction.run(nameTextField.getText()));
        Utility.setButtonStyle(nextButton);

        Button previousButton = new Button();
        previousButton.textProperty().bind(backButtonText);
        previousButton.setOnAction(_ -> backButtonAction.run(""));
        Utility.setButtonStyle(previousButton);

        buttonBar.getChildren().addAll(nextButton, previousButton);

        root.getChildren().addAll(nameText, nameTextField, buttonBar);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    /**
     * Allocates and configures cross-navigation actions linking the core star grid map workspace.
     * Handles routing splits depending on whether probe deployment or cargo initialization is clicked.
     *
     * @return Fully structured StarSelectionScreen panel view
     */
    private StarSelectionScreen createStarSelection() {
        return new StarSelectionScreen(mainWindow, () -> {
            starSelection.setVisible(false);
            starSelection.setPreselection(false);
            shipLoading.setVisible(true);
        }, () -> {
            starSelection.setVisible(false);
            shipTakeOff.setVisible(true);
            shipTakeOff.reset(mainWindow.getColony());
        });
    }

    /**
     * Instantiates the market logistics area pane split, anchoring the functional
     * ShipCargoLoader table next to its corresponding technical blueprint graphics.
     *
     * @return Fully operational cargo loader configuration layout block
     */
    private Node createShipLoading() {
        GridPane root = new GridPane();
        root.setStyle("-fx-background-color: black;" +
                " -fx-border-color: lime;" +
                " -fx-border-width: 1px;");
        root.setMaxWidth(1500);
        root.maxHeightProperty().bind(root.maxWidthProperty().multiply(0.66));

        root.setMinWidth(100);
        root.setMinHeight(100);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        root.getColumnConstraints().add(column1);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        root.getColumnConstraints().add(column2);

        cargoLoader = new ShipCargoLoader(_ -> {
            shipLoading.setVisible(false);
            starSelection.setVisible(true);
            starSelection.setPreselection(false);
            starSelection.generateProbedStarSystems();
            cargoLoader.loadCargoInShip(mainWindow.getColony());
        });

        StackPane pane = new StackPane();

        var backgroundImagePane = new BackgroundImagePane(pane, "CargoRocket.jpg");
        pane.maxHeightProperty().bind(root.heightProperty());
        pane.minHeightProperty().bind(root.heightProperty());
        pane.prefHeightProperty().bind(root.heightProperty());
        pane.maxWidthProperty().bind(root.widthProperty().divide(2));
        pane.minWidthProperty().bind(root.widthProperty().divide(2));
        pane.prefWidthProperty().bind(root.widthProperty().divide(2));


        pane.getChildren().add(backgroundImagePane);


        root.addRow(0, cargoLoader, pane);
        return root;
    }

    /**
     * Instantiates the visual vehicle checklist sequence checkpoint panel layout view.
     *
     * @return Fully structural operational checklist screen component
     */
    private ShipLaunchScreen createShipTakeoff() {
        return new ShipLaunchScreen(_ -> {
            shipTakeOff.setVisible(false);
            choosePlanet.setVisible(true);
            StarSystem selected = starSelection.getGeneratedChosenStarSystem();
            choosePlanet.setSystem(selected);
            choosePlanet.setActiveProbeAndSatelliteEffects(shipTakeOff.getActiveProbeAndSatelliteEffects());
            mainWindow.getColony().setStarSystem(selected);
        });
    }

    /**
     * Instantiates the telemetry panel interface controlling active planetary target selection.
     *
     * @return Fully operational descriptive planet selector grid layout sheet
     */
    private ChoosePlanetScreen createChoosePlanet() {
        return new ChoosePlanetScreen(_ ->{
            choosePlanet.setVisible(false);
            siteChooser.setVisible(true);
            Planet selectedPlanet = choosePlanet.getPlanet();
            // siteChooser.setPlanet(selectedPlanet);
            mainWindow.getColony().setPlanet(selectedPlanet);
        });
    }

    /**
     * Instantiates the landing coordinate grid zone tracking selector block layout.
     *
     * @return Placeholder modular structural stack node layout component
     */
    private Node createSiteChooser() {
        return new StackPane();
    }

    /**
     * Flushes transient operational registers, purges active registries,
     * and forces structural configurations back to initial prologue states.
     */
    public void prepareNewGame() {
        readStory.setVisible(true);
        commanderName.setVisible(false);
        aiName.setVisible(false);
        starSelection.setVisible(false);
        starSelection.reset();
        starSelection.setPreselection(true);
        shipLoading.setVisible(false);
        shipTakeOff.setVisible(false);
        choosePlanet.setVisible(false);
        choosePlanet.setSystem(null);
        siteChooser.setVisible(false);
    }

    public void startNewGame() {
        // this is probably the method to call once the setup phase is done I think (should have left a comment when I wrote this)
    }

    /**
     * Sets proportional baseline sizing requirements applied onto the parent scene node container.
     *
     * @param width  Horizontal resolution limit width parameters in pixels
     * @param height Vertical resolution limit height parameters in pixels
     */
    public void setSize(double width, double height) {
        ((Pane) getRoot()).setPrefWidth(width);
        ((Pane) getRoot()).setPrefHeight(height);
    }

    /**
     * Fetches reactive localized resource bundle properties assigned to tracking string keys.
     *
     * @param name Target translation property identifier tag
     * @return Localized text expression binding node
     */
    public static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, name);
    }

    /**
     * Internal operational single-argument action proxy routing named layout swaps text events.
     */
    @FunctionalInterface
    private interface Action{
        /**
         * Invokes custom dynamic text adjustments pipelines.
         *
         * @param name Raw alphanumeric input text provided via configuration fields
         */
        void run(String name);
    }

}