package com.hse.bse163.shakin; // Declares the package name

import javafx.concurrent.Task; // Imports the Task class from the javafx.concurrent package

import java.io.*; // Imports all classes in the java.io package
import java.net.*; // Imports all classes in the java.net package
import java.lang.*; // Imports all classes in the java.lang package
import java.awt.*; // Imports all classes in the java.awt package
import java.awt.event.*; // Imports all classes in the java.awt.event package
import javax.swing.*; // Imports all classes in the javax.swing package
import java.nio.charset.StandardCharsets;
import java.util.Arrays; // Imports the Arrays class from the java.util package
import java.util.HashSet; // Imports the HashSet class from the java.util package
import java.util.Objects;
import java.util.Scanner;

class Client extends JFrame implements ActionListener, MouseListener { // Declares a class named Client that extends JFrame and implements ActionListener and MouseListener

    private JPanel panel; // Declares a private JPanel object named panel
    private JLabel errorLabel; // Declares a private JLabel object named errorLabel
    private JLabel filesFolderLabel; // Declares a private JLabel object named filesFolderLabel
    private JTextField fileNameText; // Declares a private JTextField object named fileNameText
    private JButton downloadButton, folderLocation, sendButton; // Declares two private JButton objects named downloadButton and folderLocation
    private File clientDirectory; // Declares a private File object named clientDirectory
    private PrintWriter pw; // Declares a private PrintWriter object named pw
    private DataInputStream inputData; // Declares a private DataInputStream object named inputData
    private DataOutputStream output;

    private String name; // Declares a private String object named name
    private JList<String> filesList; // Declares a private JList object named filesList
    private HashSet<String> names; // Declares a private HashSet object named names
    private HashSet<String> clientFileNames; // Declares a private HashSet object named clientFileNames

    private BufferedReader in; // Declares a private BufferedReader object named in
    Scanner sc = new Scanner(System.in);

    private final JFileChooser fileChooser = new JFileChooser(); // Declares a final JFileChooser object named fileChooser
    private static DESAlgorithm des;
    private static String textFromFile;
    String key, plain_text, cipher_text;

    public Client() { // Constructor for the Client class
        super("Simple Torrent"); // Calls the constructor of the superclass JFrame with the title "Simple Torrent"
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Sets the default close operation to EXIT_ON_CLOSE
    }

    /**
     * initialize GUI
     */
    void initGUI() { // Declares a method named initGUI
        panel = new JPanel(null); // Initializes the panel object as a new JPanel object with a null layout

        Font titleFont = new Font("Helvetica", Font.BOLD, 30); // Creates a new Font object named titleFont
        JLabel titleLabel = new JLabel("Simple Torrent"); // Creates a new JLabel object named titleLabel
        titleLabel.setFont(titleFont); // Sets the font of the titleLabel to titleFont
        titleLabel.setBounds(300, 50, 700, 40); // Sets the size and position of the titleLabel
        panel.add(titleLabel); // Adds the titleLabel to the panel

        folderLocation = new JButton("Show client files"); // Initializes the folderLocation object as a new JButton object with the text "Show client files"
        folderLocation.setBounds(550, 130, 150, 40); // Sets the size and position of the folderLocation button
        panel.add(folderLocation); // Adds the folderLocation button to the panel

        Font labelFont = new Font("Helvetica Neue", Font.PLAIN, 20); // Creates a new Font object named labelFont
        JLabel fNameInpLabel = new JLabel("Enter File Name :"); // Creates a new JLabel object named fNameInpLabel
        fNameInpLabel.setFont(labelFont); // Sets the font of the fNameInpLabel to labelFont
        fNameInpLabel.setBounds(100, 650, 200, 50); // Sets the size and position of the fNameInpLabel
        panel.add(fNameInpLabel); // Adds the fNameInpLabel to the panel

        fileNameText = new JTextField(); // Initializes the fileNameText object as a new JTextField object
        fileNameText.setBounds(310, 650, 500, 50); // Sets the size and position of the fileNameText field
        panel.add(fileNameText); // Adds the fileNameText field to the panel

        downloadButton = new JButton("Download"); // Initializes the downloadButton object as a new JButton object with the text "Download"
        downloadButton.setBounds(550, 750, 200, 50); // Sets the size and position of the downloadButton
        panel.add(downloadButton); // Adds the downloadButton to the panel

        sendButton = new JButton("Send");
        sendButton.setBounds(300, 750, 200, 50); // Adjust the position as needed
        panel.add(sendButton);

        errorLabel = new JLabel(""); // Initializes the errorLabel object as a new JLabel object with no text
        errorLabel.setFont(labelFont); // Sets the font of the errorLabel to labelFont
        errorLabel.setBounds(200, 750, 600, 50); // Sets the size and position of the errorLabel
        panel.add(errorLabel); // Adds the errorLabel to the panel

        sendButton.addActionListener(this);
        downloadButton.addActionListener(this); // Adds an action listener to the downloadButton
        folderLocation.addActionListener(this); // Adds an action listener to the folderLocation button
    }

