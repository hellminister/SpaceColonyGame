package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

public class StarList extends StackPane {
    private static final String BACKGROUND_STYLE = "-fx-background-color: black;";

    private final ObjectProperty<StarSystemAsset> selectedStar = new SimpleObjectProperty<>();
    private final ScrollPane scrollPane;
    private final int maxNumRow;
    private final ObservableMap<Cell, StarSystemAsset> selectedStars;
    private HBox selected = null;
    private StarNode selectedNode;

    public StarList(List<StarSystemAsset> starSystemAssets, int maxProbes, List<StarNode> starNodes,
                    ReadOnlyBooleanProperty preselection) {
        super();
        setStyle(BACKGROUND_STYLE);
        selectedStars = FXCollections.observableHashMap();

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
            var cell = new Cell(starSystemAsset, selectedStars, maxProbes, starNodes, i/2, preselection);
            cell.getCheckBox().setOnMouseClicked(_ -> selectStar(starSystemAsset, cell, cell.getStarNode()));
            cell.setOnMouseClicked(_ -> selectStar(starSystemAsset, cell, cell.getStarNode()));

            cell.getStarNode().setOnMouseClicked(_ -> selectStar2(starSystemAsset, cell, cell.getStarNode()));

            gridPane.add(cell, i%2, i/2);
        }


    }

    private ColumnConstraints createColumnConstraints() {
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(50);
        return columnConstraints;
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

    ObservableMap<Cell, StarSystemAsset> getSelectedStarsForProbes() {
        return selectedStars;
    }

    public void reset() {
        for (Cell cell : selectedStars.keySet()) {
            cell.getCheckBox().setSelected(false);
            cell.getStarNode().unchosenForProbe();
        }
        selectedStars.clear();
    }
}