import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;

public class SenderController {
    @FXML private TextField filePathField;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;

    private File selectedFile;

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleSendFile() {
        if(selectedFile == null || !selectedFile.exists()) {
            statusLabel.setText("File Not Found");
            return;
        }

        String ip = ipField.getText();
        int port;

        try {
            port = Integer.parseInt(portField.getText());
        }catch (NumberFormatException e) {
            statusLabel.setText("Invalid Port Number");
            return;
        }

        FileSender sender = new FileSender(selectedFile.getAbsolutePath(),  ip, port);
        statusLabel.setText("Sending File ...");

        new Thread(() -> {
            sender.sendFile();
            statusLabel.setText("File Transfer is completed.");
        }).start();
    }
}