    /**
     * Connects to the server and starts the client GUI
     */
    private void run() { // Declares a private method named run
        clientDirectory = fileChooser.getCurrentDirectory(); // Gets the current directory of the file chooser and assigns it to clientDirectory

        System.out.println("At Start ClientDirectory = " + clientDirectory.toString()); // Prints the clientDirectory to the console

        StringBuilder hostBuild = new StringBuilder(); // Creates a new StringBuilder object named hostBuild
        int portNumber = Utility.getHostPort(hostBuild); // Calls the getHostPort method of the Utility class and assigns the returned value to portNumber
        String hostAddr = hostBuild.toString(); // Converts the hostBuild to a string and assigns it to hostAddr

        initGUI(); // Calls the initGUI method

        try {
            Socket clientSocket = new Socket(hostAddr, portNumber); // Creates a new Socket object named clientSocket
            InputStream inFromServer = clientSocket.getInputStream(); // Gets the input stream of the clientSocket and assigns it to inFromServer
            pw = new PrintWriter(clientSocket.getOutputStream(), true); // Creates a new PrintWriter object named pw
            in = new BufferedReader(new InputStreamReader(inFromServer)); // Creates a new BufferedReader object named in
            inputData = new DataInputStream(inFromServer); // Creates a new DataInputStream object named inputData
            output = new DataOutputStream(clientSocket.getOutputStream());

            String successIndicator = in.readLine(); // Reads a line from the input stream and assigns it to successIndicator
            System.out.println(successIndicator); // Prints the successIndicator to the console

            int len = Integer.parseInt(in.readLine()); // Reads a line from the input stream, converts it to an integer, and assigns it to len
            System.out.println(len); // Prints len to the console

            String[] temp_names = new String[len]; // Creates a new String array named temp_names with a length of len
            names = new HashSet<>(); // Initializes the names object as a new HashSet object

            for (int i = 0; i < len; i++) { // For loop from 0 to len
                String filename = in.readLine(); // Reads a line from the input stream and assigns it to filename
                System.out.println(filename); // Prints filename to the console
                names.add(filename); // Adds filename to the names set
                temp_names[i] = filename; // Assigns filename to the i-th element of temp_names
            }

            Arrays.sort(temp_names); // Sorts the temp_names array

            filesFolderLabel = new JLabel("Files in the SERVER Directory :"); // Creates a new JLabel object named filesFolderLabel
            filesFolderLabel.setBounds(350, 125, 400, 50); // Sets the size and position of the filesFolderLabel
            panel.add(filesFolderLabel); // Adds the filesFolderLabel to the panel

            filesList = new JList<>(temp_names); // Creates a new JList object named filesList
            JScrollPane scroll = new JScrollPane(filesList); // Creates a new JScrollPane object named scroll
            scroll.setBounds(300, 200, 600, 400); // Sets the size and position of the scroll pane

            panel.add(scroll); // Adds the scroll pane to the panel
            filesList.addMouseListener(this); // Adds a mouse listener to the filesList

            updateFileNames(); // Calls the updateFileNames method

        } catch (Exception e) { // Catches any exceptions
            System.out.println("Exception: " + e.getMessage()); // Prints the exception message to the console
            errorLabel.setText("Exception:" + e.getMessage()); // Sets the text of the errorLabel to the exception message
            errorLabel.setBounds(300, 125, 600, 50); // Sets the size and position of the errorLabel
            panel.revalidate(); // Revalidates the panel
        }

        getContentPane().add(panel); // Adds the panel to the content pane
        setVisible(true); // Makes the frame visible
    }


