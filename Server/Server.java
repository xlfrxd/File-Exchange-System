package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {

    // TODO: make them into a hashmap where the socket is the key and the username
    // is the value
    private static final Set<String> registeredUsernames = new HashSet<>(); // to implement register username
    private static final Set<Socket> connectedClients = new HashSet<>(); // to implement broadcast and unicast feature

    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        int port = 12345;
        ServerSocket serverSocket = new ServerSocket(port, 50, serverAddress);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }

        // serverSocket.close();
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String inputLine;

        
        while ((inputLine = in.readLine()) != null) {
            
            String[] request = inputLine.split("\\s+");
            
            if ("/join".equals(request[0])) {
                // Client joined
                out.println("Connection to the File Exchange Server is successful!");
                break;
            } else if ("/leave".equals(request[0])) {
                // Client wants to leave
                out.println("You are disconnected. Goodbye!");
                // clientSocket.close(); // Close client's socket
                in.close();
                out.close();
                clientSocket.close();
                break;
            } else if ("/register".equals(request[0])) {
                // Client wants to register
                
                if (registeredUsernames.contains(request[1])) {
                    // Sends error to the client due to existing alias
                    out.println("Error: Registration failed. Handle or alias already exists.");
                    break;
                }

                registeredUsernames.add(request[1]);
                out.println("Welcome " + request[1] + "!");
            }
        }

        
        
    }
}
