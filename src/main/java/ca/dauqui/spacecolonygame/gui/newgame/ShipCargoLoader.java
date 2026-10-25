package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;


public class ShipCargoLoader extends VBox {
    private static final int MAXIMUM_WEIGHT = 100;
    private static final int MAXIMUM_COST = 100;
    private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.gui.gui";
    private static final String SPINNER_CSS = ShipCargoLoader.class.getResource("spinner.css").toExternalForm();
    private final List<CargoItem> cargoItems;
    private final IntegerBinding remainingWeight;
    private final IntegerBinding remainingCost;


    public ShipCargoLoader(EventHandler<ActionEvent> action) {
        super();

        setMaxWidth(750);
        maxHeightProperty().bind(widthProperty().multiply(4.0/3));

        cargoItems = initialiseCargoList();

        remainingWeight = new IntegerBinding() {
            {
                for (CargoItem cargoItem : cargoItems) {
                    bind(cargoItem.getCurrentCount());
                }
            }

            @Override
            protected int computeValue() {
                int remaining = MAXIMUM_WEIGHT;
                for (CargoItem cargoItem : cargoItems) {
                    remaining -= cargoItem.getWeight() * cargoItem.getCurrentCount().get();
                }
                return remaining;
            }
        };

        remainingCost = new IntegerBinding() {
            {
                for (CargoItem cargoItem : cargoItems) {
                    bind(cargoItem.getCurrentCount());
                }
            }

            @Override
            protected int computeValue() {
                int remaining = MAXIMUM_COST;
                for (CargoItem cargoItem : cargoItems) {
                    remaining -= cargoItem.getCost() * cargoItem.getCurrentCount().get();
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

        remains.add(Utility.createLabel(StringExpression.stringExpression(remainingWeight)), 1, 0);
        remains.add(Utility.createLabel(StringExpression.stringExpression(remainingCost)), 1, 1);

        Button loadShipButton = new Button();
        loadShipButton.textProperty().bind(localizedProperty("ShipCargoLoader.LOAD_SHIP_BUTTON"));

        loadShipButton.setOnAction(action);

        Utility.setButtonStyle(loadShipButton);
        hBox.getChildren().addAll(remains, loadShipButton);

        getChildren().addAll(shipLoadingTitle, gridPane,hBox);

    }

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

        for (int i = 0; i < cargoItems.size(); i++) {
            int row = i+1;
            CargoItem cargoItem = cargoItems.get(i);

            Tooltip description = new Tooltip();
            description.textProperty().bind(cargoItem.descriptionProperty());

            Label name = Utility.createLabel(cargoItem.nameProperty());
            name.setTooltip(description);
            gridPane.add(name, 0, row);
            Label weight = Utility.createLabelUnbound();
            weight.setText(String.valueOf(cargoItem.getWeight()));
            weight.setTooltip(description);
            gridPane.add(weight, 1, row);
            Label cost = Utility.createLabelUnbound();
            cost.setText(String.valueOf(cargoItem.getCost()));
            cost.setTooltip(description);
            gridPane.add(cost, 2, row);

            Spinner<Integer> count = new Spinner<>();
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(cargoItem.getStartingCount(), Integer.MAX_VALUE,
                    cargoItem.getStartingCount(), 1);
            count.setValueFactory(valueFactory);

            count.valueProperty().addListener((_, oldValue, newValue) -> {
                if (oldValue >= newValue) {
                    cargoItem.getCurrentCount().set(newValue);
                } else if (remainingWeight.get() >= cargoItem.getWeight() && remainingCost.get() >= cargoItem.getCost()) {
                    cargoItem.getCurrentCount().set(newValue);
                } else {
                    valueFactory.setValue(oldValue);
                }
            });
            count.getStylesheets().add(SPINNER_CSS);
            count.setTooltip(description);


            count.setMaxWidth(60);

            gridPane.add(count, 3, row);
        }

        return gridPane;
    }

    private List<CargoItem> initialiseCargoList() {
        var list = new ArrayList<CargoItem>();
        list.add(new RessourceItem(5, 6, 5));
        list.add(new RessourceItem(30, 40, 0));

        return list;
    }

    public void reset(){
        for (CargoItem cargoItem : cargoItems) {
            cargoItem.reset();
        }
    }


    public static ReadOnlyStringProperty localizedProperty(String name) {
        return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, name);
    }
}