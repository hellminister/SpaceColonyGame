package ca.dauqui.spacecolonygame.gui.newgame.starselectionscreen;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Registry listing component managing transactional star selections
 * and tracking global pre-launch probe mapping balances.
 *
 * @author Jean-François Quintal
 */
class StarList extends StackPane {
    private static final String BACKGROUND_STYLE = "-fx-background-color: black;";

    private final ObjectProperty<StarSystemAsset> selectedStar = new SimpleObjectProperty<>();
    private final ScrollPane scrollPane;
    private final int maxNumRow;
    private final ObservableMap<Cell, StarSystemAsset> selectedStars;
    private HBox selected = null;
    private StarNode selectedNode;

    /**
     * Initializes the grid layout configuration and dynamically attaches
     * cross-linked star selection elements.
     *
     * @param starSystemAssets Eligible destination worlds catalogue registry
     * @param maxProbes        Maximum allowed operational probe deployment allowance
     * @param starNodes        Transient registry tracking generated 3D viewport objects
     * @param preselection     State tracking boolean checking screen authorization modes
     */
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

    /**
     * Generates standard localized width percentage constraints for data column maps.
     *
     * @return Generated structural layout parameters definition profile
     */
    private ColumnConstraints createColumnConstraints() {
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(50);
        return columnConstraints;
    }

    /**
     * Updates active navigation pointers, toggles reactive selection rings
     * inside the 3D meshes, and applies visual borders to highlighted text cells.
     *
     * @param star     The data record matching the chosen highlighted object
     * @param hBox     The physical target interface cell block receiving focus
     * @param starNode The 3D viewport proxy node matching the selection signature
     */
    private void selectStar(StarSystemAsset star, Cell hBox, StarNode starNode) {
        selectedStar.set(star);
        unselect();
        selected = hBox;
        selectedNode = starNode;
        starNode.selectedProperty().set(true);
        hBox.setBorder(Border.stroke(Color.LIME));
    }

    /**
     * Extends standard selection logic by forcing the viewport scrollpane
     * context to centre perfectly on the target row coordinate.
     *
     * @param star     The target core data record representation profile
     * @param hBox     The grid list table line element receiving focus triggers
     * @param node     The 3D node structural component mirroring actions
     */
    private void selectStar2(StarSystemAsset star, Cell hBox, StarNode node) {
        selectStar(star, hBox, node);
        scrollPane.setVvalue((double) hBox.getRowNum() / maxNumRow);

    }

    /**
     * Resets visual highlight boundaries and deactivates active 3D target selection properties.
     */
    private void unselect() {
        if (selected != null) {
            selected.setBorder(Border.stroke(Color.BLACK));
        }
        if (selectedNode != null) {
            selectedNode.selectedProperty().set(false);
        }
    }

    /**
     * @return Global observable pointer tracking the single chosen destination primary system
     */
    public ReadOnlyObjectProperty<StarSystemAsset> getSelection() {
        return selectedStar;
    }

    /**
     * @return Live transactional map recording which systems currently hold an assigned recon probe
     */
    ObservableMap<Cell, StarSystemAsset> getSelectedStarsForProbes() {
        return selectedStars;
    }

    /**
     * Clears all selected manifest records, unticking check boxes
     * and dimming 3D probe wireframes in a single unified step.
     */
    public void reset() {
        for (Cell cell : selectedStars.keySet()) {
            cell.getCheckBox().setSelected(false);
        }
        selectedStars.clear();
    }
}