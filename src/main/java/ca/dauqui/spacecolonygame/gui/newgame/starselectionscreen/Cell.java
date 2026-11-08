package ca.dauqui.spacecolonygame.gui.newgame.starselectionscreen;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;
import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Visual grid item representing a selectable star system profile
 * within the navigational launch registry.
 *
 * @author Jean-François Quintal
 */
class Cell extends HBox {
    private static final String CHECKBOX_CSS = Cell.class.getResource("checkbox.css").toExternalForm();
    private static final String BACKGROUND_STYLE = "-fx-background-color: black;";

    private final int rowNum;
    private final StarNode node;
    private final CheckBox checkBox;
    private final StarSystemAsset star;

    /**
     * Constructs a single star item cell, initializing internal probe logic
     * trackers and wiring event cross-fires to the 3D viewport engine nodes.
     *
     * @param star          The underlying immutable configuration dataset for this specific star
     * @param selectedStars The live shared structural tracking map tracking active probe deployments
     * @param maxProbes     The hard boundary limit threshold governing maximum probe allocations
     * @param starNodes     The transient registration array collection holding reference to 3D mesh nodes
     * @param rowNum        The calculated grid matrix vertical index location assigned to this layout item
     * @param preselection  The global operational screen property checking if probe deployment is enabled
     */
    Cell(StarSystemAsset star, ObservableMap<Cell, StarSystemAsset> selectedStars, int maxProbes, List<StarNode> starNodes, int rowNum,
         ReadOnlyBooleanProperty preselection) {
        super();

        // Enforce full horizontal layout growth to absorb cell clicking bounds uniformly
        this.setMaxWidth(Double.MAX_VALUE);
        this.setPickOnBounds(true);

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
            } else if (Boolean.FALSE.equals(oldValue) && Boolean.TRUE.equals(newValue)) {
                // Enforce maximum structural probe deployment caps before registering entry
                if (selectedStars.size() < maxProbes) {
                    selectedStars.put(this, star);
                } else {
                    // Safe structural fallback roll-back avoiding binding block errors
                    checkBox.setSelected(false);
                }
            }
        });
        checkBox.disableProperty().bind(preselection.not());
        checkBox.getStylesheets().add(CHECKBOX_CSS);

        // Bidirectional reactive bridge keeping checkbox state and 3D overlay loops unified
        checkBox.selectedProperty().bindBidirectional(node.markedForProbeProperty());

        Label starName = Utility.createLabel(star.name());
        getChildren().addAll(checkBox, starName);
    }

    /**
     * @return The probe tracking verification tick box component
     */
    public CheckBox getCheckBox() {
        return checkBox;
    }

    /**
     * @return The mapped 3D tactical representation element linked to this entry
     */
    public StarNode getStarNode() {
        return node;
    }

    /**
     * @return Zero-indexed tracking layout row coordination ID
     */
    public int getRowNum() {
        return rowNum;
    }
}