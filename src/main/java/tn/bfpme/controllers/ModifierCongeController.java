package tn.bfpme.controllers;

import tn.bfpme.models.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import tn.bfpme.services.ServiceConge;
import tn.bfpme.utils.StageManager;

import java.time.LocalDate;

public class ModifierCongeController {
    @FXML private Label text_info;
    @FXML private DatePicker modif_datedeb;
    @FXML private DatePicker modif_datefin;
    @FXML private TextArea modif_description;
    private Conge conge;
    private CongeCarteController congeCarteController;

    private final ServiceConge CongeS = new ServiceConge();
    public void setData(Conge conge, CongeCarteController congeCarteController) {
        this.conge = conge;
        this.congeCarteController = congeCarteController;
        modif_datedeb.setValue(conge.getDateDebut());
        modif_datefin.setValue(conge.getDateFin());
        modif_description.setText(conge.getDescription());
    }
    @FXML
    void annuler_conge(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void modifier_conge(ActionEvent actionEvent) {
        LocalDate startDate = modif_datedeb.getValue();
        LocalDate endDate = modif_datefin.getValue();
        String description = modif_description.getText();
        if (startDate != null && endDate != null && !description.isEmpty()) {
            conge.setDateDebut(startDate);
            conge.setDateFin(endDate);
            conge.setDescription(description);
            CongeS.updateConge(conge);
            text_info.setText("Modification effectuée");
            Stage stage = (Stage) modif_datedeb.getScene().getWindow();
            stage.close();
            if (congeCarteController != null) {
                congeCarteController.refreshData(conge);
            }
        } else {
            text_info.setText("Veuillez remplir tous les champs");
        }
    }
}
