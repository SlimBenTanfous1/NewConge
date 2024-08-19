package tn.bfpme.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import tn.bfpme.models.User;
import tn.bfpme.utils.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        String qry = "SELECT u.*, ur.ID_Role FROM `user` as u JOIN `user_role` ur ON ur.ID_User = u.ID_User WHERE u.`Email`=?";
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
                    SessionManager.getInstance(connectedUser);
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
            throw new RuntimeException(e);
        }
    }

    @FXML
    void FacialRecognitionButton(ActionEvent event) {
        String faceCascadePath = getClass().getResource("/assets/FacialRegDATA/XML/haarcascades/haarcascade_frontalface_alt.xml").getPath();

        // Remove leading '/' on Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            faceCascadePath = faceCascadePath.substring(1);
        }

        CascadeClassifier faceDetector = new CascadeClassifier(faceCascadePath);
        if (faceDetector.empty()) {
            System.err.println("Failed to load haarcascade_frontalface_alt.xml");
            return;
        }

        VideoCapture capture = new VideoCapture(0, Videoio.CAP_DSHOW); // Open the default camera
        if (!capture.isOpened()) {
            System.out.println("Error: Cannot open the camera.");
            return;
        }

        Task<Void> faceRecognitionTask = new Task<Void>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                while (capture.isOpened()) {
                    if (capture.read(frame)) {
                        MatOfRect faceDetections = new MatOfRect();
                        faceDetector.detectMultiScale(frame, faceDetections);

                        for (Rect rect : faceDetections.toArray()) {
                            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                        }

                        // Convert Mat to Image and display in ImageView
                        Image imageToShow = FacialRec.mat2Image(frame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));

                        if (!faceDetections.empty()) {
                            System.out.println("Attempting face recognition..."); // Debugging line
                            boolean recognized = recognizeFaceFromFrame(frame);
                            System.out.println("test1");
                            if (recognized) {
                                Platform.runLater(() -> {
                                    System.out.println("Face recognized successfully.");
                                    handleSuccessfulRecognition(event);
                                    capture.release(); // Stop the camera after recognition
                                });
                                break; // Exit the loop if face is recognized
                            } else {
                                System.out.println("Face not recognized."); // Debugging line
                            }
                        }

                        try {
                            Thread.sleep(33); // ~30 frames per second
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                capture.release(); // Release the camera after processing
                return null;
            }
        };

        Thread faceRecognitionThread = new Thread(faceRecognitionTask);
        faceRecognitionThread.setDaemon(true);
        faceRecognitionThread.start();
    }

    private boolean recognizeFaceFromFrame(Mat capturedFrame) {
        try {
            System.out.println("Converting captured frame to grayscale...");
            Mat grayCapturedImage = new Mat();
            Imgproc.cvtColor(capturedFrame, grayCapturedImage, Imgproc.COLOR_BGR2GRAY);

            // Fetch stored images from the database
            List<Mat> storedFaces = fetchStoredFacesFromDatabase();

            if (storedFaces.isEmpty()) {
                System.err.println("No stored face images found in the database.");
                return false;
            }

            for (Mat storedFace : storedFaces) {
                if (storedFace.empty()) {
                    System.err.println("Failed to load a stored face image.");
                    continue;
                }

                System.out.println("Converting stored face to grayscale...");
                Mat grayStoredFace = new Mat();
                Imgproc.cvtColor(storedFace, grayStoredFace, Imgproc.COLOR_BGR2GRAY);
                Imgproc.resize(grayCapturedImage, grayCapturedImage, grayStoredFace.size());

                System.out.println("Creating face recognizer...");
                FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
                System.out.println("Face recognizer created successfully.");

                List<Mat> images = new ArrayList<>();
                List<Integer> labels = new ArrayList<>();
                images.add(grayStoredFace);
                labels.add(1); // Label for the stored face
                faceRecognizer.train(images, new MatOfInt(1));

                int[] label = new int[1];
                double[] confidence = new double[1];
                System.out.println("Starting face recognition prediction...");
                faceRecognizer.predict(grayCapturedImage, label, confidence);

                System.out.println("Prediction complete. Label: " + label[0] + ", Confidence: " + confidence[0]);

                if (label[0] == 1 && confidence[0] < 50.0) { // Adjust confidence threshold as needed
                    System.out.println("Face matched.");
                    return true;
                } else {
                    System.out.println("Face not recognized.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception during face recognition: " + e.getMessage());
        }

        return false;
    }

    private List<Mat> fetchStoredFacesFromDatabase() {
        List<Mat> storedFaces = new ArrayList<>();

        String query = "SELECT face_data1, face_data2, face_data3, face_data4 FROM user WHERE Email=?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, LoginEmail.getText());  // Assuming the email is used to find the user
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                for (int i = 1; i <= 4; i++) {
                    byte[] faceData = rs.getBytes("face_data" + i);
                    if (faceData != null && faceData.length > 0) {
                        Mat storedFace = Imgcodecs.imdecode(new MatOfByte(faceData), Imgcodecs.IMREAD_COLOR);
                        storedFaces.add(storedFace);
                        
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching stored faces from database: " + e.getMessage());
        }

        return storedFaces;
    }



    private void handleSuccessfulRecognition(ActionEvent event) {
        try {
            navigateToProfile(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void navigateToProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Profile");
        stage.show();
        stage.toFront();
        StageManager.addStage("Profile", stage);
    }
}
