import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class FileReceiver {
    private int listenPort;
    private String savePath;

    public FileReceiver(int listenPort, String savePath) {
        this.listenPort = listenPort;
        this.savePath = savePath;
    }

    public void receiveFile(boolean acceptFile) {
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            System.out.println("Listening on port " + listenPort);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostName());

            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            String fileName = reader.readLine();
            long fileSize = Long.parseLong(reader.readLine());

            System.out.println("File Informations: ");
            System.out.println("File Name: " + fileName);
            System.out.println("File Size: " + fileSize);


            if (acceptFile) {
                writer.println("Y");

                File outFile = new File(savePath + File.separator + fileName);
                FileOutputStream fileOut = new FileOutputStream(outFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalRead= 0;

                while((bytesRead = in.read(buffer)) != -1){
                    fileOut.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    double percent = (double) totalRead / fileSize *100;
                    System.out.printf("Receiving: %.2f%%\r", percent);
                    if(totalRead >= fileSize) break;
                }

                fileOut.close();
                System.out.println("File saved successfully at " + outFile.getAbsolutePath() );

            } else {
                writer.println("N");
                System.out.println("Transfer refused. Connection closed !");
            }

            reader.close();
            writer.close();
            in.close();
            out.close();
            clientSocket.close();



        } catch (IOException e) {
            System.out.println("Error ! " + e.getMessage());
        }
    }

}
