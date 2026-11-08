module ca.dauqui.spacecolonygame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;
    requires java.desktop;

    exports ca.dauqui.spacecolonygame;
    opens ca.dauqui.spacecolonygame.gui.mainwindow to javafx.graphics;
}