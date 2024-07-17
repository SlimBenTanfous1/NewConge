package tn.bfpme.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.bfpme.models.Conge;
import tn.bfpme.models.Statut;
import tn.bfpme.models.TypeConge;
import tn.bfpme.services.ServiceConge;
import tn.bfpme.services.ServiceNotification;
import tn.bfpme.services.ServiceTypeConge;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;
import tn.bfpme.utils.StageManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class DemandeCongeController implements Initializable {
    @FXML
    private DatePicker datedebut;
    @FXML
    private DatePicker datefin;
    @FXML
    private TextArea Desc;
    @FXML
    private TextField Doc_Link;
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private Label TypeTitle;
    @FXML
    private ComboBox<TypeConge> cb_typeconge;
    @FXML
    private Pane paneConge;

    private final ServiceTypeConge serviceTypeConge = new ServiceTypeConge();
    private final ServiceConge serviceConge = new ServiceConge();
    LocalDate currentDate = LocalDate.now();
    String NotifSubject ="";
    String messageText ="";

    @FXML
    void Demander(ActionEvent event) {
        LocalDate DD = datedebut.getValue();
        LocalDate DF = datefin.getValue();
        String DOCLINK = Doc_Link.getText();
        String DESC = Desc.getText();
        TypeConge selectedTypeConge = cb_typeconge.getSelectionModel().getSelectedItem();
        if (selectedTypeConge == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Type de congé non sélectionné");
            alert.setContentText("Veuillez sélectionner un type de congé.");
            alert.showAndWait();
            return;
        }
        int IDTYPE = selectedTypeConge.getIdTypeConge();
        boolean requiresFile = selectedTypeConge.isFile();
        String docLinkToUse = requiresFile ? DOCLINK : null;
        if (datedebut.getValue() == null || datefin.getValue() == null || Desc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs requis non remplis");
            alert.setContentText("Veuillez remplir toutes les informations nécessaires.");
            alert.showAndWait();
            return;
        }
        LocalDate currentDate = LocalDate.now();
        if (DD.isBefore(currentDate) || DF.isBefore(currentDate) || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début et la date de fin doivent être postérieures ou égales à la date actuelle, et la date de début doit être antérieure ou égale à la date de fin.");
            alert.showAndWait();
            return;
        }
        if (DD == null || DF == null || DF == DD || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début doit être antérieure et différente de la date de fin.");
            alert.showAndWait();
            return;
        }

        Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText("Demande de congé créée avec succès !");
        ButtonType buttonHistorique = new ButtonType("Aller à l'historique");
        serviceConge.AddConge(new Conge(0, DD, DF, IDTYPE, Statut.En_Attente, SessionManager.getInstance().getUser().getIdUser(), docLinkToUse, DESC));
        String NotifSubject = "Vous avez reçu une nouvelle demande de congé " + selectedTypeConge.getDesignation();
        String messageText = "Vous avez reçu une nouvelle demande de congé " + selectedTypeConge.getDesignation() + " de la part de " + SessionManager.getInstance().getUser().getNom() + " " + SessionManager.getInstance().getUser().getPrenom() + " du " + DD + " au " + DF;
        //notifService.NewNotification(userService.getManagerIdByUserId(SessionManager.getInstance().getUser().getIdUser()), NotifSubject, 2, messageText);
        successAlert.getButtonTypes().setAll(buttonHistorique);
        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == buttonHistorique) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HistoriqueConge.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Historique congé");
                stage.show();
                StageManager.addStage("DemandeDepListe", stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Closing current scene...");
        }
    }

    @FXML
    void Doc_Imp(ActionEvent event) {
        String documentPath = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre document justicatif");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier", "*.pdf", "*.docx"));
        Stage stage = (Stage) datedebut.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Path destinationFolder = Paths.get("src/main/resources/assets/files");
                if (!Files.exists(destinationFolder)) {
                    Files.createDirectories(destinationFolder);
                }
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destinationPath = destinationFolder.resolve(fileName);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                documentPath = destinationPath.toString();
                System.out.println("Document uploaded successfully: " + documentPath);
                Doc_Link.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void TypeSelec(ActionEvent event) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            Pane departementPane = loader.load();
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<TypeConge> typeConges = serviceTypeConge.getAllTypeConge();
        ObservableList<TypeConge> observableTypeConges = FXCollections.observableArrayList(typeConges);
        cb_typeconge.setItems(observableTypeConges);
        cb_typeconge.setConverter(new StringConverter<TypeConge>() {
            @Override
            public String toString(TypeConge typeConge) {
                return typeConge.getDesignation();
            }
            @Override
            public TypeConge fromString(String string) {
                return cb_typeconge.getItems().stream().filter(typeConge ->
                        typeConge.getDesignation().equals(string)).findFirst().orElse(null);
            }
        });
        cb_typeconge.setOnAction(event -> {
            TypeConge selectedTypeConge = cb_typeconge.getSelectionModel().getSelectedItem();
            if (selectedTypeConge != null) {
                paneConge.setVisible(true);
                TypeTitle.setText("Conge " + selectedTypeConge.getDesignation());
            }
        });
    }

    /*@FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private ComboBox<String> cb_typeconge;
    @FXML
    private Pane paneAnnuel;
    @FXML
    private Pane paneExeptionnel;
    @FXML
    private Pane paneMaladie;
    @FXML
    private Pane paneSousSolde;
    @FXML
    private Pane paneMaternite;
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private Pane paneNaissance;
    @FXML
    private Pane paneGrossesse;
    private final ServiceConge CongeS = new ServiceConge();
    Connection cnx = MyDataBase.getInstance().getCnx();
    ObservableList<String> CongeList = FXCollections.observableArrayList("Annuel", "Exeptionnel", "Maladie", "Sous-Solde", "Maternité");
    LocalDate currentDate = LocalDate.now();
    private final ServiceNotification notifService = new ServiceNotification();
    private final ServiceUtilisateur userService = new ServiceUtilisateur();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cb_typeconge.setValue("Selectioner type");
        cb_typeconge.setItems(CongeList);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            Pane departementPane = loader.load();
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private DatePicker ANL_DD;
    @FXML
    private DatePicker ANL_DF;
    @FXML
    private TextArea ANL_Desc;

    String NotifSubject ="";
    String messageText ="";

    @FXML
    void ANL_Demander(ActionEvent event) {
        LocalDate DD = ANL_DD.getValue();
        LocalDate DF = ANL_DF.getValue();
        String DESC = ANL_Desc.getText();
        if (ANL_DD.getValue() == null || ANL_DF.getValue() == null || ANL_Desc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs requis non remplis");
            alert.setContentText("Veuillez remplir toutes les informations nécessaires.");
            alert.showAndWait();
            return;
        }
        if (DD.isBefore(currentDate) || DF.isBefore(currentDate) || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début et la date de fin doivent être postérieures ou égales à la date actuelle, et la date de début doit être antérieure ou égale à la date de fin.");
            alert.showAndWait();
            return;
        }
        if (DD == null || DF == null || DF == DD || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début doit être antérieure et différent à la date de fin.");
            alert.showAndWait();
            return;
        }
        String qry = "SELECT `Solde_Annuel` FROM `user` WHERE `ID_User`=?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, SessionManager.getInstance().getUser().getIdUser());
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                //long daysBetween = ChronoUnit.DAYS.between(DD, DF);
                if (rs.getInt("Solde_Annuel") > 0) {
                    Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText("Demande de congé créée avec succès !");
                    ButtonType buttonHistorique = new ButtonType("Aller à l'historique");
                    successAlert.getButtonTypes().setAll(buttonHistorique);
                    Optional<ButtonType> result = successAlert.showAndWait();
                    if (result.isPresent() && result.get() == buttonHistorique) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HistoriqueConge.fxml"));
                            Parent root = loader.load();
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            Scene scene = new Scene(root);
                            stage.setScene(scene);
                            stage.setTitle("Historique congé");
                            stage.show();
                            StageManager.addStage("DemandeDepListe", stage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Closing current scene...");
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Votre solde est dépassé pour cette année.");
                    alert.setContentText("Vous pouvez demander un congé sous-solde si nécessaire.");
                    alert.showAndWait();
                    paneAnnuel.setVisible(false);
                    paneSousSolde.setVisible(true);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private DatePicker EXP_DD;
    @FXML
    private DatePicker EXP_DF;
    @FXML
    private TextArea EXP_Desc;
    @FXML
    private TextField EXP_Doc_Link;

    @FXML
    void EXP_Demander(ActionEvent event) {
        LocalDate DD = EXP_DD.getValue();
        LocalDate DF = EXP_DF.getValue();
        String DESC = EXP_Desc.getText();
        String DOCLINK = EXP_Doc_Link.getText();
        if (EXP_DD.getValue() == null || EXP_DF.getValue() == null || EXP_Desc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs requis non remplis");
            alert.setContentText("Veuillez remplir toutes les informations nécessaires.");
            alert.showAndWait();
            return;
        }
        if (EXP_Doc_Link.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Document manquant");
            alert.setContentText("vous devez importer le document justificatif.");
            alert.showAndWait();
            return;
        }
        if (DD.isBefore(currentDate) || DF.isBefore(currentDate) || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début et la date de fin doivent être postérieures ou égales à la date actuelle, et la date de début doit être antérieure ou égale à la date de fin.");
            alert.showAndWait();
            return;
        }
        if (DD == null || DF == null || DF == DD || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début doit être antérieure et différent à la date de fin.");
            alert.showAndWait();
            return;
        }
        CongeS.Add(new Conge(0, DD, DF, TypeConge.Exceptionnel, Statut.En_Attente, SessionManager.getInstance().getUser().getIdUser(), DOCLINK, DESC));
        NotifSubject = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Exceptionnel;
        messageText = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Exceptionnel + " de la part de "+ SessionManager.getInstance().getUser().getNom()+" "+ SessionManager.getInstance().getUser().getPrenom() +" du "+ DD +" au "+ DF;
        notifService.NewNotification(userService.getManagerIdByUserId(SessionManager.getInstance().getUser().getIdUser()),NotifSubject,2,messageText);
        Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText("Demande de congé créée avec succès !");
        ButtonType buttonHistorique = new ButtonType("Aller à l'historique");
        successAlert.getButtonTypes().setAll(buttonHistorique);
        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == buttonHistorique) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HistoriqueConge.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Historique congé");
                stage.show();
                StageManager.addStage("DemandeDepListe", stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void EXP_Doc_Imp(ActionEvent event) {
        String documentPath = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre document justicatif");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier", "*.pdf", "*.docx"));
        Stage stage = (Stage) EXP_DD.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Path destinationFolder = Paths.get("src/main/resources/assets/files");
                if (!Files.exists(destinationFolder)) {
                    Files.createDirectories(destinationFolder);
                }
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destinationPath = destinationFolder.resolve(fileName);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                documentPath = destinationPath.toString();
                System.out.println("Document uploaded successfully: " + documentPath);
                EXP_Doc_Link.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private DatePicker MAL_DD;
    @FXML
    private DatePicker MAL_DF;
    @FXML
    private TextArea MAL_Desc;
    @FXML
    private TextField MAL_Doc_Link;

    @FXML
    void MAL_Demander(ActionEvent event) {
        LocalDate DD = MAL_DD.getValue();
        LocalDate DF = MAL_DF.getValue();
        String DESC = MAL_Desc.getText();
        String DOCLINK = MAL_Doc_Link.getText();
        if (MAL_DD.getValue() == null || MAL_DF.getValue() == null || MAL_Desc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs requis non remplis");
            alert.setContentText("Veuillez remplir toutes les informations nécessaires.");
            alert.showAndWait();
            return;
        }
        if (MAL_Doc_Link.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Certificat médical manquant");
            alert.setContentText("vous devez importer le certificat médical.");
            alert.showAndWait();
            return;
        }
        if (DD.isBefore(currentDate) || DF.isBefore(currentDate) || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début et la date de fin doivent être postérieures ou égales à la date actuelle, et la date de début doit être antérieure ou égale à la date de fin.");
            alert.showAndWait();
            return;
        }
        if (DD == null || DF == null || DF == DD || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début doit être antérieure et différent à la date de fin.");
            alert.showAndWait();
            return;
        }
        CongeS.Add(new Conge(0, DD, DF, TypeConge.Maladie, Statut.En_Attente, SessionManager.getInstance().getUser().getIdUser(), DOCLINK, DESC));
        NotifSubject = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Maladie;
        messageText = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Maladie + " de la part de "+ SessionManager.getInstance().getUser().getNom()+" "+ SessionManager.getInstance().getUser().getPrenom() +" du "+ DD +" au "+ DF;
        notifService.NewNotification(userService.getManagerIdByUserId(SessionManager.getInstance().getUser().getIdUser()),NotifSubject,2,messageText);
        Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText("Demande de congé créée avec succès !");
        ButtonType buttonHistorique = new ButtonType("Aller à l'historique");
        successAlert.getButtonTypes().setAll(buttonHistorique);
        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == buttonHistorique) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HistoriqueConge.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Historique congé");
                stage.show();
                StageManager.addStage("DemandeDepListe", stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void MAL_Doc_Imp(ActionEvent event) {
        String documentPath = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre certificat medicale");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier", "*.pdf", "*.docx"));
        Stage stage = (Stage) EXP_DD.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Path destinationFolder = Paths.get("src/main/resources/assets/files");
                if (!Files.exists(destinationFolder)) {
                    Files.createDirectories(destinationFolder);
                }
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destinationPath = destinationFolder.resolve(fileName);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                documentPath = destinationPath.toString();
                System.out.println("Certicat uploaded successfully: " + documentPath);
                MAL_Doc_Link.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private DatePicker SS_DD;
    @FXML
    private DatePicker SS_DF;
    @FXML
    private TextArea SS_Desc;

    @FXML
    void SS_Demander(ActionEvent event) {
        LocalDate DD = SS_DD.getValue();
        LocalDate DF = SS_DF.getValue();
        String DESC = SS_Desc.getText();
        if (SS_DD.getValue() == null || SS_DF.getValue() == null || SS_Desc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs requis non remplis");
            alert.setContentText("Veuillez remplir toutes les informations nécessaires.");
            alert.showAndWait();
            return;
        }
        if (DD.isBefore(currentDate) || DF.isBefore(currentDate) || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début et la date de fin doivent être postérieures ou égales à la date actuelle, et la date de début doit être antérieure ou égale à la date de fin.");
            alert.showAndWait();
            return;
        }
        if (DD == null || DF == null || DF == DD || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début doit être antérieure et différent à la date de fin.");
            alert.showAndWait();
            return;
        }
        CongeS.Add(new Conge(0, DD, DF, TypeConge.Sous_solde, Statut.En_Attente, SessionManager.getInstance().getUser().getIdUser(), "", DESC));
        NotifSubject = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Sous_solde;
        messageText = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Sous_solde + " de la part de "+ SessionManager.getInstance().getUser().getNom()+" "+ SessionManager.getInstance().getUser().getPrenom() +" du "+ DD +" au "+ DF;
        notifService.NewNotification(userService.getManagerIdByUserId(SessionManager.getInstance().getUser().getIdUser()),NotifSubject,2,messageText);
        Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText("Demande de congé créée avec succès !");
        ButtonType buttonHistorique = new ButtonType("Aller à l'historique");
        successAlert.getButtonTypes().setAll(buttonHistorique);
        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == buttonHistorique) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HistoriqueConge.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Historique congé");
                stage.show();
                StageManager.addStage("DemandeDepListe", stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Closing current scene...");
        }
    }

    @FXML
    private DatePicker GRO_DD;
    @FXML
    private DatePicker GRO_DF;
    @FXML
    private TextArea GRO_Desc;
    @FXML
    private TextField GRO_Doc_Link;

    @FXML
    void GRO_Demander(ActionEvent event) {
        LocalDate DD = GRO_DD.getValue();
        LocalDate DF = GRO_DF.getValue();
        String DESC = GRO_Desc.getText();
        String DOCLINK = GRO_Doc_Link.getText();
        if (GRO_DD.getValue() == null || GRO_DF.getValue() == null || GRO_Desc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs requis non remplis");
            alert.setContentText("Veuillez remplir toutes les informations nécessaires.");
            alert.showAndWait();
            return;
        }
        if (GRO_Doc_Link.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Certificat médical manquant");
            alert.setContentText("vous devez importer le certificat médical de grossesse.");
            alert.showAndWait();
            return;
        }
        if (DD.isBefore(currentDate) || DF.isBefore(currentDate) || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début et la date de fin doivent être postérieures ou égales à la date actuelle, et la date de début doit être antérieure ou égale à la date de fin.");
            alert.showAndWait();
            return;
        }
        if (DD == null || DF == null || DF == DD || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début doit être antérieure et différent à la date de fin.");
            alert.showAndWait();
            return;
        }
        CongeS.Add(new Conge(0, DD, DF, TypeConge.Maternité, Statut.En_Attente, SessionManager.getInstance().getUser().getIdUser(), DOCLINK, DESC));
        NotifSubject = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Maternité;
        messageText = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Maternité + " (Grossesse) de la part de "+ SessionManager.getInstance().getUser().getNom()+" "+ SessionManager.getInstance().getUser().getPrenom() +" du "+ DD +" au "+ DF;
        notifService.NewNotification(userService.getManagerIdByUserId(SessionManager.getInstance().getUser().getIdUser()),NotifSubject,2,messageText);
        Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText("Demande de congé créée avec succès !");
        ButtonType buttonHistorique = new ButtonType("Aller à l'historique");
        successAlert.getButtonTypes().setAll(buttonHistorique);
        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == buttonHistorique) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HistoriqueConge.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Historique congé");
                stage.show();
                StageManager.addStage("DemandeDepListe", stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Close the current scene or perform other actions
            System.out.println("Closing current scene...");
        }
    }

    @FXML
    void GRO_Doc_Imp(ActionEvent event) {
        String documentPath = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre certificat medicale de grossesse");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier", "*.pdf", "*.docx"));
        Stage stage = (Stage) EXP_DD.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Path destinationFolder = Paths.get("src/main/resources/assets/files");
                if (!Files.exists(destinationFolder)) {
                    Files.createDirectories(destinationFolder);
                }
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destinationPath = destinationFolder.resolve(fileName);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                documentPath = destinationPath.toString();
                System.out.println("Certicat uploaded successfully: " + documentPath);
                GRO_Doc_Link.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private DatePicker NAI_DD;
    @FXML
    private DatePicker NAI_DF;
    @FXML
    private TextArea NAI_Desc;
    @FXML
    private TextField NAI_Doc_Link;

    @FXML
    void NAI_Demander(ActionEvent event) {
        LocalDate DD = NAI_DD.getValue();
        LocalDate DF = NAI_DF.getValue();
        String DESC = NAI_Desc.getText();
        String DOCLINK = NAI_Doc_Link.getText();
        if (NAI_DD.getValue() == null || NAI_DF.getValue() == null || NAI_Desc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs requis non remplis");
            alert.setContentText("Veuillez remplir toutes les informations nécessaires.");
            alert.showAndWait();
            return;
        }
        if (NAI_Doc_Link.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Certificat médical manquant");
            alert.setContentText("vous devez importer le certificat médical de naissance.");
            alert.showAndWait();
            return;
        }
        if (DD.isBefore(currentDate) || DF.isBefore(currentDate) || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début et la date de fin doivent être postérieures ou égales à la date actuelle, et la date de début doit être antérieure ou égale à la date de fin.");
            alert.showAndWait();
            return;
        }
        if (DD == null || DF == null || DF == DD || DD.isAfter(DF)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates invalides");
            alert.setContentText("La date de début doit être antérieure et différent à la date de fin.");
            alert.showAndWait();
            return;
        }
        CongeS.Add(new Conge(0, DD, DF, TypeConge.Maternité, Statut.En_Attente, SessionManager.getInstance().getUser().getIdUser(), DOCLINK, DESC));
        NotifSubject = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Maternité;
        messageText = "vous avez reçu une nouvelle demande de congé "+ TypeConge.Maternité +" (Naissance) de la part de "+ SessionManager.getInstance().getUser().getNom()+" "+ SessionManager.getInstance().getUser().getPrenom() +" du "+ DD +" au "+ DF;
        notifService.NewNotification(userService.getManagerIdByUserId(SessionManager.getInstance().getUser().getIdUser()),NotifSubject,2,messageText);
        Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText("Demande de congé créée avec succès !");
        ButtonType buttonHistorique = new ButtonType("Aller à l'historique");
        successAlert.getButtonTypes().setAll(buttonHistorique);
        Optional<ButtonType> result = successAlert.showAndWait();
        if (result.isPresent() && result.get() == buttonHistorique) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HistoriqueConge.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Historique congé");
                stage.show();
                StageManager.addStage("DemandeDepListe", stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void NAI_Doc_Imp(ActionEvent event) {
        String documentPath = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre certificat medicale de naissance");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier", "*.pdf", "*.docx"));
        Stage stage = (Stage) EXP_DD.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Path destinationFolder = Paths.get("src/main/resources/assets/files");
                if (!Files.exists(destinationFolder)) {
                    Files.createDirectories(destinationFolder);
                }
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destinationPath = destinationFolder.resolve(fileName);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                documentPath = destinationPath.toString();
                System.out.println("Certicat uploaded successfully: " + documentPath);
                EXP_Doc_Link.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
