import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiver {
    private int listenPort;
    private String savePath;
    private volatile boolean running = true;
    private ServerSocket serverSocket;

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
        try {
            serverSocket = new ServerSocket(listenPort);
            serverSocket.setReuseAddress(true);
            System.out.println("Listening on port " + listenPort);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleSingleFileReceive(clientSocket)).start();
            }

        } catch (IOException e) {
            if (running) {
                System.out.println("Error! " + e.getMessage());
            } else {
                System.out.println("Server stopped.");
            }
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing server socket.");
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing server socket.");
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
                        double elapsedSec = (System.nanoTime() - startTime) / 1_000_000_000.0;
                        double speedKBs = elapsedSec > 0 ? (totalRead / 1024.0) / elapsedSec : 0.0;
                        double etaSeconds = speedKBs > 0 ? ((fileSize - totalRead) / 1024.0) / speedKBs : -1;

                        if (progressCallback != null) {
                            progressCallback.onProgressUpdate(progress, speedKBs, etaSeconds);
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
