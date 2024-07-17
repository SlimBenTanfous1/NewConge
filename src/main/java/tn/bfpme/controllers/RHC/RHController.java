package tn.bfpme.controllers.RHC;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.bfpme.models.Departement;
import tn.bfpme.models.Role;
import tn.bfpme.models.RoleHierarchie;
import tn.bfpme.models.User;
import tn.bfpme.services.ServiceDepartement;
import tn.bfpme.services.ServiceRole;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.SessionManager;
import tn.bfpme.utils.StageManager;

import java.io.IOException;
import java.util.List;

public class RHController {
    @FXML
    public Pane PaneCont;
    @FXML
    private AnchorPane MainAnchorPane;
    private ServiceRole roleService;
    private paneRoleController PRC;
    private paneDepController PDC;
    private paneUserController PUC;
    public void initialize() {
        roleService = new ServiceRole();
        //loadRoleHierarchie();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            Pane departementPane = loader.load();
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Pane getPaneCont() {
        return PaneCont;
    }

    @FXML
    private void showDepartementPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paneDepartement.fxml"));
            Pane departementPane = loader.load();
            PaneCont.getChildren().clear();
            PaneCont.getChildren().add(departementPane);
            centerPane(PaneCont, departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showRolesPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paneRole.fxml"));
            Pane rolePane = loader.load();
            PaneCont.getChildren().clear();
            PaneCont.getChildren().add(rolePane);
            centerPane(PaneCont, rolePane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUtilisateursPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaneUsers.fxml"));
            Pane userPane = loader.load();
            PaneCont.getChildren().clear();
            PaneCont.getChildren().add(userPane);
            centerPane(PaneCont, userPane);
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

    @FXML
    void showEmailTempPane(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paneEmailTemp.fxml"));
            Pane userPane = loader.load();
            PaneCont.getChildren().clear();
            PaneCont.getChildren().add(userPane);
            centerPane(PaneCont, userPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCongePane(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paneConges.fxml"));
            Pane userPane = loader.load();
            PaneCont.getChildren().clear();
            PaneCont.getChildren().add(userPane);
            centerPane(PaneCont, userPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
