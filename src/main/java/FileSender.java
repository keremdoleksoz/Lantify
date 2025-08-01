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

    public void sendFile() {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("[!] File not found: " + filePath);
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
            System.out.println("[*] Connected to receiver: " + destinationIP + ":" + destinationPort);
            System.out.println("[*] Sending file: " + fileName + " (" + fileSize + " bytes)");

            // Metadata gönder
            writer.println(fileName);
            writer.println(fileSize);

            String response = reader.readLine();

            if (response != null && response.equalsIgnoreCase("Y")) {
                System.out.println("[*] Receiver accepted. Starting transfer...");

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalSent = 0;

                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;

                    double percent = (double) totalSent / fileSize;

                    // GUI ProgressBar'a bildir
                    if (progressCallback != null) {
                        progressCallback.onProgressUpdate(percent); // 0.0 - 1.0
                    }

                    // Konsol çıktısı
                    System.out.printf("Progress: %.2f%%\r", percent * 100);
                }

                System.out.println("\n[✓] File sent successfully: " + fileName);

            } else {
                System.out.println("[X] Transfer was refused by receiver.");
            }

        } catch (IOException e) {
            System.out.println("[!] Transfer error: " + e.getMessage());
        }
    }
}
