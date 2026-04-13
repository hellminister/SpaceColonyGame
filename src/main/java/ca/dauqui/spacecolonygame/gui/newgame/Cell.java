package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

class Cell extends HBox {
    private static final String CHECKBOX_CSS = Cell.class.getResource("checkbox.css").toExternalForm();
    private static final String BACKGROUND_STYLE = "-fx-background-color: black;";

    private final int rowNum;
    private final StarNode node;
    private final CheckBox checkBox;
    private final StarSystemAsset star;


    Cell(StarSystemAsset star, ObservableMap<Cell, StarSystemAsset> selectedStars, int maxProbes, List<StarNode> starNodes, int rowNum,
         ReadOnlyBooleanProperty preselection) {
        super();
        this.star = star;
        this.rowNum = rowNum;
        setSpacing(10);
        setStyle(BACKGROUND_STYLE);
        setAlignment(Pos.CENTER_LEFT);

        node = new StarNode(star);
        starNodes.add(node);

        checkBox = new CheckBox();
        checkBox.selectedProperty().addListener((_, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(oldValue) && Boolean.FALSE.equals(newValue)) {
                selectedStars.remove(this);
                node.unchosenForProbe();
            } else if (Boolean.FALSE.equals(oldValue) && Boolean.TRUE.equals(newValue)) {
                if (selectedStars.size() < maxProbes) {
                    selectedStars.put(this, star);
                    node.chosenForProbe();
                } else {
                    checkBox.setSelected(false);
                }
            }
        });
        checkBox.disableProperty().bind(preselection.not());
        checkBox.getStylesheets().add(CHECKBOX_CSS);

        Label starName = Utility.createLabel(star.nameProperty());
        getChildren().addAll(checkBox, starName);
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public StarNode getStarNode() {
        return node;
    }

    public int getRowNum() {
        return rowNum;
    }
}