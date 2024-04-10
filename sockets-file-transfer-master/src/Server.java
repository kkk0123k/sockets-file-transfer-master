package com.hse.bse163.shakin; // Declares the package name

import javax.swing.*;
import java.io.*; // Imports all classes in the java.io package
import java.net.*; // Imports all classes in the java.net package
import java.nio.charset.StandardCharsets;
import java.util.*; // Imports all classes in the java.util package
import java.util.concurrent.CountDownLatch;

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
    private static DESAlgorithm des;
    private static String textFromFile;
    private final Socket clientSocket; // Declares a final Socket variable named clientSocket
    private final File serverDirectory; // Declares a final File variable named serverDirectory
    private PrintWriter pw;

    public Handler(Socket socket, String directory) { // Constructor for the Handler class
        clientSocket = socket; // Assigns the socket argument to clientSocket
        try {
            this.pw = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverDirectory = new File(directory); // Creates a new File object with the directory argument as the path and assigns it to serverDirectory
        System.out.println("Next Started ServerDirectory = " + serverDirectory); // Prints a message to the console
    }

    /**
     * Works with client (Allows to download files)
     */
    public void run() {
        try {
            InputStream clientInpStream = clientSocket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientInpStream));
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            PrintWriter clientPW = new PrintWriter(output, true);

            clientPW.println("Connection Established");

            ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(Objects.requireNonNull(serverDirectory.list())));
            int filesNumber = fileNames.size();
            clientPW.println(filesNumber);

            for (String name : fileNames)
                clientPW.println(name);

            boolean isFileTransferInProgress = false;

            // New variables to hold the name of the file being transferred and the BufferedWriter for the file
            String fileName = "";
            final StringBuilder fileContent = new StringBuilder();
            final File[] directoryToSaveFile = new File[1]; // Initialize a final one-element array to hold the directory chosen by the server

            while (true) {
                String name = in.readLine();
                char ch = 0;
                if (name != null && !name.isEmpty()) {
                    ch = name.charAt(0);
                }
                System.out.println("Received name: " + name);
                if (!isFileTransferInProgress) {
                    long fileLength = 0;
                    if (ch == '*') {
                        int n = name.length() - 1;
                        fileName = name.substring(1, n);

                        FileInputStream fileReader;
                        BufferedInputStream bufFileReader = null;

                        boolean fileExists = true;
                        System.out.println("Request to download file " + fileName + " received from Client: " + clientSocket.getInetAddress().getHostName());
                        fileName = serverDirectory + File.separator + fileName;

                        File fileToDown = new File(fileName);

                        System.out.println("File to download full name = " + fileName + "; fileToString = " + fileToDown);

                        try {
                            fileLength = fileToDown.length();
                            fileReader = new FileInputStream(fileName);
                            bufFileReader = new BufferedInputStream(fileReader);
                        } catch (FileNotFoundException e) {
                            fileExists = false;
                            System.out.println("FileNotFoundException:" + e.getMessage());
                        }
                        if (fileExists) {
                            clientPW.println("Success");
                            clientPW.println(fileLength);
                            System.out.println("Download begins");

                            Utility.sendBytes(bufFileReader, output);
                            System.out.println("Completed");

                        } else
                            clientPW.print("FileNotFound");
                    }
                }

                if (name.startsWith("FILE_TRANSFER_REQUEST")&& !isFileTransferInProgress) {
                    isFileTransferInProgress = true;
                    fileName = name.substring(21);

                    String finalFileName = fileName;
                    final CountDownLatch latch = new CountDownLatch(1); // Create a CountDownLatch with a count of 1

                    SwingUtilities.invokeLater(() -> {
                        int result = JOptionPane.showConfirmDialog(null, "Do you want to receive a file?", "File Transfer", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            pw.println("ACCEPTED");

                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            int returnValue = fileChooser.showOpenDialog(null);
                            if (returnValue == JFileChooser.APPROVE_OPTION) {
                                directoryToSaveFile[0] = fileChooser.getSelectedFile(); // Save the directory chosen by the server
                                pw.println("READY_TO_RECEIVE");
                            }
                        } else {
                            pw.println("REJECTED");
                        }
                        latch.countDown(); // Decrement the count of the latch, releasing all waiting threads
                    });

                    latch.await(); // Wait here until the latch count reaches zero

                } else if (isFileTransferInProgress) {
                    if ("END_OF_FILE".equals(name)) {
                        isFileTransferInProgress = false;
                        pw.println("FILE_RECEIVED");

                        // Display a dialog to enter the decryption key
                        String key = JOptionPane.showInputDialog("Enter the decryption key:");

                        // Decrypt the file content
                        String decryptedContent = des_decrypt(fileContent.toString(), key, directoryToSaveFile[0].getPath() + File.separator + fileName);

                        // Write the decrypted content to the file in the directory chosen by the server
                        BufferedWriter writer = new BufferedWriter(new FileWriter(directoryToSaveFile[0].getPath() + File.separator + fileName));
                        writer.write(decryptedContent);
                        writer.close();

                    } else {
                        System.out.println("Writing line to file: " + name);
                        // Instead of writing to the file, append to the StringBuilder
                        fileContent.append(name).append("\n");
                        System.out.println("Line written to file");
                    }
                }

            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("SocketClose: " + e.getMessage());
            }
        }
    }

    //this method decrypt data using des algorithm
    public static String des_decrypt(String cipher_text, String key, String filePath) {
        des = new DESAlgorithm(key);

        String[] cipher_text_array = doECB(doPadding(cipher_text));
        String plain_block;
        StringBuffer sb = new StringBuffer();

        for (String s : cipher_text_array) {
            plain_block = des.decrypt(s);
            sb.append(plain_block);
        }

        String gen_plain_text = new String(sb);
        System.out.println("Decrypted text before trim: '" + gen_plain_text + "'"); // Print the decrypted text before trimming
        gen_plain_text = gen_plain_text.trim(); // Trim trailing spaces
        System.out.println("Decrypted text after trim: '" + gen_plain_text + "'"); // Print the decrypted text after trimming
        writeDataInAFile(gen_plain_text, filePath); // Save the decrypted content to the original file in the server directory
        System.out.println("This generated plain text is written in " + filePath + " file");

        return gen_plain_text; // Return the decrypted text
    }


    //this method write data into output file
    public static void writeDataInAFile(String text, String fileName) {

        byte[] buffer = text.getBytes(StandardCharsets.UTF_8);

        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(buffer);
            fos.close();
        } catch (IOException ex) {
            System.out.println("File not found");
        }
    }

    //this method do required padding for des algorithm
    public static String doPadding(String input_text) {
        if (input_text.length() % 8 != 0) {

            int paddingLength = 8 - input_text.length() % 8;
            for (int i = 0; i < paddingLength; i++) {
                input_text = input_text.concat(" ");
            }
        } else {
            return input_text;
        }
        return input_text;
    }

    //this method perform Electronic Code Book mode in DES Implementation
    public static String[] doECB(String plain_text) {
        int start = 0, end = 8;
        int noOfBlock = plain_text.length() / 8;
        String temp;
        String[] text_array = new String[noOfBlock];

        for (int i = 0; i < noOfBlock; i++) {
            temp = plain_text.substring(start, end);
            text_array[i] = temp;
            start = end;
            end = end + 8;
        }
        return text_array;
    }

    public static boolean readDataFromAFile(String fileName) {

        StringBuffer bf = new StringBuffer();
        try {
            File file = new File(fileName);
            int fileLength = (int)file.length();
            if(fileLength == 0){
                System.out.println("no file or text is found in " + fileName);
                return false;
            }

            byte[] buffer = new byte[fileLength];
            FileInputStream fis = new FileInputStream(fileName);

            int nRead;
            while((nRead = fis.read(buffer)) != -1) {
                bf.append(new String(buffer, StandardCharsets.UTF_8));
            }

            fis.close();

        } catch (IOException ex) {
            System.out.println(fileName + " file is not found");
            return false;
        }

        textFromFile = new String(bf);
        return true;
    }
}
