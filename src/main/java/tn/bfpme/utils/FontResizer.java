package tn.bfpme.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FontResizer {

    public static void resizeFonts(Node node, double width, double height) {
        double fontSize = Math.min(width, height) / 50;
        double fontSize2 = Math.min(width, height) / 50;
        double fontSize3 = Math.min(width, height) / 50;
        fontSize = Math.max(14, Math.min(fontSize, 18));
        fontSize2 = Math.max(13, Math.min(fontSize2, 16));
        fontSize3 = Math.max(12, Math.min(fontSize3, 14));

        if (node instanceof Label) {
            Label label = (Label) node;
            label.setStyle("-fx-font-size: " + fontSize + "px;");
        } else if (node instanceof TextField) {
            ((TextField) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
        } else if (node instanceof TextInputControl) {
            ((TextArea) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
        } else if (node instanceof ComboBox) {
            ((ComboBox<?>) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
        } else if (node instanceof Button) {
            ((Button) node).setStyle("-fx-font-size: " + fontSize2 + "px;");
        } else if (node instanceof TabPane) {
            ((TabPane) node).setStyle("-fx-tab-label-font-size: " + fontSize2 + "px;");
            for (Tab tab : ((TabPane) node).getTabs()) {
                if (tab.getGraphic() instanceof Labeled) {
                    ((Labeled) tab.getGraphic()).setStyle("-fx-font-size: " + fontSize + "px;");
                } else if (tab.getGraphic() != null) {
                    resizeFonts(tab.getGraphic(), width, height);
                }
                resizeFonts(tab.getContent(), width, height);
            }
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
        } else if (node instanceof TabPane) {
            for (Tab tab : ((TabPane) node).getTabs()) {
                resizeFonts(tab.getContent(), width, height);
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
