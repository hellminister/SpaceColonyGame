package ca.dauqui.spacecolonygame.gui.settingscreen;

import ca.dauqui.spacecolonygame.gui.common.BackgroundImagePane;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.gui.mainwindow.MainWindow;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.SupportedLanguage;
import ca.dauqui.spacecolonygame.settings.GameParameters;
import ca.dauqui.spacecolonygame.settings.GameSettings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Configuration screen scene allowing players to modify persistent user preferences
 * including UI language, pre-flight checklist visibility, and procedural world generation seeds.
 *
 * @author Jean-François Quintal
 */
public class SettingsScreen extends Scene {

    private static final String CHECKBOX_CSS = "/ca/dauqui/spacecolonygame/gui/newgame/starselectionscreen/checkbox.css";

    public SettingsScreen(MainWindow mainWindow) {
        super(new StackPane());
        StackPane root = (StackPane) getRoot();
        root.setStyle("-fx-background-color: black;");
        root.setAlignment(Pos.CENTER);

        BackgroundImagePane background = new BackgroundImagePane(root, "milky-way-full-stars-space.jpg");
        root.getChildren().add(background);

        VBox panel = new VBox(20);
        panel.setMaxSize(650, 450);
        panel.setPadding(new Insets(25));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: black; -fx-border-color: lime; -fx-border-width: 1px;");

        // Title
        Label titleLabel = Utility.createLabel(localizedProperty("SettingsScreen.TITLE"));
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        // Settings Form Grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(18);
        formGrid.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(55);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(45);
        formGrid.getColumnConstraints().addAll(col1, col2);

        // 1. Language Selector
        Label langLabel = Utility.createLabel(localizedProperty("SettingsScreen.LANGUAGE"));
        ComboBox<SupportedLanguage> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll(SupportedLanguage.getAll());
        languageCombo.setStyle("-fx-base: black; -fx-text-fill: lime; -fx-border-color: lime;");
        languageCombo.setMaxWidth(Double.MAX_VALUE);

        // Select the currently active language immediately
        SupportedLanguage currentLang = GameSettings.LanguageSetting.ACTIVE_LANGUAGE.get();
        if (currentLang != null) {
            languageCombo.getSelectionModel().select(currentLang);
        }

        // Update GameSettings when the user picks a new language in the ComboBox
        languageCombo.getSelectionModel().selectedItemProperty().addListener((_, _, newLang) -> {
            if (newLang != null && !newLang.equals(GameSettings.LanguageSetting.ACTIVE_LANGUAGE.get())) {
                GameSettings.LanguageSetting.ACTIVE_LANGUAGE.property().set(newLang);
            }
        });

        // Keep ComboBox synchronized if ACTIVE_LANGUAGE changes externally
        GameSettings.LanguageSetting.ACTIVE_LANGUAGE.readOnlyProperty().addListener((_, _, newLang) -> {
            if (newLang != null && !newLang.equals(languageCombo.getSelectionModel().getSelectedItem())) {
                languageCombo.getSelectionModel().select(newLang);
            }
        });

        languageCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SupportedLanguage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.displayName());
                    setStyle("-fx-text-fill: lime; -fx-background-color: black;");
                }
            }
        });

        formGrid.add(langLabel, 0, 0);
        formGrid.add(languageCombo, 1, 0);

        // 2. Pre-flight Checklist Toggle
        Label checklistLabel = Utility.createLabel(localizedProperty("SettingsScreen.PREFLIGHT_CHECKLIST"));
        CheckBox checklistBox = createStyledCheckBox();
        checklistBox.selectedProperty().bindBidirectional(GameSettings.BooleanSetting.ENABLE_PREFLIGHT_CHECKLIST.property());

        formGrid.add(checklistLabel, 0, 1);
        formGrid.add(checklistBox, 1, 1);

        // 3. Random Seed Toggle
        Label randomSeedLabel = Utility.createLabel(localizedProperty("SettingsScreen.RANDOM_SEED"));
        CheckBox randomSeedBox = createStyledCheckBox();
        randomSeedBox.selectedProperty().bindBidirectional(GameSettings.BooleanSetting.USE_RANDOM_SEED.property());

        formGrid.add(randomSeedLabel, 0, 2);
        formGrid.add(randomSeedBox, 1, 2);

        // 4. Custom Seed Input + Reroll Button
        Label seedValueLabel = Utility.createLabel(localizedProperty("SettingsScreen.CUSTOM_SEED"));
        HBox seedInputBox = createSeedInputControls();

        formGrid.add(seedValueLabel, 0, 3);
        formGrid.add(seedInputBox, 1, 3);

        // Back Button
        Button backButton = new Button();
        backButton.textProperty().bind(localizedProperty("SettingsScreen.BACK"));
        Utility.setButtonStyle(backButton);
        backButton.setOnAction(_ -> mainWindow.returnToStartScreen());

        panel.getChildren().addAll(titleLabel, formGrid, backButton);
        root.getChildren().add(panel);
    }

    /**
     * Constructs the world seed text field and reroll control row.
     * Binds editability to manual seed mode so randomized seeds remain crisp, readable,
     * and copyable without allowing accidental manual edits.
     *
     * @return An aligned {@link HBox} containing the seed text field and reroll button
     */
    private HBox createSeedInputControls() {
        TextField seedField = new TextField(String.valueOf(GameSettings.LongSetting.WORLD_SEED.get()));
        seedField.setStyle("-fx-background-color: black; -fx-text-fill: lime; -fx-border-color: lime;");
        HBox.setHgrow(seedField, Priority.ALWAYS);

        // Lock manual editing when random seed generation is active while keeping text crisp and copyable
        seedField.editableProperty().bind(GameSettings.BooleanSetting.USE_RANDOM_SEED.readOnlyProperty().not());

        // Synchronize text display whenever the underlying world seed updates externally
        GameSettings.LongSetting.WORLD_SEED.readOnlyProperty().addListener((_, _, newVal) -> {
            String textVal = String.valueOf(newVal);
            if (!seedField.getText().equals(textVal)) {
                seedField.setText(textVal);
            }
        });

        // Parse manual input as a 64-bit long or hash alphanumeric strings deterministically
        seedField.textProperty().addListener((_, _, newText) -> {
            if (!seedField.isEditable() || newText == null || newText.isBlank()) {
                return;
            }
            try {
                long parsed = Long.parseLong(newText.trim());
                GameSettings.LongSetting.WORLD_SEED.property().set(parsed);
            } catch (NumberFormatException _) {
                GameSettings.LongSetting.WORLD_SEED.property().set(newText.trim().hashCode());
            }
        });

        Button rerollButton = new Button("🎲");
        Utility.setButtonStyle(rerollButton);
        rerollButton.disableProperty().bind(GameSettings.BooleanSetting.USE_RANDOM_SEED.readOnlyProperty());
        rerollButton.setOnAction(_ -> {
            long randomSeed = ThreadLocalRandom.current().nextLong();
            GameSettings.LongSetting.WORLD_SEED.property().set(randomSeed);
        });

        HBox container = new HBox(5, seedField, rerollButton);
        container.setAlignment(Pos.CENTER_LEFT);
        return container;
    }

    private CheckBox createStyledCheckBox() {
        CheckBox checkBox = new CheckBox();
        var cssUrl = getClass().getResource(CHECKBOX_CSS);
        if (cssUrl != null) {
            checkBox.getStylesheets().add(cssUrl.toExternalForm());
        }
        return checkBox;
    }

    private static ReadOnlyStringProperty localizedProperty(String key) {
        return Localize.getProperty(GameParameters.StringParam.LOCALIZATION_GUI_BUNDLE.get(), key);
    }

    public void setSize(double width, double height) {
        ((Pane) getRoot()).setPrefWidth(width);
        ((Pane) getRoot()).setPrefHeight(height);
    }
}