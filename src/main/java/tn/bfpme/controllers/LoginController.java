package tn.bfpme.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;

import java.io.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import tn.bfpme.models.User;
import tn.bfpme.utils.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private AnchorPane MainAnchorPane;
    @FXML
    private TextField LoginEmail;
    @FXML
    private PasswordField LoginMDP;
    @FXML
    private TextField showPasswordField;
    @FXML
    private Button toggleButton;
    @FXML
    private ImageView toggleIcon;
    @FXML
    private ImageView imageView;
    @FXML
    private Connection cnx;

    private Image showPasswordImage;
    private Image hidePasswordImage;
    private VideoCapture capture;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the database connection
        cnx = MyDataBase.getInstance().getCnx();  // Ensure this line is called

        // Rest of your initialization code
        String style = "-fx-background-color: transparent; -fx-border-color: transparent transparent #eab53f transparent; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 3 0;";
        LoginEmail.setStyle(style);
        LoginMDP.setStyle(style);
        Platform.runLater(() -> {
            Stage stage = (Stage) MainAnchorPane.getScene().getWindow();
            stage.widthProperty().addListener((obs, oldVal, newVal) -> FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight()));
            stage.heightProperty().addListener((obs, oldVal, newVal) -> FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight()));
            FontResizer.resizeFonts(MainAnchorPane, stage.getWidth(), stage.getHeight());
        });
        // Load the images
        showPasswordImage = new Image(getClass().getResourceAsStream("/assets/imgs/hide.png"));
        hidePasswordImage = new Image(getClass().getResourceAsStream("/assets/imgs/show.png"));
        toggleIcon.setImage(showPasswordImage);

        // Toggle visibility of password fields
        toggleButton.setOnAction(event -> {
            if (showPasswordField.isVisible()) {
                LoginMDP.setText(showPasswordField.getText());
                LoginMDP.setVisible(true);
                LoginMDP.setManaged(true);
                showPasswordField.setVisible(false);
                showPasswordField.setManaged(false);
                toggleIcon.setImage(showPasswordImage);
            } else {
                showPasswordField.setText(LoginMDP.getText());
                showPasswordField.setVisible(true);
                showPasswordField.setManaged(true);
                LoginMDP.setVisible(false);
                LoginMDP.setManaged(false);
                toggleIcon.setImage(hidePasswordImage);
            }
        });
    }


    private void togglePasswordVisibility() {
        if (showPasswordField.isVisible()) {
            LoginMDP.setText(showPasswordField.getText());
            LoginMDP.setVisible(true);
            LoginMDP.setManaged(true);
            showPasswordField.setVisible(false);
            showPasswordField.setManaged(false);
            toggleIcon.setImage(showPasswordImage);
        } else {
            showPasswordField.setText(LoginMDP.getText());
            showPasswordField.setVisible(true);
            showPasswordField.setManaged(true);
            LoginMDP.setVisible(false);
            LoginMDP.setManaged(false);
            toggleIcon.setImage(hidePasswordImage);
        }
    }

    @FXML
    void Login(ActionEvent event) {
        cnx = MyDataBase.getInstance().getCnx();
        String qry = "SELECT u.*, ur.ID_Role " +
                "FROM `user` as u " +
                "JOIN `user_role` ur ON ur.ID_User = u.ID_User " +
                "WHERE u.`Email`=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setString(1, LoginEmail.getText());
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                String storedEncryptedPassword = rs.getString("MDP");
                String decryptedPassword = EncryptionUtil.decrypt(storedEncryptedPassword);
                if (decryptedPassword.equals(LoginMDP.getText())) {
                    User connectedUser = new User(
                            rs.getInt("ID_User"),
                            rs.getString("Nom"),
                            rs.getString("Prenom"),
                            rs.getString("Email"),
                            storedEncryptedPassword,
                            rs.getString("Image"),
                            rs.getInt("ID_Manager"),
                            rs.getInt("ID_Departement"),
                            rs.getInt("ID_Role")
                    );
                    connectedUser.setIdRole(rs.getInt("ID_Role"));
                    populateUserSolde(connectedUser);

                    // Initialize SessionManager
                    System.out.println("User before SessionManager initialization: " + connectedUser);
                    SessionManager.getInstance(connectedUser);
                    System.out.println("User after SessionManager initialization: " + SessionManager.getInstance().getUser());


                    navigateToProfile(event);
                } else {
                    System.out.println("Login failed: Invalid email or password.");
                }
            } else {
                System.out.println("Login failed: Invalid email or password.");
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void FacialRecognitionButton(ActionEvent event) {
        //executeNotebook();
        startFacialRecognition(event);
    }


    private Boolean runPythonFaceRecognitionScript(String capturedImagePath, List<String> storedImagePaths) {
        try {
            List<String> command = new ArrayList<>();
            command.add("C:\\Users\\slimb\\AppData\\Local\\Programs\\Python\\Python312\\python.exe");
            command.add("C:\\Users\\slimb\\OneDrive\\Bureau\\NewProjectGestionconge\\src\\main\\resources\\assets\\Adapted_Face_Recognition.py");
            command.add(capturedImagePath);
            command.addAll(storedImagePaths);

            System.out.println("Executing command: " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);

            Map<String, String> env = pb.environment();
            /*env.put("PATH", "C:\\Users\\slimb\\AppData\\Local\\Programs\\Python\\Python312\\");
            env.put("PYTHONPATH", "C:\\Users\\slimb\\AppData\\Local\\Programs\\Python\\Python312\\Lib\\site-packages");

            // Optional: Set the working directory if needed
            pb.directory(new File("C:\\Users\\slimb\\OneDrive\\Bureau\\NewProjectGestionconge\\src\\main\\resources\\assets"));
            */
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println("Python output: " + line);
            }

            int exitCode = process.waitFor();
            System.out.println("Python script exited with code: " + exitCode);

            if (exitCode != 0) {
                System.err.println("Python script failed to execute properly. Exit code: " + exitCode);
                System.err.println("Full output:\n" + output.toString());
                return false;
            }

            String result = output.toString().trim();
            System.out.println("Python script output: " + result);

            return result.equalsIgnoreCase("True");

        } catch (IOException e) {
            System.err.println("IOException while running Python script: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            System.err.println("InterruptedException while waiting for Python script to finish: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private void startFacialRecognition(ActionEvent event) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                System.out.println("Starting facial recognition task...");
                List<String> storedImagePaths = fetchStoredImagePathsFromDatabase(SessionManager.getInstance().getUser().getIdUser());

                // Ensure that at least one image path is retrieved
                if (storedImagePaths.isEmpty()) {
                    System.err.println("No stored images found for the user.");
                    return false;
                }

                String capturedImagePath = storedImagePaths.get(0);
                System.out.println("Captured image path set to: " + capturedImagePath);

                Boolean result = runPythonFaceRecognitionScript(capturedImagePath, storedImagePaths);
                System.out.println("Facial recognition result: " + result);
                return result;
            }

            @Override
            protected void succeeded() {
                System.out.println("Facial recognition succeeded.");
                if (getValue()) {
                    showAlert("Face recognized", "Welcome back!");
                } else {
                    showAlert("Face not recognized", "Please try again.");
                }
            }

            @Override
            protected void failed() {
                System.err.println("Facial recognition task failed.");
                showAlert("Error", "An error occurred while running the face recognition script.");
            }
        };

        new Thread(task).start();
    }


    private List<String> fetchStoredImagePathsFromDatabase(int userId) {
        List<String> imagePaths = new ArrayList<>();
        String query = "SELECT face_data1, face_data2, face_data3, face_data4 FROM user WHERE ID_User = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Retrieved stored image paths from database.");
                for (int i = 1; i <= 4; i++) {
                    String faceDataPath = rs.getString("face_data" + i);
                    if (faceDataPath != null && !faceDataPath.isEmpty()) {
                        imagePaths.add(faceDataPath);
                    }
                }
            } else {
                System.out.println("No stored image paths found for user ID: " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching stored image paths: " + e.getMessage());
        }

        return imagePaths;
    }
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    private void navigateToProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Profile");
        stage.show();
    }


    private void populateUserSolde(User user) {
        String soldeQuery = "SELECT us.*, tc.Designation FROM user_solde us JOIN typeconge tc ON us.ID_TypeConge = tc.ID_TypeConge WHERE us.ID_User = ?";
        try (PreparedStatement soldeStm = cnx.prepareStatement(soldeQuery)) {
            soldeStm.setInt(1, user.getIdUser());
            ResultSet soldeRs = soldeStm.executeQuery();
            while (soldeRs.next()) {
                int typeCongeId = soldeRs.getInt("ID_TypeConge");
                double totalSolde = soldeRs.getDouble("TotalSolde");
                String typeConge = soldeRs.getString("Designation");
                user.setSoldeByType(typeCongeId, totalSolde, typeConge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
