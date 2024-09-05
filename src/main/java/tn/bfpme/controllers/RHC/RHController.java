package tn.bfpme.controllers.RHC;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tn.bfpme.utils.FontResizer;

import java.io.IOException;

public class RHController {
    @FXML
    private AnchorPane MainAnchorPane,BtnsLayAP;
    @FXML
    public Pane PaneCont;
    @FXML
    public Pane sideMenuPane;

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
        Platform.runLater(() -> {
            Stage stage = (Stage) MainAnchorPane.getScene().getWindow();
            stage.widthProperty().addListener((obs, oldVal, newVal) -> FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight()));
            stage.heightProperty().addListener((obs, oldVal, newVal) -> FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight()));
            FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight());
        });
        sideMenuPane.setVisible(false);
        PaneCont.setVisible(false);
        BtnsLayAP.setVisible(true);
    }

    @FXML
    private void showDepartementPane() {
        loadPane("/paneDepartement.fxml");
        BtnsLayAP.setVisible(false);
        PaneCont.setVisible(true);
        sideMenuPane.setVisible(true);
    }

    @FXML
    private void showRolesPane() {
        loadPane("/paneRole.fxml");
        BtnsLayAP.setVisible(false);
        PaneCont.setVisible(true);
        sideMenuPane.setVisible(true);
    }

    @FXML
    private void showUtilisateursPane() {
        loadPane("/paneUsers.fxml");
        BtnsLayAP.setVisible(false);
        PaneCont.setVisible(true);
        sideMenuPane.setVisible(true);
    }

    @FXML
    private void showEmailTempPane() {
        loadPane("/paneEmailTemp.fxml");
        BtnsLayAP.setVisible(false);
        PaneCont.setVisible(true);
        sideMenuPane.setVisible(true);
    }

    @FXML
    private void showCongePane() {
        loadPane("/paneConges.fxml");
        BtnsLayAP.setVisible(false);
        PaneCont.setVisible(true);
        sideMenuPane.setVisible(true);
    }

    @FXML
    private void showStatsPane() {
        loadPane("/Stat.fxml");
        BtnsLayAP.setVisible(false);
        PaneCont.setVisible(true);
        sideMenuPane.setVisible(true);
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
