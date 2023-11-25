package Server;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.ECFieldF2m;
import java.util.HashMap;

public class Server {
    private static HashMap<String, String> clientUsernameMap = new HashMap<String, String>();
    private static InetAddress serverAddress;
    private static int port;
    public static void main(String[] args) throws IOException {
        serverAddress = InetAddress.getByName("127.0.0.1");
        port = 12345;
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
        String clientId = "";

        File folder = new File("./dir/");
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

                clientId = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                clientUsernameMap.put(clientId, ""); // Add clientSocket to HashMap
            } else if ("/leave".equals(request[0])) {
                // Client wants to leave
                out.writeUTF("You are disconnected. Goodbye!");
                clientUsernameMap.remove(clientId); // Remove clientSocket from HashMap
                clientSocket.close(); // Close client's socket
                break; // Stop reading client input
            } else if ("/register".equals(request[0])) { // Client wants to register
                System.out.println(request[1]);

                Boolean isValid = false;
                if (!clientUsernameMap.containsValue(request[1])) {
                    isValid = true;
                } 

                out.writeBoolean(isValid);

                if(isValid)
                    // Register username to existing clientSocket within the HashMap
                    clientUsernameMap.replace(clientId, request[1]);

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

                long totalBytes = in.readLong(); // Read file size from client
                long totalBytesRead = 0;

                while ((bytesRead = in.read(buffer)) != 0) {
                    bos.write(buffer, 0, bytesRead);

                    totalBytesRead += bytesRead;
                    if (totalBytes == totalBytesRead)
                        break;
                }
                // Close streams
                bos.close();
                fos.close();

                // Get current timestamp
                LocalDateTime timestamp = LocalDateTime.now();
                String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                out.writeUTF(
                        clientUsernameMap.get(clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort()) +
                                "<" + formattedTimestamp + ">: Uploaded " + fileName);

            } else if ("/get".equals(request[0])) {
                // Client wants to get files//byte buffer and shit

                Boolean fileFound = false;
                listOfFiles = folder.listFiles(); // Get updated list of files when /get is called


                for (int i = 0; i < listOfFiles.length; i++) {

                    // Search for file by name
                    if (listOfFiles[i].getName().equals(request[1])) {

                        fileFound = true;
                        break;
                    }
                }

                if (!fileFound) { // Check if file exists in client
                    out.writeUTF("Error: File not found."); // Unsuccessful sending of a file that does not exist in dir
                } else {

                    out.writeBoolean(fileFound); // Send fileFound state

                    File sendFile = new File("./dir/" + request[1]);
                    FileInputStream fis = new FileInputStream(sendFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    long fileSize = sendFile.length(); // Get file size

                    out.writeLong(fileSize); // Send file size

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead); // Send file contents
                    }

                    // Close streams
                    bis.close();
                    fis.close();
                }
            } else if ("/bc".equals(request[0])) { // Broadcast message to everyone in the server
                // Get client who broadcasted
                String broadcasterUsername = clientUsernameMap.get(clientSocket.getInetAddress());

                
                // Send message to everyone else in the HashMap EXCEPT broadcaster
                for (String otherClient : clientUsernameMap.keySet()) {
                    
                    Socket otherClientSocket = new Socket(serverAddress, port);
                    DataOutputStream dos = new DataOutputStream(otherClientSocket.getOutputStream());
                    dos.writeUTF("/bc");
                    dos.writeUTF("[Broadcast from "+broadcasterUsername+"]:"+in.readUTF());
                    otherClientSocket.close();
                }
            }
        }
    }
}
