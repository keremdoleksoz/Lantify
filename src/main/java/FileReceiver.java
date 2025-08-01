import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiver {
    private int listenPort;
    private String savePath;

    public FileReceiver(int listenPort, String savePath) {
        this.listenPort = listenPort;
        this.savePath = savePath;
    }

    public interface ProgressCallback {
        void onProgressUpdate(double progress, double speedKBs, double etaSeconds);
    }

    private ProgressCallback progressCallback;

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public interface ConfirmationCallback {
        boolean confirm(String fileName, long fileSize);
    }

    private ConfirmationCallback confirmationCallback;

    public void setConfirmationCallback(ConfirmationCallback callback) {
        this.confirmationCallback = callback;
    }

    public void startReceiving() {
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            System.out.println("Listening on port " + listenPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostName());

                new Thread(() -> handleSingleFileReceive(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("Error! " + e.getMessage());
        }
    }

    private void handleSingleFileReceive(Socket clientSocket) {
        try (
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                PrintWriter writer = new PrintWriter(out, true)
        ) {
            String fileName = reader.readLine();
            long fileSize = Long.parseLong(reader.readLine());

            System.out.println("File Info: " + fileName + " - " + fileSize + " bytes");

            boolean acceptFile = confirmationCallback == null || confirmationCallback.confirm(fileName, fileSize);

            if (acceptFile) {
                writer.println("Y");

                File outFile = new File(savePath + File.separator + fileName);
                try (FileOutputStream fileOut = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalRead = 0;

                    long startTime = System.nanoTime();

                    while ((bytesRead = in.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;

                        double progress = (double) totalRead / fileSize;

                        // Transfer hızı ve ETA hesapla
                        long elapsedTimeNs = System.nanoTime() - startTime;
                        double elapsedSec = elapsedTimeNs / 1_000_000_000.0;
                        double speedKBs = elapsedSec > 0 ? (totalRead / 1024.0) / elapsedSec : 0.0;

                        double remainingBytes = fileSize - totalRead;
                        double etaSeconds = speedKBs > 0 ? (remainingBytes / 1024.0) / speedKBs : -1;

                        System.out.printf("Receiving: %.2f%%, Speed: %.2f KB/s, ETA: %.1f sec\r", progress * 100, speedKBs, etaSeconds);

                        if (progressCallback != null) {
                            progressCallback.onProgressUpdate(progress, speedKBs, etaSeconds);
                        }

                        if (totalRead >= fileSize) break;
                    }
                }

                System.out.println("\nFile saved successfully at " + outFile.getAbsolutePath());

            } else {
                writer.println("N");
                System.out.println("Transfer refused.");
            }

        } catch (IOException e) {
            System.out.println("Error receiving file: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket.");
            }
        }
    }
}
