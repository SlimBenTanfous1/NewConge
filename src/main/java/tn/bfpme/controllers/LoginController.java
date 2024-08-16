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
import org.mindrot.jbcrypt.BCrypt;
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

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
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
    private Timer timer;
    private boolean cameraActive = false;
    private volatile boolean stopCamera = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    @FXML
    void Login(ActionEvent event) {
        cnx = MyDataBase.getInstance().getCnx();
        String qry = "SELECT u.*, ur.ID_Role " +
                "FROM `user` as u " +
                "JOIN `user_role` ur ON ur.ID_User = u.ID_User " +
                "WHERE u.`Email`=?";
        System.out.println("essaie connection");
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
            e.printStackTrace();
        }
    }

    @FXML
    void FacialRecognitionButton(ActionEvent event) throws UnsupportedEncodingException {
        String faceCascadePath = getClass().getResource("/assets/FacialRegDATA/XML/haarcascades/haarcascade_frontalface_alt.xml").getPath();
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

                        // Save the captured frame
                        String capturedImagePath = "src/main/resources/assets/FacialRegDATA/Captured/captured_frame.jpg";
                        Imgcodecs.imwrite(capturedImagePath, frame);

                        // Perform face recognition
                        boolean recognized = recognizeFace(capturedImagePath);
                        if (recognized) {
                            Platform.runLater(() -> {
                                System.out.println("Face recognized successfully.");
                                // Continue with login process
                                handleSuccessfulRecognition(event);
                            });
                            break; // Exit the loop if face is recognized
                        } else {
                            Platform.runLater(() -> System.out.println("Face not recognized."));
                        }

                        try {
                            Thread.sleep(1000); // Add delay to simulate real-time processing
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
    private void handleSuccessfulRecognition(ActionEvent event) {
        // This method is called when the face is successfully recognized
        // You can either automatically log the user in or ask them to proceed with a button click

        // Navigate to the user's profile or main dashboard
        try {
            navigateToProfile(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean recognizeFace(String capturedImagePath) {
        Mat capturedImage = Imgcodecs.imread(capturedImagePath);
        File directory = new File("src/main/resources/assets/users"); // Update to your desired path
        File[] storedFaceFiles = directory.listFiles((dir, name) -> name.endsWith(".jpg"));

        if (storedFaceFiles == null || storedFaceFiles.length == 0) {
            System.err.println("No stored face images found.");
            return false;
        }

        for (File storedFaceFile : storedFaceFiles) {
            Mat storedFace = Imgcodecs.imread(storedFaceFile.getAbsolutePath());
            if (storedFace.empty()) {
                System.err.println("Failed to load stored face image: " + storedFaceFile.getName());
                continue;
            }

            if (compareFaces(capturedImage, storedFace)) {
                System.out.println("Face matched with: " + storedFaceFile.getName());
                return true;
            }
        }
        return false;
    }


    private boolean compareFaces(Mat capturedImage, Mat storedFace) {
        Mat grayCapturedImage = new Mat();
        Mat grayStoredFace = new Mat();
        Imgproc.cvtColor(capturedImage, grayCapturedImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(storedFace, grayStoredFace, Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(grayCapturedImage, grayCapturedImage, grayStoredFace.size());
        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
        List<Mat> images = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        images.add(grayStoredFace);
        labels.add(1); // Label for the stored face
        faceRecognizer.train(images, new MatOfInt(1));
        int[] label = new int[1];
        double[] confidence = new double[1];
        faceRecognizer.predict(grayCapturedImage, label, confidence);
        return label[0] == 1 && confidence[0] < 50.0; // Adjust confidence threshold as needed
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
        /*if (!this.cameraActive) {
            this.capture = new VideoCapture(0, Videoio.CAP_DSHOW);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                String faceCascadePath = "src/main/resources/assets/FacialRegDATA/XML/haarcascades/haarcascade_frontalface_alt.xml";
                CascadeClassifier faceDetector = new CascadeClassifier(faceCascadePath);
                if (faceDetector.empty()) {
                    System.err.println("Failed to load haarcascade_frontalface_alt.xml");
                    return;
                }
                TimerTask frameGrabber = new TimerTask() {
                    @Override
                    public void run() {
                        Mat frame = new Mat();
                        if (capture.read(frame)) {
                            MatOfRect faceDetections = new MatOfRect();
                            faceDetector.detectMultiScale(frame, faceDetections);
                            for (Rect rect : faceDetections.toArray()) {
                                Imgproc.rectangle(frame, new org.opencv.core.Point(rect.x, rect.y), new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                            }
                            Image imageToShow = FacialRec.mat2Image(frame);
                            Platform.runLater(() -> {
                                imageView.setImage(imageToShow);
                                imageView.setFitWidth(380);
                                imageView.setPreserveRatio(true);
                            });
                            String capturedImagePath = "src/main/resources/assets/FacialRegDATA/Captured/captured_frame.jpg";
                            Imgcodecs.imwrite(capturedImagePath, frame);
                            boolean recognized = recognizeFace(capturedImagePath);
                            if (recognized) {
                                System.out.println("Face recognized successfully.");
                                cameraActive = false; // stop the camera
                            } else {
                                System.out.println("Face not recognized.");
                            }
                        }
                    }
                };
                this.timer = new Timer();
                this.timer.schedule(frameGrabber, 0, 33);
            } else {
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            this.cameraActive = false;
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
            this.capture.release();
            imageView.setImage(null);
        }*/

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
        //stage.setMaximized(true);
        stage.show();
        stage.toFront();
        StageManager.addStage("Profile", stage);
    }
}
