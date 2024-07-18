package tn.bfpme.controllers.RHC;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.bfpme.models.TypeConge;
import tn.bfpme.services.ServiceTypeConge;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AttributionSoldeController {

    @FXML
    private ListView<TypeConge> Liste_TypeConge;
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
    private RadioButton OuiFichier;
    @FXML
    private RadioButton NonFichier;
    @FXML
    private Button Ajout_Solde;
    @FXML
    private Button Modifier_Solde;
    @FXML
    private Button Supprimer_Solde;
    @FXML
    private Label labelSolde;

    private ServiceTypeConge serviceTypeConge;
    private ServiceUtilisateur serviceUtilisateur;

    public AttributionSoldeController() {
        this.serviceTypeConge = new ServiceTypeConge();
        this.serviceUtilisateur = new ServiceUtilisateur();
    }

    @FXML
    public void initialize() {
        loadSoldeConge();
    }

    @FXML
    private void loadSoldeConge() {
        List<TypeConge> soldeCongeList = serviceTypeConge.getAllTypeConge();
        Liste_TypeConge.getItems().setAll(soldeCongeList);
    }

    @FXML
    public void Recherche_Solde() {
        String searchText = RechercheSol.getText().trim().toLowerCase();
        List<TypeConge> filteredList = serviceTypeConge.searchTypeConge(searchText);
        Liste_TypeConge.getItems().setAll(filteredList);
    }

    @FXML
    public void AjouterTypeButton() {
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int periodeJ = Integer.parseInt(PeriodeJourTF.getText().trim());
        int periodeM = Integer.parseInt(PeriodeMoisTF.getText().trim());
        int periodeA = Integer.parseInt(PeriodeAnTF.getText().trim());
        boolean file = OuiFichier.isSelected();

        serviceTypeConge.AddTypeConge(designation, pas, periodeJ, periodeM, periodeA, file);
        loadSoldeConge();
        distributeNewLeaveTypeToUsers(designation);
        labelSolde.setText("Solde ajouté et distribué aux utilisateurs.");
    }
    @FXML
    public void ModifierTypeButton() {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());
        String designation = Designation_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int periodeJ = Integer.parseInt(PeriodeJourTF.getText().trim());
        int periodeM = Integer.parseInt(PeriodeMoisTF.getText().trim());
        int periodeA = Integer.parseInt(PeriodeAnTF.getText().trim());
        boolean fileRequired = OuiFichier.isSelected(); // Assuming OuiRadioButton is the RadioButton for 'Oui' option

        serviceTypeConge.updateTypeConge(idSolde, designation, pas, periodeJ, periodeM, periodeA, fileRequired);
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
}
