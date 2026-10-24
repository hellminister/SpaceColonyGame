package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gui.common.Utility;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class InfoGrid<T> extends GridPane {
    private final int nbCols;

    public InfoGrid(int nbCol) {
        super();
        nbCols = nbCol;
        for (int i = 0; i < nbCols; i++) {
            var col = new ColumnConstraints();
            col.setPercentWidth(100.0 / nbCols);
            this.getColumnConstraints().add(col);
        }

        setVgap(5);
    }

    public Label addInfo(int col, int row, ReadOnlyStringProperty readOnlyStringProperty, ReadOnlyObjectProperty<T> selection, ExtractFieldString<T> getSpectralType) {
        return addInfo(col, row, new InfoBinding<>(readOnlyStringProperty, selection, getSpectralType));
    }

    public Label addInfo(int col, int row, StringBinding labelText) {
        Label label = Utility.createLabelUnbound();
        label.textProperty().bind(labelText);
        add(label, col, row);
        return label;
    }

    public Label addInfo(int col, int row, ReadOnlyStringProperty readOnlyStringProperty) {
        Label label = Utility.createLabel(readOnlyStringProperty);
        add(label, col, row);

        return label;
    }


    private static class InfoBinding<T> extends StringBinding {
        private final ReadOnlyStringProperty infoLabel;
        private final ReadOnlyObjectProperty<T> star;
        private final ExtractFieldString<T> extractor;

        InfoBinding(ReadOnlyStringProperty infoLabel, ReadOnlyObjectProperty<T> star, ExtractFieldString<T> extractor) {
            this.infoLabel = infoLabel;
            this.star = star;
            this.extractor = extractor;
            bind(infoLabel, star);
        }

        @Override
        protected String computeValue() {
            if (star.get() != null) {
                return infoLabel.get() + " " + extractor.extractString(star.get());
            } else {
                return infoLabel.get();
            }
        }
    }

    @FunctionalInterface
    public interface ExtractFieldString<T> {
        String extractString(T star);
    }
}