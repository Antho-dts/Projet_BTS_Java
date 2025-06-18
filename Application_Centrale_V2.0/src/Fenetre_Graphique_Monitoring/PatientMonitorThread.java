//--------------------- Package and Imports -----------------------//
package Fenetre_Graphique_Monitoring;

import org.jfree.chart.ChartPanel;
import Utils.Tempo;

//--------------------- Main Class -----------------------//
public class PatientMonitorThread extends Thread {

    //--------------------- Attributes -----------------------//
    private String fullName;
    private WS_Client wsClient;
    private ThreadJFreeChart pulseChartThread;
    private ThreadJFreeChart oxygenChartThread;
    private PatientData patientData;

    //--------------------- Inner Class: PatientData -----------------------//
    public static class PatientData {
        private int heartRate;
        private double bloodOxygen;
        private double accelerationX;
        private double accelerationY;
        private double accelerationZ;
        private double gpsX;
        private double gpsY;

        public PatientData() {
            this.heartRate = 0;
            this.bloodOxygen = 0.0;
            this.accelerationX = 0.0;
            this.accelerationY = 0.0;
            this.accelerationZ = 0.0;
            this.gpsX = 0.0;
            this.gpsY = 0.0;
        }

        //--------------------- Getter and Setter Management -----------------------//
        public int getHeartRate() {
            return heartRate;
        }

        public void setHeartRate(int heartRate) {
            this.heartRate = heartRate;
        }

        public double getBloodOxygen() {
            return bloodOxygen;
        }

        public void setBloodOxygen(double bloodOxygen) {
            this.bloodOxygen = bloodOxygen;
        }

        public double getAccelerationX() {
            return accelerationX;
        }

        public void setAccelerationX(double accelerationX) {
            this.accelerationX = accelerationX;
        }

        public double getAccelerationY() {
            return accelerationY;
        }

        public void setAccelerationY(double accelerationY) {
            this.accelerationY = accelerationY;
        }

        public double getAccelerationZ() {
            return accelerationZ;
        }

        public void setAccelerationZ(double accelerationZ) {
            this.accelerationZ = accelerationZ;
        }

        public double getGpsX() {
            return gpsX;
        }

        public void setGpsX(double gpsX) {
            this.gpsX = gpsX;
        }

        public double getGpsY() {
            return gpsY;
        }

        public void setGpsY(double gpsY) {
            this.gpsY = gpsY;
        }
    }

    //--------------------- Constructor -----------------------//
    public PatientMonitorThread(String fullName, WS_Client wsClient) {
        this.fullName = fullName;
        this.wsClient = wsClient;
        this.patientData = new PatientData();

        this.pulseChartThread = new ThreadJFreeChart(this, false);
        this.oxygenChartThread = new ThreadJFreeChart(this, true);
    }

    //--------------------- Main Thread Loop -----------------------//
    @Override
    public void run() {
        pulseChartThread.start();
        oxygenChartThread.start();

        while (true) {
            new Tempo(200);
            wsClient.sendRequestPatientData(fullName);
            new Tempo(200);
            String data = wsClient.getPatientData(fullName);

            if ("0".equals(data)) {
                // Reset all patient data to zero
                patientData.setHeartRate(0);
                patientData.setBloodOxygen(0.0);
                patientData.setAccelerationX(0.0);
                patientData.setAccelerationY(0.0);
                patientData.setAccelerationZ(0.0);
                patientData.setGpsX(0.0);
                patientData.setGpsY(0.0);
            } else {
                // Parse and update patient data
                String[] parts = data.split("/");
                for (String part : parts) {
                    String[] keyValue = part.split(":");
                    if (keyValue.length < 2) continue;

                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim().replace(',', '.');

                    switch (key) {
                        case "Heart Rate":
                            patientData.setHeartRate(Integer.parseInt(value));
                            break;
                        case "Blood Oxygen":
                            patientData.setBloodOxygen(Double.parseDouble(value));
                            break;
                        case "Acceleration X":
                            patientData.setAccelerationX(Double.parseDouble(value));
                            break;
                        case "Acceleration Y":
                            patientData.setAccelerationY(Double.parseDouble(value));
                            break;
                        case "Acceleration Z":
                            patientData.setAccelerationZ(Double.parseDouble(value));
                            break;
                        case "GPS X":
                            patientData.setGpsX(Double.parseDouble(value));
                            break;
                        case "GPS Y":
                            patientData.setGpsY(Double.parseDouble(value));
                            break;
                    }
                }
            }
        }
    }

    //--------------------- Getter and Setter Management -----------------------//
    public PatientData getPatientData() {
        return patientData;
    }

    public ChartPanel getGetChartMonThreadJFreeChartPulse() {
        return pulseChartThread.getChart();
    }

    public ChartPanel getGetChartMonThreadJFreeChartOxy() {
        return oxygenChartThread.getChart();
    }
}
