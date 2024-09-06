package tn.bfpme.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.bfpme.utils.FontResizer;
import tn.bfpme.utils.MyDataBase;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class StatController implements Initializable {
    @FXML
    private AnchorPane MainAnchorPane;

    @FXML
    private PieChart PieType;
    @FXML
    private PieChart PieDepart;
    @FXML
    private BarChart<String, Number> BarDepart;


    @FXML
    private LineChart<String, Number> LineDepart;



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NavigationHeader.fxml"));
            AnchorPane departementPane = loader.load();
            AnchorPane.setTopAnchor(departementPane, 0.0);
            AnchorPane.setLeftAnchor(departementPane, 0.0);
            AnchorPane.setRightAnchor(departementPane, 0.0);
            MainAnchorPane.getChildren().add(departementPane);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        /*Platform.runLater(() -> {
            Stage stage = (Stage) MainAnchorPane.getScene().getWindow();
            stage.widthProperty().addListener((obs, oldVal, newVal) -> FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight()));
            stage.heightProperty().addListener((obs, oldVal, newVal) -> FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight()));
            FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight());
        });*/
        loadPieChartData();
        loadPieDepartChartData();
        loadBarDepartData();
        loadLineDepartData();
    }
    private void loadPieChartData() {
        try (Connection cnx = MyDataBase.getInstance().getCnx()) {
            String query = "SELECT tc.Designation, COUNT(c.ID_Conge) AS ApprovedCount " +
                    "FROM conge c " +
                    "JOIN typeconge tc ON c.TypeConge = tc.ID_TypeConge " +
                    "WHERE c.Statut = 'Approuvé' " +
                    "AND YEAR(c.DateDebut) = YEAR(CURDATE()) " +  // Filter for current year
                    "GROUP BY tc.Designation";

            PreparedStatement preparedStatement = cnx.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            while (resultSet.next()) {
                String leaveType = resultSet.getString("Designation");
                int approvedCount = resultSet.getInt("ApprovedCount");

                // Add data to the PieChart
                pieChartData.add(new PieChart.Data(leaveType, approvedCount));
            }

            PieType.setData(pieChartData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPieDepartChartData() {
        try (Connection cnx = MyDataBase.getInstance().getCnx()) {
            String query = "SELECT d.nom AS DepartmentName, COUNT(c.ID_Conge) AS ApprovedCount " +
                    "FROM conge c " +
                    "JOIN user u ON c.ID_User = u.ID_User " +
                    "JOIN departement d ON u.ID_Departement = d.ID_Departement " +
                    "WHERE c.Statut = 'Approuvé' " +
                    "AND YEAR(c.DateDebut) = YEAR(CURDATE()) " +  // Filter for current year
                    "GROUP BY d.nom";

            PreparedStatement preparedStatement = cnx.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            while (resultSet.next()) {
                String departmentName = resultSet.getString("DepartmentName");
                int approvedCount = resultSet.getInt("ApprovedCount");

                // Add data to the PieChart
                pieChartData.add(new PieChart.Data(departmentName, approvedCount));
            }

            PieDepart.setData(pieChartData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBarDepartData() {
        try (Connection cnx = MyDataBase.getInstance().getCnx()) {
            // Query to sum the TotalSolde for departments that have at least one user
            String query = "SELECT d.nom AS DepartmentName, IFNULL(SUM(us.TotalSolde), 0) AS TotalUnusedLeaves " +
                    "FROM departement d " +
                    "JOIN user u ON d.ID_Departement = u.ID_Departement " +  // Only include departments with users
                    "LEFT JOIN user_solde us ON u.ID_User = us.ID_User " +
                    "GROUP BY d.nom";

            PreparedStatement preparedStatement = cnx.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Solde des Congés");

            while (resultSet.next()) {
                String departmentName = resultSet.getString("DepartmentName");
                double totalUnusedLeaves = resultSet.getDouble("TotalUnusedLeaves");

                // Add data to the BarChart
                series.getData().add(new XYChart.Data<>(departmentName, totalUnusedLeaves));
            }

            BarDepart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLineDepartData() {
        try (Connection cnx = MyDataBase.getInstance().getCnx()) {
            // Query to get the total leave taken per month for all employees in the current year
            String query = "SELECT MONTH(c.DateDebut) AS Month, COUNT(c.ID_Conge) AS LeavesTaken " +
                    "FROM conge c " +
                    "WHERE c.Statut = 'Approuvé' " +  // Only count approved leaves
                    "AND YEAR(c.DateDebut) = YEAR(CURDATE()) " +  // Filter for current year
                    "GROUP BY MONTH(c.DateDebut) " +
                    "ORDER BY Month";

            PreparedStatement preparedStatement = cnx.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Create a new series for the total leave taken each month
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Congés Prises");

            while (resultSet.next()) {
                int month = resultSet.getInt("Month");
                int leavesTaken = resultSet.getInt("LeavesTaken");

                // Add data to the LineChart
                series.getData().add(new XYChart.Data<>(getMonthName(month), leavesTaken));
            }

            LineDepart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getMonthName(int month) {
        String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return months[month - 1];
    }


}

