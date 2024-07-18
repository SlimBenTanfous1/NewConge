
package tn.bfpme.controllers.RHC;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class RHController {
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private Pane PaneCont;

    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            Pane departementPane = loader.load();
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDepartementPane() {
        loadPane("/paneDepartement.fxml");
    }

    @FXML
    private void showRolesPane() {
        loadPane("/paneRole.fxml");
    }

    @FXML
    private void showUtilisateursPane() {
        loadPane("/paneUsers.fxml");
    }

    @FXML
    private void showEmailTempPane() {
        loadPane("/paneEmailTemp.fxml");
    }

    @FXML
    private void showCongePane() {
        loadPane("/paneConges.fxml");
    }

    private void loadPane(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane pane = loader.load();
            PaneCont.getChildren().clear();
            PaneCont.getChildren().add(pane);
            centerPane(PaneCont, pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void centerPane(Pane container, Pane pane) {
        pane.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            double containerWidth = container.getWidth();
            double containerHeight = container.getHeight();
            double paneWidth = newValue.getWidth();
            double paneHeight = newValue.getHeight();
            double x = (containerWidth - paneWidth) / 2;
            double y = (containerHeight - paneHeight) / 2;
            pane.setLayoutX(x);
            pane.setLayoutY(y);
        });
    }
    public Pane getPaneCont() {
        return PaneCont;
    }
}

