package ca.dauqui.spacecolonygame.gui.startscreen;

import ca.dauqui.spacecolonygame.gui.common.BackgroundImagePane;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.mainwindow.MainWindow;
import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

public class StartScreen extends Scene {
    private static final Logger LOG = Logger.getLogger(StartScreen.class.getName());
    private final MainWindow mainWindow;

    public StartScreen(MainWindow mainWindow) {
        super(new StackPane());
        var root = (StackPane) getRoot();

        this.mainWindow = mainWindow;

        root.setStyle("-fx-background-color: black");
        root.setAlignment(Pos.CENTER);


        // this pane is to permit resizing of the window
        var backgroundImagePane = new BackgroundImagePane(root, "milky-way-full-stars-space.jpg");
        root.getChildren().add(backgroundImagePane);

        var buttonBar = new VBox();
        buttonBar.setAlignment(Pos.CENTER);

        Button newGameButton = new Button();
        newGameButton.textProperty().bind(Target.NEW_GAME.localizedProperty());
        newGameButton.setOnAction(_ -> mainWindow.switchToNewGameScene());
        Utility.setButtonStyle(newGameButton);

        Button loadGameButton = new Button();
        loadGameButton.textProperty().bind(Target.LOAD_GAME.localizedProperty());
        Utility.setButtonStyle(loadGameButton);

        buttonBar.getChildren().addAll(newGameButton, loadGameButton);
        root.getChildren().addAll(buttonBar);
    }


    @Localized
    private enum Target implements Localizable {
        NEW_GAME,
        LOAD_GAME,
        ;

        private static final Localize<Target> local = new Localize<>(Target.class, "gui/startScreenTarget.txt");

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