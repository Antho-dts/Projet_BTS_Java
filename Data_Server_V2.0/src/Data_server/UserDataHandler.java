// Refactored version of UserDataHandler.java

package Data_server;

import java.sql.*;
import org.java_websocket.WebSocket;

public class UserDataHandler implements Runnable {

    private final WebSocket conn;
    private final String email;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final String heartRate;
    private final String bloodOxygen;
    private final String accelerationX;
    private final String accelerationY;
    private final String accelerationZ;
    private final String gpsX;
    private final String gpsY;

    private boolean running;

    public UserDataHandler(WebSocket conn, String email, String dbUrl, String dbUser, String dbPassword,
                           String heartRate, String bloodOxygen, String accelerationX,
                           String accelerationY, String accelerationZ, String gpsX, String gpsY) {
        this.conn = conn;
        this.email = email;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.heartRate = heartRate;
        this.bloodOxygen = bloodOxygen;
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
        this.gpsX = gpsX;
        this.gpsY = gpsY;
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

                int userId = getUserId(connection);
                if (userId == -1) {
                    System.out.println("ERROR: User does not exist: " + email);
                    return;
                }

                boolean inserted = insertMeasurement(connection, userId);
                conn.send(inserted ? "SUCCESS" : "ERROR");

            } catch (SQLException e) {
                System.out.println("ERROR: Database connection problem.");
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000); // 1-second delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, exiting...");
                running = false;
            }
        }
    }

    private int getUserId(Connection connection) throws SQLException {
        String query = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    private boolean insertMeasurement(Connection connection, int userId) throws SQLException {
        String insertQuery = "INSERT INTO data_user (user_id, heart_rate, blood_oxygen, acceleration_x, acceleration_y, acceleration_z, gps_x, gps_y) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setInt(1, userId);
            stmt.setString(2, heartRate);
            stmt.setString(3, bloodOxygen);
            stmt.setString(4, accelerationX);
            stmt.setString(5, accelerationY);
            stmt.setString(6, accelerationZ);
            stmt.setString(7, gpsX);
            stmt.setString(8, gpsY);
            return stmt.executeUpdate() > 0;
        }
    }
}
