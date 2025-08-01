import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import java.io.File;

public class ReceiverController {
    @FXML private TextField portField;
    @FXML private TextField savePathField;
    @FXML private Label statusLabel;

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
    private void handleReceive(){
        int port;

        String folderPath=savePathField.getText();

        if(folderPath == null || folderPath.trim().isEmpty()){
            statusLabel.setText("Select a Folder Path");
            return;
        }

        File folder = new File(folderPath);
        if(!folder.exists() || !folder.isDirectory()){
            statusLabel.setText("Invalid File Path");
            return;
        }

        try {
            port = Integer.parseInt(portField.getText());
        }catch (NumberFormatException e){
            statusLabel.setText("Invalid Port Number");
            return;
        }

        statusLabel.setText("Connecting...");

        FileReceiver receiver = new FileReceiver(port, folder.getAbsolutePath());

        new Thread(() -> {

            receiver.receiveFile(true);

            statusLabel.setText("File transfer is completed.");
        }).start();
    }

}
