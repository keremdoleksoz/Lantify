import java.io.*;
import java.net.Socket;

public class FileSender {
    private String filePath;
    private String destinationIP;
    private int destinationPort;

    public FileSender(String filePath, String destinationIP, int destinationPort){
        this.filePath = filePath;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
    }

    public interface ProgressCallback {
        void onProgressUpdate(double progress); // 0.0 - 1.0 arasında
    }

    private ProgressCallback progressCallback;

    public void setProgressCallback(ProgressCallback callback){
        this.progressCallback = callback;
    }

    public void sendFile(){
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File does not exist");
            return;
        }

        String fileName = file.getName();
        long fileSize = file.length();

        try (
                Socket socket = new Socket(destinationIP, destinationPort);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                PrintWriter writer = new PrintWriter(out, true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                FileInputStream fileIn = new FileInputStream(file)
        ) {
            writer.println(fileName);
            writer.println(fileSize);

            String response = reader.readLine();

            if (response.equalsIgnoreCase("Y")) {
                System.out.println("Receiver accepted. Sending file...");

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalSent = 0;

                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;

                    double percent = (double) totalSent / fileSize;

                    // Konsol çıktısı
                    System.out.printf("Sending: %.2f%%\r", percent * 100);

                    // GUI ProgressBar'a gönder
                    if (progressCallback != null) {
                        progressCallback.onProgressUpdate(percent); // 0.0 - 1.0 arası
                    }
                }

                System.out.println("\nFile sent successfully");

            } else {
                System.out.println("Receiver not accepted");
            }

        } catch (IOException e) {
            System.out.println("Error! " + e.getMessage());
        }
    }
}
