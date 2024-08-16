package tn.bfpme.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FontResizer {

    public static void resizeFonts(Node node, double width, double height) {
        double fontSize = Math.min(width, height) / 50;
        double fontSize2 = Math.min(width, height) / 50;
        double fontSize3 = Math.min(width, height) / 50;
        fontSize = Math.max(16, Math.min(fontSize, 28));
        fontSize2 = Math.max(13, Math.min(fontSize2, 16));
        fontSize3 = Math.max(12, Math.min(fontSize3, 14));

        if (node instanceof Label) {
            ((Label) node).setStyle("-fx-font-size: " + fontSize + "px;");
        } else if (node instanceof TextField) {
            ((TextField) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
        } else if (node instanceof TextInputControl) {
            ((TextInputControl) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
        } else if (node instanceof ComboBox) {
            ((ComboBox<?>) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
        } else if (node instanceof Button) {
            ((Button) node).setStyle("-fx-font-size: " + fontSize2 + "px;");
        } else if (node instanceof TabPane) {
            TabPane tabPane = (TabPane) node;
            for (Tab tab : tabPane.getTabs()) {
                Node tabLabel = findTabLabel(tab);
                if (tabLabel != null) {
                    tabLabel.setStyle("-fx-font-size: " + fontSize2 + "px;");
                }
                resizeFonts(tab.getContent(), width, height);
            }
        } else if (node instanceof ListView) {
            ((ListView<?>) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
        } else if (node instanceof TreeView) {
            ((TreeView<?>) node).setStyle("-fx-font-size: " + fontSize3 + "px;");
            for (TreeItem<?> item : ((TreeView<?>) node).getRoot().getChildren()) {
                resizeFonts(item.getGraphic(), width, height);
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
        }
    }

    private static Node findTabLabel(Tab tab) {
        if (tab.getGraphic() instanceof Labeled) {
            return tab.getGraphic();
        }
        if (tab.getText() != null && !tab.getText().isEmpty()) {
            Label label = new Label(tab.getText());
            tab.setGraphic(label);
            tab.setText(null);
            return label;
        }
        return null;
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
