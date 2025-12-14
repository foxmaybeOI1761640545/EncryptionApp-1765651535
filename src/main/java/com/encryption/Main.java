package com.encryption;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/com/encryption/ui/main-view.fxml"))
        );
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/encryption/ui/theme.css")).toExternalForm()
        );
        stage.setTitle("String Encryptor (AES-GCM + PBKDF2)");
        stage.setScene(scene);
        if (root instanceof Region r) {
            double w = r.getPrefWidth();
            double h = r.getPrefHeight();
            if (w > 0 && h > 0) {
                stage.setWidth(w);
                stage.setHeight(h);
            }
        }
        stage.centerOnScreen();
        final double[] init = new double[2];
        stage.setOnShown(e -> {
            init[0] = stage.getWidth();
            init[1] = stage.getHeight();
            stage.setMinWidth(init[0]);
            stage.setMinHeight(init[1]);
            stage.setMaxWidth(init[0]);
            stage.setMaxHeight(init[1]);
            Platform.runLater(stage::centerOnScreen);
        });
        final boolean[] isFixing = {false};
        stage.maximizedProperty().addListener((obs, ov, nv) -> {
            if (isFixing[0]) return;
            if (nv) {
                stage.setMaxWidth(Double.MAX_VALUE);
                stage.setMaxHeight(Double.MAX_VALUE);
                Platform.runLater(() -> {
                    if (!stage.isMaximized()) return;
                    isFixing[0] = true;
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                    isFixing[0] = false;
                });
            } else {
                stage.setMinWidth(init[0]);
                stage.setMinHeight(init[1]);
                stage.setMaxWidth(init[0]);
                stage.setMaxHeight(init[1]);
                stage.setWidth(init[0]);
                stage.setHeight(init[1]);
                stage.centerOnScreen();
            }
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
