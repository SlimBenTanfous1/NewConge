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
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageRequest;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tn.bfpme.models.User;
import tn.bfpme.utils.EncryptionUtil;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;

import java.io.File;
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
    private VideoCapture camera;

    private Connection cnx;

    @FXML
    public void initialize() {
        cnx = MyDataBase.getInstance().getCnx();
        initializeCamera(); // Initialize camera once
    }

    private void initializeCamera() {
        try {
            camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                showAlert("Error", "Unable to access camera.");
                System.err.println("Error: Camera could not be opened.");
            } else {
                System.out.println("Camera initialized successfully.");
            }
        } catch (Exception e) {
            showAlert("Error", "Camera initialization failed.");
            e.printStackTrace();
        }
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
    private void FacialRecognitionButton(ActionEvent event) {
        try {
            if (camera == null || !camera.isOpened()) {
                showAlert("Error", "Camera is not accessible.");
                return;
            }

            Mat capturedFrame = captureImageFromCamera();
            if (capturedFrame == null || capturedFrame.empty()) {
                showAlert("Error", "Failed to capture image from camera.");
                return;
            }

            // Perform facial recognition using AWS Rekognition
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    System.out.println("Starting facial recognition with AWS Rekognition...");
                    return searchFaceInRekognition(capturedFrame);
                }

                @Override
                protected void succeeded() {
                    System.out.println("Facial recognition task succeeded.");

                    if (getValue()) {
                        try {
                            User recognizedUser = getRecognizedUser(capturedFrame); // Use capturedFrame instead of tempImagePath

                            if (recognizedUser != null) {
                                // Initialize the SessionManager with the recognized user
                                SessionManager.getInstance(recognizedUser);

                                // Navigate to profile after successful login
                                navigateToProfile(event);
                            } else {
                                showAlert("Face not recognized", "Please try again.");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            showAlert("Error", "Database error during facial recognition.");
                        }
                    } else {
                        showAlert("Face not recognized", "Please try again.");
                    }
                }

                @Override
                protected void failed() {
                    System.err.println("Facial recognition task failed.");
                    showAlert("Error", "An error occurred during facial recognition.");
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred during facial recognition.");
        }
    }

    private Mat captureImageFromCamera() {
        try {
            Mat frame = new Mat();
            if (camera.isOpened()) {
                camera.read(frame);
                if (frame.empty()) {
                    System.err.println("Error: Captured frame is empty.");
                    return null;
                }
                Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY); // Convert to grayscale
                System.out.println("Image captured successfully.");
                return frame;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error during image capture: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean searchFaceInRekognition(Mat capturedFrame) {
        try {
            // Convert the Mat object to a temporary image file
            String tempImagePath = saveCapturedImage(capturedFrame);

            RekognitionClient rekognitionClient = RekognitionClient.builder()
                    .region(Region.EU_CENTRAL_1)
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .build();

            String bucketName = "facialrecjava";
            String collectionId = "MyCollection"; // Your Rekognition collection ID
            String s3ObjectName = "captured_face.jpg"; // Image name uploaded to S3

            S3Object s3Object = S3Object.builder()
                    .bucket(bucketName)
                    .name(s3ObjectName)
                    .build();

            software.amazon.awssdk.services.rekognition.model.Image image = software.amazon.awssdk.services.rekognition.model.Image.builder()
                    .s3Object(s3Object)
                    .build();

            SearchFacesByImageRequest searchFacesByImageRequest = SearchFacesByImageRequest.builder()
                    .collectionId(collectionId)
                    .image(image)
                    .build();

            SearchFacesByImageResponse searchFacesByImageResponse = rekognitionClient.searchFacesByImage(searchFacesByImageRequest);
            List<FaceMatch> faceMatches = searchFacesByImageResponse.faceMatches();

            return !faceMatches.isEmpty();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String saveCapturedImage(Mat frame) {
        // Save the captured frame to a temporary file
        String tempImagePath = System.getProperty("java.io.tmpdir") + "captured_face.jpg";
        Imgcodecs.imwrite(tempImagePath, frame);
        System.out.println("Saved captured image to: " + tempImagePath);

        // Upload the image to S3
        uploadImageToS3(tempImagePath);

        return tempImagePath;
    }
    private void uploadImageToS3(String imagePath) {
        try {
            S3Client s3 = S3Client.builder()
                    .region(Region.EU_CENTRAL_1)
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .build();

            String bucketName = "facialrecjava";
            String key = "captured_face.jpg"; // You might want to use a unique name for each upload

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3.putObject(putObjectRequest, RequestBody.fromFile(new File(imagePath)));

            System.out.println("Uploaded image to S3: " + key);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean basicFaceDetectionTest(Mat capturedFrame) {
        try {
            System.out.println("Starting basic face detection...");

            // Load the Haar cascade for face detection
            String haarCascadePath = "src/main/resources/assets/FacialRegDATA/XML/haarcascades/haarcascade_frontalface_default.xml"; // Replace with the correct path to the Haar cascade
            CascadeClassifier faceDetector = new CascadeClassifier(haarCascadePath);

            if (faceDetector.empty()) {
                System.err.println("Failed to load Haar cascade file.");
                return false;
            }

            // Detect faces in the captured frame
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(capturedFrame, faceDetections);

            if (faceDetections.toArray().length > 0) {
                System.out.println("Face detected in the captured frame.");
                return true;
            } else {
                System.out.println("No faces detected in the captured frame.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception during basic face detection: " + e.getMessage());
            return false;
        }
    }

    private User getRecognizedUser(Mat capturedFrame) throws SQLException {
        String qry = "SELECT u.*, ur.ID_Role " +
                "FROM `user` as u " +
                "JOIN `user_role` ur ON ur.ID_User = u.ID_User";
        PreparedStatement stm = cnx.prepareStatement(qry);
        ResultSet rs = stm.executeQuery();

        while (rs.next()) {
            User user = initializeUserFromResultSet(rs);

            // Load images directly from S3 instead of local paths
            List<Mat> storedImages = loadStoredImagesFromS3(user);

            // Compare the captured frame against all stored images of the user
            if (compareFaces(capturedFrame, storedImages)) {
                return user; // Return the recognized user
            }
        }
        return null;
    }

    private List<Mat> loadStoredImagesFromS3(User user) {
        List<Mat> images = new ArrayList<>();
        String[] faceDataPaths = {user.getFace_data1(), user.getFace_data2(), user.getFace_data3(), user.getFace_data4()};

        for (String s3Key : faceDataPaths) {
            if (s3Key != null && !s3Key.isEmpty()) {
                // Download image from S3 and convert it to Mat
                Mat image = downloadImageFromS3(s3Key);
                if (image != null && !image.empty()) {
                    images.add(image);
                } else {
                    System.err.println("Failed to download image from S3 with key: " + s3Key);
                }
            }
        }

        return images;
    }

    private Mat downloadImageFromS3(String key) {
        try {
            S3Client s3 = S3Client.builder()
                    .region(Region.EU_CENTRAL_1)
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket("facialrecjava")
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3.getObject(getObjectRequest);
            return Imgcodecs.imdecode(new MatOfByte(s3Object.readAllBytes()), Imgcodecs.IMREAD_GRAYSCALE);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean compareFaces(Mat capturedFrame, List<Mat> storedImages) {
        try {
            // Convert the captured frame to grayscale if it's not already
            Mat grayscaleCapturedFrame = new Mat();
            if (capturedFrame.channels() > 1) {
                Imgproc.cvtColor(capturedFrame, grayscaleCapturedFrame, Imgproc.COLOR_BGR2GRAY);
            } else {
                grayscaleCapturedFrame = capturedFrame;
            }

            // Initialize the face recognizer (using LBPH in this case)
            FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();

            // Train the recognizer with the stored images
            List<Mat> images = new ArrayList<>();
            Mat labels = new Mat(storedImages.size(), 1, CvType.CV_32SC1);

            for (int i = 0; i < storedImages.size(); i++) {
                images.add(storedImages.get(i));
                labels.put(i, 0, i);  // Assuming each image has a unique label
            }

            faceRecognizer.train(images, labels);

            // Predict the label of the captured frame
            int[] label = new int[1];
            double[] confidence = new double[1];
            faceRecognizer.predict(grayscaleCapturedFrame, label, confidence);

            // Set a threshold for confidence to consider the prediction as successful
            double confidenceThreshold = 50.0; // You can adjust this value as needed

            if (confidence[0] < confidenceThreshold) {
                System.out.println("Face recognized with confidence: " + confidence[0]);
                return true; // Face recognized successfully
            } else {
                System.out.println("Face not recognized. Confidence: " + confidence[0]);
                return false; // Face not recognized
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false if any exception occurs
        }
    }

    private List<Mat> loadStoredImages(User user) {
        List<Mat> images = new ArrayList<>();
        String[] faceDataPaths = {user.getFace_data1(), user.getFace_data2(), user.getFace_data3(), user.getFace_data4()};

        for (String path : faceDataPaths) {
            if (path != null && !path.isEmpty()) {
                Mat image = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
                if (image != null && !image.empty()) {
                    images.add(image);
                } else {
                    System.err.println("Failed to load image from path: " + path);
                }
            }
        }

        return images;
    }

    private ResultSet getAllUsers() throws SQLException {
        String qry = "SELECT * FROM `user`";
        PreparedStatement stm = cnx.prepareStatement(qry);
        return stm.executeQuery();
    }

    private User initializeUserFromResultSet(ResultSet rs) throws SQLException {
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
