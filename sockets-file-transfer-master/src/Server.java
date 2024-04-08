package com.hse.bse163.shakin; // Declares the package name

import java.io.*; // Imports all classes in the java.io package
import java.net.*; // Imports all classes in the java.net package
import java.util.*; // Imports all classes in the java.util package

public class Server { // Declares a public class named Server
    /**
     * @param args first argument — directory path (if no directory path — throws error)
     *             second argument — port number (default 8888)
     */
    public static void main(String[] args) throws Exception { // Declares the main method
        String serverDirectory; // Declares a String variable named serverDirectory
        if (args.length == 0) { // If there are no command-line arguments
            System.out.println("Enter directory as the first command line argument and port as the second (Default port: 8888)"); // Prints a message to the console
        } else { // If there are command-line arguments

            ServerSocket listenerSocket; // Declares a ServerSocket variable named listenerSocket
            serverDirectory = args[0]; // Assigns the first command-line argument to serverDirectory
            System.out.println("Server has started"); // Prints a message to the console
            System.out.println("Waiting for clients"); // Prints a message to the console

            if (args.length >= 2) // If there are at least two command-line arguments
                listenerSocket = new ServerSocket(Integer.parseInt(args[1])); // Creates a new ServerSocket object with the second command-line argument as the port number and assigns it to listenerSocket
            else // If there is only one command-line argument
                listenerSocket = new ServerSocket(8888); // Creates a new ServerSocket object with 8888 as the port number and assigns it to listenerSocket

            try {
                int id = 1; // Declares an integer variable named id and initializes it to 1
                while (true) { // While loop that runs indefinitely
                    Socket clientSocket = listenerSocket.accept(); // Accepts a connection from a client and assigns the resulting Socket object to clientSocket

                    System.out.println("Client with ID " + id + " connected from " + clientSocket.getInetAddress().getHostName()); // Prints a message to the console
                    Thread server = new Handler(clientSocket, serverDirectory); // Creates a new Handler object with clientSocket and serverDirectory as the arguments and assigns it to server
                    id++; // Increments id by 1
                    server.start(); // Starts the server thread
                }
            } catch (Exception ex) { // Catches any exceptions
                try {
                    listenerSocket.close(); // Closes the listenerSocket
                } catch (IOException e) { // Catches any IOExceptions
                    System.out.println("SocketClose: " + e.getMessage()); // Prints a message to the console
                }
            }
        }
    }
}


class Handler extends Thread { // Declares a class named Handler that extends Thread

    private final Socket clientSocket; // Declares a final Socket variable named clientSocket
    private final File serverDirectory; // Declares a final File variable named serverDirectory

    public Handler(Socket socket, String directory) { // Constructor for the Handler class
        clientSocket = socket; // Assigns the socket argument to clientSocket
        serverDirectory = new File(directory); // Creates a new File object with the directory argument as the path and assigns it to serverDirectory
        System.out.println("Next Started ServerDirectory = " + serverDirectory); // Prints a message to the console
    }

    /**
     * Works with client (Allows to download files)
     */
    public void run() { // Overrides the run method of the Thread class
        try {
            InputStream clientInpStream = clientSocket.getInputStream(); // Gets the input stream of the clientSocket and assigns it to clientInpStream
            BufferedReader in = new BufferedReader(new InputStreamReader(clientInpStream)); // Creates a new BufferedReader object and assigns it to in
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream()); // Gets the output stream of the clientSocket, creates a new DataOutputStream object with it, and assigns it to output
            PrintWriter clientPW = new PrintWriter(output, true); // Creates a new PrintWriter object and assigns it to clientPW

            clientPW.println("Connection Established"); // Sends a message to the client

            ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(serverDirectory.list())); // Gets the list of filenames in the serverDirectory, converts it to an array, and assigns it to fileNames
            int filesNumber = fileNames.size(); // Gets the size of fileNames and assigns it to filesNumber
            clientPW.println(filesNumber); // Sends filesNumber to the client

            for (String name : fileNames) // For each name in fileNames
                clientPW.println(name); // Sends name to the client

