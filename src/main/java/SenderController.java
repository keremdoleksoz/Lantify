import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SenderController {
    @FXML private TextField filePathField;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label currentFileLabel;

    private List<File> selectedFiles = new ArrayList<>();

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files != null && !files.isEmpty()) {
            selectedFiles = files;
            StringBuilder paths = new StringBuilder();
            for (File file : files) {
                paths.append(file.getAbsolutePath()).append("\n");
            }
            filePathField.setText(paths.toString());
        }
    }

    @FXML
    private void handleSendFile() {
        if (selectedFiles.isEmpty()) {
            statusLabel.setText("No files selected");
            return;
        }

        String ip = ipField.getText();
        int port;

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Port Number");
            return;
        }

        progressBar.setProgress(0);
        statusLabel.setText("Sending files...");

        new Thread(() -> {
            for (int i = 0; i < selectedFiles.size(); i++) {
                File file = selectedFiles.get(i);

                Platform.runLater(() -> currentFileLabel.setText("Sending: " + file.getName()));

                FileSender sender = new FileSender(file.getAbsolutePath(), ip, port);
                int index = i;

                sender.setProgressCallback(progress -> Platform.runLater(() -> {
                    double totalProgress = (index + progress) / selectedFiles.size();
                    progressBar.setProgress(totalProgress);
                }));

                sender.sendFile();
            }

            Platform.runLater(() -> {
                statusLabel.setText("All files sent successfully.");
                currentFileLabel.setText("Done.");
            });

        }).start();
    }
}
