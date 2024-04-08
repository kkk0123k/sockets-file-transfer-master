package com.hse.bse163.shakin; // Declares the package name

import javax.swing.*; // Imports all classes in the javax.swing package
import javax.swing.plaf.basic.BasicProgressBarUI; // Imports BasicProgressBarUI class from javax.swing.plaf.basic package
import java.awt.*; // Imports all classes in the java.awt package
import java.io.BufferedInputStream; // Imports BufferedInputStream class from java.io package
import java.io.DataOutputStream; // Imports DataOutputStream class from java.io package

public class Utility { // Declares a public class named Utility

    public static class ProgressDialog { // Declares a public static inner class named ProgressDialog

        private final JFrame frame = new JFrame(); // Creates a new JFrame object
        private final JDialog dialog = new JDialog(frame, "Download Progress", false); // Creates a new JDialog object
        private final JProgressBar progressBar = new JProgressBar(); // Creates a new JProgressBar object

        public ProgressDialog() { // Constructor for ProgressDialog class

            frame.setBounds(0, 0, 1000, 100); // Sets the size of the frame
            dialog.setBounds(0, 0, 800, 80); // Sets the size of the dialog
            frame.setUndecorated(true); // Removes the title bar from the frame

            progressBar.setMinimum(0); // Sets the minimum value of the progress bar
            progressBar.setMaximum(100); // Sets the maximum value of the progress bar
            progressBar.setValue(0); // Sets the initial value of the progress bar
            progressBar.setStringPainted(true); // Enables the painting of progress string

            progressBar.setForeground(new Color(210, 105, 24)); // Sets the foreground color of the progress bar

            progressBar.setPreferredSize(new Dimension(500, 20)); // Sets the preferred size of the progress bar

            progressBar.setUI(new ProgressUI()); // Sets the look and feel of the progress bar

            dialog.setUndecorated(true); // Removes the title bar from the dialog

            dialog.getContentPane().add(progressBar); // Adds the progress bar to the dialog
            dialog.pack(); // Sizes the dialog so that it fits around its subcomponents
            dialog.setDefaultCloseOperation(0); // Sets the operation that will happen by default when the user initiates a "close" on the dialog

            Toolkit kit = dialog.getToolkit(); // Gets the toolkit of the dialog
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); // Gets the local graphics environment
            GraphicsDevice[] gs = ge.getScreenDevices(); // Gets the screen devices
            Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration()); // Gets the screen insets
            Dimension d = kit.getScreenSize(); // Gets the screen size
            int max_width = (d.width - in.left - in.right); // Calculates the maximum width
            int max_height = (d.height - in.top - in.bottom); // Calculates the maximum height
            dialog.setLocation((max_width - dialog.getWidth()) / 2, (max_height - dialog.getHeight()) / 2); // Sets the location of the dialog

            dialog.setVisible(true); // Makes the dialog visible
            progressBar.setVisible(true); // Makes the progress bar visible
            dialog.setAlwaysOnTop(true); // Sets the dialog to always be on top
        }

        public void showDialog() { // Method to show the dialog
            dialog.setVisible(true); // Makes the dialog visible
            dialog.setAlwaysOnTop(true); // Sets the dialog to always be on top
        }

        public void closeDialog() { // Method to close the dialog
            if (dialog.isVisible()) { // Checks if the dialog is visible
                dialog.getContentPane().remove(progressBar); // Removes the progress bar from the dialog
                dialog.getContentPane().validate(); // Validates the dialog
                dialog.setVisible(false); // Makes the dialog invisible
            }
        }

        public void updateBar(int val) { // Method to update the progress bar
            progressBar.setValue(val); // Sets the value of the progress bar
        }

        public static class ProgressUI extends BasicProgressBarUI { // Declares a public static inner class named ProgressUI
            private Rectangle r = new Rectangle(); // Creates a new Rectangle object

            protected void paintIndeterminate(Graphics g, JComponent c) { // Method to paint the progress bar
                Graphics2D g2d = (Graphics2D) g; // Casts the Graphics object to Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Sets the rendering hint for antialiasing
                r = getBox(r); // Gets the box for the progress bar
                g.setColor(progressBar.getForeground()); // Sets the color of the Graphics object
                g.fillOval(r.x, r.y, r.width, r.height); // Fills an oval defined by the specified rectangle with the current color
            }
        }
    }

    public static void configureFileChooser(final JFileChooser fileChooser) { // Declares a public static method named configureFileChooser

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Sets the file selection mode to directories only
        fileChooser.setDialogTitle("Select directory to save file"); // Sets the title of the dialog
    }

    public static int getHostPort(StringBuilder host) { // Declares a public static method named getHostPort
        JTextField hostField = new JTextField(15); // Creates a new JTextField object for the host
        JTextField portField = new JTextField(15); // Creates a new JTextField object for the port

        JPanel hpPanel = new JPanel(); // Creates a new JPanel object
        JLabel hostLabel = new JLabel("Host:"); // Creates a new JLabel object for the host
        hpPanel.add(hostLabel); // Adds the host label to the panel
        hpPanel.add(hostField); // Adds the host field to the panel
        hpPanel.add(Box.createHorizontalStrut(50)); // Adds a horizontal strut to the panel
        hpPanel.add(new JLabel("Port:")); // Adds a port label to the panel
        hpPanel.add(portField); // Adds the port field to the panel

        boolean ok = false; // Initializes a boolean variable named ok
        String message = "Please Enter Host and Port"; // Initializes a string variable named message
        while (!ok) { // While loop until ok is true
            int result = JOptionPane.showConfirmDialog(null, hpPanel, message, JOptionPane.OK_CANCEL_OPTION); // Shows a confirm dialog
            ok = result == JOptionPane.OK_OPTION; // Sets ok to true if the result is OK_OPTION
            try {
                Integer.parseInt(portField.getText()); // Tries to parse the text of the port field to an integer
            } catch (NumberFormatException e) { // Catches a NumberFormatException
                ok = false; // Sets ok to false
                message = "Please Enter Host and Port ( PORT MUST BE INT )"; // Sets the message
            }
        }

        host.append(hostField.getText()); // Appends the text of the host field to the host
        return Integer.parseInt(portField.getText()); // Returns the integer value of the text of the port field
    }

    public static boolean hasPermission(long fileSize) { // Declares a public static method named hasPermission

        JPanel hpPanel = new JPanel(); // Creates a new JPanel object
        JLabel askLabel = new JLabel(String.format("Do you want to download a file of size = %.4f MB ?", fileSize / 1e6)); // Creates a new JLabel object

        hpPanel.add(askLabel); // Adds the ask label to the panel
        String message = "Do you??"; // Initializes a string variable named message
        int result = JOptionPane.showConfirmDialog(null, hpPanel, message, JOptionPane.OK_CANCEL_OPTION); // Shows a confirm dialog

        return result == JOptionPane.OK_OPTION; // Returns true if the result is OK_OPTION, false otherwise
    }

    public static void sendBytes(BufferedInputStream in, DataOutputStream out) throws Exception { // Declares a public static method named sendBytes

        int count; // Declares an integer variable named count
        byte[] buffer = new byte[4096]; // Declares a byte array named buffer
        while ((count = in.read(buffer)) > 0) // While loop until the count is less than or equal to 0
            out.write(buffer, 0, count); // Writes the buffer to the output stream

    }
} // End of Utility class
