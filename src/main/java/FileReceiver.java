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
        void onProgressUpdate(double progress); // 0.0 - 1.0
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

                // Her dosya transferi için ayrı bir thread
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

                    while ((bytesRead = in.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;

                        double progress = (double) totalRead / fileSize;
                        System.out.printf("Receiving: %.2f%%\r", progress * 100);

                        if (progressCallback != null) {
                            progressCallback.onProgressUpdate(progress);
                        }

                        if (totalRead >= fileSize) break;
                    }
                }

                System.out.println("File saved successfully at " + outFile.getAbsolutePath());

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
