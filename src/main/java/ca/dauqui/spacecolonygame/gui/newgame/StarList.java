package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

public class StarList extends StackPane {
    private static final String BACKGROUND_STYLE = "-fx-background-color: black;";

    private final ObjectProperty<StarSystemAsset> selectedStar = new SimpleObjectProperty<>();
    private final ScrollPane scrollPane;
    private final int maxNumRow;
    private HBox selected = null;
    private StarNode selectedNode;

    public StarList(List<StarSystemAsset> starSystemAssets, ObservableList<StarSystemAsset> selectedStars, int maxProbes, List<StarNode> starNodes) {
        super();
        setStyle(BACKGROUND_STYLE);

        scrollPane = new ScrollPane();
        getChildren().add(scrollPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(BACKGROUND_STYLE);
        maxNumRow = starSystemAssets.size()/2-1;

        GridPane gridPane = new GridPane();
        gridPane.getColumnConstraints().addAll(createColumnConstraints(), createColumnConstraints());
        scrollPane.setContent(gridPane);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setStyle(BACKGROUND_STYLE);


        for (int i = 0; i < starSystemAssets.size(); i++) {
            StarSystemAsset starSystemAsset = starSystemAssets.get(i);
            var cell = createCell(starSystemAsset, selectedStars, maxProbes, starNodes, i/2);

            gridPane.add(cell, i%2, i/2);
        }


    }

    private ColumnConstraints createColumnConstraints() {
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(50);
        return columnConstraints;
    }

    private Cell createCell(StarSystemAsset star, ObservableList<StarSystemAsset> selectedStars, int maxProbes, List<StarNode> starNodes, int row) {
        Cell hBox = new Cell(row);
        hBox.setSpacing(10);
        hBox.setStyle(BACKGROUND_STYLE);
        hBox.setAlignment(Pos.CENTER_LEFT);

        StarNode node = new StarNode(star);
        starNodes.add(node);

        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().addListener((_, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(oldValue) && Boolean.FALSE.equals(newValue)) {
                selectedStars.remove(star);
                node.unchosenForProbe();
            } else if (Boolean.FALSE.equals(oldValue) && Boolean.TRUE.equals(newValue)) {
                if (selectedStars.size() < maxProbes) {
                    selectedStars.add(star);
                    node.chosenForProbe();
                } else {
                    checkBox.setSelected(false);
                }
            }
        });

        checkBox.setOnMouseClicked(_ -> selectStar(star, hBox, node));

        Label starName = Utility.createLabel(star.nameProperty());
        hBox.getChildren().addAll(checkBox, starName);

        hBox.setOnMouseClicked(_ -> selectStar(star, hBox, node));

        node.setOnMouseClicked(_ -> selectStar2(star, hBox, node));

        return hBox;
    }



    private void selectStar(StarSystemAsset star, Cell hBox, StarNode starNode) {
        selectedStar.set(star);
        unselect();
        selected = hBox;
        selectedNode = starNode;
        starNode.select();
        hBox.setBorder(Border.stroke(Color.LIME));
    }

    private void selectStar2(StarSystemAsset star, Cell hBox, StarNode node) {
        selectStar(star, hBox, node);
        scrollPane.setVvalue((double) hBox.getRowNum() / maxNumRow);

    }

    private void unselect() {
        if (selected != null) {
            selected.setBorder(Border.stroke(Color.BLACK));
        }
        if (selectedNode != null) {
            selectedNode.unselect();
        }
    }

    public ReadOnlyObjectProperty<StarSystemAsset> getSelection() {
        return selectedStar;
    }

    private static class Cell extends HBox {
        private final int rowNum;

        private Cell(int rowNum) {
            super();
            this.rowNum = rowNum;
        }

        public int getRowNum(){
            return rowNum;
        }
    }
}