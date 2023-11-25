package Server;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

public class Server {
    private static Set<String> registeredUsernames = new HashSet<>();
    private static HashMap<InetAddress, String> clientUsernameMap = new HashMap<InetAddress, String>();

    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        int port = 12345;
        ServerSocket serverSocket = new ServerSocket(port, 50, serverAddress);

        while (!serverSocket.isClosed()) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> {
                try {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        serverSocket.close();
    }

    private static void handleClient(Socket clientSocket) throws IOException {

        File folder = new File("./dir");
        File[] listOfFiles = folder.listFiles();

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        String inputLine = "";
        System.out.println(inputLine);
        while ((inputLine = in.readUTF()) != null) {
            String[] request = inputLine.split("\\s+");

            // Client requests
            System.out.println(inputLine);

            if ("/join".equals(request[0])) { // Client joined
                out.writeUTF("Connection to the File Exchange Server is successful!");

                clientUsernameMap.put(clientSocket.getInetAddress(), ""); // Add clientSocket to HashMap
            } else if ("/leave".equals(request[0])) {
                // Client wants to leave
                out.writeUTF("You are disconnected. Goodbye!");
                clientUsernameMap.remove(clientSocket.getInetAddress()); // Remove clientSocket from HashMap
                clientSocket.close(); // Close client's socket
                break; // Stop reading client input
            } else if ("/register".equals(request[0])) { // Client wants to register
                System.out.println(request[1]);
                if (clientUsernameMap.containsValue(request[1])) {
                    out.writeUTF("Error: Registration failed. Handle or alias already exists.");
                } else {
                    out.writeUTF("Welcome " + request[1] + "!");
                    clientUsernameMap.replace(clientSocket.getInetAddress(), request[1]); // Register username to
                                                                                          // existing clientSocket
                                                                                          // within the HashMap

                }
            } else if ("/dir".equals(request[0])) { // Client wants to view directory contents
                StringBuilder fileListString = new StringBuilder("Server Directory,"); // TODO: issue with \n

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        fileListString.append(listOfFiles[i].getName()).append(","); // TODO: issue with \n
                    } else if (listOfFiles[i].isDirectory()) {
                        fileListString.append("Directory ").append(listOfFiles[i].getName()).append("\n");
                    }
                }

                out.writeUTF(fileListString.toString()); // Output filenames of /Server folder

            } else if ("/store".equals(request[0])) { // Client wants to upload files

                // Receive file name from client
                String fileName = request[1];

                // Create a new file with the given fileName
                File receivedFile = new File("./dir/" + fileName);
                FileOutputStream fos = new FileOutputStream(receivedFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) > 0) {
                    bos.write(buffer, 0, bytesRead);
                }

                // Check for the end-of-file marker
                if ("END_OF_FILE".equals(in.readUTF())) {
                    System.out.println("File transfer complete.");
                } else {
                    System.out.println("Error: Unexpected data received.");
                }

                // Close streams
                bos.close();
                fos.close();

                // Get current timestamp
                LocalDateTime timestamp = LocalDateTime.now();
                String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                out.writeUTF(
                        clientUsernameMap.get(clientSocket.getInetAddress()) +
                                "<" + formattedTimestamp + ">: Uploaded " + fileName);

            } else if ("/fetch".equals(request[0])) {
                // Client wants to fetch files//byte buffer and shit

                for (int i = 0; i < listOfFiles.length; i++) {

                    // Search for file by name
                    if (listOfFiles[i].getName().equals(request[1])) {

                        out.writeUTF("Found!");

                    } else {

                        out.writeUTF("Error: File not found."); // Unsuccessful sending of a file that does not exist in
                                                                // dir

                    }
                }
            }

            // out.println("Error: File not found in the server."); // Unsuccessful fetching
            // of a file that does not exist in dir.
        }

        // in.close();
        // out.close();
        // clientSocket.close();
    }
}
