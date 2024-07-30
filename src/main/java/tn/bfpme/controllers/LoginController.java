package tn.bfpme.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.bfpme.models.User;
import tn.bfpme.utils.*;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private TextField LoginEmail;
    @FXML
    private PasswordField LoginMDP;
    @FXML
    private TextField showPasswordField;
    @FXML
    private Button toggleButton;
    @FXML
    private ImageView toggleIcon;
    @FXML
    private Connection cnx;

    private Image showPasswordImage;
    private Image hidePasswordImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String style = "-fx-background-color: transparent; -fx-border-color: transparent transparent #eab53f transparent; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 3 0;";
        LoginEmail.setStyle(style);
        LoginMDP.setStyle(style);

        // Load the images
        showPasswordImage = new Image(getClass().getResourceAsStream("/assets/imgs/hide.png"));
        hidePasswordImage = new Image(getClass().getResourceAsStream("/assets/imgs/show.png"));
        toggleIcon.setImage(showPasswordImage);

        // Toggle visibility of password fields
        toggleButton.setOnAction(event -> {
            if (showPasswordField.isVisible()) {
                LoginMDP.setText(showPasswordField.getText());
                LoginMDP.setVisible(true);
                LoginMDP.setManaged(true);
                showPasswordField.setVisible(false);
                showPasswordField.setManaged(false);
                toggleIcon.setImage(showPasswordImage);
            } else {
                showPasswordField.setText(LoginMDP.getText());
                showPasswordField.setVisible(true);
                showPasswordField.setManaged(true);
                LoginMDP.setVisible(false);
                LoginMDP.setManaged(false);
                toggleIcon.setImage(hidePasswordImage);
            }
        });
    }

    @FXML
    void Login(ActionEvent event) {
        cnx = MyDataBase.getInstance().getCnx();
        String qry = "SELECT u.*, ur.ID_Role " +
                "FROM `user` as u " +
                "JOIN `user_role` ur ON ur.ID_User = u.ID_User " +
                "WHERE u.`Email`=?";

        try {
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setString(1, LoginEmail.getText());
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                String storedEncryptedPassword = rs.getString("MDP");

                // Decrypt the stored password
                String decryptedPassword = EncryptionUtil.decrypt(storedEncryptedPassword);

                // Check if the decrypted password matches the entered password
                if (decryptedPassword.equals(LoginMDP.getText())) {
                    User connectedUser = new User(
                            rs.getInt("ID_User"),
                            rs.getString("Nom"),
                            rs.getString("Prenom"),
                            rs.getString("Email"),
                            storedEncryptedPassword, // Keep the encrypted password for session
                            rs.getString("Image"),
                            rs.getInt("ID_Manager"),
                            rs.getInt("ID_Departement"),
                            rs.getInt("ID_Role")
                    );
                    connectedUser.setIdRole(rs.getInt("ID_Role"));
                    populateUserSolde(connectedUser);
                    SessionManager.getInstance(connectedUser);
                    navigateToProfile(event);
                } else {
                    System.out.println("Login failed: Invalid email or password.");
                }
            } else {
                System.out.println("Login failed: Invalid email or password.");
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    void FacialRecognitionButton(ActionEvent event) {

    }

    private void populateUserSolde(User user) {
        String soldeQuery = "SELECT us.*, tc.Designation FROM user_solde us JOIN typeconge tc ON us.ID_TypeConge = tc.ID_TypeConge WHERE us.ID_User = ?";
        try (PreparedStatement soldeStm = cnx.prepareStatement(soldeQuery)) {
            soldeStm.setInt(1, user.getIdUser());
            ResultSet soldeRs = soldeStm.executeQuery();
            while (soldeRs.next()) {
                int typeCongeId = soldeRs.getInt("ID_TypeConge");
                double totalSolde = soldeRs.getDouble("TotalSolde");
                String typeConge = soldeRs.getString("Designation");
                user.setSoldeByType(typeCongeId, totalSolde, typeConge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void navigateToProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Profile");
        //stage.setMaximized(true);
        stage.show();
        stage.toFront();
        StageManager.addStage("Profile", stage);
    }
}
