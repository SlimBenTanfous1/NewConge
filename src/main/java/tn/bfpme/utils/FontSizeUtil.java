package tn.bfpme.utils;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

public class FontSizeUtil {
    public static void bindFontSize(Pane root, Scene scene, double widthScaleFactor, double heightScaleFactor) {
        root.getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label label = (Label) node;
                label.styleProperty().bind(Bindings.createStringBinding(() -> {
                    double widthBasedFontSize = scene.getWidth() * widthScaleFactor;
                    double heightBasedFontSize = scene.getHeight() * heightScaleFactor;
                    double newFontSize = Math.min(widthBasedFontSize, heightBasedFontSize);
                    return "-fx-font-size: " + newFontSize + "px;";
                }, scene.widthProperty(), scene.heightProperty()));
            } else if (node instanceof Pane) {
                bindFontSize((Pane) node, scene, widthScaleFactor, heightScaleFactor);
            }
        });
    }
}
