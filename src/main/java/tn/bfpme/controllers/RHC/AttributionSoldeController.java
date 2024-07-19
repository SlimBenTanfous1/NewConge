package tn.bfpme.controllers.RHC;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import tn.bfpme.models.TypeConge;
import tn.bfpme.models.User;
import tn.bfpme.services.ServiceTypeConge;
import tn.bfpme.services.ServiceUserSolde;
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
    private Spinner<Integer> periodespinner;
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
    private Label periodlabel;
    @FXML
    private Button btnRemoveFilter;
    @FXML
    private Label labelSolde;

    private ObservableList<TypeConge> originalList;
    private FilteredList<TypeConge> filteredList;
    private ServiceTypeConge serviceTypeConge;
    private ServiceUtilisateur serviceUtilisateur;
    private ServiceUserSolde serviceUserSolde;

    public AttributionSoldeController() {
        this.serviceTypeConge = new ServiceTypeConge();
        this.serviceUtilisateur = new ServiceUtilisateur();
        this.serviceUserSolde = new ServiceUserSolde();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colPas.setCellValueFactory(new PropertyValueFactory<>("pas"));
        colPeriodeJ.setCellValueFactory(new PropertyValueFactory<>("periodeJ"));
        colPeriodeM.setCellValueFactory(new PropertyValueFactory<>("periodeM"));
        colPeriodeA.setCellValueFactory(new PropertyValueFactory<>("periodeA"));
        colFile.setCellValueFactory(new PropertyValueFactory<>("file"));
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3650, 0);
        periodespinner.setValueFactory(valueFactory);
        periodespinner.setEditable(true);
        periodespinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePeriodLabel(newValue));
        loadSoldeConge();
        RechercheSol.addEventHandler(KeyEvent.KEY_RELEASED, event -> Recherche_Solde());
        btnRemoveFilter.setOnAction(event -> {
            removeFilter();
            clearTextFields();
        });
        Table_TypeConge.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateFields(newValue));
    }

    private void populateFields(TypeConge typeConge) {
        if (typeConge != null) {
            ID_Solde.setText(String.valueOf(typeConge.getIdTypeConge()));
            Designation_Solde.setText(typeConge.getDesignation());
            Pas_Solde.setText(String.valueOf(typeConge.getPas()));
            int totalDays = typeConge.getPeriodeJ() + typeConge.getPeriodeM() * 30 + typeConge.getPeriodeA() * 365;
            periodespinner.getValueFactory().setValue(totalDays);
            updatePeriodLabel(totalDays);
            if (typeConge.isFile()) {
                fileOuiRadioButton.setSelected(true);
            } else {
                fileNonRadioButton.setSelected(true);
            }
        }
    }

    private void clearTextFields() {
        ID_Solde.clear();
        Designation_Solde.clear();
        Pas_Solde.clear();
        periodespinner.getValueFactory().setValue(0);
        periodlabel.setText("");
        fileToggleGroup.selectToggle(null); // Clear radio button selection
    }

    private void splitPeriod(int totalDays, int[] periods) {
        periods[0] = totalDays / 365; // years
        totalDays %= 365;
        periods[1] = totalDays / 30; // months
        periods[2] = totalDays % 30; // days
    }

    private void updatePeriodLabel(int totalDays) {
        int[] periods = new int[3]; // 0: years, 1: months, 2: days
        splitPeriod(totalDays, periods);
        String periodText = String.format("%d année%s, %d mois, %d jour%s",
                periods[0], periods[0] > 1 ? "s" : "",
                periods[1],
                periods[2], periods[2] > 1 ? "s" : "");
        periodlabel.setText(periodText);
    }

    @FXML
    private void loadSoldeConge() {
        List<TypeConge> soldeCongeList = serviceTypeConge.getAllTypeConge();
        filteredList = new FilteredList<>(FXCollections.observableArrayList(soldeCongeList), p -> true);
        Table_TypeConge.setItems(filteredList);
        distributeExistingLeaveTypesToUsers();

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
    public void AjouterTypeButton() {
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int totalDays = periodespinner.getValue();
        int[] periods = new int[3]; // 0: years, 1: months, 2: days
        splitPeriod(totalDays, periods);
        boolean file = fileOuiRadioButton.isSelected();
        if (serviceTypeConge.existsByDesignation(designation)) {
            labelSolde.setText("Type de congé avec cette désignation existe déjà.");
            return;
        }
        serviceTypeConge.AddTypeConge(designation, pas, periods[2], periods[1], periods[0], file);
        loadSoldeConge();
        distributeNewLeaveTypeToUsers(designation);
        labelSolde.setText("Type de congé ajouté.");
    }
    @FXML
    public void ModifierTypeButton() {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int totalDays = periodespinner.getValue();
        int[] periods = new int[3]; // 0: years, 1: months, 2: days
        splitPeriod(totalDays, periods);
        boolean file = fileOuiRadioButton.isSelected();

        serviceTypeConge.updateTypeConge(idSolde, designation, pas, periods[2], periods[1], periods[0], file);
        loadSoldeConge();
        labelSolde.setText("Solde modifié.");
    }

    @FXML
    public void SupprimerTypeButton() {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());

        serviceTypeConge.deleteTypeConge(idSolde);
        loadSoldeConge();
        labelSolde.setText("Solde supprimé.");
        clearTextFields(); // Clear text fields after deletion
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
    private void distributeExistingLeaveTypesToUsers() {
        List<TypeConge> soldeCongeList = serviceTypeConge.getAllTypeConge();
        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            for (TypeConge typeConge : soldeCongeList) {
                int idSolde = typeConge.getIdTypeConge();
                double pas = typeConge.getPas();

                String distributeQuery = "INSERT INTO user_Solde (ID_User, ID_TypeConge, TotalSolde) SELECT ID_User, ?, 0 FROM user WHERE NOT EXISTS (SELECT 1 FROM user_Solde WHERE user_Solde.ID_User = user.ID_User AND user_Solde.ID_TypeConge = ?)";
                try (PreparedStatement distributeStmt = conn.prepareStatement(distributeQuery)) {
                    distributeStmt.setInt(1, idSolde);
                    distributeStmt.setInt(2, idSolde);
                    distributeStmt.executeUpdate();
                }

                String updateUserSoldeQuery = "UPDATE user_Solde SET TotalSolde = TotalSolde + ? WHERE ID_TypeConge = ?";
                try (PreparedStatement updateUserSoldeStmt = conn.prepareStatement(updateUserSoldeQuery)) {
                    updateUserSoldeStmt.setDouble(1, pas);
                    updateUserSoldeStmt.setInt(2, idSolde);
                    updateUserSoldeStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
