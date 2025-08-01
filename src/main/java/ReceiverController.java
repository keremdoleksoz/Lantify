import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import java.io.File;

public class ReceiverController {
    @FXML private TextField portField;
    @FXML private TextField savePathField;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label speedLabel;
    @FXML private Label etaLabel;

    private File selectedFolder;

    @FXML
    private void handleChooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedFolder = directoryChooser.showDialog(null);
        if (selectedFolder != null) {
            savePathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    @FXML
    private void handleReceive() {
        int port;

        String folderPath = savePathField.getText();

        if (folderPath == null || folderPath.trim().isEmpty()) {
            statusLabel.setText("Select a Folder Path");
            return;
        }

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            statusLabel.setText("Invalid File Path");
            return;
        }

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Port Number");
            return;
        }

        statusLabel.setText("Waiting for incoming file...");
        progressBar.setProgress(0);
        speedLabel.setText("Speed: -");
        etaLabel.setText("ETA: -");

        FileReceiver receiver = new FileReceiver(port, folder.getAbsolutePath());

        // Progress callback: speed, eta, percent
        receiver.setProgressCallback((progress, speedKBs, etaSeconds) -> Platform.runLater(() -> {
            progressBar.setProgress(progress);
            speedLabel.setText(String.format("Speed: %.2f KB/s", speedKBs));
            etaLabel.setText(String.format("ETA: %.1f sec", etaSeconds));
        }));

        // Onay ekranı
        receiver.setConfirmationCallback((fileName, fileSize) -> {
            final boolean[] userDecision = {false};
            final Object lock = new Object();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Incoming File");
                alert.setHeaderText("Do you want to receive this file?");
                alert.setContentText("File Name: " + fileName + "\nFile Size: " + (fileSize / 1024) + " KB");

                ButtonType yes = new ButtonType("Yes");
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(yes, no);

                alert.showAndWait().ifPresent(response -> {
                    userDecision[0] = response == yes;
                    synchronized (lock) {
                        lock.notify();
                    }
                });
            });

            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return userDecision[0];
        });

        new Thread(() -> {
            receiver.startReceiving();
            Platform.runLater(() -> {
                statusLabel.setText("File transfer is completed.");
                speedLabel.setText("Speed: -");
                etaLabel.setText("ETA: -");
            });
        }).start();
    }
}
