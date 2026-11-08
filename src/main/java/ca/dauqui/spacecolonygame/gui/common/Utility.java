package ca.dauqui.spacecolonygame.gui.common;

import javafx.beans.binding.StringExpression;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Universal static operational factory supplying stylized graphical components,
 * black-box string formatting algorithms, and thermodynamic black-body stellar colour shaders.
 *
 * @author Jean-François Quintal
 */
public final class Utility {
    private static final String OUTPOST_BUTTON_CSS_PATH = Utility.class.getResource("outpostbutton.css").toExternalForm();
    private Utility(){
        /* Static utility constructor stub */
    }

    /**
     * Instantiates a text label fully bound onto an active dynamic text expression model.
     *
     * @param text Dynamic alphanumeric calculation layout tracking string parameters
     * @return Prefabricated responsive green Label component node
     */
    public static Label createLabel(StringExpression text) {
        Label label = createLabelUnbound();
        label.textProperty().bind(text);
        return label;
    }

    /**
     * Instantiates an unbound label component initialized with target theme styling fonts.
     *
     * @return Raw standalone styled label component layer
     */
    public static Label createLabelUnbound() {
        Label label = new Label();
        label.setFont(new Font("Arial", 20));
        label.setTextFill(Color.LIME);
        return label;
    }

    /**
     * Hard-wires retro inline styles onto standard navigation button controls.
     *
     * @param button The targeted button object node to receive flat colour styling rules
     */
    public static void setButtonStyle(Button button) {
        button.setStyle("""
                -fx-base: black;\
                 -fx-text-fill: lime;\
                 -fx-border-color: lime;\
                 -fx-border-width: 1px;""");

    }

    /**
     * Flexible scientific alphanumeric text processor rendering custom precision metric output.
     *
     * @param value    Raw floating point numerical value to format
     * @param decimals Strict rounding precision boundary limit threshold
     * @param unit     Localized string unit symbol to append at the end (e.g., "AU", "km")
     * @return Formatted plain readable text parameter string
     */
    public static String formatMetric(double value, int decimals, String unit) {
        String format = "%." + decimals + "f %s";
        return String.format(format, value, unit);
    }

    /**
     * Injects custom external cascading stylesheets definitions onto strategic control buttons.
     *
     * @param button Target interface button entity block receiving custom class selectors
     */
    public static void applyOutpostSquareButtonStyle(Button button) {
        button.getStylesheets().add(OUTPOST_BUTTON_CSS_PATH);
        button.getStyleClass().add("outpost-button");
    }

    /**
     * Computational thermodynamic shader translating raw effective surface temperatures (Kelvin)
     * into realistic visible light spectrum colours according to black-body radiation profiles.
     *
     * @param temp Real surface temperature value evaluated in Kelvin (or -1 for anomalous bodies)
     * @return Computed Colour vector structure to map directly into 3D sphere shaders material
     */
    public static Color getStarColor(double temp) {
        if (temp == -1) {
            return Color.MAGENTA;
        } else if (temp <= 3000) {
            return Color.rgb((int)(255.0 * (temp / 3000.0)), 0, 0);
        } else if (temp <= 4000) {
            return Color.rgb(255, (int)(127.0 * ((temp - 3000.0) / 1000.0)), 0);
        } else if (temp <= 6000) {
            return Color.rgb(255, 127 + (int)(128.0 * ((temp - 4000.0) / 2000.0)), 0);
        } else if (temp <= 10000) {
            return Color.rgb(255, 255, (int)(255.0 * ((temp - 6000.0) / 4000.0)));
        } else if (temp <= 25000) {
            return Color.rgb(255 - (int)(255.0 * ((25000.0 - temp) / 15000.0)), 255 - (int)(255.0 * ((25000.0 - temp) / 15000.0)), 255);
        } else {
            return Color.rgb(0, 0, 255);
        }
    }
}