    /**
     * Handles double mouse click on the scrollpane
     * @param click
     */
    public void mouseClicked(MouseEvent click) { // Overrides the mouseClicked method of the MouseListener interface
        if (click.getClickCount() == 2) { // Checks if the click count is 2
            String selectedItem = filesList.getSelectedValue(); // Gets the selected value from the filesList and assigns it to selectedItem
            fileNameText.setText(selectedItem); // Sets the text of the fileNameText field to selectedItem
            panel.revalidate(); // Revalidates the panel
        }
    }


    public void mousePressed(MouseEvent e) { } // Overrides the mousePressed method of the MouseListener interface with an empty implementation

    public void mouseEntered(MouseEvent e) { } // Overrides the mouseEntered method of the MouseListener interface with an empty implementation

    public void mouseExited(MouseEvent e) { } // Overrides the mouseExited method of the MouseListener interface with an empty implementation

    public void mouseReleased(MouseEvent e) { } // Overrides the mouseReleased method of the MouseListener interface with an empty implementation

    /**
     * Updates list of file names of the last client directory
     */
    void updateFileNames() { // Declares a method named updateFileNames
        clientFileNames = new HashSet<>(Arrays.asList(Objects.requireNonNull(clientDirectory.list()))); // Gets the list of filenames in the clientDirectory, converts it to an array, and assigns it to clientFileNames
    }

    /**
     * Switches scroll pane contents between server files and client files
     */
    void updateFileList(boolean showServerFiles) { // Declares a method named updateFileList with a boolean parameter named showServerFiles
        String[] temp_names; // Declares a String array named temp_names
        if (!showServerFiles) { // If showServerFiles is false
            temp_names = clientFileNames.toArray(new String[0]); // Converts clientFileNames to an array and assigns it to temp_names
            folderLocation.setText("Show server files"); // Sets the text of the folderLocation button to "Show server files"
            filesFolderLabel.setText("Files in the CLIENT Directory :"); // Sets the text of the filesFolderLabel to "Files in the CLIENT Directory :"
        } else { // If showServerFiles is true
            temp_names = names.toArray(new String[0]); // Converts names to an array and assigns it to temp_names
            folderLocation.setText("Show client files"); // Sets the text of the folderLocation button to "Show client files"
            filesFolderLabel.setText("Files in the SERVER Directory :"); // Sets the text of the filesFolderLabel to "Files in the SERVER Directory :"
        }

        Arrays.sort(temp_names); // Sorts the temp_names array
        filesList.setListData(temp_names); // Sets the list data of the filesList to temp_names
    }

