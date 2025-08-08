import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Logo
        Image logoImage = new Image(getClass().getResource("/icon/lantify-icon-trans.png").toExternalForm());
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(200);
        logoView.setPreserveRatio(true);

        // Header
        Label label = new Label("Lantify - LAN Transfer File Application");
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Buttons
        Button sendButton = new Button("Send");
        Button receiveButton = new Button("Receive");

        sendButton.setPrefSize(200, 120);
        receiveButton.setPrefSize(200, 120);

        sendButton.setStyle("-fx-font-size: 16px;");
        receiveButton.setStyle("-fx-font-size: 16px;");

        sendButton.setOnAction(e -> openSenderWindow());
        receiveButton.setOnAction(e -> openReceiverWindow());

        HBox buttonBox = new HBox(40, sendButton, receiveButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Bottom info
        Label tipLabel = new Label(" ( For optimal use, start the receiver computer first. ) ");
        tipLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");

        // Generic layout
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setSpacing(15);

        VBox.setMargin(logoView, new Insets(0, 0, -10, 0));
        VBox.setMargin(label, new Insets(0, 0, 10, 0));
        VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));
        VBox.setMargin(tipLabel, new Insets(10, 0, 0, 0));

        layout.getChildren().addAll(logoView, label, buttonBox, tipLabel);
        layout.setStyle("-fx-background-color: #3f51b5;");

        // Scene settings
        Scene scene = new Scene(layout, 540, 450);
        stage.setTitle("Lantify");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/lantify-icon.png")));
        stage.setScene(scene);
        stage.show();
    }

    private void openSenderWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sender.fxml"));
            Parent root = loader.load();
            Stage senderStage = new Stage();
            senderStage.setTitle("Send File");
            senderStage.setScene(new Scene(root));
            senderStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/lantify-icon.png")));
            senderStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openReceiverWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/receiver.fxml"));
            Parent root = loader.load();
            Stage receiverStage = new Stage();
            receiverStage.setTitle("Receive File");
            receiverStage.setScene(new Scene(root));
            receiverStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/lantify-icon.png")));
            receiverStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] arguments) {
        launch();
    }
}
