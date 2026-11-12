package ca.dauqui.spacecolonygame.gui.newgame.cargoloader;

import ca.dauqui.spacecolonygame.gamedata.Colony;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.settings.GameParameters;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * UI layout component managing the cargo manifest initialization screen
 * before launching a new colony game instance.
 *
 * @author Jean-François Quintal
 */
public class ShipCargoLoader extends VBox {

    private static final String SPINNER_CSS = ShipCargoLoader.class.getResource("spinner.css").toExternalForm();
    private final List<CargoItem> cargoItems;
    private final IntegerBinding remainingWeight;
    private final IntegerBinding remainingCost;
    private final int maximumWeight;
    private final int startingMoney;

    /**
     * Initializes the loader interface, sets up reactive constraints bindings,
     * and assembles the UI layout nodes.
     *
     * @param action Event handler executed upon confirmation of the cargo load
     */
    public ShipCargoLoader(EventHandler<ActionEvent> action) {
        super();

        // Retrieve global budget and weight limits from the configuration files
        maximumWeight = CargoMarket.getMaxWeight();
        startingMoney = CargoMarket.getInitialMoney();

        setMaxWidth(750);
        maxHeightProperty().bind(widthProperty().multiply(4.0/3));

        cargoItems = initialiseCargoList();

        // Reactive integer binding tracking available weight room left in the cargo bay
        remainingWeight = new IntegerBinding() {
            {
                // Register every item counter as a dependency for this binding expression
                for (CargoItem cargoItem : cargoItems) {
                    bind(cargoItem.currentCount());
                }
            }

            @Override
            protected int computeValue() {
                int remaining = maximumWeight;
                for (CargoItem cargoItem : cargoItems) {
                    remaining -= cargoItem.weight() * cargoItem.currentCount().get();
                }
                return remaining;
            }
        };

        // Reactive integer binding tracking available financial credits left for purchases
        remainingCost = new IntegerBinding() {
            {
                // Register every item counter as a dependency for this binding expression
                for (CargoItem cargoItem : cargoItems) {
                    bind(cargoItem.currentCount());
                }
            }

            @Override
            protected int computeValue() {
                int remaining = startingMoney;
                for (CargoItem cargoItem : cargoItems) {
                    remaining -= cargoItem.cost() * cargoItem.currentCount().get();
                }
                return remaining;
            }
        };

        Label shipLoadingTitle = Utility.createLabel(localizedProperty("ShipCargoLoader.SHIP_LOADING_TITLE"));

        GridPane gridPane = fillGridPane();

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPrefWidth(750);

        Label weightLeft = Utility.createLabel(Bindings.concat(localizedProperty("ShipCargoLoader.REMAINING_WEIGHT_LABEL"), " : "));
        Label costLeft = Utility.createLabel(Bindings.concat(localizedProperty("ShipCargoLoader.REMAINING_COST_LABEL"), " : "));

        GridPane remains = new GridPane();
        remains.setPrefWidth(500);
        remains.minWidthProperty().bind(hBox.widthProperty().multiply(0.66));

        remains.add(weightLeft, 0, 0);
        remains.add(costLeft, 0, 1);

        // Bind layout score values directly to reactive text expressions
        remains.add(Utility.createLabel(StringExpression.stringExpression(remainingWeight)), 1, 0);
        remains.add(Utility.createLabel(StringExpression.stringExpression(remainingCost)), 1, 1);

        Button loadShipButton = new Button();
        loadShipButton.textProperty().bind(localizedProperty("ShipCargoLoader.LOAD_SHIP_BUTTON"));
        loadShipButton.setOnAction(action);

        Utility.setButtonStyle(loadShipButton);
        hBox.getChildren().addAll(remains, loadShipButton);

        getChildren().addAll(shipLoadingTitle, gridPane,hBox);

    }

