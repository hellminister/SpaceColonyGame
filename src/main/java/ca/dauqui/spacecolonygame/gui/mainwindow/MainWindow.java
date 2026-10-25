package ca.dauqui.spacecolonygame.gui.mainwindow;

import ca.dauqui.spacecolonygame.gamedata.Colony;
import ca.dauqui.spacecolonygame.gamedata.StaticDataAssets;
import ca.dauqui.spacecolonygame.gui.newgame.NewGameScene;
import ca.dauqui.spacecolonygame.gui.startscreen.StartScreen;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.application.Application;

import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainWindow extends Application {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";

    private Stage primaryStage;

    private final StartScreen startScreen;
    private final NewGameScene newGameScene;

    private final StaticDataAssets staticDataAssets;
    private Colony colony;

    public MainWindow() {
        staticDataAssets = new StaticDataAssets();
        startScreen = new StartScreen(this);
        newGameScene = new NewGameScene(this);

    }

    @Override
    public void start(Stage stage) {
        double maxScreenHeight = 0;
        double maxScreenWidth = 0;

        for (Screen screen : Screen.getScreens()) {
            if (maxScreenHeight < screen.getVisualBounds().getHeight()) {
                maxScreenHeight = screen.getVisualBounds().getHeight();
            }
            if (maxScreenWidth < screen.getVisualBounds().getWidth()) {
                maxScreenWidth = screen.getVisualBounds().getWidth();
            }
        }


        primaryStage = stage;

        primaryStage.titleProperty().bind(Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "MainWindow.TITLE"));
        primaryStage.setMaxWidth(maxScreenWidth);
        primaryStage.setMaxHeight(maxScreenHeight);
        primaryStage.setScene(startScreen);
        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    public void returnToStartScreen(){
        primaryStage.setScene(startScreen);
    }

    public void switchToNewGameScene() {
        if (colony != null && !colony.isSaved()) {
            // TODO asks and warn about the state of the save and that the current progress might be lost
            // TODO call return if they decide not to continue;
        }
        colony = new Colony();
        newGameScene.prepareNewGame();
        newGameScene.setSize(primaryStage.getWidth(), primaryStage.getHeight());
        primaryStage.setScene(newGameScene);
        newGameScene.startNewGame();
    }

    public Colony getColony() {
        return colony;
    }

    public StaticDataAssets getAssets(){
        return staticDataAssets;
    }
}