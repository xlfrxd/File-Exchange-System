package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public void sendCommandToServer(String command) {

    }

    public static void main(String[] args) {

        while (true) {
            try {
                Socket socket = null;

                Scanner scan = new Scanner(System.in);
                BufferedReader in = null;
                PrintWriter out = null;

                String userInput = "";
                String errorString = "";

                Boolean connectedState = false;
                Boolean registeredState = false;
                String username = "User";

                do {

                    System.out.print(username + ": ");

                    userInput = scan.nextLine();

                    String[] command = userInput.split("\\s+");
                    if ("/join".equals(command[0])) { // Join server via ip and port
                        if (command.length != 3) { // Command must have ip address and port as arguments
                            errorString = "Error: Command parameters do not match or is not allowed.";
                            break;
                        }

                        try {
                            socket = new Socket(command[1], Integer.valueOf(command[2]));
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            out = new PrintWriter(socket.getOutputStream(), true);

                            out.println(userInput); // Send valid command to server
                            System.out.println(in.readLine()); // Receive response from server
                            connectedState = true;

                            
                            // Note: Do not close PrintWriter and BufferedReader here
                        } catch (Exception e) {
                            // ...
                        }

                        System.out.println(socket.isConnected());
                    } else if ("/leave".equals(command[0])) { // Leave server
                        System.out.println(socket.isConnected());
                        if (command.length != 1) { // Command must have only 1 argument
                            errorString = "Error: Command parameters do not match or is not allowed.";
                            break;
                        }
                        
                        try {

                            out.println(userInput); // Send valid command to server
                            
                            String response = in.readLine(); // Receive response from server
                            System.out.println(response);

                            connectedState = false;
                        } catch (Exception e) {
                            errorString = "Error: Disconnection failed. Please connect to the server first.";
                            e.printStackTrace();
                            break;
                        } finally {
                            try {
                                // Close resources after receiving the server's response
                                if (socket != null)
                                    socket.close();
                            } catch (IOException e) {
                                System.out.println("Error occurred while closing resources");
                            }
                        }
                    } else if (("/register".equals(command[0]) || "/store".equals(command[0])
                            || "/dir".equals(command[0]) || "/get".equals(command[0]))) {
                        if (!connectedState) { // Error for handling client must first be connected to a server
                            errorString = "Error: Disconnection failed. Please connect to the server first"; // TODO:
                                                                                                             // Should I
                                                                                                             // reword
                                                                                                             // this
                                                                                                             // error?
                                                                                                             // "Disconnection
                                                                                                             // failed"
                                                                                                             // seems
                                                                                                             // wrong
                            break;
                        }

                        if ("/register".equals(command[0])) { // Register client
                            if (command.length != 2) {
                                errorString = "Error: Command parameters do not match or is not allowed.";
                                break;
                            }
                            try {

                                out.println(command); // Send valid command to server
                                String res = in.readLine(); // Receive response from server

                                System.out.println(res);

                                String validString = "Welcome " + command[1] + "!";

                                if (validString.equals(res)) {
                                    registeredState = true;
                                } else {
                                    errorString = "Error: Registration failed. Handle or alias already exists.";
                                    break;
                                }
                                in.close();
                                out.close();
                            } catch (Exception e) {
                                System.out.println("Error with handling registration.");
                                e.printStackTrace();
                                break;
                            }
                        }

                    } else if ("/?".equals(command[0])) { // View command list
                        if (command.length == 1) {
                            System.out.println("Available commands:");

                            System.out.println(
                                    "          /join <server_ip_add> <port>: Connect to the server application.");
                            System.out.println("          /leave: Disconnect from the server application.");
                            System.out.println("          /register <handle>: Register a unique handle or alias.");
                            System.out.println("          /store <filename>: Send a file to the server.");
                            System.out.println("          /dir: Request the directory file list from the server.");
                            System.out.println("          /get <filename>: Fetch a file from the server.");
                        } else {
                            System.out.println("Error: Command parameters do not match or are not allowed.");
                            break;
                        }
                    } else { // Unknown or wrong syntax error
                        errorString = "Error: Command not found.";
                        break;
                    }

                } while ((userInput != null));

                // Print errors
                if (!(errorString.isEmpty())) {
                    System.out.println(errorString);
                    errorString = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
