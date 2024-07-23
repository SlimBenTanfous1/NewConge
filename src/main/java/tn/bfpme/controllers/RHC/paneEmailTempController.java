package tn.bfpme.controllers.RHC;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import tn.bfpme.models.EmailsTemplates;
import tn.bfpme.models.Role;
import tn.bfpme.models.User;
import tn.bfpme.services.ServiceEmailTemp;
import tn.bfpme.services.ServiceRole;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class paneEmailTempController implements Initializable {
    @FXML
    private TextArea MessageTF;
    @FXML
    private ListView<EmailsTemplates> ObjListView;
    @FXML
    private TextField RechercheTemp;
    @FXML
    private TextField objectTF;
    @FXML
    private Button btnSave, btnSaveEdit, btnCancel;


    private final ServiceEmailTemp emailtempService = new ServiceEmailTemp();
    private FilteredList<EmailsTemplates> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Load();
        btnSave.setVisible(false);
        btnCancel.setVisible(false);
        btnSaveEdit.setVisible(false);
        MessageTF.setDisable(true);
        objectTF.setDisable(true);
        ObjListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                objectTF.setText(newValue.getObject());
                MessageTF.setText(newValue.getMessage());
                objectTF.setDisable(true);
                MessageTF.setDisable(true);
                btnSave.setVisible(false);
                btnSaveEdit.setVisible(false);
                btnCancel.setVisible(false);
            }
        });
        RechercheTemp.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(emailtemp -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return emailtemp.getObject().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private void Load() {
        try {
            List<EmailsTemplates> emailtempList = emailtempService.getAllEmailsTemplates();
            ObservableList<EmailsTemplates> emailstemps = FXCollections.observableArrayList(emailtempList);
            ObjListView.setItems(emailstemps);
            ObjListView.setCellFactory(param -> new ListCell<EmailsTemplates>() {
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
            showError("Failed to load roles: " + e.getMessage());
        }
    }

    @FXML
    void Save(ActionEvent event) {
        String obj = objectTF.getText();
        String msg = MessageTF.getText();
        if (MessageTF.getText().isEmpty() || objectTF.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champs requis non remplis", "Veuillez remplir toutes les informations nécessaires.");
            return;
        }
        emailtempService.AddEmailTemp(obj, msg);
        Load();
        btnSave.setVisible(false);
    }

    @FXML
    void SaveEdit(ActionEvent event) {
        EmailsTemplates selectedEmailTemp = ObjListView.getSelectionModel().getSelectedItem();
        String obj = objectTF.getText();
        String msg = MessageTF.getText();
        if (MessageTF.getText().isEmpty() || objectTF.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champs requis non remplis", "Veuillez remplir toutes les informations nécessaires.");
            return;
        }
        emailtempService.UpdateEmailTemp(selectedEmailTemp.getId_Email(), obj, msg);
        Load();
        btnSaveEdit.setVisible(false);
    }

    @FXML
    void Cancel(ActionEvent event) {
        objectTF.clear();
        MessageTF.clear();
        objectTF.setDisable(true);
        MessageTF.setDisable(true);
        btnCancel.setVisible(false);
        btnSaveEdit.setVisible(false);
        btnSave.setVisible(false);
    }

    @FXML
    void AjouterTemp(ActionEvent event) {
        objectTF.setDisable(false);
        MessageTF.setDisable(false);
        objectTF.clear();
        MessageTF.clear();
        btnSave.setVisible(true);
        btnCancel.setVisible(true);
        btnSaveEdit.setVisible(false);
    }

    @FXML
    void ModifierTemp(ActionEvent event) {
        objectTF.setDisable(false);
        MessageTF.setDisable(false);
        btnSaveEdit.setVisible(true);
        btnCancel.setVisible(true);
        btnSave.setVisible(false);

    }
    @FXML
    void Deselect(MouseEvent event) {
        objectTF.clear();
        MessageTF.clear();
        objectTF.setDisable(true);
        MessageTF.setDisable(true);
    }
    @FXML
    void EmailTempRecherche(ActionEvent actionEvent) {
        String searchText = RechercheTemp.getText().trim();
        for (EmailsTemplates emailtemp : emailtempService.getAllEmailsTemplates()) {
            if (emailtemp.getObject().equalsIgnoreCase(searchText)) {
                RechercheTemp.setText(emailtemp.getObject());
                break;
            }
        }
    }

    @FXML
    void SupprimerTemp(ActionEvent event) {
        EmailsTemplates selectedEmailTemp = ObjListView.getSelectionModel().getSelectedItem();
        if (selectedEmailTemp != null) {
            emailtempService.DeleteEmailTelp(selectedEmailTemp.getId_Email());
            Load();
        }
    }

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
