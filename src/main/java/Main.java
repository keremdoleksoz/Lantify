import java.util.Scanner;

public class Main {
    public static void main(String [] arguments){
        Scanner scanner = new Scanner(System.in);
        System.out.println("For avoiding problems please run at Receiver computer first");
        System.out.println("1- File Send \n2- File Receive");
        System.out.println("Enter your choice");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            System.out.println("Enter the file path");
            String filePath = scanner.nextLine();
            System.out.println("Enter the destination IP");
            String destinationIP  = scanner.nextLine();
            System.out.println("Enter the destination port");
            int destinationPort  = scanner.nextInt();

            FileSender sender = new FileSender(filePath, destinationIP, destinationPort);

            sender.sendFile();

        }
        else if (choice == 2) {
            System.out.println("Enter the listening port");
            int listenPort = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter the saving path");
            String savePath = scanner.nextLine();

            FileReceiver receiver = new FileReceiver(listenPort, savePath);
            receiver.receiveFile();

        }
        else {
            System.out.println("Invalid choice");
        }

        scanner.close();


    }
}