    /**
     * Runs Download of the file with showing it in the progress bar
     */
    private void runProgress(String successIndicator, long fileSize, File directory, Utility.ProgressDialog progress) throws IOException { // Declares a private method named runProgress with parameters successIndicator, fileSize, directory, and progress

        byte[] data = new byte[4096]; // Declares a byte array named data with a size of 4096

        if (successIndicator.equals("Success")) { // If successIndicator is "Success"
            File f = new File(directory, name); // Creates a new File object named f

            FileOutputStream fileOut = new FileOutputStream(f); // Creates a new FileOutputStream object named fileOut
            DataOutputStream dataOut = new DataOutputStream(fileOut); // Creates a new DataOutputStream object named dataOut

            System.out.println("File to save directory = " + f); // Prints the file to save directory to the console
            long totalDown = 0; // Declares a long variable named totalDown and initializes it to 0
            long remaining = fileSize; // Declares a long variable named remaining and initializes it to fileSize
            System.out.println("File size = " + fileSize); // Prints the file size to the console
            int cnt; // Declares an integer variable named cnt

            while ((cnt = inputData.read(data, 0, (int)Math.min(data.length, remaining))) > 0) { // While loop until cnt is less than or equal to 0

                System.out.println("Cnt = " + cnt); // Prints cnt to the console
                totalDown += cnt; // Adds cnt to totalDown
                remaining -= cnt; // Subtracts cnt from remaining
                final int percent = (int) (totalDown * 100 / fileSize); // Calculates the percentage of the download progress and assigns it to percent

                System.out.println("Total = " + totalDown); // Prints totalDown to the console
                System.out.println("Remain = " + remaining); // Prints remaining to the console
                System.out.println("Percent = " + percent); // Prints percent to the console

                SwingUtilities.invokeLater(() ->  progress.updateBar(percent)); // Updates the progress bar on the Event Dispatch Thread

                if (fileSize < 1e3) // If fileSize is less than 1e3
                    try {
                        Thread.sleep(500); // Makes the current thread sleep for 500 milliseconds
                    } catch (InterruptedException e) { // Catches an InterruptedException
                        e.printStackTrace(); // Prints the stack trace of the InterruptedException
                    }
                else if (fileSize < 1e6) // If fileSize is less than 1e6
                    try {
                        Thread.sleep(10); // Makes the current thread sleep for 10 milliseconds
                    } catch (InterruptedException e) { // Catches an InterruptedException
                        e.printStackTrace(); // Prints the stack trace of the InterruptedException
                    }

                dataOut.write(data, 0, cnt); // Writes cnt bytes from the data array to the dataOut stream
            }
            System.out.println("Completed"); // Prints "Completed" to the console
            errorLabel.setText("Completed"); // Sets the text of the errorLabel to "Completed"

            // If clientFileNames does not contain name
            clientFileNames.add(name); // Adds name to clientFileNames

            System.out.println("client directory before updating = " + clientDirectory); // Prints the client directory before updating to the console
            updateFileNames(); // Calls the updateFileNames method
            updateFileList(false); // Calls the updateFileList method with false as the argument
            fileOut.close(); // Closes the fileOut stream
        } else { // If successIndicator is not "Success"
            System.out.println("Requested file not found on the server."); // Prints "Requested file not found on the server." to the console
            errorLabel.setText("Requested file not found on the server."); // Sets the text of the errorLabel to "Requested file not found on the server."
            panel.revalidate(); // Revalidates the panel
        }
    }
    /**
     * handles all button click events
     */
    public void actionPerformed(ActionEvent event) { // Overrides the actionPerformed method of the ActionListener interface
        if (event.getSource() == folderLocation) { // If the source of the event is the folderLocation button
            updateFileList("Show server files".equals(folderLocation.getText())); // Calls the updateFileList method with the result of the comparison between the text of the folderLocation button and "Show server files" as the argument

        } else if (event.getSource() == downloadButton) { // If the source of the event is the downloadButton
            try {

                String fileChooseMsg = "Select directory to save file"; // Declares a String variable named fileChooseMsg and initializes it to "Select directory to save file"

                File bufDir; // Declares a File variable named bufDir
                Utility.configureFileChooser(fileChooser); // Calls the configureFileChooser method of the Utility class with fileChooser as the argument

                int ret = fileChooser.showDialog(null, fileChooseMsg); // Shows a dialog with fileChooseMsg as the title and assigns the return value to ret
                bufDir = clientDirectory; // Assigns clientDirectory to bufDir
                if (ret == JFileChooser.APPROVE_OPTION) // If ret is APPROVE_OPTION
                {
                    bufDir = fileChooser.getSelectedFile(); // Gets the selected file from the file chooser and assigns it to bufDir
                    System.out.println("file chooser curr dir after choosing directory = " + fileChooser.getCurrentDirectory()); // Prints the current directory of the file chooser after choosing directory to the console
                    System.out.println("file chooser select file after choosing directory = " + fileChooser.getSelectedFile()); // Prints the selected file of the file chooser after choosing directory to the console
                    System.out.println("Buf Dir after choosing directory = " + bufDir); // Prints bufDir after choosing directory to the console
                    clientDirectory = bufDir; // Assigns bufDir to clientDirectory
                }

                final File directory = bufDir; // Declares a final File variable named directory and initializes it to bufDir

                if (!directory.exists()) // If directory does not exist
                    directory.mkdir(); // Creates a directory

                System.out.println("directory after choosing directory = " + directory); // Prints directory after choosing directory to the console

                name = fileNameText.getText(); // Gets the text from the fileNameText field and assigns it to name
                String file = "*" + name + "*"; // Declares a String variable named file and initializes it to "*" + name + "*"

                inputData.skipBytes(inputData.available()); // Skips bytes in the inputData stream
                pw.println(file); // Prints file to the pw stream

                String s = in.readLine(); // Reads a line from the input stream and assigns it to s
                long fileSize = Long.parseLong(in.readLine()); // Reads a line from the input stream, converts it to a long, and assigns it to fileSize

                if (Utility.hasPermission(fileSize)) { // If the user has permission to download the file
                    final Utility.ProgressDialog progress = new Utility.ProgressDialog(); // Creates a new ProgressDialog object named progress


                    final Task task = new Task() { // Declares a final Task object named task
                        @Override
                        protected Object call() { // Overrides the call method of the Task class
                            progress.showDialog(); // Shows the dialog
                            return 0; // Returns 0
                        }
                    };

                    Thread someThread = new Thread(() -> { // Creates a new Thread object named someThread

                        try {
                            runProgress(s, fileSize, directory, progress); // Calls the runProgress method
                        } catch (IOException e) { // Catches an IOException

                        }
                        // Closes the dialog on the Event Dispatch Thread
                        SwingUtilities.invokeLater(progress::closeDialog);
                    });
                    someThread.start(); // Starts the someThread thread

                }

            } catch (Exception exc) { // Catches any exceptions
                System.out.println("Exception: " + exc.getMessage()); // Prints the exception message to the console
                errorLabel.setText("Exception:" + exc.getMessage()); // Sets the text of the errorLabel to the exception message
                panel.revalidate(); // Revalidates the panel
            }
        }
        else if (event.getSource() == sendButton) {
            // Code to upload a file to the server
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File fileToUpload = fileChooser.getSelectedFile();

                System.out.println("File selected: " + fileToUpload.getName());  // Print the name of the selected file
                
                // Start a new thread to send the file
                new Thread(() -> {
                    try {
                        System.out.println("Starting file transfer...");  // Print a message before starting the file transfer
                        sendFile(fileToUpload);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        }

    }

    // Client
    void sendFile(File file) throws IOException {
        // Start a new thread
        new Thread(() -> {
            try {
                // First, send a message to the server indicating that you want to send a file
                pw.println("FILE_TRANSFER_REQUEST" + file.getName());
                System.out.println("Sending file transfer request...");
                // Then, wait for the server's response
                System.out.println("Waiting for server response...");
                String serverResponse = in.readLine();
                System.out.println("Received response from server: " + serverResponse);

                if (serverResponse.equals("ACCEPTED")) {
                    // Wait for the server to be ready to receive the file
                    serverResponse = in.readLine();
                    if (!"READY_TO_RECEIVE".equals(serverResponse)) {
                        System.out.println("Server is not ready to receive the file");
                        return;
                    }

                    // Display a dialog to enter the encryption key
                    String key = JOptionPane.showInputDialog("Enter the encryption key:");

                    // Encrypt the file
                    if (readDataFromAFile(String.valueOf(file))) {
                        plain_text = textFromFile;
                    }
                    des_encrypt(plain_text, key);

                    // Send the encrypted file
                    File encryptedFile = new File("output.txt");
                    BufferedReader reader = new BufferedReader(new FileReader(encryptedFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        pw.println(line);
                        System.out.println("Sent line: " + line); // Prints the sent line to the console
                    }
                    pw.println("END_OF_FILE");  // Send "END_OF_FILE" without a newline character at the end
                    System.out.println("Sent END_OF_FILE"); // Prints a message to the console
                    reader.close();

                    // Wait for a response from the server after sending the "END_OF_FILE" message
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    if ("FILE_RECEIVED".equals(serverResponse)) {
                        System.out.println("File transfer successful");
                    } else {
                        System.out.println("File transfer failed");
                    }

                    // After sending the file, you can inform the user that the file transfer was successful
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "The server has accepted the file transfer.", "File Transfer", JOptionPane.INFORMATION_MESSAGE);
                    });
                } else if (serverResponse.equals("REJECTED")) {
                    // If the server rejected the file transfer, inform the user
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "The server has denied the file transfer.", "File Transfer", JOptionPane.WARNING_MESSAGE);
                    });
                }
            } catch (Exception e) {
                System.out.println("Exception in sendFile: " + e);
                e.printStackTrace();
            }
        }).start();
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

    //this method encrypt data using des algorithm
    public static void des_encrypt(String plain_text, String key) {
        des = new DESAlgorithm(key);

        String[] plain_text_array = doECB(doPadding(plain_text));
        String cipher_block;
        StringBuffer sb = new StringBuffer();

        for (String s : plain_text_array) {
            cipher_block = des.encrypt(s);
            sb.append(cipher_block);
        }

        String cipher_text = new String(sb);
        writeDataInAFile(cipher_text, "output.txt");
        //System.out.println("\nCipher text: " + cipher_text);
        System.out.println("This cipher text is written in output.txt file");
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

    public static void main(String[] args) {
        Client tcp = new Client();
        tcp.setSize(1000, 1000); // Adjust the size as needed
        tcp.run();
    }
}