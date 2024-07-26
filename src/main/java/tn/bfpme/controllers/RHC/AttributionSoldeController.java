package tn.bfpme.controllers.RHC;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import tn.bfpme.models.TypeConge;
import tn.bfpme.services.ServiceTypeConge;
import tn.bfpme.services.ServiceUserSolde;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.MyDataBase;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AttributionSoldeController implements Initializable {

    @FXML
    private TableView<TypeConge> Table_TypeConge;
    @FXML
    private TableColumn<TypeConge, String> colDesignation;
    @FXML
    private TableColumn<TypeConge, Double> colPas;
    @FXML
    private TableColumn<TypeConge, Double> colLimite;
    @FXML
    private TableColumn<TypeConge, String> colPeriode;
    @FXML
    private TableColumn<TypeConge, Boolean> colFile;
    @FXML
    private TextField RechercheSol, Pas_Solde, ID_Solde, Designation_Solde, Limite_Solde;
    @FXML
    private RadioButton fileOuiRadioButton, fileNonRadioButton;
    @FXML
    private ToggleGroup fileToggleGroup;
    @FXML
    private Label periodlabel, labelSolde;
    @FXML
    private ComboBox<String> ComboPeriode;
    @FXML
    private Button btnSave, btnSaveEdit, btnCancel, Ajout_Solde, Supprimer_Solde, Modifier_Solde, btnRemoveFilter;
    @FXML
    private HBox Hfirst, HEnrgBox;
    private int state = 0;

    private ObservableList<TypeConge> originalList;
    private FilteredList<TypeConge> filteredList;
    private ServiceTypeConge serviceTypeConge;
    private ServiceUtilisateur serviceUtilisateur;
    private ServiceUserSolde serviceUserSolde;

    private HashMap<String, Integer> periodDaysMap;

    public AttributionSoldeController() {
        this.serviceTypeConge = new ServiceTypeConge();
        this.serviceUtilisateur = new ServiceUtilisateur();
        this.serviceUserSolde = new ServiceUserSolde();
        this.periodDaysMap = new HashMap<>();
        this.periodDaysMap.put("Semestriel", 180);
        this.periodDaysMap.put("Trimestriel", 90);
        this.periodDaysMap.put("Annuel", 365);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ComboPeriode.setItems(FXCollections.observableArrayList("Semestriel", "Trimestriel", "Annuel"));

        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colPas.setCellValueFactory(new PropertyValueFactory<>("pas"));
        colLimite.setCellValueFactory(new PropertyValueFactory<>("limite"));
        colPeriode.setCellValueFactory(new PropertyValueFactory<>("periode"));
        colFile.setCellValueFactory(new PropertyValueFactory<>("file"));

        ComboPeriode.valueProperty().addListener((obs, oldVal, newVal) -> updatePeriodLabel(newVal));

        loadSoldeConge();
        RechercheSol.addEventHandler(KeyEvent.KEY_RELEASED, event -> Recherche_Solde());
        btnRemoveFilter.setOnAction(event -> {
            removeFilter();
            clearTextFields();
        });
        Table_TypeConge.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateFields(newValue));
        formDisableOption(true);
        toggleButtonVisibility(true);
    }

    @FXML
    private void loadSoldeConge() {
        List<TypeConge> soldeCongeList = serviceTypeConge.getAllTypeConge();
        filteredList = new FilteredList<>(FXCollections.observableArrayList(soldeCongeList), p -> true);
        Table_TypeConge.setItems(filteredList);
    }

    @FXML
    public void Recherche_Solde() {
        String searchText = RechercheSol.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            removeFilter();
        } else {
            filteredList.setPredicate(typeConge -> typeConge.getDesignation().toLowerCase().contains(searchText));
        }
    }

    @FXML
    public void SaveAjout() {
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        String periode = ComboPeriode.getValue();
        double limite = Double.parseDouble(Limite_Solde.getText().trim());
        boolean file = fileOuiRadioButton.isSelected();
        if (Designation_Solde.getText().isEmpty() || Pas_Solde.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champs requis non remplis", "Veuillez remplir toutes les informations nécessaires.");
            return;
        }
        if (serviceTypeConge.existsByDesignation(designation)) {
            labelSolde.setText("Type de congé avec cette désignation existe déjà.");
            return;
        }
        serviceTypeConge.AddTypeConge(designation, pas, file, limite, periode);
        loadSoldeConge();
        distributeNewLeaveTypeToUsers(designation);
        labelSolde.setText("Type de congé ajouté.");
        toggleButtonVisibility(true);
    }

    @FXML
    public void SaveEdit(ActionEvent event) {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        String periode = ComboPeriode.getValue();
        double limite = Double.parseDouble(Limite_Solde.getText().trim());
        boolean file = fileOuiRadioButton.isSelected();
        if (Designation_Solde.getText().isEmpty() || Pas_Solde.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champs requis non remplis", "Veuillez remplir toutes les informations nécessaires.");
            return;
        }
        serviceTypeConge.updateTypeConge(idSolde, designation, pas, file, limite, periode);
        loadSoldeConge();
        labelSolde.setText("Solde modifié.");
        toggleButtonVisibility(true);
    }

    @FXML
    public void AjouterTypeButton() {
        state = 1;
        formDisableOption(false);
        clearTextFields();
        toggleButtonVisibility(false);
    }

    @FXML
    void Cancel(ActionEvent event) {
        reset();
    }

    @FXML
    public void ModifierTypeButton() {
        TypeConge selected = Table_TypeConge.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun congé sélectionné", "Veuillez sélectionner un congé à modifier.");
            return;
        }
        state = 2;
        formDisableOption(false);
        toggleButtonVisibility(false);
    }

    @FXML
    public void SupprimerTypeButton() {
        TypeConge selected = Table_TypeConge.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun congé sélectionné", "Veuillez sélectionner un congé à supprimer.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Êtes-vous sûr?");
        alert.setHeaderText("Confirmation de la suppression");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce type de congé?");
        ButtonType oui = new ButtonType("Oui");
        ButtonType non = new ButtonType("Non");
        alert.getButtonTypes().setAll(oui, non);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == oui) {
            int idSolde = selected.getIdTypeConge();
            serviceTypeConge.deleteTypeConge(idSolde);
            loadSoldeConge();
            labelSolde.setText("Solde supprimé.");
            clearTextFields(); // Clear text fields after deletion
        }
    }

    @FXML
    public void removeFilter() {
        RechercheSol.clear();
        filteredList.setPredicate(p -> true);
    }

    private void distributeNewLeaveTypeToUsers(String designation) {
        int idSolde = serviceTypeConge.getSoldeCongeIdByDesignation(designation);
        double pas = serviceTypeConge.getPasBySoldeId(idSolde);
        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            String distributeQuery = "INSERT INTO user_Solde (ID_User, ID_TypeConge, TotalSolde) SELECT ID_User, ?, 0 FROM user";
            try (PreparedStatement distributeStmt = conn.prepareStatement(distributeQuery)) {
                distributeStmt.setInt(1, idSolde);
                distributeStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void populateFields(TypeConge typeConge) {
        if (typeConge != null) {
            ID_Solde.setText(String.valueOf(typeConge.getIdTypeConge()));
            Designation_Solde.setText(typeConge.getDesignation());
            Pas_Solde.setText(String.valueOf(typeConge.getPas()));
            Limite_Solde.setText(String.valueOf(typeConge.getLimite()));
            ComboPeriode.setValue(typeConge.getPeriode());
            updatePeriodLabel(typeConge.getPeriode());

            if (typeConge.isFile()) {
                fileOuiRadioButton.setSelected(true);
            } else {
                fileNonRadioButton.setSelected(true);
            }

            formDisableOption(true);
        }
    }

    private void clearTextFields() {
        ID_Solde.clear();
        Designation_Solde.clear();
        Pas_Solde.clear();
        Limite_Solde.clear();
        ComboPeriode.setValue(null);
        periodlabel.setText("");
        fileToggleGroup.selectToggle(null); // Clear radio button selection
    }

    private void updatePeriodLabel(String periode) {
        String periodText = "";
        switch (periode) {
            case "Semestriel":
                periodText = "6 mois";
                break;
            case "Trimestriel":
                periodText = "3 mois";
                break;
            case "Annuel":
                periodText = "1 année";
                break;
            default:
                periodText = "Période non définie";
                break;
        }
        periodlabel.setText(periodText);
    }

    private void formDisableOption(boolean arg) {
        Designation_Solde.setDisable(arg);
        Pas_Solde.setDisable(arg);
        Limite_Solde.setDisable(arg);
        fileOuiRadioButton.setDisable(arg);
        fileNonRadioButton.setDisable(arg);
        ComboPeriode.setDisable(arg);
    }

    private void formClear() {
        Designation_Solde.clear();
        Pas_Solde.clear();
        Limite_Solde.clear();
        ComboPeriode.setValue(null);
        periodlabel.setText("");
        fileOuiRadioButton.setSelected(false);
        fileNonRadioButton.setSelected(false);
    }

    private void toggleButtonVisibility(boolean showCrud) {
        Hfirst.setVisible(showCrud);
        Hfirst.setDisable(!showCrud);
        HEnrgBox.setVisible(!showCrud);
        HEnrgBox.setDisable(showCrud);
    }

    private void reset() {
        Table_TypeConge.getSelectionModel().clearSelection();
        Table_TypeConge.setDisable(false);
        clearTextFields();
        formDisableOption(true);
        toggleButtonVisibility(true);
    }

    public void SaveButton(ActionEvent actionEvent) {
        if (state == 1) {
            SaveAjout();
        } else if (state == 2) {
            SaveEdit(actionEvent);
        }
        state = 0;
    }
}