    /**
     * Builds out the market interface grid row layout and assigns custom Spinner controls.
     * Enforces preventional boundary validations to block budget overruns or mass limits.
     *
     * @return Formatted layout GridPane containing the catalogue entries
     */
    private GridPane fillGridPane() {
        GridPane gridPane = new GridPane();

        ColumnConstraints columnConstraints0 = new ColumnConstraints();
        columnConstraints0.setHalignment(HPos.LEFT);
        columnConstraints0.setPrefWidth(450);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHalignment(HPos.CENTER);
        columnConstraints1.setPrefWidth(100);
        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setHalignment(HPos.CENTER);
        columnConstraints2.setPrefWidth(100);
        ColumnConstraints columnConstraints3 = new ColumnConstraints();
        columnConstraints3.setHalignment(HPos.CENTER);
        columnConstraints3.setPrefWidth(100);

        gridPane.getColumnConstraints().addAll(columnConstraints0, columnConstraints1, columnConstraints2, columnConstraints3);

        Label cargoTitle = Utility.createLabel(localizedProperty("ShipCargoLoader.TITLE_CARGO"));
        gridPane.add(cargoTitle, 0, 0);
        GridPane.setHalignment(cargoTitle, HPos.CENTER);
        gridPane.add(Utility.createLabel(localizedProperty("ShipCargoLoader.TITLE_WEIGHT")), 1, 0);
        gridPane.add(Utility.createLabel(localizedProperty("ShipCargoLoader.TITLE_COST")), 2, 0);
        gridPane.add(Utility.createLabel(localizedProperty("ShipCargoLoader.TITLE_COUNT")), 3, 0);

        // Map data elements dynamically to interface lines
        for (int i = 0; i < cargoItems.size(); i++) {
            int row = i+1;
            CargoItem cargoItem = cargoItems.get(i);

            Tooltip description = new Tooltip();
            description.textProperty().bind(cargoItem.descriptionProperty());
            description.setShowDelay(Duration.millis(100));
            description.setHideDelay(Duration.ONE);

            Label name = Utility.createLabel(cargoItem.nameProperty());
            name.setTooltip(description);
            cellSizeLabel(name);
            name.setPickOnBounds(true);
            gridPane.add(name, 0, row);
            Label weight = Utility.createLabelUnbound();
            weight.setText(String.valueOf(cargoItem.weight()));
            weight.setTooltip(description);
            cellSizeLabel(weight);
            weight.setPickOnBounds(true);
            gridPane.add(weight, 1, row);
            Label cost = Utility.createLabelUnbound();
            cost.setText(String.valueOf(cargoItem.cost()));
            cost.setTooltip(description);
            cellSizeLabel(cost);
            cost.setPickOnBounds(true);
            gridPane.add(cost, 2, row);

            Spinner<Integer> count = new Spinner<>();
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(cargoItem.startingCount(), Integer.MAX_VALUE,
                    cargoItem.startingCount(), 1);
            count.setValueFactory(valueFactory);

            // Flow synchronization: Spinner adjustments -> Model mutations with strict cap validations
            count.valueProperty().addListener((_, oldValue, newValue) -> {
                if (oldValue >= newValue) {
                    // Item reductions are always authorized down to the minimum set in the spinner factory
                    cargoItem.currentCount().set(newValue);
                } else if (remainingWeight.get() >= cargoItem.weight() && remainingCost.get() >= cargoItem.cost()) {
                    // Purchases authorized only if remaining margins are safe
                    cargoItem.currentCount().set(newValue);
                } else {
                    // Revert display back to prior state if limit rules are broken
                    valueFactory.setValue(oldValue);
                }
            });

            // Flow synchronization: Model mutations -> Spinner updates (triggered by global resets)
            cargoItem.currentCount().addListener((_, _, newModelValue) -> {
                if (!newModelValue.equals(count.getValue())) {
                    valueFactory.setValue(newModelValue.intValue());
                }
            });

            count.getStylesheets().add(SPINNER_CSS);
            count.setTooltip(description);
            count.setPickOnBounds(true);


            count.setMaxWidth(60);

            gridPane.add(count, 3, row);
        }

        return gridPane;
    }

    /**
     * To make a label take all available space in a GridPane cell
     * @param label
     */
    private static void cellSizeLabel(Label label){
        GridPane.setHgrow(label, Priority.ALWAYS);
        GridPane.setVgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Unloads the selected payload configurations directly into the colony inventory systems
     * using safe pattern-matching checks.
     *
     * @param colony Target colony entity structure receiving the packed assets
     */
    public void loadCargoInShip(Colony colony) {
        for (CargoItem cargoItem : cargoItems) {
            if (cargoItem.currentCount().get() > 0) {
                switch (cargoItem) {
                    case ResourceItem ri -> colony.addInResourceInventory(ri.resourceType(), (ri.quantityPerUnit()*ri.currentCount().get()));
                    case SatelliteItem si -> colony.addInSatelliteInventory(si.satelliteType(), si.currentCount().get());
                    case BuildingItem bi -> colony.addInBuildingInventory(bi.building(), bi.currentCount().get());
                }
            }
        }
        reset();
    }

    /**
     * Compiles multiple market sectors together to build out the full catalogue matrix.
     *
     * @return The list of available items to sell
     */
    private List<CargoItem> initialiseCargoList() {
        var list = new ArrayList<CargoItem>();

        list.addAll(CargoMarket.createResourceMarketItems());
        list.addAll(CargoMarket.createSatelliteMarketItems());
        list.addAll(CargoMarket.createBuildingMarketItems());

        return list;
    }

    /**
     * Resets user manifest alterations and brings selections back to baseline configurations.
     */
    public void reset(){
        for (CargoItem cargoItem : cargoItems) {
            cargoItem.reset();
        }
    }

    /**
     * Fetches reactive localized resource bundle properties assigned to tracking string keys.
     */
    private static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(GameParameters.StringParam.LOCALIZATION_GUI_BUNDLE.get(), name);
    }
}