package ca.dauqui.spacecolonygame.gui.newgame;

import ca.dauqui.spacecolonygame.gui.common.Utility;
import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.List;

public class StarField extends BorderPane {
    private static final double ANGLE_CHANGE = 5 * (Math.PI/180);

    public StarField(List<StarNode> stars) {
        setStyle("-fx-background-color: black;");

        Group content3D = new Group();

        Sphere sun = new Sphere(10);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.YELLOW);
        sun.setMaterial(material);
        sun.setTranslateX(0.0);
        sun.setTranslateY(0.0);
        sun.setTranslateZ(0.0);

        content3D.getChildren().add(sun);

        for (StarNode star : stars) {
            content3D.getChildren().add(star);
        }

        StackPane root = new StackPane(content3D);
        HBox hBox = new HBox();

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty().subtract(hBox.heightProperty()));
        root.setClip(clip);

        setCenter(root);

        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);

        Rotate xRotation = new Rotate();
        xRotation.setAxis(Rotate.X_AXIS);

        Rotate yRotation = new Rotate();
        yRotation.setAxis(Rotate.Y_AXIS);

        Rotate zRotation = new Rotate();
        zRotation.setAxis(Rotate.Z_AXIS);

        content3D.getTransforms().addAll(xRotation, yRotation, zRotation);

        DoubleProperty alf = new SimpleDoubleProperty(0);
        DoubleProperty bet = new SimpleDoubleProperty(0);
        DoubleProperty gam = new SimpleDoubleProperty(0);

        Button x1 = matrixRotateNodeButton( ()-> {
            alf.set(alf.get()+ ANGLE_CHANGE);
            matrixRotateNode(content3D, alf.get(), bet.get(), gam.get());
        }, Target.ROLL_PLUS.localizedProperty());

        Button x2 = matrixRotateNodeButton( () -> {
            alf.set(alf.get() - ANGLE_CHANGE);
            matrixRotateNode(content3D, alf.get(), bet.get(), gam.get());
        }, Target.ROLL_MINUS.localizedProperty());

        Button y1 = matrixRotateNodeButton( () -> {
            bet.set(bet.get() + ANGLE_CHANGE);
            matrixRotateNode(content3D, alf.get(), bet.get(), gam.get());
        }, Target.PITCH_PLUS.localizedProperty());
        Button y2 = matrixRotateNodeButton( () -> {
            bet.set(bet.get() - ANGLE_CHANGE);
            matrixRotateNode(content3D, alf.get(), bet.get(), gam.get());
        }, Target.PITCH_MINUS.localizedProperty());

        Button z1 = matrixRotateNodeButton( () -> {
            gam.set(gam.get() + ANGLE_CHANGE);
            matrixRotateNode(content3D, alf.get(), bet.get(), gam.get());
        }, Target.YAW_PLUS.localizedProperty());
        Button z2 = matrixRotateNodeButton( () -> {
            gam.set(gam.get() - ANGLE_CHANGE);
            matrixRotateNode(content3D, alf.get(), bet.get(), gam.get());
        }, Target.YAW_MINUS.localizedProperty());

        hBox.getChildren().addAll(x1, x2, y1, y2, z1, z2);

        setBottom(hBox);
    }

    private Button matrixRotateNodeButton(ButtonAction action, ReadOnlyStringProperty text) {
        Button button = new Button();
        button.textProperty().bind(text);
        Utility.setButtonStyle(button);

        button.setOnAction(_ -> action.action());

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            // Interval for updates in nanoseconds (e.g., 100ms = 100,000,000 ns)
            private final long interval = 100_000_000L;

            @Override
            public void handle(long now) {
                // Check if enough time has passed since the last update
                if (now - lastUpdate >= interval) {
                    action.action();
                    lastUpdate = now;
                }
            }
        };

        button.setOnMousePressed(_ -> timer.start());
        button.setOnMouseReleased(_ -> timer.stop());

        return button;
    }



    private void matrixRotateNode(Node n, double alf, double bet, double gam){
        double a11=Math.cos(alf)*Math.cos(gam);
        double a12=Math.cos(bet)*Math.sin(alf)+Math.cos(alf)*Math.sin(bet)*Math.sin(gam);
        double a13=Math.sin(alf)*Math.sin(bet)-Math.cos(alf)*Math.cos(bet)*Math.sin(gam);
        double a21=-Math.cos(gam)*Math.sin(alf);
        double a22=Math.cos(alf)*Math.cos(bet)-Math.sin(alf)*Math.sin(bet)*Math.sin(gam);
        double a23=Math.cos(alf)*Math.sin(bet)+Math.cos(bet)*Math.sin(alf)*Math.sin(gam);
        double a31=Math.sin(gam);
        double a32=-Math.cos(gam)*Math.sin(bet);
        double a33=Math.cos(bet)*Math.cos(gam);

        double d = Math.acos((a11+a22+a33-1d)/2d);
        if(d!=0d){
            double den=2d*Math.sin(d);
            Point3D p= new Point3D((a32-a23)/den,(a13-a31)/den,(a21-a12)/den);
            n.setRotationAxis(p);
            n.setRotate(Math.toDegrees(d));
        }
    }

    @FunctionalInterface
    private interface ButtonAction {
        void action();
    }

    @Localized
    private enum Target implements Localizable {
        ROLL_PLUS,
        ROLL_MINUS,
        PITCH_PLUS,
        PITCH_MINUS,
        YAW_PLUS,
        YAW_MINUS,
        ;

        private static final Localize<Target> local = new Localize<>(Target.class, "gui/starField.txt");

        @Override
        public ReadOnlyStringProperty localizedProperty() {
            return local.getString(this);
        }
        @Override
        public String toString() {
            return local.getString(this).get();
        }
    }
}