package Client;

import java.io.BufferedReader;
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

                        try { // Connect to server (valid: 127.0.0.1, 12345)
                            socket = new Socket(command[1], Integer.valueOf(command[2]));
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            out = new PrintWriter(socket.getOutputStream(), true);
                        } catch (Exception e) { // Incorrect IP and/or port number error
                            errorString = "Error: Connection to the Server has failed! Please check IP Address and Port Number.";
                            break;
                        }
                        
                        out.println(userInput); // Send valid command to server
                    } else if ("/leave".equals(command[0])) { // Leave server
                        if (command.length != 1) { // Command must have only 1 argument
                            errorString = "Error: Command parameters do not match or is not allowed.";
                            break;
                        }

                        out.println(userInput); // Send valid command to server

                        socket.close();
                    } else if ("/?".equals(command[0])) { // View command list
                        System.out.println("Viewed commands");
                    } else { // Unknown or wrong syntax error
                        errorString = "Error: Command not found.";
                        break;
                    }

                    // Print server messages/responses
                    if (in != null)
                        System.out.println(in.readLine());
                } while ((userInput != null));

                // Print errors 
                if (!(errorString.isEmpty())) {
                    System.out.println(errorString);
                    errorString = "";
                }

                if (in != null && out != null && socket != null) {
                    in.close();
                    out.close();
                    scan.close();
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
