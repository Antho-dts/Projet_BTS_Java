// Refactored version of UserMonitorThread.java

package Data_server;

import java.sql.*;
import org.java_websocket.WebSocket;

public class UserMonitorThread extends Thread {

    private final WebSocket conn;
    private final String email;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public UserMonitorThread(WebSocket conn, String email, String dbUrl, String dbUser, String dbPassword) {
        this.conn = conn;
        this.email = email;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && conn.isOpen()) {
                Thread.sleep(1000);
                WS_Data_Server.LoginDataAngular(conn, email);
                
                if (isAdminUser()) {
                    Thread.sleep(10000);
                    WS_Data_Server.SendUserListAngular(conn, email);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("UserMonitorThread interrupted for: " + email);
        }
    }

    private boolean isAdminUser() {
        String query = "SELECT Admin FROM user WHERE email = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("Admin") == 1;
            }

        } catch (SQLException e) {
            System.err.println("Error checking admin status for " + email + ": " + e.getMessage());
            return false;
        }
    }
} 