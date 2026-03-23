package ca.dauqui.spacecolonygame.gui.common;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class Utility {
    private Utility(){}


    public static Label createLabel(StringBinding text) {
        Label label = createLabelUnbound();
        label.textProperty().bind(text);
        return label;
    }

    public static Label createLabel(ReadOnlyStringProperty text) {
        Label label = createLabelUnbound();
        label.textProperty().bind(text);
        return label;
    }

    public static Label createLabelUnbound() {
        Label label = new Label();
        label.setFont(new Font("Arial", 20));
        label.setTextFill(Color.LIME);
        return label;
    }

    public static void setButtonStyle(Button button) {
        button.setStyle("""
                -fx-base: black;\
                 -fx-text-fill: lime;\
                 -fx-border-color: lime;\
                 -fx-border-width: 1px;""");

    }
}