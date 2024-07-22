package tn.bfpme.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.bfpme.models.*;
import tn.bfpme.services.ServiceConge;
import tn.bfpme.services.ServiceEmailTemp;
import tn.bfpme.services.ServiceNotification;
import tn.bfpme.utils.Mails;
import tn.bfpme.utils.SessionManager;
import tn.bfpme.utils.StageManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MailingDemandeController implements Initializable {
    ObservableList<String> RaisonsList = FXCollections.observableArrayList(
            "Alternative Proposée",
            "Équité et Équilibre",
            "Politique de Rotation des Congés",
            "Congés Cumulés Non Autorisés",
            "Remplacement Non Disponible",
            "Évaluation de Performance ou Audit"
    );

    @FXML
    private Label mail_dest;
    @FXML
    private TextField mail_obj;
    @FXML
    private TextArea mail_text;
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private ComboBox<EmailsTemplates> raison_mail;

    String employeeName, startDate, endDate, managerName, managerRole;
    private Conge conge;
    private User user;
    private final ServiceConge serviceConge = new ServiceConge();
    private final ServiceEmailTemp emailtempService = new ServiceEmailTemp();
    private final ServiceNotification notifService = new ServiceNotification();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*raison_mail.setValue("Sélectionner une raison");
        raison_mail.setItems(RaisonsList);*/
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
        try {
            List<EmailsTemplates> emailtempList = emailtempService.getAllEmailsTemplates();
            ObservableList<EmailsTemplates> emailstemps = FXCollections.observableArrayList(emailtempList);
            raison_mail.setItems(emailstemps);
            raison_mail.setCellFactory(param -> new ListCell<EmailsTemplates>() {
                @Override
                protected void updateItem(EmailsTemplates item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getObject() == null) {
                        setText(null);
                    } else {
                        setText(item.getObject());
                    }
                }
            });
            raison_mail.setButtonCell(new ListCell<EmailsTemplates>() {
                @Override
                protected void updateItem(EmailsTemplates item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getObject() == null) {
                        setText(null);
                    } else {
                        setText(item.getObject());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        User manager = SessionManager.getInstance().getUser();
        String role = SessionManager.getInstance().getUserRoleName();
        if (manager != null) {
            managerName = manager.getPrenom() + " " + manager.getNom();
            managerRole = String.valueOf(role);
        }
    }

    public void setData(Conge conge, User user) {
        this.conge = conge;
        this.user = user;
        employeeName = user.getPrenom() + " " + user.getNom();
        startDate = String.valueOf(conge.getDateDebut());
        endDate = String.valueOf(conge.getDateFin());
        mail_dest.setText(user.getEmail());
    }

    @FXML
    void selectRaison(ActionEvent event) {
        raison_mail.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mail_obj.setText(newValue.getObject());
                mail_text.setText(newValue.getMessage());
            }
        });
    }

    @FXML
    void Annuler_mail(ActionEvent event) {
        mail_text.setText("");
        mail_obj.setText("");

    }

    @FXML
    void Envoyer_mail(ActionEvent event) {
        String to = mail_dest.getText();
        String subject = mail_obj.getText();
        String messageText = mail_text.getText();
        serviceConge.NewMessage(messageText,user.getIdUser(),conge.getIdConge());
        if (conge.getStatut() == Statut.Approuvé) {
            String NotifSubject = "Votre Demande de congé " + conge.getTypeConge() + " a été approuvé.";
            notifService.NewNotification(user.getIdUser(), NotifSubject, 1, messageText);
        } else if (conge.getStatut() == Statut.Rejeté) {
            String NotifSubject = "Votre Demande de congé " + conge.getTypeConge() + " a été rejeté à cause de " + subject;
            notifService.NewNotification(user.getIdUser(), NotifSubject, 0, messageText);
        }

        //Mails.sendEmail(to,subject,messageText);
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
    }

}
