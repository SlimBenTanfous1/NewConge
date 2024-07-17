package tn.bfpme.controllers.RHC;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tn.bfpme.models.User;
import tn.bfpme.services.ServiceUtilisateur;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CardUserRHController {
    @FXML
    private HBox HBoxBtns;
    @FXML
    private Pane UserCard;
    @FXML
    private Label carddepart;
    @FXML
    private Label cardemail;
    @FXML
    private ImageView cardimage;
    @FXML
    private Label cardnameprename;
    @FXML
    private Label cardrole;

    private final ServiceUtilisateur UserS = new ServiceUtilisateur();
    int uid;
    String unom, uprenom, uemail, umdp, urole, udepart, updp;
    double SAnn, SExp, SMala, SMater;

    public void setData(User user, String roleName, String departmentName) {
        String imagePath = user.getImage();
        if (imagePath != null) {
            try {
                File file = new File(imagePath);
                FileInputStream inputStream = new FileInputStream(file);
                Image image = new Image(inputStream);
                cardimage.setImage(image);
            } catch (FileNotFoundException e) {
                System.err.println("Image file not found: " + imagePath);
            }
        } else {
            System.err.println("Image path is null for user: " + user);
        }
        cardnameprename.setText(user.getNom() + " " + user.getPrenom());
        cardemail.setText(user.getEmail());
        cardrole.setText(roleName);
        carddepart.setText(departmentName);
        UserCard.setStyle("-fx-border-radius: 5px;-fx-border-color:#808080");

        uprenom = user.getPrenom();
        uid = user.getIdUser();
        unom = user.getNom();
        uemail = user.getEmail();
        umdp = user.getMdp();
        urole = roleName;
        udepart = departmentName;
        updp = user.getImage();
        SAnn = user.getSoldeAnnuel();
        SMala = user.getSoldeMaladie();
        SMater = user.getSoldeMaternite();
        SExp = user.getSoldeExceptionnel();
    }

    @FXML
    void ModifierUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paneUsers.fxml"));
            Parent paneUsersRoot = loader.load();
            paneUserController pUC = loader.getController();
            pUC.ID_A.setText(String.valueOf(uid));
            pUC.nom_A.setText(unom);
            pUC.Prenom_A.setText(uprenom);
            pUC.email_A.setText(uemail);
            pUC.MDP_A.setText(umdp);
            pUC.image_A.setText(updp);
            pUC.S_Ann.setText(String.valueOf(SAnn));
            pUC.S_exc.setText(String.valueOf(SExp));
            pUC.S_mal.setText(String.valueOf(SMala));
            pUC.S_mat.setText(String.valueOf(SMater));
            String imagePath = updp;
            if (imagePath != null) {
                try {
                    File file = new File(imagePath);
                    FileInputStream inputStream = new FileInputStream(file);
                    Image image = new Image(inputStream);
                    pUC.PDPimageHolder.setImage(image);
                } catch (FileNotFoundException e) {
                    System.err.println("Image file not found: " + imagePath);
                }
            }
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/RH_Interface.fxml"));
            Parent mainRoot = mainLoader.load();
            RHController mainController = mainLoader.getController();
            mainController.getPaneCont().getChildren().setAll(paneUsersRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(mainRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void SupprimerUser(ActionEvent event) {
        UserS.DeleteByID(uid);
        ((GridPane) UserCard.getParent()).getChildren().remove(UserCard);
    }
}
