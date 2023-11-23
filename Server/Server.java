package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        int port = 12345;
        ServerSocket serverSocket = new ServerSocket(port, 50, serverAddress);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }

        //serverSocket.close();
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String inputLine;

        out.println("Connection to the File Exchange Server is successful!");

        while ((inputLine = in.readLine()) != null) {
            if ("/join".equals(inputLine)) {
                // Client joined
                out.println("Client [" + clientSocket.getInetAddress() + "]");
                break;
            } else if ("/leave".equals(inputLine)) {
                // Client wants to leave
                out.println("You are disconnected. Goodbye!");
                //clientSocket.close(); // Close client's socket
                break;
            }
        }

        in.close();
        out.close();
        clientSocket.close();
    }
}
