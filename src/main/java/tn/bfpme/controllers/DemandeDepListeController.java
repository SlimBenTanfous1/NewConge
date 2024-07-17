package tn.bfpme.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.control.ContextMenu;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.bfpme.models.*;
import tn.bfpme.services.ServiceConge;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.SessionManager;
import tn.bfpme.utils.StageManager;

public class DemandeDepListeController implements Initializable {
    @FXML
    private GridPane DemandesContainer;
    @FXML
    private TextField Recherche_demande;
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private ComboBox<String> comboTri;
    @FXML
    public Button NotifBtn;
    private Conge conge;
    private final ServiceConge CongeS = new ServiceConge();
    private final ServiceUtilisateur UserS = new ServiceUtilisateur();
    ObservableList<String> TriListe = FXCollections.observableArrayList("Type", "Nom", "Prenom", "Date Debut", "Date Fin");


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        load();
        comboTri.setValue("Selectioner");
        comboTri.setItems(TriListe);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            Pane departementPane = loader.load();
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Recherche(KeyEvent event) {
    }

    @FXML
    void TriPar(ActionEvent event) {
        String TYPE = comboTri.getValue();
        if (TYPE != null) {
            switch (TYPE) {
                case "Type":
                    triGenerique(UserS.TriType());
                    break;
                case "Nom":
                    triGenerique(UserS.TriNom());
                    break;
                case "Prenom":
                    triGenerique(UserS.TriPrenom());
                    break;
                case "Date Debut":
                    triGenerique(UserS.TriDateDebut());
                    break;
                case "Date Fin":
                    triGenerique(UserS.TriDateFin());
                    break;
                default:
                    break;
            }
        }
    }

    private void triGenerique(UserConge userConge) {
        int row = 0;
        int column = 0;
        try {
            DemandesContainer.getChildren().clear();
            List<User> users = userConge.getUsers();
            List<Conge> conges = userConge.getConges();
            for (Conge conge : conges) {
                for (User user : users) {
                    if (conge.getIdUser() == user.getIdUser()) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("/UserCarte.fxml"));
                        try {
                            Pane cardBox = fxmlLoader.load();
                            UserCarteController cardu = fxmlLoader.getController();
                            cardu.setData(conge, user);
                            DemandesContainer.add(cardBox, column, row);
                            GridPane.setMargin(cardBox, new Insets(10));
                            cardBox.setMaxWidth(Double.MAX_VALUE);
                            column++;
                            if (column == 1) {
                                column = 0;
                                row++;
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading UserCarte.fxml: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        DemandesContainer.getColumnConstraints().clear();
        for (int i = 0; i < 3; i++) { // Three columns
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.ALWAYS);
            DemandesContainer.getColumnConstraints().add(columnConstraints);
        }
        DemandesContainer.setVgap(8);
        DemandesContainer.setHgap(10);
        DemandesContainer.setPadding(new Insets(8));
        int row = 0;
        int column = 0;
        try {
            UserConge userConge = UserS.afficherusers();
            List<User> users = userConge.getUsers();
            List<Conge> conges = userConge.getConges();
            for (Conge conge : conges) {
                for (User user : users) {
                    if (conge.getIdUser() == user.getIdUser()) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("/UserCarte.fxml"));
                        try {
                            Pane cardBox = fxmlLoader.load();
                            UserCarteController cardu = fxmlLoader.getController();
                            cardu.setData(conge, user);
                            DemandesContainer.add(cardBox, column, row);
                            GridPane.setMargin(cardBox, new Insets(10));
                            cardBox.setMaxWidth(Double.MAX_VALUE);
                            column++;
                            if (column == 1) {
                                column = 0;
                                row++;
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading UserCarte.fxml: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in load method: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void LesApprouves(ActionEvent event) {
        int row = 0;
        int column = 0;
        try {
            DemandesContainer.getChildren().clear();
            UserConge userConge = UserS.AfficherApprove();
            List<User> users = userConge.getUsers();
            List<Conge> conges = userConge.getConges();
            for (Conge conge : conges) {
                for (User user : users) {
                    if (conge.getIdUser() == user.getIdUser()) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("/UserCarte.fxml"));
                        try {
                            Pane cardBox = fxmlLoader.load();
                            UserCarteController cardu = fxmlLoader.getController();
                            cardu.setData(conge, user);
                            DemandesContainer.add(cardBox, column, row);
                            GridPane.setMargin(cardBox, new Insets(10));
                            cardBox.setMaxWidth(Double.MAX_VALUE);
                            column++;
                            if (column == 1) {
                                column = 0;
                                row++;
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading UserCarte.fxml: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void LesEnAttente(ActionEvent event) {
        int row = 0;
        int column = 0;
        try {
            DemandesContainer.getChildren().clear();
            UserConge userConge = UserS.afficherusers();
            List<User> users = userConge.getUsers();
            List<Conge> conges = userConge.getConges();
            for (Conge conge : conges) {
                for (User user : users) {
                    if (conge.getIdUser() == user.getIdUser()) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("/UserCarte.fxml"));
                        try {
                            Pane cardBox = fxmlLoader.load();
                            UserCarteController cardu = fxmlLoader.getController();
                            cardu.setData(conge, user);
                            DemandesContainer.add(cardBox, column, row);
                            GridPane.setMargin(cardBox, new Insets(10));
                            cardBox.setMaxWidth(Double.MAX_VALUE);
                            column++;
                            if (column == 1) {
                                column = 0;
                                row++;
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading UserCarte.fxml: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void LesRejetes(ActionEvent event) {
        int row = 0;
        int column = 0;
        try {
            DemandesContainer.getChildren().clear();
            UserConge userConge = UserS.AfficherReject();
            List<User> users = userConge.getUsers();
            List<Conge> conges = userConge.getConges();
            for (Conge conge : conges) {
                for (User user : users) {
                    if (conge.getIdUser() == user.getIdUser()) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("/UserCarte.fxml"));
                        try {
                            Pane cardBox = fxmlLoader.load();
                            UserCarteController cardu = fxmlLoader.getController();
                            cardu.setData(conge, user);
                            DemandesContainer.add(cardBox, column, row);
                            GridPane.setMargin(cardBox, new Insets(10));
                            cardBox.setMaxWidth(Double.MAX_VALUE);
                            column++;
                            if (column == 1) {
                                column = 0;
                                row++;
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading UserCarte.fxml: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
