package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import ca.dauqui.spacecolonygame.gui.common.BackgroundImagePane;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.mainwindow.MainWindow;
import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NewGameScene extends Scene {
    private static final Logger LOG = Logger.getLogger(NewGameScene.class.getName());
    private static final int MAX_SELECTED_STARS = 4;

    private final MainWindow mainWindow;

    private final Node readStory;
    private final Node commanderName;
    private final Node aiName;
    private final Node starPreselection;
    private final Node shipLoading;
    private final Node starSelection;
    private final Node shipTakeOff;

    private StarList stars;

    private final ObservableList<StarSystemAsset> selectedStars;


    public NewGameScene(MainWindow mainWindow) {
        super(new StackPane());
        var root = (StackPane) getRoot();
        this.mainWindow = mainWindow;

        selectedStars = FXCollections.observableArrayList();


        root.setStyle("-fx-background-color: black");
        root.setAlignment(Pos.CENTER);

        // this pane is to permit resizing of the window
        var backgroundImagePane = new BackgroundImagePane(root, "milky-way-full-stars-space.jpg");
        root.getChildren().add(backgroundImagePane);

        readStory = createReadStory();
        commanderName = createCommanderName();
        aiName = createAiName();
        starPreselection = createStarPreselection();
        shipLoading = createShipLoading();
        starSelection = createStarSelection();
        shipTakeOff = createShipTakeoff();

        root.getChildren().addAll(readStory, commanderName, aiName, starPreselection, shipLoading, starSelection, shipTakeOff);

        readStory.setVisible(true);
        commanderName.setVisible(false);
        aiName.setVisible(false);
        starPreselection.setVisible(false);
        shipLoading.setVisible(false);
        starSelection.setVisible(false);
        shipTakeOff.setVisible(false);

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
        VBox root = new VBox();

        root.setStyle("-fx-background-color: black;" +
                " -fx-border-color: lime;" +
                " -fx-border-width: 1px;");

        root.setMaxSize(400, 100);
        root.setSpacing(10);

        Label commanderNameText = Utility.createLabel(Target.COMMANDER_LABEL.localizedProperty());

        TextField commanderNameTextField = new TextField();
        commanderNameTextField.setText(Target.COMMANDER_DEFAULT_NAME.toString());
        Target.COMMANDER_DEFAULT_NAME.localizedProperty().addListener((_, oldValue, newValue) -> {
            String previous = commanderNameTextField.getText();
            if (previous.trim().equals(oldValue.trim())) {
                commanderNameTextField.setText(newValue);
            }
        });

        commanderNameTextField.setMaxWidth(300);


        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER);

        Button nextButton  = new Button();
        nextButton.textProperty().bind(Target.COMMANDER_NEXT.localizedProperty());
        nextButton.setOnAction(_ -> {
            String commanderNameString = commanderNameTextField.getText();

            mainWindow.getColony().setCommanderName(commanderNameString);

            commanderName.setVisible(false);
            aiName.setVisible(true);
            // add here what is needed when switching to aiName
        });
        Utility.setButtonStyle(nextButton);

        Button previousButton = new Button();
        previousButton.textProperty().bind(Target.COMMANDER_BACK.localizedProperty());
        previousButton.setOnAction(_ -> {
            commanderName.setVisible(false);
            readStory.setVisible(true);
            // add here what is needed when switching to story
        });
        Utility.setButtonStyle(previousButton);

        buttonBar.getChildren().addAll(nextButton, previousButton);

        root.getChildren().addAll(commanderNameText, commanderNameTextField, buttonBar);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    private Node createAiName() {
        VBox root = new VBox();

        root.setStyle("-fx-background-color: black;" +
                " -fx-border-color: lime;" +
                " -fx-border-width: 1px;");

        root.setMaxSize(400, 100);
        root.setSpacing(10);

        Label aiNameText = Utility.createLabel(Target.AI_LABEL.localizedProperty());

        TextField aiNameTextField = new TextField();
        aiNameTextField.setText(Target.AI_DEFAULT_NAME.toString());
        Target.AI_DEFAULT_NAME.localizedProperty().addListener((_, oldValue, newValue) -> {
            String previous = aiNameTextField.getText();
            if (previous.trim().equals(oldValue.trim())) {
                aiNameTextField.setText(newValue);
            }
        });

        aiNameTextField.setMaxWidth(300);


        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER);

        Button nextButton  = new Button();
        nextButton.textProperty().bind(Target.COMMANDER_NEXT.localizedProperty());
        nextButton.setOnAction(_ -> {
            String aiNameString = aiNameTextField.getText();

            mainWindow.getColony().setAIName(aiNameString);

            aiName.setVisible(false);
            starPreselection.setVisible(true);
            // add here what is needed when switching to starPreselection
        });
        Utility.setButtonStyle(nextButton);

        Button previousButton = new Button();
        previousButton.textProperty().bind(Target.AI_BACK.localizedProperty());
        previousButton.setOnAction(_ -> {
            aiName.setVisible(false);
            commanderName.setVisible(true);
            // add here what is needed when switching to commanderName
        });
        Utility.setButtonStyle(previousButton);

        buttonBar.getChildren().addAll(nextButton, previousButton);

        root.getChildren().addAll(aiNameText, aiNameTextField, buttonBar);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    private Node createStarPreselection() {
        GridPane root = new GridPane();
        root.setStyle("-fx-background-color: black;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: lime");
        root.setGridLinesVisible(true);
        var colConst1 = new ColumnConstraints();
        colConst1.setPercentWidth(50);
        var colConst2 = new ColumnConstraints();
        colConst2.setPercentWidth(50);

        root.getColumnConstraints().addAll(colConst1, colConst2);

        root.setMaxWidth(1500);
        root.maxHeightProperty().bind(root.maxWidthProperty().multiply(0.66));

        var rowConst1 = new RowConstraints();
        rowConst1.setPercentHeight(75);
        var rowConst2 = new RowConstraints();
        rowConst2.setPercentHeight(25);
        root.getRowConstraints().addAll(rowConst1, rowConst2);

        root.setHgap(10);
        root.setVgap(10);

        List<StarNode> starNodes = new ArrayList<>();

        VBox starListBox = new VBox();
        starListBox.setAlignment(Pos.CENTER);
        Label starListLabel = Utility.createLabel(Target.STAR_LIST_LABEL.localizedProperty());

        stars = new StarList(mainWindow.getAssets().getStarSystems(), selectedStars, MAX_SELECTED_STARS, starNodes);

        Label probes = Utility.createLabel(Target.PROBES_LEFT.localizedProperty());
        probes.textProperty().unbind();
        probes.textProperty().bind(Bindings.concat(Target.PROBES_LEFT.localizedProperty(), " ", Bindings.subtract(MAX_SELECTED_STARS, Bindings.size(selectedStars)) ));

        Button launchButton = new Button();
        launchButton.textProperty().bind(Target.LAUNCH_PROBE_BUTTON.localizedProperty());
        Utility.setButtonStyle(launchButton);
        launchButton.setOnAction(_ -> {});

        starListBox.getChildren().addAll(starListLabel, stars, probes, launchButton);
        root.add(starListBox, 1, 0);

        VBox descriptionBox = new VBox();
        descriptionBox.setSpacing(10);
        Label descriptionTitle = Utility.createLabel(Target.DESCRIPTION.localizedProperty());

        Label description = Utility.createLabelUnbound();
        description.setWrapText(true);

        descriptionBox.getChildren().addAll(descriptionTitle, description);

        root.add(descriptionBox, 1, 1);

        VBox starInfoBox = new VBox();
        starInfoBox.setSpacing(10);
        starInfoBox.setAlignment(Pos.TOP_CENTER);

        Label starNameLabel = new Label();
        starNameLabel.setFont(new Font("Arial", 20));
        starNameLabel.setTextFill(Color.DARKORANGE);

        GridPane starInfosGrid =  new GridPane();
        var colConst1InfoPane = new ColumnConstraints();
        colConst1InfoPane.setPercentWidth(50);
        var colConst2InfoPane = new ColumnConstraints();
        colConst2InfoPane.setPercentWidth(50);

        starInfosGrid.getColumnConstraints().addAll(colConst1InfoPane, colConst2InfoPane);
        starInfosGrid.setVgap(5);
        starInfoBox.getChildren().addAll(starNameLabel, starInfosGrid);

        Label spectral = Utility.createLabelUnbound();
        spectral.textProperty().bind(new StarInfoBinding(Target.SPECTRAL_LABEL.localizedProperty(), stars.getSelection(), StarSystemAsset::getSpectralType));
        starInfosGrid.add(spectral, 0, 0);

        Label mass = Utility.createLabelUnbound();
        mass.textProperty().bind(new StarInfoBinding(Target.MASS_LABEL.localizedProperty(), stars.getSelection(), star -> String.format("%.2f",
                star.getMass())));
        starInfosGrid.add(mass, 1, 0);

        Label distance = Utility.createLabelUnbound();
        distance.textProperty().bind(new StarInfoBinding(Target.DISTANCE_LABEL.localizedProperty(), stars.getSelection(),
                star -> String.format("%.4f", star.getDistance())));
        starInfosGrid.add(distance, 0, 1);

        Label mainShipTravel = Utility.createLabelUnbound();
        mainShipTravel.textProperty().bind(new StarInfoBinding(Target.MAIN_SHIP_TRAVEL_LABEL.localizedProperty(), stars.getSelection(),
                star -> String.format("%.2f", star.getDistance()/0.75)));
        starInfosGrid.add(mainShipTravel, 1, 1);

        Label humanityTravel = Utility.createLabelUnbound();
        humanityTravel.textProperty().bind(new StarInfoBinding(Target.HUMANITY_TRAVEL_LABEL.localizedProperty(), stars.getSelection(),
                star -> String.format("%.2f", star.getDistance()/0.005)));
        starInfosGrid.add(humanityTravel, 0, 2);

        Label humanitySurvival = Utility.createLabelUnbound();
        humanitySurvival.textProperty().bind(new StarInfoBinding(Target.HUMANITY_TRANSIT_SURVIVAL_LABEL.localizedProperty(), stars.getSelection(),
                star -> String.format("%.4f", (1/((star.getDistance()-30)*0.0009) + 110))));
        starInfosGrid.add(humanitySurvival, 1, 2);



        stars.getSelection().addListener((_, _, newValue) -> {
            if (newValue != null) {
                description.textProperty().bind(newValue.descriptionProperty());
                starNameLabel.textProperty().bind(newValue.nameProperty());
            } else {
                description.setText("");
                starNameLabel.setText("");
            }
        });

        root.add(starInfoBox, 0, 1);

        StarField starField = new StarField(starNodes);

        root.add(starField, 0, 0);

        return root;
    }


    private Node createShipLoading() {
        StackPane root = new StackPane();

        return root;
    }

    private Node createStarSelection() {
        StackPane root = new StackPane();

        return root;
    }

    private Node createShipTakeoff() {
        StackPane root = new StackPane();

        return root;
    }


    public void prepareNewGame() {
        readStory.setVisible(true);
        commanderName.setVisible(false);
        aiName.setVisible(false);
        starPreselection.setVisible(false);
        shipLoading.setVisible(false);
        starSelection.setVisible(false);
        shipTakeOff.setVisible(false);
    }

    public void startNewGame() {
    }

    public void setSize(double width, double height) {
        ((Pane) getRoot()).setPrefWidth(width);
        ((Pane) getRoot()).setPrefHeight(height);
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
        STAR_LIST_LABEL,
        LAUNCH_PROBE_BUTTON,
        PROBES_LEFT,
        DESCRIPTION,
        SPECTRAL_LABEL,
        DISTANCE_LABEL,
        MAIN_SHIP_TRAVEL_LABEL,
        MASS_LABEL,
        HUMANITY_TRAVEL_LABEL,
        HUMANITY_TRANSIT_SURVIVAL_LABEL,
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

    private static class StarInfoBinding extends StringBinding {
        private final ReadOnlyStringProperty infoLabel;
        private final ReadOnlyObjectProperty<StarSystemAsset> star;
        private final ExtractFieldString extractor;

        StarInfoBinding(ReadOnlyStringProperty infoLabel, ReadOnlyObjectProperty<StarSystemAsset> star, ExtractFieldString extractor) {
            this.infoLabel = infoLabel;
            this.star = star;
            this.extractor = extractor;
            bind(infoLabel, star);
        }

        @Override
        protected String computeValue() {
            if (star.get() != null) {
                return infoLabel.get() + " " + extractor.extractString(star.get());
            } else {
                return infoLabel.get();
            }
        }
    }

    @FunctionalInterface
    private interface ExtractFieldString {
        String extractString(StarSystemAsset star);
    }

}