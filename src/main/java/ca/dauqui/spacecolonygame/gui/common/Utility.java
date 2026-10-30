package ca.dauqui.spacecolonygame.gui.common;

import javafx.beans.binding.StringExpression;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class Utility {
    private static final String OUTPOST_BUTTON_CSS_PATH = Utility.class.getResource("outpostbutton.css").toExternalForm();
    private Utility(){}

    public static Label createLabel(StringExpression text) {
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

    public static void applyOutpostSquareButtonStyle(Button button) {
        button.getStylesheets().add(OUTPOST_BUTTON_CSS_PATH);
        button.getStyleClass().add("outpost-button");
    }

    public static Color getStarColor(double temp) {
        if (temp == -1){
           return (Color.MAGENTA);
        } else if (temp <= 3000){
            return (Color.rgb((int)(255.0 * (temp / 3000)), 0, 0));
        } else if (temp <= 4000) {
            return (Color.rgb(255, (int)(127.0 * ((4000- temp)/1000)), 0));
        } else if (temp <= 6000) {
            return (Color.rgb(255, 127 + (int)(128.0 * ((6000- temp)/2000)), 0));
        } else if (temp <= 10000) {
            return (Color.rgb(255, 255, (int)(255.0 * ((10000- temp) / 4000))));
        } else if (temp <= 25000) {
            return (Color.rgb(255 - (int)(255.0 * ((25000- temp) / 15000)), 255 - (int)(255.0 * ((25000- temp) / 15000)), 255));
        } else {
            return (Color.rgb(0, 0, 255));
        }
    }
}