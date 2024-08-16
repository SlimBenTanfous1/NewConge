package tn.bfpme.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.*;

public class CameraFeedController {

    @FXML
    private ImageView cameraView;  // ImageView to display the camera feed
    @FXML
    private Label instructionLabel;  // Label to display face orientation instructions
    @FXML
    private Button continueButton;  // Button to proceed to the next instruction
    @FXML
    private Button retryButton;  // Button to retry the current instruction

    private VideoCapture camera;
    private boolean capturing;
    private Stage stage;

    private int pictureCount = 0;
    private final String[] instructions = {
            "Face directly to the camera",
            "Face right",
            "Face left",
            "Face down"
    };

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void startFacialRecognition() {
        camera = new VideoCapture(0); // Open the default camera
        if (!camera.isOpened()) {
            System.err.println("Error: Camera not accessible");
            return;
        }

        capturing = true;
        updateInstruction();
        captureFrame();
    }

    private void captureFrame() {
        new Thread(() -> {
            while (capturing) {
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    Image imageToShow = matToImage(frame);
                    Platform.runLater(() -> cameraView.setImage(imageToShow));
                } else {
                    System.err.println("Error: Unable to capture image");
                }
                try {
                    Thread.sleep(30); // Refresh rate for the live feed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private void storeFaceImage(byte[] faceImage) {
        try {
            // Specify the directory path within your project
            String directoryPath = System.getProperty("user.dir") + "/src/main/resources/assets/users/";

            // Create the directory if it doesn't exist
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();  // Use mkdirs() to create any necessary parent directories
            }

            // Create a unique filename for each image
            String fileName = "face_image_" + pictureCount + ".jpg";
            String filePath = directoryPath + fileName;

            // Write the byte array to a file
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(faceImage);
            fos.close();

            System.out.println("Stored image " + pictureCount + " at " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void handleContinue() {
        // Capture the current image
        Mat frame = new Mat();
        if (camera.read(frame)) {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, matOfByte);
            byte[] faceImage = matOfByte.toArray();
            storeFaceImage(faceImage);
        }

        pictureCount++;
        if (pictureCount < instructions.length) {
            updateInstruction();
        } else {
            // Finished capturing all images
            capturing = false;
            camera.release();
            stage.close();
        }
    }

    @FXML
    private void handleRetry() {
        // Simply allow the user to retry the current step
        updateInstruction();
    }

    private void updateInstruction() {
        Platform.runLater(() -> instructionLabel.setText(instructions[pictureCount]));
    }
}
