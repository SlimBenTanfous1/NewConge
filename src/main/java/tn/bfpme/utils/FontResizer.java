package tn.bfpme.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FontResizer {

    public static void resizeFonts(Node node, double width, double height) {
        double fontSize = Math.min(width, height) / 50; // Adjust the divisor for desired scaling
        fontSize = Math.max(14, Math.min(fontSize, 20)); // Limiting font size to be between 14 and 20

        if (node instanceof Label) {
            ((Label) node).setFont(new Font(fontSize));
        } else if (node instanceof TextField) {
            ((TextField) node).setStyle("-fx-font-size: " + fontSize + "px;");
        } else if (node instanceof TextArea) {
            ((TextArea) node).setStyle("-fx-font-size: " + fontSize + "px;");
        } else if (node instanceof ComboBox) {
            ((ComboBox<?>) node).setStyle("-fx-font-size: " + fontSize + "px;");
        }

        if (node instanceof Pane) {
            for (Node child : ((Pane) node).getChildren()) {
                resizeFonts(child, width, height);
            }
        } else if (node instanceof AnchorPane) {
            for (Node child : ((AnchorPane) node).getChildren()) {
                resizeFonts(child, width, height);
            }
        } else if (node instanceof HBox) {
            for (Node child : ((HBox) node).getChildren()) {
                resizeFonts(child, width, height);
            }
        } else if (node instanceof VBox) {
            for (Node child : ((VBox) node).getChildren()) {
                resizeFonts(child, width, height);
            }
        } else if (node instanceof ScrollPane) {
            resizeFonts(((ScrollPane) node).getContent(), width, height);
        } else if (node instanceof GridPane) {
            for (Node child : ((GridPane) node).getChildren()) {
                resizeFonts(child, width, height);
            }
        }
    }

    public static Stage getStageFromNode(Node node) {
        Scene scene = node.getScene();
        if (scene != null) {
            Window window = scene.getWindow();
            if (window instanceof Stage) {
                return (Stage) window;
            }
        }
        return null;
    }
}
