package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.beans.Observable;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Generic responsive layout data grid compiling and rendering text telemetry dashboards.
 * Dynamically partitions workspace column spans and handles localized property bindings.
 *
 * @param <T> Targeted underlying structural data entity model type to extract fields from
 * @author Jean-François Quintal
 */
public class InfoGrid<T> extends GridPane {

    /**
     * Constructs a formatted grid panel matrix, enforcing uniform column size spacing constraints.
     *
     * @param nbCol Total percentage slices allocation to apply across layout boundaries (in other word, the number of columns)
     */
    public InfoGrid(int nbCol) {
        super();
        for (int i = 0; i < nbCol; i++) {
            var col = new ColumnConstraints();
            col.setPercentWidth(100.0 / nbCol);
            this.getColumnConstraints().add(col);
        }

        setVgap(5);
    }

    /**
     * Generates a reactively bound display field attached to specific target proxy extractors.
     *
     * @param col                      Zero-indexed horizontal matrix grid layout coordinate
     * @param row                      Zero-indexed vertical matrix grid layout coordinate
     * @param readOnlyStringProperty   Prefix data text label description block
     * @param selection                Observable pointer tracking currently highlighted structural records
     * @param getSpectralType          Functional delegate layer mapping string property conversions
     * @param toObserve                Additional observable dependencies triggering string re-evaluation when invalidated
     * @return Fully operational bound text label Node component
     */
    public Label addInfo(int col, int row, ReadOnlyStringProperty readOnlyStringProperty, ReadOnlyObjectProperty<T> selection, ExtractFieldString<T> getSpectralType,
                         Observable... toObserve) {
        return addInfo(col, row, new InfoBinding<>(readOnlyStringProperty, selection, getSpectralType, toObserve));
    }

    /**
     * Appends an operational label string node onto the layout frame. Enforces max cell boundaries growth
     * configurations to maximize hover intersection room for mouse tooltips.
     *
     * @param col       Target table column cell coordinate index location
     * @param row       Target table row cell coordinate index location
     * @param labelText Fully compiled dynamic text formula calculation string
     * @return Contextual label node bound and positioned on the pane layout tree
     */
    public Label addInfo(int col, int row, StringExpression labelText) {
        Label label = Utility.createLabel(labelText);

        // Enforce maximum cell bounds absorption to optimize layout hitboxes for Tooltips
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setPickOnBounds(true);

        add(label, col, row);

        // Instruct parent grid layout calculations to fully expand this node space horizontally and vertically
        GridPane.setHgrow(label, Priority.ALWAYS);
        GridPane.setVgrow(label, Priority.ALWAYS);

        return label;
    }

    /**
     * Internal extraction text pipeline tracking target source changes
     * and string format formulations dynamically.
     *
     * @param <T> Concrete item payload data profile reference signature
     */
    private static class InfoBinding<T> extends StringBinding {
        private final ReadOnlyStringProperty infoLabel;
        private final ReadOnlyObjectProperty<T> star;
        private final ExtractFieldString<T> extractor;

        /**
         * Hooks tracking dependencies up to the evaluation pipeline engine.
         *
         * @param infoLabel Underlying persistent label prefix description identifier
         * @param star      The reactive target record observation property node
         * @param extractor Functional callback mapping telemetry data calculations
         * @param toObserve Additional observable dependencies monitored for invalidation triggers
         */
        InfoBinding(ReadOnlyStringProperty infoLabel, ReadOnlyObjectProperty<T> star, ExtractFieldString<T> extractor, Observable... toObserve) {
            this.infoLabel = infoLabel;
            this.star = star;
            this.extractor = extractor;
            bind(infoLabel, star);
            bind(toObserve);
        }

        @Override
        protected String computeValue() {
            // Concatenate structural information strings dynamically matching target selection data fields
            if (star.get() != null) {
                return infoLabel.get() + " " + extractor.extractString(star.get());
            } else {
                return infoLabel.get();
            }
        }
    }

    /**
     * Functional layout descriptor mapping telemetry data properties conversions into readable strings.
     *
     * @param <T> Targeted underlying structural data entity model reference profile
     */
    @FunctionalInterface
    public interface ExtractFieldString<T> {
        /**
         * Transforms internal data attributes back into plain readable text descriptions.
         *
         * @param star Concrete data structural unit evaluated by the framework
         * @return Formatted alphanumeric output presentation layer string
         */
        String extractString(T star);
    }
}