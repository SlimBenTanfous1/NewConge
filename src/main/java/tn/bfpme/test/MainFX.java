package tn.bfpme.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import tn.bfpme.utils.StageManager;

import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Gestion de Congés - Connection");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/imgs/logo_bfpme.png")));
            primaryStage.setScene(scene);
            primaryStage.show();
            Platform.runLater(() -> {
                primaryStage.setMaximized(true);
              /*  Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                primaryStage.setX(primaryScreenBounds.getMinX());
                primaryStage.setY(primaryScreenBounds.getMinY());
                primaryStage.setWidth(primaryScreenBounds.getWidth());
                primaryStage.setHeight(primaryScreenBounds.getHeight());*/
            });
            StageManager.addStage(primaryStage);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
