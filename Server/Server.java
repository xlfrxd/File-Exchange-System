package Server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static Set<String> registeredUsernames = new HashSet<>();

    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        int port = 12345;
        ServerSocket serverSocket = new ServerSocket(port, 50, serverAddress);

        while (!serverSocket.isClosed()) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }

        serverSocket.close();
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        File folder = new File("./dir");
        File[] listOfFiles = folder.listFiles();

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String inputLine = "";
        System.out.println(inputLine);
        while ((inputLine = in.readLine()) != null) {
            String[] request = inputLine.split("\\s+");

            // Client requests
            System.out.println(inputLine);

            if ("/join".equals(request[0])) {
                // Client joined
                out.println("Connection to the File Exchange Server is successful!");
            } else if ("/leave".equals(request[0])) {
                // Client wants to leave
                out.println("You are disconnected. Goodbye!");
                clientSocket.close(); // Close client's socket
                break;
            } else if ("/register".equals(request[0])) {
                // Client wants to register
                if (registeredUsernames.contains(request[1])) {
                    out.println("Error: Registration failed. Handle or alias already exists.");
                } else {
                    out.println("Welcome " + request[1] + "!");
                    registeredUsernames.add(request[1]);
                }
            } else if ("/dir".equals(request[0])) {
                // Client wants to view directory contents
                StringBuilder fileListString = new StringBuilder("Server Directory,"); // TODO: issue with \n

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        fileListString.append(listOfFiles[i].getName()).append(","); // TODO: issue with \n
                    } else if (listOfFiles[i].isDirectory()) {
                        fileListString.append("Directory ").append(listOfFiles[i].getName()).append("\n");
                    }
                }

                out.println(fileListString.toString()); // Output filenames of /Server folder

            } else if ("/store".equals(request[0])) {
                // Client wants to upload files

                // Receive file name from client
                String fileName = request[1];

                // Create a new file with the given fileName
                File receivedFile = new File("./dir/" + fileName);
                InputStream is = clientSocket.getInputStream();
                FileOutputStream fos = new FileOutputStream(receivedFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                // Receive file contents from the client
                byte[] buffer = new byte[1024];
                int read, totalRead = 0;

                while ((read = is.read(buffer)) > 0) {
                    bos.write(buffer, 0, read);
                    bos.flush();

                    // Check for the end-of-file marker
                    if (new String(buffer, 0, totalRead).equals("END_OF_FILE")) {
                        break;
                    }
                }

                // Close streams
                bos.close();
                fos.close();

                System.out.println("eof?");

                // Optional: Receive and print an "EOF" marker from the client
                // String eofMarker = in.readLine();
                // System.out.println("Received: EOF");
                out.println("User1<2023-11-06 16:48:05>: Uploaded " + fileName); // TODO: Fill up missing args

            } else if ("/fetch".equals(request[0])) {
                // Client wants to fetch files//byte buffer and shit

                for (int i = 0; i < listOfFiles.length; i++) {

                    // Search for file by name
                    if (listOfFiles[i].getName().equals(request[1])) {

                        out.println("Found!");

                    } else {

                        out.println("Error: File not found."); // Unsuccessful sending of a file that does not exist in
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
