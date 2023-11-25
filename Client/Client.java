package Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static String username = "User";

    public static void main(String[] args) {
        String userInput = "";
        String errorString = "";

        Boolean isConnected = false; // for register, store, fetch, dir
        Boolean isRegistered = false; // for store, fetch, dir

        File folder = new File("./dir");
        File[] listOfFiles = folder.listFiles();

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
                        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        out = new DataOutputStream(socket.getOutputStream());

                    } catch (Exception e) { // Incorrect IP and/or port number error
                        errorString = "Error: Connection to the Server has failed! Please check IP Address and Port Number.";
                        continue;
                    }

                    out.writeUTF(userInput); // Send join command to server (string)
                    System.out.println(in.readUTF()); // Receive response from server
                    isConnected = true;

                } else if ("/leave".equals(command[0])) { // Leave server
                    if (command.length != 1) { // Command must have only 1 argument
                        errorString = "Error: Command parameters do not match or is not allowed.";
                        continue;
                    }

                    try {
                        out.writeUTF(userInput); // Send leave command to server
                        System.out.println(in.readUTF()); // Receive response from server

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

                    if (!isConnected) { // Client is not connected to the server
                        errorString = "Error: Disconnection failed. Please connect to the server first."; // TODO:
                                                                                                          // misleading
                                                                                                          // comment
                        continue;
                    }

                    if (isRegistered) { // Client is already registered
                        errorString = "Error: Disconnection failed. Please connect to the server first."; // TODO:
                                                                                                          // misleading
                                                                                                          // comment ->
                                                                                                          // can a
                                                                                                          // client
                                                                                                          // register
                                                                                                          // twice?
                                                                                                          // (after
                                                                                                          // leaving
                                                                                                          // then
                                                                                                          // rejoining)
                        continue;
                    }

                    try {
                        out.writeUTF(userInput); // Send register command to server
                        System.out.println(in.readUTF()); // Receive response from server

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

                    if (!isConnected || !isRegistered) {
                        errorString = "Error: Disconnection failed. Please connect to the server first."; // TODO:
                                                                                                          // misleading
                                                                                                          // comment,
                                                                                                          // must be
                                                                                                          // connected
                                                                                                          // to server
                                                                                                          // and
                                                                                                          // registered
                                                                                                          // to view
                                                                                                          // directory
                                                                                                          // files
                        continue;
                    }

                    try {
                        out.writeUTF(userInput); // Send dir command to server
                        String[] dirString = in.readUTF().split(","); // Receive response from server then split (file
                                                                      // names are delimited by ',')

                        for (int i = 0; i < dirString.length; i++) {
                            System.out.println(dirString[i]); // Print file names
                            if (i == 0)
                                System.out.print("\n");
                        }

                    } catch (Exception e) {
                        System.out.println("Error: Registration failed. Handle or alias already exists. /c"); // TODO: i
                                                                                                              // dont
                                                                                                              // know
                                                                                                              // what
                                                                                                              // error i
                                                                                                              // should
                                                                                                              // put
                                                                                                              // here
                                                                                                              // what if
                                                                                                              // none
                        continue;
                    }

                } else if ("/store".equals(command[0])) { // Send file to server

                    if (command.length != 2) { // Command must have only 2 arguments
                        errorString = "Error: Command parameters do not match or is not allowed.";
                        continue;
                    }

                    Boolean fileFound = false;

                    for (int i = 0; i < listOfFiles.length; i++) {

                        // Search for file by name in client dir
                        if (listOfFiles[i].getName().equals(command[1])) {

                            // System.out.println("Found!"); // debug

                            fileFound = true; // Set fileFound state
                            break; // Break out of loop after finding file
                        }
                    }

                    if (!fileFound) { // Unsuccessful sending of a file that does not exist in dir
                        System.out.println("Error: File not found.");
                        continue;

                    }

                    out.writeUTF(userInput); // Send "/store" command to server

                    File sendFile = new File("./dir/" + command[1]);
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

                    System.out.println(in.readUTF()); // Receive server response

                } else if ("/fetch".equals(command[0])) { // Fetch file from server
                    if (command.length != 2) { // Command must have only 2 arguments
                        errorString = "Error: Command parameters do not match or is not allowed.";
                        continue;
                    }

                    out.writeUTF(userInput); // Send "/fetch" command to server
                    
                    if (!in.readBoolean()) { // Check if file name exists
                        errorString = "Error: File not found in the server.";
                        continue;
                    }
                    
                    // Create a new file with the given fileName
                    String fileName = command[1];
                    File receivedFile = new File("./dir/" + fileName);
                    FileOutputStream fos = new FileOutputStream(receivedFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    
                    long totalBytes = in.readLong(); // Receive file size from server
                    long totalBytesRead = 0;
                    
                    while ((bytesRead = in.read(buffer)) != 0) { // Write to new file
                        bos.write(buffer, 0, bytesRead);
                        
                        totalBytesRead += bytesRead;
                        if (totalBytes == totalBytesRead) 
                        break;
                    }
                    // Close streams
                    bos.close();
                    fos.close();
                    
                    System.out.println("File received from Server: " + fileName);
                    
                } else if ("/?".equals(command[0])) { // View commands
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

                // Refresh inputs and error strings
                userInput = "";
                errorString = "";
                command = new String[0];
            } while (true);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scan.close();
        }
    }
}
