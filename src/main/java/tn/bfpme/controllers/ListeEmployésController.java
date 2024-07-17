package tn.bfpme.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import tn.bfpme.models.User;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.MyDataBase;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;


public class ListeEmployésController implements Initializable {

    @FXML
    private AnchorPane MainAnchorPane;

    @FXML
    private TextField Recherche_conge;

    @FXML
    private GridPane UserContainer;

    ServiceUtilisateur UserS = new ServiceUtilisateur();
    Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        load();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            Pane departementPane = loader.load();
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(List<User> users) {
        UserContainer.getChildren().clear();
        int column = 0;
        int row = 0;
        try {
            for (User user : users) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/UserCard.fxml"));
                Pane userBox = fxmlLoader.load();
                UserCardController cardC = fxmlLoader.getController();
                cardC.HBoxBtns.setVisible(false);
                cardC.setDataUser(user);
                if (column == 3) {
                    column = 0;
                    ++row;
                }
                UserContainer.add(userBox, column++, row);
                GridPane.setMargin(userBox, new Insets(12));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void load() {
        List<User> users = UserS.ShowUnder();
        load(users);
    }

    @FXML
    void Recherche(KeyEvent event) {
        UserContainer.getChildren().clear();
        String recherche = Recherche_conge.getText();
        int column = 0;
        int row = 0;
        try {
            for (User user : UserS.RechercheUnder(recherche)) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/UserCard.fxml"));
                Pane userBox = fxmlLoader.load();
                UserCardController cardC = fxmlLoader.getController();
                cardC.HBoxBtns.setVisible(false);
                cardC.setDataUser(user);
                if (column == 3) {
                    column = 0;
                    ++row;
                }
                UserContainer.add(userBox, column++, row);
                GridPane.setMargin(userBox, new Insets(12));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



