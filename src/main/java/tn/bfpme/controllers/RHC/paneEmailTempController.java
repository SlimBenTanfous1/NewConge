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
    private final ServiceEmailTemp emailtempService = new ServiceEmailTemp();
    private FilteredList<EmailsTemplates> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Load();
        ObjListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                objectTF.setText(newValue.getObject());
                MessageTF.setText(newValue.getMessage());
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
    void AjouterTemp(ActionEvent event) {
        String obj = objectTF.getText();
        String msg = MessageTF.getText();
        emailtempService.AddEmailTemp(obj, msg);
        Load();
    }

    @FXML
    void ModifierTemp(ActionEvent event) {
        EmailsTemplates selectedEmailTemp = ObjListView.getSelectionModel().getSelectedItem();
        if (selectedEmailTemp != null) {
            String obj = objectTF.getText();
            String msg = MessageTF.getText();
            emailtempService.UpdateEmailTemp(selectedEmailTemp.getId_Email(), obj, msg);
            Load();

        }
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
}
