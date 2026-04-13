module ca.dauqui.spacecolonygame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.management;
    requires java.naming;
    requires java.desktop;
    requires jdk.compiler;

    exports ca.dauqui.spacecolonygame;
    exports ca.dauqui.spacecolonygame.gui.mainwindow;
}