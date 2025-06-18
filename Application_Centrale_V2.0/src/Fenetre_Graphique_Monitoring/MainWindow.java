//--------------------- Package and Imports -----------------------//
package Fenetre_Graphique_Monitoring;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import Utils.Tempo;

//--------------------- Main Class -----------------------//
public class MainWindow extends JFrame {

	//--------------------- Attributes -----------------------//	
    private static final long serialVersionUID = 1L;
    private JPanel formPanel, serverPanel, formTab, mainContentPanel, upperPanel;
    private JButton submitButton, clearConsoleButton;
    private JTabbedPane tabbedPane;
    private JLabel firstNameLabel, lastNameLabel, genderLabel, birthDateLabel, emailLabel, passwordLabel, confirmPasswordLabel;
    private GridBagConstraints serverGridConstraints, gridConstraints;
    private JTextPane consoleTextPane;
    private JScrollPane consoleScrollPane;
    private boolean submitButtonState = false, booleanButtonState;
    private WS_Client myClient_WS;
    private String patientListFrame, fullName, newPatientListFrame;
    private String[] pairs, lastNames, firstNames, fullNames;
    private Style defaultStyle, errorStyle, infoStyle, style;
    private StyleContext sc;
    private OutputStream out, err;
    private Document doc;
    private HashMap<String, Boolean> checkboxStates = new HashMap<>();


    private JLabel patientListLabel, statusLabel;
    private JPasswordField confirmPasswordField, passwordField;
    private JTextField firstNameField, lastNameField, emailField;
    private String[] genders = {"Male", "Female"};
    private JSpinner.DateEditor dateEditor;
    private JComboBox<String> genderDropdown;
    private JSpinner birthDateSpinner;
    private JPanel listPanel, wrapperPanel;
    private boolean isSelected;

    private PatientPanel patientPanel;
    public PatientMonitorThread patientMonitorThread;

