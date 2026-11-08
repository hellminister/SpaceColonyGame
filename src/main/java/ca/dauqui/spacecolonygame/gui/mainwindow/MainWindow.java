package ca.dauqui.spacecolonygame.gui.mainwindow;

import ca.dauqui.spacecolonygame.gamedata.Colony;
import ca.dauqui.spacecolonygame.gui.newgame.NewGameScene;
import ca.dauqui.spacecolonygame.gui.startscreen.StartScreen;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.application.Application;

import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Primary window controller and entry checkpoint for the application framework.
 * Supervises the core stage lifecycle and manages transactional scene swaps between
 * the main title dashboard and campaign setup scenes.
 *
 * @author Jean-François Quintal
 */
public class MainWindow extends Application {
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";

    private Stage primaryStage;

    private final StartScreen startScreen;
    private final NewGameScene newGameScene;

    private Colony colony;

    /**
     * Pre-instantiates core application screen layers, linking mutual multi-tier references
     * to support fluid cross-scene communication routines.
     */
    public MainWindow() {
        startScreen = new StartScreen(this);
        newGameScene = new NewGameScene(this);

    }

    /**
     * Initializes the master Stage framework, dynamically evaluating display boundaries
     * to enforce maximize monitor limits, and mounts the default title screen context.
     *
     * @param stage The primary graphic canvas viewport provided by the JavaFX execution thread
     */
    @Override
    public void start(Stage stage) {
        double maxScreenHeight = 0;
        double maxScreenWidth = 0;

        // Iterate over active physical monitors to determine optimal visual sizing boundaries
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

    /**
     * Routes the active viewport layer back to the baseline home title dashboard screen.
     */
    public void returnToStartScreen(){
        primaryStage.setScene(startScreen);
    }

    /**
     * Safely initiates campaign configuration flows, instantiating clean data registry models
     * and forcing proportional container updates before executing structural view swaps.
     */
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

    /**
     * @return The active core database domain layout model tracking current colony states
     */
    public Colony getColony() {
        return colony;
    }

}