            while (true) { // While loop that runs indefinitely
                String name = in.readLine(); // Reads a line from the input stream and assigns it to name
                char ch = name.charAt(0); // Gets the first character of name and assigns it to ch

                long fileLength = 0; // Declares a long variable named fileLength and initializes it to 0

                if (ch == '*') { // If ch is '*'
                    int n = name.length() - 1; // Gets the length of name, subtracts 1, and assigns it to n
                    String fileName = name.substring(1, n); // Gets a substring of name from index 1 to n and assigns it to fileName

                    FileInputStream fileReader; // Declares a FileInputStream variable named fileReader
                    BufferedInputStream bufFileReader = null; // Declares a BufferedInputStream variable named bufFileReader and initializes it to null

                    boolean fileExists = true; // Declares a boolean variable named fileExists and initializes it to true
                    System.out.println("Request to download file " + fileName + " received from Client: " + clientSocket.getInetAddress().getHostName()); // Prints a message to the console
                    fileName = serverDirectory + File.separator + fileName; // Appends fileName to serverDirectory and assigns it to fileName

                    File fileToDown = new File(fileName); // Creates a new File object named fileToDown

                    System.out.println("File to download full name = " + fileName + "; fileToString = " + fileToDown); // Prints a message to the console

                    try {
                        fileLength = fileToDown.length(); // Gets the length of fileToDown and assigns it to fileLength
                        fileReader = new FileInputStream(fileName); // Creates a new FileInputStream object with fileName as the argument and assigns it to fileReader
                        bufFileReader = new BufferedInputStream(fileReader); // Creates a new BufferedInputStream object with fileReader as the argument and assigns it to bufFileReader
                    } catch (FileNotFoundException e) { // Catches any FileNotFoundExceptions
                        fileExists = false; // Sets fileExists to false
                        System.out.println("FileNotFoundException:" + e.getMessage()); // Prints a message to the console
                    }
                    if (fileExists) { // If fileExists is true
                        clientPW.println("Success"); // Sends a message to the client
                        clientPW.println(fileLength); // Sends fileLength to the client
                        System.out.println("Download begins"); // Prints a message to the console

                        Utility.sendBytes(bufFileReader, output); // Calls the sendBytes method of the Utility class
                        System.out.println("Completed"); // Prints a message to the console

                    } else // If fileExists is false
                        clientPW.print("FileNotFound"); // Sends a message to the client
                }
            }

        } catch (Exception ex) { // Catches any exceptions
            System.out.println(ex.getMessage()); // Prints the exception message to the console
        } finally { // Finally block
            try {
                clientSocket.close(); // Closes the clientSocket
            } catch (IOException e) { // Catches any IOExceptions
                System.out.println("SocketClose: " + e.getMessage()); // Prints a message to the console
            }
        }
    }


}



//        int size = 9022386;
//        byte[] data = new byte[fileLength];
//        int bytes = 0;
//        int cnt = in.read(data, 0, data.length);
//        out.write(data, 0, fileLength);
//        out.flush();


//else{
//        try {
//        boolean isEnded = true;
//        System.out.println("Request to upload file " + name + " received from " + clientSocket.getInetAddress().getHostName() + "...");
//
//        File directory = new File(serverDirectory);
//        if (!directory.exists()) {
//        System.out.println("Directory made");
//        directory.mkdir();
//        }
//
//        fileLength =  in.read();
////                    int size = 9022386;
//        byte[] data = new byte[fileLength];
//        File fileToUp = new File(directory, name);
//        FileOutputStream fileOut = new FileOutputStream(fileToUp);
//        DataOutputStream dataOut = new DataOutputStream(fileOut);
//
//        while (isEnded) {
//        int buf;
//        buf = clientInpStream.read(data, 0, data.length);
//        if (buf == -1) {
//        isEnded = false;
//        System.out.println("Completed");
//        } else {
//        dataOut.write(data, 0, buf);
//        dataOut.flush();
//        }
//        }
//        fileOut.close();
//        } catch (Exception exc) {
//        System.out.println(exc.getMessage());
//        }
//        }
