//--------------------- Package and Imports -----------------------//
package Fenetre_Graphique_Monitoring;

import java.awt.EventQueue;
import java.net.URI;

/***************************************************************
 * FILENAME   : Start.java
 * AUTHOR     : Anthony Desdoits
 * VERSION    : 2.0
 * CREATED ON : 06/01/2025
 * UPDATED ON : 03/06/2025
 *
 * DESCRIPTION:
 * Program allowing the launch of the monitoring application
 *
 * NOTES:
 * For more security, put the server's IP and port in a secure file.
 *
 ***************************************************************/

public class Start {

    private static PatientAddThread patientAddThread = null;
    private static WS_Client clientWS = null;
    private static MainWindow mainWindow = null;

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9290;

    // ----------------- Application Entry Point -------------------- //
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                System.out.println("[START] Application launching...");

                // Initialize WebSocket client
                clientWS = new WS_Client(new URI("ws://" + SERVER_IP + ":" + SERVER_PORT), mainWindow);
                clientWS.connect();

                // Create main window
                mainWindow = new MainWindow(clientWS);

                // Start background monitoring thread
                patientAddThread = new PatientAddThread(mainWindow, clientWS);
                clientWS.setMonitoringThread(patientAddThread);
                patientAddThread.start();

                System.out.println("[START] Application ready.");
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to launch application:");
                e.printStackTrace();
            }
        });
    }
}