package ca.dauqui.spacecolonygame.gui.newgame.chooseplanetscreen;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * UI visual representation component for a single planetary body within
 * the star system selection interface.
 * <p>
 * Displays physical scaling estimations based on astronomical units, structural
 * orbital distance vectors, and toggleable tactical selection wrappers.
 * </p>
 *
 * @author Jean-François Quintal
 */
class PlanetNode extends Group {
    private final Planet planet;
    private final double planetRadius;

    /**
     * Constructs a graphical planet node, processing radius unit metrics
     * and establishing reactive position layouts.
     *
     * @param planet The core domain model object containing astronomical data properties
     * @param selectedPlanet The global reactive screen property tracking which node holds user focus
     */
    public PlanetNode(Planet planet, ReadOnlyObjectProperty<PlanetNode> selectedPlanet) {
        this.planet = planet;
        Group selectedCircle = new Group();

        // Reactive binding pipeline: Visibility automatically toggles if this instance matches the global selection property
        selectedCircle.visibleProperty().bind(selectedPlanet.isEqualTo(this));

        // Enforce full bounds input capture so clicks register on transparent background filler room
        setPickOnBounds(true);

        // Invisible anchoring vertical baseline to enforce layout boundaries
        Line line = new Line();
        line.setStartX(0);
        line.setStartY(-500);
        line.setEndX(0);
        line.setEndY(500);
        line.setStroke(Color.TRANSPARENT);

        // Formatted astronomical distance textual readout
        Label distance = new Label(String.format("%.4f AU", planet.distance()));
        distance.setStyle("-fx-font-size: 20px;" +
                " -fx-text-fill: lime");
        distance.setTranslateY(290);
        // Centers the label dynamically relative to its computed graphical width
        distance.translateXProperty().bind(distance.widthProperty().divide(-2));

        // Visible structural indicator string tracking orbital alignment vectors
        Line  planetLine = new Line();
        planetLine.setStartX(0);
        planetLine.setStartY(-500);
        planetLine.setEndX(0);
        planetLine.setEndY(290);
        planetLine.setStroke(Color.LIGHTSLATEGRAY);

        Circle planetImage = new Circle();
        // Dynamically compute visual radius, scaling Jovian systems higher than terrestrial profiles
        planetRadius = Math.min(80, planet.radius() * (planet.radiusUnit().contains("j") ? 30 : 10));

        planetImage.setRadius(planetRadius);
        // Distinguish gas giants from rocky worlds using different base colour tones
        Color planetColor = planet.radiusUnit().contains("j") ? Color.RED : Color.DARKORANGE;
        planetImage.setFill(planetColor);

        // Highlight ring layer tracking selected user focus states
        Circle selected = new Circle();
        selected.setRadius(planetRadius + 10);
        selected.setStroke(Color.LIME);
        selected.setFill(Color.TRANSPARENT);
        selectedCircle.getChildren().add(selected);

        // so that the node won't dance in the HBox
        selectedCircle.setManaged(false);

        // need to place the selection circle manually now
        selectedCircle.setLayoutX(0);
        selectedCircle.setLayoutY(0);

        getChildren().addAll(line, planetLine, planetImage, distance, selectedCircle);

    }

    /**
     * Overridden so the HBox won't dance when this object is selected
     * @param height This parameter is ignored by Group
     * @return double the radius of the planet
     */
    @Override
    public double prefWidth(double height) {
        return planetRadius * 2;
    }

    /**
     * @return The underlying architectural data entity profile for this planetary object
     */
    public Planet getPlanet() {
        return this.planet;
    }
}