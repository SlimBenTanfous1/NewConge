package tn.bfpme.controllers.RHC;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import tn.bfpme.services.ServiceConge;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/*public class AttributionSoldeController {

    @FXML
    private ListView<SoldeConge> Liste_Solde;
    @FXML
    private TextField RechercheSol;
    @FXML
    private TextField ID_Solde;
    @FXML
    private TextField Designation_Solde;
    @FXML
    private TextField Type_Solde;
    @FXML
    private TextField Pas_Solde;
    @FXML
    private TextField Periode_Solde;
    @FXML
    private Button Ajout_Solde;
    @FXML
    private Button Modifier_Solde;
    @FXML
    private Button Supprimer_Solde;
    @FXML
    private Label labelSolde;

    private ServiceSoldeConge serviceSoldeConge;
    private ServiceUtilisateur serviceUtilisateur;

    public AttributionSoldeController() {
        this.serviceSoldeConge = new ServiceSoldeConge();
        this.serviceUtilisateur = new ServiceUtilisateur();
    }

    @FXML
    public void initialize() {
        loadSoldeConge();
    }

    @FXML
    private void loadSoldeConge() {
        List<SoldeConge> soldeCongeList = serviceSoldeConge.getAllSoldeConges();
        Liste_Solde.getItems().setAll(soldeCongeList);
    }

    @FXML
    public void Recherche_Solde() {
        String searchText = RechercheSol.getText().trim().toLowerCase();
        List<SoldeConge> filteredList = serviceSoldeConge.searchSoldeConges(searchText);
        Liste_Solde.getItems().setAll(filteredList);
    }

    @FXML
    public void ajouterSolde() {
        String designation = Designation_Solde.getText().trim();
        String type = Type_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int periode = Integer.parseInt(Periode_Solde.getText().trim());

        serviceSoldeConge.addSoldeConge(designation, type, pas, periode);
        loadSoldeConge();
        distributeNewLeaveTypeToUsers(designation);
        labelSolde.setText("Solde ajouté et distribué aux utilisateurs.");
    }

    @FXML
    public void modifierSolde() {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());
        String designation = Designation_Solde.getText().trim();
        String type = Type_Solde.getText().trim();
        double pas = Double.parseDouble(Pas_Solde.getText().trim());
        int periode = Integer.parseInt(Periode_Solde.getText().trim());

        serviceSoldeConge.updateSoldeConge(idSolde, designation, type, pas, periode);
        loadSoldeConge();
        labelSolde.setText("Solde modifié.");
    }

    @FXML
    public void supprimerSolde() {
        int idSolde = Integer.parseInt(ID_Solde.getText().trim());

        serviceSoldeConge.deleteSoldeConge(idSolde);
        loadSoldeConge();
        labelSolde.setText("Solde supprimé.");
    }

    private void distributeNewLeaveTypeToUsers(String designation) {
        int idSolde = serviceSoldeConge.getSoldeCongeIdByDesignation(designation);
        double pas = serviceSoldeConge.getPasBySoldeId(idSolde);

        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            String distributeQuery = "INSERT INTO user_Solde (ID_User, idSolde) SELECT ID_User, ? FROM user";
            try (PreparedStatement distributeStmt = conn.prepareStatement(distributeQuery)) {
                distributeStmt.setInt(1, idSolde);
                distributeStmt.executeUpdate();
            }

            String updateUserSoldeQuery = "UPDATE user_Solde SET Solde = Solde + ? WHERE idSolde = ?";
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
*/