    //--------------------- Constructor -----------------------//
    public MainWindow(WS_Client _WS_Client) {
        if (_WS_Client == null) {
            throw new IllegalArgumentException("WS_Client cannot be null");
        }
        this.myClient_WS = _WS_Client;

        //--------------------- Window Setup -----------------------//
        setTitle("Central Application");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //--------------------- Tab Initialization -----------------------//
        this.tabbedPane = new JTabbedPane();
        getContentPane().add(this.tabbedPane);

        this.patientPanel = new PatientPanel(this.myClient_WS);
        this.tabbedPane.addTab("Home", this.patientPanel.getHomePanel());

        this.mainContentPanel = this.patientPanel.getmainContentPanel();
        
        // Tab to Add a Patient
        this.formTab = new JPanel(new BorderLayout());
        this.formTab.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel for the form
        this.formPanel = new JPanel(new GridBagLayout());
        this.formPanel.setBackground(Color.WHITE);
        this.formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
                "Add a Patient",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 16),
                new Color(52, 73, 94)
        ));

        this.gridConstraints = new GridBagConstraints();
        this.gridConstraints.insets = new Insets(10, 10, 10, 10);
        this.gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        this.gridConstraints.anchor = GridBagConstraints.WEST;

        // First Name Field
        this.firstNameLabel = new JLabel("First Name:");
        this.firstNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 0;
        this.formPanel.add(this.firstNameLabel, this.gridConstraints);

        this.firstNameField = new JTextField(20);
        this.firstNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 1;
        this.gridConstraints.gridy = 0;
        this.formPanel.add(this.firstNameField, this.gridConstraints);

        // Last Name Field
        this.lastNameLabel = new JLabel("Last Name:");
        this.lastNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 1;
        this.formPanel.add(this.lastNameLabel, this.gridConstraints);

        this.lastNameField = new JTextField(20);
        this.lastNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 1;
        this.gridConstraints.gridy = 1;
        this.formPanel.add(this.lastNameField, this.gridConstraints);

        // Gender Field
        this.genderLabel = new JLabel("Gender:");
        this.genderLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 2;
        this.formPanel.add(this.genderLabel, this.gridConstraints);

        this.genderDropdown = new JComboBox<>(genders);
        this.genderDropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 1;
        this.gridConstraints.gridy = 2;
        this.formPanel.add(this.genderDropdown, this.gridConstraints);

        // Birth Date Field
        this.birthDateLabel = new JLabel("Birth Date:");
        this.birthDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 3;
        this.formPanel.add(birthDateLabel, this.gridConstraints);

        this.birthDateSpinner = new JSpinner(new SpinnerDateModel());
        this.dateEditor = new JSpinner.DateEditor(this.birthDateSpinner, "dd/MM/yyyy");
        this.birthDateSpinner.setEditor(this.dateEditor);
        this.birthDateSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 1;
        this.gridConstraints.gridy = 3;
        this.formPanel.add(this.birthDateSpinner, this.gridConstraints);

        // Email Field
        this.emailLabel = new JLabel("Email:");
        this.emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 4;
        this.formPanel.add(this.emailLabel, this.gridConstraints);

        this.emailField = new JTextField(20);
        this.emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 1;
        this.gridConstraints.gridy = 4;
        this.formPanel.add(this.emailField, this.gridConstraints);

        // Password Field
        this.passwordLabel = new JLabel("Password:");
        this.passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 5;
        this.formPanel.add(this.passwordLabel, this.gridConstraints);

        this.passwordField = new JPasswordField(20);
        this.passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 1;
        this.gridConstraints.gridy = 5;
        this.formPanel.add(this.passwordField, this.gridConstraints);

        // Confirm Password Field
        this.confirmPasswordLabel = new JLabel("Confirm your Password:");
        this.confirmPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 6;
        this.formPanel.add(this.confirmPasswordLabel, this.gridConstraints);

        this.confirmPasswordField = new JPasswordField(20);
        this.confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        this.gridConstraints.gridx = 1;
        this.gridConstraints.gridy = 6;
        this.formPanel.add(this.confirmPasswordField, this.gridConstraints);

        // Submit Button
        this.submitButton = new JButton("Add Patient");
        this.submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        this.submitButton.setBackground(new Color(52, 73, 94));
        this.submitButton.setForeground(Color.WHITE);
        this.submitButton.setFocusPainted(false);
        this.gridConstraints.gridx = 0;
        this.gridConstraints.gridy = 7;
        this.gridConstraints.gridwidth = 2;
        this.gridConstraints.anchor = GridBagConstraints.CENTER;
        this.formPanel.add(this.submitButton, this.gridConstraints);

        this.formTab.add(this.formPanel, BorderLayout.CENTER);

        this.tabbedPane.addTab("Add a Patient", this.formTab);

        // Tab with the console (replaces Server Connection)
        this.serverPanel = new JPanel(new BorderLayout());
        this.serverPanel.setBackground(new Color(52, 73, 94));

        // Upper Panel for the clear button
        this.upperPanel = new JPanel(new GridBagLayout());
        this.upperPanel.setBackground(new Color(52, 73, 94));

        this.serverGridConstraints = new GridBagConstraints();
        this.serverGridConstraints.insets = new Insets(10, 10, 10, 10);
        this.serverGridConstraints.fill = GridBagConstraints.HORIZONTAL;

        // Button to clear the console
        this.clearConsoleButton = new JButton("Clear Console");
        this.clearConsoleButton.setBackground(new Color(220, 220, 220));
        this.clearConsoleButton.setForeground(Color.BLACK);
        this.clearConsoleButton.setFont(new Font("Arial", Font.BOLD, 14));
        this.serverGridConstraints.gridx = 0;
        this.serverGridConstraints.gridy = 0;
        this.upperPanel.add(this.clearConsoleButton, this.serverGridConstraints);

        // Creating the console
        this.consoleTextPane = new JTextPane();
        this.consoleTextPane.setEditable(false);
        this.consoleTextPane.setBackground(Color.BLACK);

        // Style for the console
        this.sc = StyleContext.getDefaultStyleContext();

        // Style for normal messages (white)
        this.defaultStyle = this.sc.addStyle("Default", null);
        StyleConstants.setForeground(this.defaultStyle, Color.WHITE);
        StyleConstants.setFontFamily(this.defaultStyle, "Monospaced");
        StyleConstants.setFontSize(this.defaultStyle, 14);

        // Style for errors (red)
        this.errorStyle = this.sc.addStyle("Error", null);
        StyleConstants.setForeground(this.errorStyle, Color.RED);
        StyleConstants.setFontFamily(this.errorStyle, "Monospaced");
        StyleConstants.setFontSize(this.errorStyle, 14);

        // Style for info (green)
        this.infoStyle = this.sc.addStyle("Info", null);
        StyleConstants.setForeground(this.infoStyle, Color.GREEN);
        StyleConstants.setFontFamily(this.infoStyle, "Monospaced");
        StyleConstants.setFontSize(this.infoStyle, 14);

        // Adding the console to a JScrollPane
        this.consoleScrollPane = new JScrollPane(this.consoleTextPane);
        this.consoleScrollPane.setPreferredSize(new Dimension(700, 400));

        // Redirecting System.out and System.err to the console
        redirectSystemStreams();

        // Adding components to the server panel
        this.serverPanel.add(upperPanel, BorderLayout.NORTH);
        this.serverPanel.add(this.consoleScrollPane, BorderLayout.CENTER);

        this.tabbedPane.addTab("Console", this.serverPanel);

        // ActionListener for the console clear button
        clearConsoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearConsole();
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!submitButtonState) {
                    setSubmitButtonState(true);
                } else {
                    setSubmitButtonState(false);
                }
            }
        });

        // Adding an initial message to the console
        printInfo("Console started");
        System.out.println("Ready to receive messages");

        setVisible(true);
    }

    //--------------------- Console Management -----------------------//

    private void redirectSystemStreams() {
        this.out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendToConsole(String.valueOf((char) b), "Default");
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                appendToConsole(new String(b, off, len), "Default");
            }
        };

        this.err = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendToConsole(String.valueOf((char) b), "Error");
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                appendToConsole(new String(b, off, len), "Error");
            }
        };

        System.setOut(new PrintStream(this.out, true));
        System.setErr(new PrintStream(this.err, true));
    }

    private void appendToConsole(final String text, final String styleName) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                doc = consoleTextPane.getDocument();
                try {
                    style = StyleContext.getDefaultStyleContext().getStyle(styleName);
                    doc.insertString(doc.getLength(), text, style);
                    consoleTextPane.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearConsole() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                consoleTextPane.setText("");
            }
        });
    }

    public void printInfo(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                doc = consoleTextPane.getDocument();
                try {
                    style = StyleContext.getDefaultStyleContext().getStyle("Info");
                    doc.insertString(doc.getLength(), message + "\n", style);
                    consoleTextPane.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void printError(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                doc = consoleTextPane.getDocument();
                try {
                    style = StyleContext.getDefaultStyleContext().getStyle("Error");
                    doc.insertString(doc.getLength(), message + "\n", style);
                    consoleTextPane.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //--------------------- Patient Management -----------------------//

    public void addPatientListElements() {
        if (this.myClient_WS == null) {
            System.out.println("Error: myClient_WS is null");
            return;
        }

        while (!this.myClient_WS.isOpen()) {
            new Tempo(1);
        }

        this.newPatientListFrame = myClient_WS.getPatientListFrame();

        if (this.newPatientListFrame == null) {
            System.out.println("No patient information available");
            return;
        }

        // Remove the "LIST/" prefix if present
        if (this.newPatientListFrame.startsWith("LIST/")) {
            this.newPatientListFrame = this.newPatientListFrame.substring(6);
        }

        // Replace "/" with " "
        this.newPatientListFrame = this.newPatientListFrame.replace("/", " ");
        this.newPatientListFrame = this.newPatientListFrame.replace("\"", "");

        if (!this.newPatientListFrame.equals(this.patientListFrame)) {
            this.patientListFrame = this.newPatientListFrame;
            clearInfoPanels();

            // Split the frame into pairs using ";"
            this.pairs = patientListFrame.split(";");

            // Initialize arrays for last names and first names
            this.lastNames = new String[this.pairs.length];
            this.firstNames = new String[this.pairs.length];

            for (int i = 0; i < this.pairs.length; i++) {
                // Split each pair into last name and first name using " "
                this.fullNames = this.pairs[i].split(" ");
                if (this.fullNames.length == 2) {
                    this.lastNames[i] = this.fullNames[0];
                    this.firstNames[i] = this.fullNames[1];
                    this.fullName = this.lastNames[i] + " " + this.firstNames[i];
                    System.out.println("Adding: " + this.fullName);
                    addPatientList(this.fullName);
                } else {
                    System.out.println("Incorrect format for the pair: " + this.pairs[i]);
                }
            }
        }
    }

    public void addPatientList(String fullName) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Patient list panel
                listPanel = new JPanel(new BorderLayout());
                listPanel.setMaximumSize(new Dimension(180, 30));
                listPanel.setPreferredSize(new Dimension(180, 30));
                listPanel.setOpaque(false);

                // Label for status
                statusLabel = new JLabel("Offline"); // Default, you can change this dynamically
                statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                statusLabel.setForeground(Color.RED); // Green for "online", you can change this for "offline"

                patientListLabel = new JLabel(fullName);
                patientListLabel.setFont(new Font("Arial", Font.BOLD, 14));

                // Create a new instance of checkbox
                final JCheckBox checkBox = new JCheckBox();
                checkBox.setOpaque(false);

                // Retrieve the previous state of the checkbox if available
                checkBox.setSelected(getCheckboxState(fullName));

                // Start a thread for this patient
                patientMonitorThread = new PatientMonitorThread(fullName, myClient_WS);
                patientMonitorThread.start();

                // Create an enhanced info panel for this patient
                final JPanel infoPanel = patientPanel.createPatientPanel(fullName, patientMonitorThread);

                // Initialize the visibility of the panel according to the state of the checkbox
                infoPanel.setVisible(checkBox.isSelected());

                // Add the info panel to the main panel
                patientPanel.getPanelsContainer().add(infoPanel);

                patientPanel.resizePanels();

                // Add a listener to save the state of the checkbox
                checkBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Update the state of the checkbox in the HashMap
                        isSelected = checkBox.isSelected();

                        // Show or hide the info panel according to the state of the checkbox
                        infoPanel.setVisible(isSelected);

                        // Refresh the display
                        mainContentPanel.revalidate();
                        mainContentPanel.repaint();
                    }
                });

                listPanel.add(statusLabel, BorderLayout.NORTH); // Add the status label at the top
                listPanel.add(patientListLabel, BorderLayout.CENTER);
                listPanel.add(checkBox, BorderLayout.EAST);

                wrapperPanel = new JPanel(new BorderLayout());
                wrapperPanel.setMaximumSize(new Dimension(180, 35));
                wrapperPanel.add(listPanel, BorderLayout.CENTER);
                wrapperPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                wrapperPanel.setOpaque(false);

                patientPanel.getPatientsListPanel().add(wrapperPanel);
                patientPanel.getPatientsListPanel().revalidate();
                patientPanel.getPatientsListPanel().repaint();
            }
        });
    }

    public void clearInfoPanels() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                patientPanel.getPanelsContainer().removeAll();
                patientPanel.getPanelsContainer().revalidate();
                patientPanel.getPanelsContainer().repaint();

                patientPanel.getPatientsListPanel().removeAll();
                patientPanel.getPatientsListPanel().revalidate();
                patientPanel.getPatientsListPanel().repaint();
            }
        });
    }


    //--------------------- Getter and Setter Management -----------------------//

    public void setSubmitButtonState(boolean booleanButtonState) {
        this.submitButtonState = booleanButtonState;
    }

    public boolean getSubmitButtonState() {
        this.booleanButtonState = this.submitButtonState;
        return this.booleanButtonState;
    }

    public HashMap<String, Boolean> getCheckboxStates() {
        return checkboxStates;
    }

    public void setCheckboxState(String fullName, boolean state) {
        checkboxStates.put(fullName, state);
    }

    public boolean getCheckboxState(String fullName) {
        return checkboxStates.getOrDefault(fullName, false);
    }
    
    public JTextField getFirstNameField() {
        return firstNameField;
    }

    public void setFirstNameField(JTextField firstNameField) {
        this.firstNameField = firstNameField;
    }

    public JTextField getLastNameField() {
        return lastNameField;
    }

    public void setLastNameField(JTextField lastNameField) {
        this.lastNameField = lastNameField;
    }

    public JComboBox<String> getGenderDropdown() {
        return genderDropdown;
    }

    public void setGenderDropdown(JComboBox<String> genderDropdown) {
        this.genderDropdown = genderDropdown;
    }

    public JSpinner getBirthDateSpinner() {
        return birthDateSpinner;
    }

    public void setBirthDateSpinner(JSpinner birthDateSpinner) {
        this.birthDateSpinner = birthDateSpinner;
    }

    public JTextField getEmailField() {
        return emailField;
    }

    public void setEmailField(JTextField emailField) {
        this.emailField = emailField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(JPasswordField passwordField) {
        this.passwordField = passwordField;
    }

    public JPasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    public void setConfirmPasswordField(JPasswordField confirmPasswordField) {
        this.confirmPasswordField = confirmPasswordField;
    }

}
