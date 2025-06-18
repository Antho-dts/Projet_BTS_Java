// Cleaned and optimized version of DataHandlerThread.java

package Data_server;

import java.sql.*;
import org.java_websocket.WebSocket;

public class DataHandlerThread extends Thread {

    private final WebSocket conn;
    private final String fullName;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public DataHandlerThread(WebSocket conn, String fullName, String dbUrl, String dbUser, String dbPassword) {
        this.conn = conn;
        this.fullName = fullName;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public void run() {
        if (conn == null) {
            System.err.println("ERROR: WebSocket connection is null.");
            return;
        }

        String[] nameParts = fullName.trim().split(" ");
        if (nameParts.length != 2) {
            sendMessage("ERROR: Incorrect format. Use 'LastName FirstName'.");
            return;
        }

        String lastName = nameParts[0];
        String firstName = nameParts[1];

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            if (connection == null || connection.isClosed()) {
                sendMessage("ERROR: Unable to connect to the database.");
                return;
            }

            int userId = getUserId(connection, lastName, firstName);
            if (userId == -1) {
                sendMessage("No person found with the specified first and last name.");
                return;
            }

            String measurement = getLatestMeasurement(connection, userId);
            sendMessage(measurement == null
                ? "DATA/" + fullName + "/No measurements found for the user"
                : "DATA/" + fullName + "/" + measurement);

        } catch (SQLException e) {
            handleException("ERROR: Database connection issue.", e);
        } catch (Exception e) {
            handleException("ERROR: An unexpected exception occurred.", e);
        }
    }

    private int getUserId(Connection connection, String lastName, String firstName) throws SQLException {
        String query = "SELECT id FROM user WHERE lastName = ? AND firstName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, lastName);
            stmt.setString(2, firstName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    private String getLatestMeasurement(Connection connection, int userId) throws SQLException {
        String query = "SELECT * FROM data_user WHERE user_id = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return String.format(
                        "Heart Rate: %d/ Blood Oxygen: %.2f/ Acceleration X: %.3f/ Acceleration Y: %.3f/ Acceleration Z: %.3f/ GPS X: %.6f/ GPS Y: %.6f",
                        rs.getInt("heart_rate"),
                        rs.getBigDecimal("blood_oxygen"),
                        rs.getBigDecimal("acceleration_x"),
                        rs.getBigDecimal("acceleration_y"),
                        rs.getBigDecimal("acceleration_z"),
                        rs.getBigDecimal("gps_x"),
                        rs.getBigDecimal("gps_y")
                    );
                }
            }
        }
        return null;
    }

    private void sendMessage(String message) {
        if (conn != null && conn.isOpen()) {
            conn.send(message);
        }
    }

    private void handleException(String errorMessage, Exception e) {
        System.err.println(errorMessage);
        e.printStackTrace();
        sendMessage(errorMessage);
    }
}
