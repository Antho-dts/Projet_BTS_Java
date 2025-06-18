//--------------------- Package and Imports -----------------------//
package Fenetre_Graphique_Monitoring;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

import Utils.Tempo;

//--------------------- Main Class -----------------------//
public class PatientAddThread extends Thread {

    //--------------------- Attributes -----------------------//
    private MainWindow mainWindow;
    private boolean stopThreadFlag = false;

    private String firstName, lastName, gender, email, password, confirmPassword;
    private String registrationFrame = null;
    private String patientRegistrationStatus;
    private Date birthDate;

    private WS_Client WS_Client;

    //--------------------- Constructor -----------------------//
    public PatientAddThread(MainWindow mainWindow, WS_Client _webSocketClient) {
        this.mainWindow = mainWindow;
        this.WS_Client = _webSocketClient;
    }

    //--------------------- Main Thread Loop -----------------------//
    @Override
    public void run() {
        while (!stopThreadFlag) {
            if (mainWindow != null && mainWindow.getSubmitButtonState()) {
                handleFormSubmission();
                setTrameRegister(registrationFrame);
                WS_Client.sendRegisterRequest();
                handleRegistrationResponse();
                mainWindow.setSubmitButtonState(false);
            }

            if (WS_Client != null) {
                WS_Client.sendListRequest();
                if (mainWindow != null) {
                    mainWindow.addPatientListElements();
                }
            }

            try {
                Thread.sleep(500); // Avoid tight loop
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    //--------------------- Form Submission Logic -----------------------//
    private void handleFormSubmission() {
        firstName = mainWindow.getFirstNameField().getText().trim();
        lastName = mainWindow.getLastNameField().getText().trim();
        gender = (String) mainWindow.getGenderDropdown().getSelectedItem();
        birthDate = (Date) mainWindow.getBirthDateSpinner().getValue();
        email = mainWindow.getEmailField().getText().trim();
        password = new String(mainWindow.getPasswordField().getPassword());
        confirmPassword = new String(mainWindow.getConfirmPasswordField().getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() || gender == null || gender.isEmpty() || birthDate == null || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(birthDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        registrationFrame = String.format("REGISTER/%s/%s/%s/%s/%s/%s/", lastName, firstName, gender, formattedDate, email, password);
    }

    //--------------------- Registration Response Handling -----------------------//
    private void handleRegistrationResponse() {
        if (WS_Client != null) {
            new Tempo(1000);
            patientRegistrationStatus = WS_Client.getResponseStatus();

            if (patientRegistrationStatus != null) {
                switch (patientRegistrationStatus) {
                    case "SUCCESS":
                        JOptionPane.showMessageDialog(null,
                                "Patient successfully registered!",
                                "Registration Complete",
                                JOptionPane.INFORMATION_MESSAGE);
                        clearInputFields();
                        break;

                    case "ERREUR":
                        JOptionPane.showMessageDialog(null,
                                "Error: This patient already exists in the database!",
                                "Registration Error",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        }
    }

    //--------------------- Form Reset -----------------------//
    private void clearInputFields() {
        mainWindow.getFirstNameField().setText("");
        mainWindow.getLastNameField().setText("");
        mainWindow.getGenderDropdown().setSelectedIndex(0);
        mainWindow.getBirthDateSpinner().setValue(new Date());
        mainWindow.getEmailField().setText("");
        mainWindow.getPasswordField().setText("");
        mainWindow.getConfirmPasswordField().setText("");
    }

    //--------------------- Getter and Setter Management -----------------------//
    public String setTrameRegister(String registrationFrame) {
        this.registrationFrame = registrationFrame;
        return this.registrationFrame;
    }

    public String getTrameRegister() {
        return this.registrationFrame;
    }

    //--------------------- Thread Control -----------------------//
    public void stopThreadSurveillanceBouton() {
        stopThreadFlag = true;
    }
}
