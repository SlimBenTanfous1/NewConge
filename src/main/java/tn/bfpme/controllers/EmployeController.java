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
            Pane departementPane = loader.load();
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchUserConges() {
        ObservableList<Conge> HisUserList = FXCollections.observableArrayList();
        String sql = "SELECT DateDebut, DateFin, TypeConge FROM conge WHERE ID_User = ? AND Statut = ?";
        cnx = MyDataBase.getInstance().getCnx();
        try {
            PreparedStatement stm = cnx.prepareStatement(sql);
            stm.setInt(1, SessionManager.getInstance().getUser().getIdUser());
            stm.setString(2, String.valueOf(Statut.Approuvé));
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                HisUserList.add(new Conge(rs.getDate("DateDebut").toLocalDate(), rs.getDate("DateFin").toLocalDate(), serviceConge.getCongeTypeName(rs.getInt("TypeConge"))));
            }
            indexColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(TableHistorique.getItems().indexOf(cellData.getValue()) + 1));
            TableType.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
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
        Pane pane = new Pane();
        pane.setPrefSize(130, 130);
        pane.getStyleClass().add("paneRoundedCorners");

        Label soldeTypeLabel = new Label(serviceConge.getCongeTypeName(solde.getID_TypeConge())); // Use a method to get the type name
        soldeTypeLabel.setLayoutX(28);
        soldeTypeLabel.setLayoutY(24);
        soldeTypeLabel.setPrefSize(74, 36);
        soldeTypeLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        soldeTypeLabel.getStyleClass().addAll("FontSize-18", "RobotoItalic");

        Label soldeValueLabel = new Label(String.valueOf(solde.getTotalSolde()));
        soldeValueLabel.setLayoutX(28);
        soldeValueLabel.setLayoutY(60);
        soldeValueLabel.setPrefSize(74, 36);
        soldeValueLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        soldeValueLabel.getStyleClass().addAll("FontSize-14", "RobotoRegular");

        pane.getChildren().addAll(soldeTypeLabel, soldeValueLabel);
        return pane;
    }
}
