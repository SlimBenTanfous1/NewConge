package tn.bfpme.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import tn.bfpme.models.*;
import tn.bfpme.services.ServiceConge;
import tn.bfpme.services.ServiceUserSolde;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeController implements Initializable {
    @FXML
    private Label CU_dep;
    @FXML
    private Label CU_email;
    @FXML
    private Label CU_nomprenom;
    @FXML
    private Label CU_role;
    @FXML
    private ImageView CU_pdp;
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private TableView<Conge> TableHistorique;
    @FXML
    private TableColumn<Conge, LocalDate> TableDD;
    @FXML
    private TableColumn<Conge, LocalDate> TableDF;
    @FXML
    private TableColumn<Conge, TypeConge> TableType;
    @FXML
    private TableColumn<Conge, Integer> indexColumn;
    @FXML
    private HBox soldeHBox; // Reference to the HBox for solde

    private Connection cnx;
    private final ServiceConge serviceConge = new ServiceConge();
    private final ServiceUserSolde serviceUserSolde = new ServiceUserSolde();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        indexColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(TableHistorique.getItems().indexOf(cellData.getValue()) + 1));
        indexColumn.setSortable(false);
        fetchUserConges();
        reloadUserData();
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
    }

    private void fetchUserConges() {
        ObservableList<Conge> HisUserList = FXCollections.observableArrayList();
        String sql = "SELECT c.DateDebut, c.DateFin, t.Designation " +
                "FROM conge c " +
                "JOIN typeconge t ON c.TypeConge = t.ID_TypeConge " +
                "WHERE c.ID_User = ? AND c.Statut = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stm = cnx.prepareStatement(sql)) {
            stm.setInt(1, SessionManager.getInstance().getUser().getIdUser());
            stm.setString(2, String.valueOf(Statut.Approuvé));
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    Conge conge = new Conge(
                            rs.getDate("DateDebut").toLocalDate(),
                            rs.getDate("DateFin").toLocalDate(),
                            rs.getString("Designation")
                    );
                    HisUserList.add(conge);
                }
            }
            indexColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(TableHistorique.getItems().indexOf(cellData.getValue()) + 1));
            TableType.setCellValueFactory(new PropertyValueFactory<>("Designation"));
            TableDD.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
            TableDF.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
            TableHistorique.setItems(HisUserList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadUserData() {
        User currentUser = SessionManager.getInstance().getUser();
        String departement = SessionManager.getInstance().getUserDepartmentName();
        String role = SessionManager.getInstance().getUserRoleName();
        CU_dep.setText(departement);
        CU_email.setText(currentUser.getEmail());
        CU_nomprenom.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        CU_role.setText(role);
        String imagePath = currentUser.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    Image image = new Image(inputStream);
                    CU_pdp.setImage(image);
                } catch (FileNotFoundException e) {
                    System.err.println("Image file not found: " + imagePath);
                }
            } else {
                System.err.println("Image file does not exist: " + imagePath);
            }
        } else {
            System.err.println("Image path is null or empty for user: " + currentUser);
        }
        populateUserSoldes(currentUser.getIdUser());
    }

    private void populateUserSoldes(int userId) {
        List<UserSolde> soldes = serviceUserSolde.getUserSoldes(userId);
        soldeHBox.getChildren().clear(); // Clear existing panes
        for (UserSolde solde : soldes) {
            Pane soldePane = createSoldePane(solde);
            soldeHBox.getChildren().add(soldePane);
        }
    }

    private Pane createSoldePane(UserSolde solde) {
        // Create the Pane
        Pane pane = new Pane();
        pane.setPrefSize(130, 130);
        pane.setMinSize(130, 130);
        pane.setMaxSize(130, 130);
        pane.getStyleClass().add("paneRoundedCorndersMALADIE");

        // Create the first Label for solde type
        Label soldeTypeLabel = new Label(serviceConge.getCongeTypeName(solde.getID_TypeConge()));
        soldeTypeLabel.setPrefSize(130, 36);
        soldeTypeLabel.setLayoutY(36); // Adjust this value to center within the Pane
        soldeTypeLabel.setTextAlignment(TextAlignment.CENTER);
        soldeTypeLabel.getStyleClass().addAll("FontSize-18", "RobotoItalic");
        soldeTypeLabel.setFont(new Font("Roboto Regular", 18));
        soldeTypeLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // Create the second Label for solde value
        Label soldeValueLabel = new Label(String.valueOf(solde.getTotalSolde()));
        soldeValueLabel.setPrefSize(130, 36);
        soldeValueLabel.setLayoutY(72); // Adjust this value to move the number upwards
        soldeValueLabel.setTextAlignment(TextAlignment.CENTER);
        soldeValueLabel.getStyleClass().addAll("FontSize-14", "RobotoRegular");
        soldeValueLabel.setFont(new Font("Roboto Regular", 14));
        soldeValueLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // Add the Labels to the Pane
        pane.getChildren().addAll(soldeTypeLabel, soldeValueLabel);

        return pane;
    }
}
