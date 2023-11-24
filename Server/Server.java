package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
                StringBuilder fileListString = new StringBuilder("Server Directory,"); //TODO: issue with \n

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        fileListString.append(listOfFiles[i].getName()).append(","); //TODO: issue with \n
                    } 
                    else if (listOfFiles[i].isDirectory()) { 
                        fileListString.append("Directory ").append(listOfFiles[i].getName()).append("\n");
                    }
                }

                System.out.println(fileListString);
                
                
               
                out.println(fileListString.toString());  // Output filenames of /Server folder


            } else if ("/store".equals(request[0])) {
                // Client wants to store files
                


            } else if ("/fetch".equals(request[0])) {
                // Client wants to fetch files//byte buffer and shit

            }
        }

        // in.close();
        // out.close();
        // clientSocket.close();
    }
}
