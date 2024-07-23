package tn.bfpme.controllers.RHC;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class RHController {
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    public Pane PaneCont;

    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            AnchorPane departementPane = loader.load();
            AnchorPane.setTopAnchor(departementPane, 0.0);
            AnchorPane.setLeftAnchor(departementPane, 0.0);
            AnchorPane.setRightAnchor(departementPane, 0.0);
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
            pane.prefWidthProperty().bind(PaneCont.widthProperty());
            pane.prefHeightProperty().bind(PaneCont.heightProperty());
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
