package Fenetre_Graphique_Monitoring;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WS_Client extends WebSocketClient {

    //--------------------- Attributes -----------------------//
    private String patientListFrame;
    private int calculatedAge;
    private String registerFrame;
    private String responseStatus;
    private String header;

    private PatientAddThread addPatientThread;

    private String[] parts;
    private String[] nameParts;
    private final Map<String, String> patientDataMap = new HashMap<>();

    //--------------------- Constructor -----------------------//
    public WS_Client(URI serverUri, MainWindow mainWindow) {
        super(serverUri);
    }

    //--------------------- WebSocket Event Handlers -----------------------//
    @Override
    public void onOpen(ServerHandshake handshakeData) {
        System.out.println("✅ Connected to server!");
    }

    @Override
    public void onMessage(String message) {
        parts = message.split("/");

        if (parts.length < 1) {
            System.out.println("ERROR: Invalid frame received.");
            return;
        }

        header = parts[0].trim();

        switch (header) {
            case "LIST" : handleListFrame(message);
            break;
            case "AGE" : handleAgeFrame(message);
            break;
            case "DATA" : handleDataFrame(message);
            break;
            case "SUCCESS", "ERREUR" : handleResponseFrame(header, message);
            break;
            default : System.out.println("ERROR: Unrecognized frame header: " + header);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("❌ Disconnected from server: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("⚠️ Error: " + ex.getMessage());
    }

    //--------------------- Frame Handlers -----------------------//
    private void handleListFrame(String message) {
        if (parts.length < 1) {
            System.out.println("ERROR: Invalid LIST frame.");
            return;
        }
        patientListFrame = message.substring(5);
    }

    private void handleAgeFrame(String message) {
        if (parts.length < 2) {
            System.out.println("ERROR: Invalid AGE frame.");
            return;
        }
        Ageframe(message);
    }

    private void handleResponseFrame(String header, String message) {
        System.out.println("📩 Message from server: " + message);
        responseStatus = header;
    }

    private void handleDataFrame(String message) {
        DataForeachPatient(message);
    }

    //--------------------- Data Processing Methods -----------------------//
    public void Ageframe(String message) {
        try {
            calculatedAge = Integer.parseInt(message.substring(4));
            setCalculatedAge(calculatedAge);
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Age format error: " + message);
        }
    }

    public void DataForeachPatient(String message) {
        if (message.contains("No measurements found for the user")) {
            String fullName = parts[1];
            patientDataMap.put(fullName, "0");
            return;
        }

        if (parts.length < 9) {
            System.out.println("ERROR: Invalid DATA frame length: " + parts.length);
            return;
        }

        nameParts = parts[1].trim().split(" ");
        if (nameParts.length < 1) {
            System.out.println("ERROR: Invalid name format");
            return;
        }

        String fullName = parts[1];
        StringBuilder dataBuilder = new StringBuilder();
        for (int i = 2; i <= 8; i++) {
            dataBuilder.append(parts[i]);
            if (i < 9) {
                dataBuilder.append("/");
            }
        }
        patientDataMap.put(fullName, dataBuilder.toString());
    }

    //--------------------- Getter and Setter Management -----------------------//
    public void setMonitoringThread(PatientAddThread thread) {
        this.addPatientThread = thread;
    }

    public void sendListRequest() {
        send("LIST/");
    }

    public void sendAgeRequest(String fullName) {
        send("CALCUL/" + fullName);
    }

    public void sendRegisterRequest() {
        if (addPatientThread != null) {
            registerFrame = addPatientThread.getTrameRegister();
            send(registerFrame);
        }
    }

    public void sendRequestPatientData(String fullName) {
        send("USERDATA/" + fullName);
    }

    public String getPatientData(String fullName) {
        return patientDataMap.get(fullName);
    }

    public int setCalculatedAge(int _calculatedAge) {
        this.calculatedAge = _calculatedAge;
        return this.calculatedAge;
    }

    public int getCalculatedAge() {
        return calculatedAge;
    }

    public String getResponseStatus() {
        return this.responseStatus;
    }

    public String setPatientListFrame(String patientListFrame) {
        this.patientListFrame = patientListFrame;
        return this.patientListFrame;
    }

    public String getPatientListFrame() {
        return this.patientListFrame;
    }

}
