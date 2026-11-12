package ca.dauqui.spacecolonygame.gui.startscreen;

import ca.dauqui.spacecolonygame.gui.common.BackgroundImagePane;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.mainwindow.MainWindow;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.settings.GameParameters;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Main application title interface container loaded upon bootstrap.
 * Hosts primary navigation dispatch actions to route users into campaign creation flows.
 *
 * @author Jean-François Quintal
 */
public class StartScreen extends Scene {
    private final MainWindow mainWindow;

    /**
     * Constructs the foundational title menu canvas, mapping responsive spatial backgrounds
     * and binding translation bundles onto primary operational navigation buttons.
     *
     * @param mainWindow Core master window coordinator supervising primary view scene alterations
     */
    public StartScreen(MainWindow mainWindow) {
        super(new StackPane());
        var root = (StackPane) getRoot();

        this.mainWindow = mainWindow;

        root.setStyle("-fx-background-color: black");
        root.setAlignment(Pos.CENTER);


        // Resizeable space illustration backdrop layer constraint template
        var backgroundImagePane = new BackgroundImagePane(root, "milky-way-full-stars-space.jpg");
        root.getChildren().add(backgroundImagePane);

        var buttonBar = new VBox(15);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setMaxWidth(280);

        Button newGameButton = new Button();
        newGameButton.textProperty().bind(localizedProperty("StartScreen.NEW_GAME"));
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        // Dispatch window routing layout swap triggers to fire campaign configurations setups
        newGameButton.setOnAction(_ -> mainWindow.switchToNewGameScene());
        Utility.setButtonStyle(newGameButton);

        Button loadGameButton = new Button();
        loadGameButton.textProperty().bind(localizedProperty("StartScreen.LOAD_GAME"));
        loadGameButton.setMaxWidth(Double.MAX_VALUE);
        Utility.setButtonStyle(loadGameButton);

        Button settingsButton = new Button();
        settingsButton.textProperty().bind(localizedProperty("StartScreen.SETTINGS"));
        settingsButton.setMaxWidth(Double.MAX_VALUE);
        // Dispatch window routing layout swap triggers to fire campaign configurations setups
        settingsButton.setOnAction(_ -> mainWindow.switchToSettingScene());
        Utility.setButtonStyle(settingsButton);

        buttonBar.getChildren().addAll(newGameButton, loadGameButton, settingsButton);
        root.getChildren().addAll(buttonBar);
    }

    /**
     * Fetches reactive localized resource bundle properties assigned to tracking string keys.
     *
     * @param name Target translation property identifier tag
     * @return Localized text expression binding node
     */
    public static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(GameParameters.StringParam.LOCALIZATION_GUI_BUNDLE.get(), name);
    }


    public void setSize(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height) {
        ((Pane) getRoot()).prefWidthProperty().bind(width);
        ((Pane) getRoot()).prefHeightProperty().bind(height);
        ((Pane) getRoot()).minWidthProperty().bind(width);
        ((Pane) getRoot()).minHeightProperty().bind(height);
        ((Pane) getRoot()).maxWidthProperty().bind(width);
        ((Pane) getRoot()).maxHeightProperty().bind(height);
    }
}