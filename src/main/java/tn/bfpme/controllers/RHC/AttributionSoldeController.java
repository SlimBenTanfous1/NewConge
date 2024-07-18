package tn.bfpme.controllers.RHC;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import tn.bfpme.models.*;
import tn.bfpme.services.ServiceTypeConge;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.MyDataBase;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AttributionSoldeController implements Initializable {


    @FXML
    private TableView<TypeConge> Table_TypeConge;
    @FXML
    private TableColumn<TypeConge, String> colDesignation;
    @FXML
    private TableColumn<TypeConge, Double> colPas;
    @FXML
    private TableColumn<TypeConge, Integer> colPeriodeJ;
    @FXML
    private TableColumn<TypeConge, Integer> colPeriodeM;
    @FXML
    private TableColumn<TypeConge, Integer> colPeriodeA;
    @FXML
    private TableColumn<TypeConge, Boolean> colFile;
    @FXML
    private TextField RechercheSol;
    @FXML
    private TextField ID_Solde;
    @FXML
    private TextField Designation_Solde;
    @FXML
    private TextField Pas_Solde;
    @FXML
    private TextField PeriodeJourTF;
    @FXML
    private TextField PeriodeMoisTF;
    @FXML
    private TextField PeriodeAnTF;
    @FXML
    private RadioButton fileOuiRadioButton;
    @FXML
    private RadioButton fileNonRadioButton;
    @FXML
    private ToggleGroup fileToggleGroup;
    @FXML
    private Button Ajout_Solde;
    @FXML
    private Button Modifier_Solde;
    @FXML
    private Button Supprimer_Solde;
    @FXML
    private Button btnRemoveFilter;
    @FXML
    private Label labelSolde;

    private ServiceTypeConge serviceTypeConge;
    private ObservableList<TypeConge> originalList;
    private FilteredList<TypeConge> filteredList;

    private ServiceUtilisateur serviceUtilisateur;

    public AttributionSoldeController() {
        this.serviceTypeConge = new ServiceTypeConge();
        this.serviceUtilisateur = new ServiceUtilisateur();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colPas.setCellValueFactory(new PropertyValueFactory<>("pas"));
        colPeriodeJ.setCellValueFactory(new PropertyValueFactory<>("periodeJ"));
        colPeriodeM.setCellValueFactory(new PropertyValueFactory<>("periodeM"));
        colPeriodeA.setCellValueFactory(new PropertyValueFactory<>("periodeA"));
        colFile.setCellValueFactory(new PropertyValueFactory<>("file"));

        loadSoldeConge();

        RechercheSol.addEventHandler(KeyEvent.KEY_RELEASED, event -> Recherche_Solde());

        btnRemoveFilter.setOnAction(event -> removeFilter());

    }

    private void populateFields(TypeConge typeConge) {
        //ID.setText(String.valueOf(typeConge.getIdTypeConge()));
        Designation_Solde.setText(typeConge.getDesignation());
        Pas_Solde.setText(String.valueOf(typeConge.getPas()));
        PeriodeJourTF.setText(String.valueOf(typeConge.getPeriodeJ()));
        PeriodeMoisTF.setText(String.valueOf(typeConge.getPeriodeM()));
        PeriodeAnTF.setText(String.valueOf(typeConge.getPeriodeA()));
        fileOuiRadioButton.setSelected(typeConge.isFile());
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
        } else {
            filteredList.setPredicate(typeConge -> typeConge.getDesignation().toLowerCase().contains(searchText));
        }
    }

    @FXML
    public void AjouterTypeButton() {
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int periodeJ = Integer.parseInt(PeriodeJourTF.getText().trim());
        int periodeM = Integer.parseInt(PeriodeMoisTF.getText().trim());
        int periodeA = Integer.parseInt(PeriodeAnTF.getText().trim());
        boolean file = fileOuiRadioButton.isSelected();

        // Check if a TypeConge with the same designation already exists
        if (serviceTypeConge.existsByDesignation(designation)) {
            labelSolde.setText("Type de congé avec cette désignation existe déjà.");
            return;
        }

        serviceTypeConge.AddTypeConge(designation, pas, periodeJ, periodeM, periodeA, file);
        loadSoldeConge();
        labelSolde.setText("Type de congé ajouté.");
    }
    @FXML
    public void ModifierTypeButton() {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int periodeJ = Integer.parseInt(PeriodeJourTF.getText().trim());
        int periodeM = Integer.parseInt(PeriodeMoisTF.getText().trim());
        int periodeA = Integer.parseInt(PeriodeAnTF.getText().trim());
        boolean file = fileOuiRadioButton.isSelected();

        serviceTypeConge.updateTypeConge(idSolde, designation, pas, periodeJ, periodeM, periodeA, file);
        loadSoldeConge();
        labelSolde.setText("Solde modifié.");
    }

    @FXML
    public void SupprimerTypeButton() {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());

        serviceTypeConge.deleteTypeConge(idSolde);
        loadSoldeConge();
        labelSolde.setText("Solde supprimé.");
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

            String updateUserSoldeQuery = "UPDATE user_Solde SET TotalSolde = TotalSolde + ? WHERE ID_TypeConge = ?";
            try (PreparedStatement updateUserSoldeStmt = conn.prepareStatement(updateUserSoldeQuery)) {
                updateUserSoldeStmt.setDouble(1, pas);
                updateUserSoldeStmt.setInt(2, idSolde);
                updateUserSoldeStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void removeFilter() {
        RechercheSol.clear();
        filteredList.setPredicate(p -> true);
    }



}
