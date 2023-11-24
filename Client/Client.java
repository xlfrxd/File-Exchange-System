package Client;

import java.io.BufferedReader;
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

        Boolean isConnected = false; // for register, store, fetch, dir
        Boolean isRegistered = false; // for store, fetch, dir

        Scanner scan = new Scanner(System.in);
        try {
            do {
                // Print errors
                if (!(errorString.isEmpty())) {
                    System.out.println(errorString);
                    errorString = "";
                }

                System.out.print(username + ": ");

                scan = new Scanner(System.in);
                userInput = scan.nextLine();

                String[] command = userInput.split("\\s+");
                if ("/join".equals(command[0])) { // Join server via ip and port

                    if (command.length != 3) { // Command must have ip address and port as arguments
                        errorString = "Error: Command parameters do not match or is not allowed.";
                        continue;
                    }

                    try { // Connect to server (valid: 127.0.0.1, 12345)
                        socket = new Socket(command[1], Integer.valueOf(command[2]));
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(socket.getOutputStream(), true);

                    } catch (Exception e) { // Incorrect IP and/or port number error
                        errorString = "Error: Connection to the Server has failed! Please check IP Address and Port Number.";
                        continue;
                    }

                    out.println(userInput); // Send join command to server
                    System.out.println(in.readLine()); // Receive response from server
                    isConnected = true;

                } else if ("/leave".equals(command[0])) { // Leave server
                    if (command.length != 1) { // Command must have only 1 argument
                        errorString = "Error: Command parameters do not match or is not allowed.";
                        continue;
                    }

                    try {
                        out.println(userInput); // Send leave command to server
                        System.out.println(in.readLine()); // Receive response from server

                        socket.close(); // Close connection
                    } catch (Exception e) {
                        errorString = "Error: Disconnection failed. Please connect to the server first.";
                        continue;
                    }
                    isConnected = false;
                } else if ("/register".equals(command[0])) { // Register client to server

                    if (command.length != 2) { // Command must have only 2 arguments
                        errorString = "Error: Command parameters do not match or is not allowed.";
                        continue;
                    }
 
                    if(!isConnected){ // Client is not connected to the server
                        errorString = "Error: Disconnection failed. Please connect to the server first."; // TODO: misleading comment
                        continue;
                    }

                    if(isRegistered) { // Client is already registered
                        errorString = "Error: Disconnection failed. Please connect to the server first."; // TODO: misleading comment -> can i client register twice?
                        continue;
                    }

                    try {
                        out.println(userInput); // Send register command to server
                        System.out.println(in.readLine()); // Receive response from server

                        isRegistered = true;
                    } catch (Exception e) {
                        System.out.println("Error: Registration failed. Handle or alias already exists. /c");
                        continue;
                    }

                } else if ("/dir".equals(command[0])) { // View server directory

                    if (command.length != 1) { // Command must have only 2 arguments
                        errorString = "Error: Command parameters do not match or is not allowed.";
                        continue;
                    }
                    
                    if(!isConnected || !isRegistered){
                        errorString = "Error: Disconnection failed. Please connect to the server first."; // TODO: misleading comment, must be connected to server and 
                                                                                                          // registered to view directory files
                        continue;
                    }

                    try {
                        out.println(userInput); // Send dir command to server
                        System.out.println(in.readLine()); // Receive response from server

                    } catch (Exception e) {
                        System.out.println("Error: Registration failed. Handle or alias already exists. /c"); // TODO: i dont know what error i should put here what if none
                        continue;
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
                        errorString = "Error: Command parameters do not match or are not allowed.";
                    }
                } else { // Unknown or wrong syntax error
                    errorString = "Error: Command not found.";
                }
            } while (true);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scan.close();
        }
    }
}
