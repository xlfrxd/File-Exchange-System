package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static String username = "User";

    
        
    
    public static void main(String[] args) {
        String userInput = "";
        
        String errorString = "";
        
        
        while (true) {
            try {
                Scanner scan = new Scanner(System.in);
                do {

                    System.out.print(username + ": ");

                    userInput = scan.nextLine();

                    String[] command = userInput.split("\\s+");
                    if ("/join".equals(command[0])) { // Join server via ip and port

                        if (command.length != 3) { // Command must have ip address and port as arguments
                            errorString = "Error: Command parameters do not match or is not allowed.";
                            break;
                        }

                        try { // Connect to server (valid: 127.0.0.1, 12345)
                            socket = new Socket(command[1], Integer.valueOf(command[2]));
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            out = new PrintWriter(socket.getOutputStream(), true);
                        } catch (Exception e) { // Incorrect IP and/or port number error
                            errorString = "Error: Connection to the Server has failed! Please check IP Address and Port Number.";
                            break;
                        }

                        out.println(userInput); // Send valid command to server
                        System.out.println(in.readLine()); // Receive response from server

                    } else if ("/leave".equals(command[0])) { // Leave server
                        if (command.length != 1) { // Command must have only 1 argument
                            errorString = "Error: Command parameters do not match or is not allowed.";
                            break;
                        }

                        try {
                            out.println(userInput); // Send valid command to server
                            System.out.println(in.readLine()); // Receive response from server
                            
                            socket.close(); // Close connection
                        } catch (Exception e) {
                            System.out.println("Error: Disconnection failed. Please connect to the server first.");
                            break;
                        } 
                    }  else if ("/?".equals(command[0])) { // View command list
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
