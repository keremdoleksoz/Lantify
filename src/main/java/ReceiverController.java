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
        String folderPath = savePathField.getText();
        int port;

        if (folderPath == null || folderPath.trim().isEmpty()) {
            statusLabel.setText("Select a folder path.");
            return;
        }

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            statusLabel.setText("Invalid folder path.");
            return;
        }

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid port number.");
            return;
        }

        statusLabel.setText("Waiting for sender...");
        progressBar.setProgress(0);

        FileReceiver receiver = new FileReceiver(port, folderPath);

        // Progress bar güncelleyicisi
        receiver.setProgressCallback(progress -> Platform.runLater(() -> {
            progressBar.setProgress(progress);
        }));

        new Thread(() -> {
            receiver.receiveFile(true); // otomatik kabul

            Platform.runLater(() -> {
                statusLabel.setText("File transfer completed.");
                progressBar.setProgress(1.0); // %100 tamamlandı
            });
        }).start();
    }
}
