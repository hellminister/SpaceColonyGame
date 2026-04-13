package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gui.common.BackgroundImagePane;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.mainwindow.MainWindow;
import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.util.logging.Logger;

public class NewGameScene extends Scene {
    private static final Logger LOG = Logger.getLogger(NewGameScene.class.getName());

    private final MainWindow mainWindow;

    private final Node readStory;
    private final Node commanderName;
    private final Node aiName;
    private final StarSelectionScreen starSelection;
    private final Node shipLoading;
    private final Node shipTakeOff;
    private ShipCargoLoader cargoLoader;
    private final ChoosePlanetScreen choosePlanet;


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

        root.getChildren().addAll(readStory, commanderName, aiName, starSelection, shipLoading, shipTakeOff, choosePlanet);

        readStory.setVisible(true);
        commanderName.setVisible(false);
        aiName.setVisible(false);
        shipLoading.setVisible(false);
        starSelection.setVisible(false);
        starSelection.setPreselection(true);
        shipTakeOff.setVisible(false);
        choosePlanet.setVisible(false);

    }

    private Node createReadStory() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: black;" +
                " -fx-border-color: lime;" +
                " -fx-border-width: 1px;");

        root.setMaxSize(400, 500);

        Label storyText = Utility.createLabel(Target.STORY.localizedProperty());

        Button readStoryNextButton = new Button();
        readStoryNextButton.textProperty().bind(Target.STORY_NEXT.localizedProperty());

        readStoryNextButton.setOnAction(_ -> {
            readStory.setVisible(false);
            commanderName.setVisible(true);
            // add here what is needed when switching to commander name
        });

        Utility.setButtonStyle(readStoryNextButton);

        root.setSpacing(30);

        root.getChildren().addAll(storyText, readStoryNextButton);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    private Node createCommanderName() {
        return createSetNamesBox(Target.COMMANDER_LABEL,
                Target.COMMANDER_DEFAULT_NAME,
                Target.COMMANDER_NEXT,
                name -> {
                    mainWindow.getColony().setCommanderName(name);

                    commanderName.setVisible(false);
                    aiName.setVisible(true);
                },
                Target.COMMANDER_BACK,
                _ -> {
                    commanderName.setVisible(false);
                    readStory.setVisible(true);
                });
    }

    private Node createAiName() {
        return createSetNamesBox(Target.AI_LABEL,
                Target.AI_DEFAULT_NAME,
                Target.AI_NEXT,
                name -> {
            mainWindow.getColony().setAIName(name);

            aiName.setVisible(false);
            starSelection.setVisible(true);
            starSelection.setPreselection(true);
        },
                Target.AI_BACK,
                _ -> {
            aiName.setVisible(false);
            commanderName.setVisible(true);
        });
    }

    private Node createSetNamesBox(Target label1, Target defaultName, Target nextButtonText, Action nextButtonAction, Target backButtonText, Action backButtonAction){
        VBox root = new VBox();

        root.setStyle("-fx-background-color: black;" +
                " -fx-border-color: lime;" +
                " -fx-border-width: 1px;");

        root.setMaxSize(400, 100);
        root.setSpacing(10);

        Label nameText = Utility.createLabel(label1.localizedProperty());

        TextField nameTextField = new TextField();
        nameTextField.setText(defaultName.toString());
        defaultName.localizedProperty().addListener((_, oldValue, newValue) -> {
            String previous = nameTextField.getText();
            if (previous.trim().equals(oldValue.trim())) {
                nameTextField.setText(newValue);
            }
        });

        nameTextField.setMaxWidth(300);

        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER);

        Button nextButton  = new Button();
        nextButton.textProperty().bind(nextButtonText.localizedProperty());
        nextButton.setOnAction(_ -> nextButtonAction.run(nameTextField.getText()));
        Utility.setButtonStyle(nextButton);

        Button previousButton = new Button();
        previousButton.textProperty().bind(backButtonText.localizedProperty());
        previousButton.setOnAction(_ -> backButtonAction.run(""));
        Utility.setButtonStyle(previousButton);

        buttonBar.getChildren().addAll(nextButton, previousButton);

        root.getChildren().addAll(nameText, nameTextField, buttonBar);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    private StarSelectionScreen createStarSelection() {
        return new StarSelectionScreen(mainWindow, () -> {
            starSelection.setVisible(false);
            starSelection.setPreselection(false);
            shipLoading.setVisible(true);
        }, () -> {
            starSelection.setVisible(false);
            shipTakeOff.setVisible(true);
        });
    }


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

    private Node createShipTakeoff() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black");

        Button takeoffButton = new Button();
        takeoffButton.textProperty().bind(Target.TAKEOFF_NEXT.localizedProperty());
        takeoffButton.setOnAction(_ -> {
            shipTakeOff.setVisible(false);
            choosePlanet.setVisible(true);
            choosePlanet.setSystem(starSelection.getGeneratedChosenStarSystem());
        });

        root.getChildren().add(takeoffButton);
        return root;
    }

    private ChoosePlanetScreen createChoosePlanet() {
        var root = new ChoosePlanetScreen();

        return root;
    }


    public void prepareNewGame() {
        readStory.setVisible(true);
        commanderName.setVisible(false);
        aiName.setVisible(false);
        starSelection.setVisible(false);
        starSelection.reset();
        shipLoading.setVisible(false);
        shipTakeOff.setVisible(false);
        choosePlanet.setVisible(false);
    }

    public void startNewGame() {
    }

    public void setSize(double width, double height) {
        ((Pane) getRoot()).setPrefWidth(width);
        ((Pane) getRoot()).setPrefHeight(height);
    }

    @FunctionalInterface
    private interface Action{
        void run(String name);
    }

    @Localized
    private enum Target implements Localizable {
        STORY,
        STORY_NEXT,
        COMMANDER_LABEL,
        COMMANDER_NEXT,
        COMMANDER_BACK,
        COMMANDER_DEFAULT_NAME,
        AI_LABEL,
        AI_NEXT,
        AI_BACK,
        AI_DEFAULT_NAME,
        TAKEOFF_NEXT,
        ;

        private static final Localize<Target> local = new Localize<>(Target.class, "gui/newGameSceneTarget.txt");

        @Override
        public ReadOnlyStringProperty localizedProperty() {
            return local.getString(this);
        }
        @Override
        public String toString() {
            return local.getString(this).get();
        }
    }

}