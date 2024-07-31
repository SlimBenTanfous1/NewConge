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
import tn.bfpme.utils.FacialRec;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;
import tn.bfpme.utils.StageManager;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String style = "-fx-background-color: transparent; -fx-border-color: transparent transparent #eab53f transparent; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 3 0;";
        LoginEmail.setStyle(style);
        LoginMDP.setStyle(style);

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
        try {
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setString(1, LoginEmail.getText());
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                String storedHashedPassword = rs.getString("MDP");
                String enteredPassword = LoginMDP.getText();

                if (BCrypt.checkpw(enteredPassword, storedHashedPassword)) {
                    User connectedUser = new User(
                            rs.getInt("ID_User"),
                            rs.getString("Nom"),
                            rs.getString("Prenom"),
                            rs.getString("Email"),
                            storedHashedPassword,
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
        }
    }

    @FXML
    void FacialRecognitionButton(ActionEvent event) {
        String faceCascadePath = "src/main/resources/assets/FacialRegDATA/XML/haarcascades/haarcascade_frontalface_alt.xml";
        CascadeClassifier faceDetector = new CascadeClassifier(faceCascadePath);
        if (faceDetector.empty()) {
            System.err.println("Failed to load haarcascade_frontalface_alt.xml");
            return;
        }

        VideoCapture capture = new VideoCapture(0, Videoio.CAP_DSHOW); // Try using DirectShow backend
        if (!capture.isOpened()) {
            System.out.println("Error: Cannot open the camera.");
            return;
        }

        Task<Void> faceRecognitionTask = new Task<Void>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                while (capture.read(frame)) {
                    MatOfRect faceDetections = new MatOfRect();
                    faceDetector.detectMultiScale(frame, faceDetections);

                    for (Rect rect : faceDetections.toArray()) {
                        Imgproc.rectangle(frame, new org.opencv.core.Point(rect.x, rect.y), new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                    }
                    Image imageToShow = FacialRec.mat2Image(frame);
                    Platform.runLater(() -> imageView.setImage(imageToShow));
                    String capturedImagePath = "src/main/resources/assets/FacialRegDATA/Captured/captured_frame.jpg";
                    Imgcodecs.imwrite(capturedImagePath, frame);
                    boolean recognized = recognizeFace(capturedImagePath);
                    if (recognized) {
                        System.out.println("Face recognized successfully.");
                        break;
                    } else {
                        System.out.println("Face not recognized.");
                    }
                    try {
                        Thread.sleep(33); // ~30 frames per second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                capture.release();
                return null;
            }
        };

        Thread faceRecognitionThread = new Thread(faceRecognitionTask);
        faceRecognitionThread.setDaemon(true);
        faceRecognitionThread.start();
    }

    private boolean recognizeFace(String capturedImagePath) {
        Mat capturedImage = Imgcodecs.imread(capturedImagePath);
        File directory = new File("src/main/resources/assets/FacialRegDATA");
        File[] storedFaceFiles = directory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (storedFaceFiles == null) {
            System.err.println("No stored face images found.");
            return false;
        }

        for (File storedFaceFile : storedFaceFiles) {
            Mat storedFace = Imgcodecs.imread(storedFaceFile.getAbsolutePath());
            if (storedFace.empty()) {
                System.err.println("Failed to load stored face image: " + storedFaceFile.getName());
                continue;
            }

            System.out.println("Comparing with stored face: " + storedFaceFile.getName());
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
