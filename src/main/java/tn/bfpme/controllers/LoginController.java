package tn.bfpme.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import tn.bfpme.models.User;
import tn.bfpme.utils.EncryptionUtil;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

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

    private Connection cnx;

    @FXML
    public void initialize() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @FXML
    void Login(ActionEvent event) {
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
                            rs.getInt("ID_Role"),
                            rs.getString("face_data1"),
                            rs.getString("face_data2"),
                            rs.getString("face_data3"),
                            rs.getString("face_data4")
                    );
                    connectedUser.setIdRole(rs.getInt("ID_Role"));
                    populateUserSolde(connectedUser);

                    // Initialize SessionManager with the connected user
                    SessionManager.getInstance(connectedUser);

                    // Navigate to profile after successful login
                    navigateToProfile(event);
                } else {
                    showAlert("Login failed", "Invalid email or password.");
                }
            } else {
                showAlert("Login failed", "Invalid email or password.");
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void FacialRecognitionButton(ActionEvent event) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                System.out.println("Starting facial recognition task...");

                // SQL query to fetch user data
                String qry = "SELECT u.*, ur.ID_Role " +
                        "FROM `user` as u " +
                        "JOIN `user_role` ur ON ur.ID_User = u.ID_User " +
                        "WHERE u.`Email`=?";
                try {
                    PreparedStatement stm = cnx.prepareStatement(qry);
                    stm.setString(1, LoginEmail.getText());  // Assuming email is used for matching
                    ResultSet rs = stm.executeQuery();

                    if (rs.next()) {
                        System.out.println("User found in database.");

                        // Initialize the user based on the ResultSet
                        User connectedUser = new User(
                                rs.getInt("ID_User"),
                                rs.getString("Nom"),
                                rs.getString("Prenom"),
                                rs.getString("Email"),
                                rs.getString("MDP"),
                                rs.getString("Image"),
                                rs.getInt("ID_Manager"),
                                rs.getInt("ID_Departement"),
                                rs.getInt("ID_Role"),   // Check if this column exists
                                rs.getString("face_data1"),
                                rs.getString("face_data2"),
                                rs.getString("face_data3"),
                                rs.getString("face_data4")
                        );
                        connectedUser.setIdRole(rs.getInt("ID_Role"));

                        System.out.println("User object created successfully.");

                        // Verify face recognition
                        boolean faceRecognized = performFacialRecognition(connectedUser);
                        System.out.println("Facial recognition result: " + faceRecognized);

                        if (faceRecognized) {
                            // If face is recognized, set up the session and log the user in
                            SessionManager.getInstance(connectedUser);
                            Platform.runLater(() -> {
                                try {
                                    navigateToProfile(event);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            return true;
                        } else {
                            System.err.println("Face not recognized.");
                            return false;
                        }
                    } else {
                        System.err.println("No matching user found for the provided email.");
                        return false;
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    showAlert("Success", "Face recognized and logged in successfully.");
                } else {
                    showAlert("Failure", "Face not recognized.");
                }
            }

            @Override
            protected void failed() {
                System.err.println("Facial recognition task failed.");
                showAlert("Error", "An error occurred during facial recognition.");
            }
        };

        new Thread(task).start();
    }

    private boolean performFacialRecognition(User user) {
        try {
            // Mock recognition logic for now
            System.out.println("Performing facial recognition...");

            // Assume comparison of face_data1 with live image data
            String faceData1 = user.getFace_data1();
            if (faceData1 != null && !faceData1.isEmpty()) {
                // Mock success for testing
                return true;
            }

            // Return false if no valid face data is found
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /*private boolean authenticateViaFacialRecognition() throws SQLException {
        String email = LoginEmail.getText();
        String qry = "SELECT * FROM `user` WHERE `Email`=?";
        PreparedStatement stm = cnx.prepareStatement(qry);
        stm.setString(1, email);
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            User user = initializeUserFromResultSet(rs);

            // Capture the live feed image and compare it with the stored images
            if (performFacialRecognition(user)) {
                SessionManager.getInstance(user); // Initialize session if face is recognized
                return true;
            }
        }
        return false;
    }*/

    /*private User initializeUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(

                rs.getInt("ID_User"),
                rs.getString("Nom"),
                rs.getString("Prenom"),
                rs.getString("Email"),
                rs.getString("MDP"),
                rs.getString("Image"),
                rs.getInt("ID_Manager"),
                rs.getInt("ID_Departement"),
                rs.getInt("ID_Role"),
                rs.getString("face_data1"),
                rs.getString("face_data2"),
                rs.getString("face_data3"),
                rs.getString("face_data4")
        );
    }*/


    private List<Mat> loadStoredImages(User user) {
        List<Mat> images = new ArrayList<>();
        String[] faceDataPaths = {user.getFace_data1(), user.getFace_data2(), user.getFace_data3(), user.getFace_data4()};
        for (String path : faceDataPaths) {
            if (path != null && !path.isEmpty()) {
                Mat image = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
                images.add(image);
            }
        }
        return images;
    }

    private Mat captureImageFromCamera() {
        VideoCapture camera = new VideoCapture(0);
        Mat frame = new Mat();
        if (camera.isOpened()) {
            camera.read(frame);
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY); // Convert to grayscale
        }
        camera.release();
        return frame;
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
