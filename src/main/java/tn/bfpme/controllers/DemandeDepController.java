package tn.bfpme.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.bfpme.models.Conge;
import tn.bfpme.models.Statut;
import tn.bfpme.models.TypeConge;
import tn.bfpme.models.User;
import tn.bfpme.services.ServiceConge;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.Mails;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;
import tn.bfpme.utils.StageManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemandeDepController implements Initializable {
    @FXML
    private Label CongePerson;
    @FXML
    private Label labelDD;
    @FXML
    private Label labelDF;
    @FXML
    private Label labelDesc;
    @FXML
    private Label labelJours;
    @FXML
    private Label labelType;
    @FXML
    private HBox DocFichHBOX;
    @FXML
    private HBox HBoxAppRef;
    Connection cnx = MyDataBase.getInstance().getCnx();
    private Conge conge;
    private User user;
    private int CongeDays;
    String employeeName, startDate, endDate, managerName, managerRole;
    String to, Subject, MessageText;
    private final ServiceConge serviceConge = new ServiceConge();
    private final ServiceUtilisateur serviceUser = new ServiceUtilisateur();
    public void setData(Conge conge, User user) {
        this.conge = conge;
        this.user = user;
        CongePerson.setText(user.getNom() + " " + user.getPrenom());
        labelDD.setText(String.valueOf(conge.getDateDebut()));
        labelDF.setText(String.valueOf(conge.getDateFin()));
        labelDesc.setText(conge.getDescription());
        labelType.setText(conge.getDesignation());
        CongeDays = (int) ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin());
        labelJours.setText(String.valueOf(CongeDays) + " Jours");
        if (this.conge.getFile().isBlank()) {
            DocFichHBOX.setVisible(false);
        }
        if (serviceUser.getManagerIdByUserId2(conge.getIdUser()) == SessionManager.getInstance().getUser().getIdUser()) {
            HBoxAppRef.setVisible(true);
            if (this.conge.getStatut() != Statut.En_Attente) {
                HBoxAppRef.setVisible(false);
            }
        } else {
            HBoxAppRef.setVisible(false);
        }
        employeeName = user.getPrenom() + " " + user.getNom();
        startDate = String.valueOf(conge.getDateDebut());
        endDate = String.valueOf(conge.getDateFin());
        to = user.getEmail();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User manager = SessionManager.getInstance().getUser();
        String role = SessionManager.getInstance().getUserRoleName();
        String departement = SessionManager.getInstance().getUserDepartmentName();
        if (manager != null) {
            managerName = manager.getPrenom() + " " + manager.getNom();
            managerRole = String.valueOf(role);
        }
    }

    @FXML
    void AfficherCongFichier(ActionEvent event) {
        String filePath = "src/main/resources/assets/files/" + conge.getFile();
        File file = new File(filePath);
        if (file.exists()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(file);
            } catch (IOException e) {
                Logger.getLogger(CongeCarteController.class.getName()).log(Level.SEVERE, null, e);
            }
        } else {
            System.out.println("File not found: " + filePath);
        }
    }

    @FXML
    void ApproverConge(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Êtes-vous sûrs?");
        alert.setHeaderText("Êtes-vous certain de vouloir approuver cette demande ?");
        ButtonType Oui = new ButtonType("Oui");
        ButtonType Non = new ButtonType("Non");
        alert.getButtonTypes().setAll(Oui, Non);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == Oui) {
            String Subject = "Approbation de Demande de Congé";
            String NotifContent = "";
            String MessageText = Mails.generateApprobationDemande(employeeName, startDate, endDate, managerName, managerRole);
            //Mails.sendEmail(to, Subject, MessageText); //Mailing
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeDepListe.fxml"));
                Parent root = loader.load();
                DemandeDepListeController controller = loader.getController();
                StageManager.closeAllStages();
                Stage demandeDepListeStage = new Stage();
                Scene scene = new Scene(root);
                demandeDepListeStage.setScene(scene);
                demandeDepListeStage.setTitle("Mailing de Demande");
                demandeDepListeStage.show();
                StageManager.addStage("DemandeDepListe", demandeDepListeStage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int congeDays = (int) ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            Alert cbon = new Alert(Alert.AlertType.INFORMATION);
            cbon.setTitle("Demande approuvée");
            cbon.setHeaderText("La demande de congé " + this.conge.getDesignation() + " de " + this.user.getNom() + " " + this.user.getPrenom() + " a été approuvée");
            cbon.showAndWait();
            this.conge.setStatut(Statut.Approuvé);
            serviceConge.updateUserSolde(this.user.getIdUser(), conge.getTypeConge().getIdTypeConge(), congeDays);
            serviceConge.updateStatutConge(this.conge.getIdConge(), Statut.Approuvé);
            //serviceConge.updateBalance(userId, IDTYPE, currentBalance - daysBetween);
        }
    }

    @FXML
    void RefuserConge(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Êtes vous sûrs?");
        alert.setHeaderText("Êtes-vous certain de vouloir rejeter cette demande ?");
        ButtonType Oui = new ButtonType("Oui");
        ButtonType Non = new ButtonType("Non");
        alert.getButtonTypes().setAll(Oui, Non);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == Oui) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/MailingDemande.fxml"));
                Parent root = loader.load();
                MailingDemandeController controller = loader.getController();
                controller.setData(conge, user);
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();
                Stage newStage = StageManager.getStage("DemandeDepListe");
                if (newStage == null) {
                    newStage = new Stage();
                    StageManager.addStage("DemandeDepListe", newStage);
                }
                Scene scene = new Scene(root);
                newStage.setScene(scene);
                newStage.setTitle("Mailing de Demande");
                newStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to load the MailingDemande.fxml");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("An unexpected error occurred");
            }
        }
        this.conge.setStatut(Statut.Rejeté);
        serviceConge.updateStatutConge(this.conge.getIdConge(), Statut.Rejeté);
    }

    @FXML
    void retour(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }


}
