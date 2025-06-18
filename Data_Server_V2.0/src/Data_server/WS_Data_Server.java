package Data_server;

import java.net.InetSocketAddress;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WS_Data_Server extends WebSocketServer {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/vitalCaredb";
    private static final String DB_USER = "vitalAdmin";
    private static final String DB_PASSWORD = "ViCare2025!dbs";

    private static final String HOST = "localhost";
    private static final int PORT = 9290;

    private static WS_Data_Server serverInstance;

    private final Map<String, WebSocket> userSockets = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> socketEmails = new ConcurrentHashMap<>();
    private final Map<String, Thread> userThreads = new ConcurrentHashMap<>();
    private final Map<String, String> emailToThreadId = new ConcurrentHashMap<>();

    public WS_Data_Server(String host, int port) {
        super(new InetSocketAddress(host, port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the WebSocket server!");
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
        handleLogout(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message received: " + message);
        handleMessage(conn, message);
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket Error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started!");
    }

    private void handleMessage(WebSocket conn, String message) {
    	
    	/*CryptoAES256 crypto = new CryptoAES256();
    	String decryptedmessage = null;
		try {
			decryptedmessage = crypto.DecryptAES256(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(decryptedmessage);
    	*/
    	
    	message = message.replace("\"", "");
        String[] parts = message.split("/");
        if (parts.length < 1) {
            conn.send("ERROR: Invalid frame");
            return;
        }

        String header = parts[0].trim();
        switch (header) {
            case "LOGIN" : handleLogin(conn, parts);
            break;
            case "REGISTER" : handleRegister(conn, parts);
            break;
            case "LIST" : handleSendUserList(conn);
            break;
            case "DATA" : handleStoreUserData(conn, parts);
            break;
            case "CALCUL" : handleUserAgeMonitoring(conn, parts);
            break;
            case "USERDATA" : handlefetchUserData(conn, parts);
            break;
            case "LOGOUT" : handleLogout(conn);
            break;
            default : conn.send("ERROR: Unknown command");
            break;
            
        }
    }

    private void handleLogin(WebSocket conn, String[] parts) {
        if (parts.length == 3) {
            LoginAngular(conn, parts[1].trim(), parts[2].trim());
        } else if (parts.length == 4) {
            LoginAndroid(conn, parts[1].trim(), parts[2].trim(), parts[3].trim());
        } else {
            conn.send("ERROR: Invalid LOGIN frame");
        }
    }

    private void handleRegister(WebSocket conn, String[] parts) {
        if (parts.length < 7) {
            conn.send("ERROR: Invalid REGISTER frame");
            return;
        }
        RegisterUser(conn, parts[2].trim(), parts[1].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[6].trim());
    }
    
    private void handleSendUserList(WebSocket conn) {
    	SendUserList(conn);
    }

    private void handleStoreUserData(WebSocket conn, String[] parts) {
        if (parts.length < 9) {
            conn.send("ERROR: Invalid DATA frame");
            return;
        }
        StoreUserData(conn, parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
    }
    
    private void handleUserAgeMonitoring(WebSocket conn, String[] parts) {
    	UserAgeMonitoring(conn, parts);
    }
    
    private void handlefetchUserData(WebSocket conn, String[] parts) {
    	if (parts.length < 2) {
    		conn.send("ERROR: Invalid USERDATA frame");
    		return;
    	}
    	FetchUserData(conn, parts[1].trim());
    }
    
    private void handleLogout(WebSocket conn) {
        Logout(conn);
    }

    
    

//     _____              .___             .__    .___
//    /  _  \   ____    __| _/______  ____ |__| __| _/
//   /  /_\  \ /    \  / __ |\_  __ \/  _ \|  |/ __ | 
//  /    |    \   |  \/ /_/ | |  | \(  <_> )  / /_/ | 
//  \____|__  /___|  /\____ | |__|   \____/|__\____ | 
//          \/     \/      \/                      \/ 
    
    private void LoginAndroid(WebSocket conn, String email, String password, String pairing) {
    	String query = "SELECT id FROM user WHERE email = ? AND password = ?";
    	try (Connection connDb = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    			PreparedStatement stmt = connDb.prepareStatement(query)) {
    		
    		stmt.setString(1, email);
    		stmt.setString(2, password);
    		ResultSet rs = stmt.executeQuery();
    		
    		if (rs.next()) {
    			conn.send("SUCCESS");
    		} else {
    			conn.send("ERROR: Login failed");
    		}
    	} catch (SQLException e) {
    		conn.send("ERROR: Database issue");
    		e.printStackTrace();
    	}
    }
    
    private void StoreUserData(WebSocket conn, String email, String heart_rate, String blood_oxygen,
            String acceleration_x, String acceleration_y, String acceleration_z,
            String gps_x, String gps_y) {
    	new Thread(new UserDataHandler(conn, email, DB_URL, DB_USER, DB_PASSWORD,
                heart_rate, blood_oxygen, acceleration_x,
                acceleration_y, acceleration_z, gps_x, gps_y)).start();;
    }

//      _____                       .__                
//     /  _  \   ____    ____  __ __|  | _____ _______ 
//    /  /_\  \ /    \  / ___\|  |  \  | \__  \\_  __ \
//   /    |    \   |  \/ /_/  >  |  /  |__/ __ \|  | \/
//   \____|__  /___|  /\___  /|____/|____(____  /__|   
//           \/     \//_____/                 \/       
    
    private void LoginAngular(WebSocket conn, String email, String password) {
 	
    	String query = "SELECT id FROM user WHERE email = ? AND password = ?";
    	try (Connection connDb = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    			PreparedStatement stmt = connDb.prepareStatement(query)) {
    		
    		stmt.setString(1, email);
    		stmt.setString(2, password);
    		ResultSet rs = stmt.executeQuery();
    		
    		if (rs.next()) {
    			conn.send("LOGIN/" + email);
    			userSockets.put(email, conn);
    			socketEmails.put(conn, email);
    			
    			String threadId = UUID.randomUUID().toString();
    			emailToThreadId.put(email, threadId);
    			Thread monitor = new Thread(new UserMonitorThread(conn, email, DB_URL, DB_USER, DB_PASSWORD));
    			monitor.start();
    			userThreads.put(threadId, monitor);
    		} else {
    			conn.send("ERROR: Login failed");
    		}
    	} catch (SQLException e) {
    		conn.send("ERROR: Database issue");
    		e.printStackTrace();
    	}
    }
    
    public static void SendUserListAngular(WebSocket conn, String email) {
    	String query = "SELECT lastName, firstName FROM user";
    	try (Connection connDb = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    			PreparedStatement stmt = connDb.prepareStatement(query);
    			ResultSet rs = stmt.executeQuery()) {
    		
    		StringBuilder response = new StringBuilder("LIST/");
    		while (rs.next()) {
    			response.append(rs.getString("lastName")).append("/")
    			.append(rs.getString("firstName")).append(";");
    		}
    		
    		if (response.length() > 5) response.setLength(response.length() - 1);
    		conn.send(response.toString() + email);
    	} catch (SQLException e) {
    		conn.send("ERROR: Database issue");
    		e.printStackTrace();
    	}
    }
    
    public static void LoginDataAngular(WebSocket conn, String email) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String selectUserId = "SELECT id FROM user WHERE email = ?";
            int userId = -1;
            try (PreparedStatement selectStmt = connection.prepareStatement(selectUserId)) {
                selectStmt.setString(1, email);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    }
                }
            }
            if (userId == -1) {
                conn.send("ERROR: User not found.");
                return;
            }
            String selectMeasure = "SELECT * FROM data_user WHERE user_id = ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectMeasure)) {
                selectStmt.setInt(1, userId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        String data = String.format(
                            "DATA: Heart Rate: %d, Blood Oxygen: %.2f, Acceleration X: %.2f, Acceleration Y: %.2f, Acceleration Z: %.2f, GPS X: %.2f, GPS Y: %.2f/%s",
                            rs.getInt("heart_rate"), rs.getDouble("blood_oxygen"),
                            rs.getDouble("acceleration_x"), rs.getDouble("acceleration_y"), rs.getDouble("acceleration_z"),
                            rs.getDouble("gps_x"), rs.getDouble("gps_y"), email);
                        if (conn.isOpen()) {
                            System.out.println(data);
                            conn.send(data);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            conn.send("ERROR: Database connection problem.");
            e.printStackTrace();
        }
    }

//     _____                .__  __               .__                
//    /     \   ____   ____ |__|/  |_  ___________|__| ____    ____  
//   /  \ /  \ /  _ \ /    \|  \   __\/  _ \_  __ \  |/    \  / ___\ 
//  /    Y    (  <_> )   |  \  ||  | (  <_> )  | \/  |   |  \/ /_/  >
//  \____|__  /\____/|___|  /__||__|  \____/|__|  |__|___|  /\___  / 
//          \/            \/                              \//_____/  
    
    private void SendUserList(WebSocket conn) {
    	String query = "SELECT lastName, firstName FROM user";
        try (Connection connDb = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = connDb.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            StringBuilder response = new StringBuilder("LIST/");
            while (rs.next()) {
                response.append(rs.getString("lastName")).append("/")
                        .append(rs.getString("firstName")).append(";");
            }

            if (response.length() > 5) response.setLength(response.length() - 1);
            conn.send(response.toString());
        } catch (SQLException e) {
            conn.send("ERROR: Database issue");
            e.printStackTrace();
        }
    }
    
    private void FetchUserData(WebSocket conn,String email) {
    	new Thread(new DataHandlerThread(conn, email, DB_URL, DB_USER, DB_PASSWORD)).start();
    }
    
    private void UserAgeMonitoring(WebSocket conn, String[] parts) {
    	if (parts.length < 2) {
    		conn.send("ERROR: Invalid CALCUL frame");
    		return;
    	}
    	String[] name = parts[1].split(" ");
    	if (name.length < 2) {
    		conn.send("ERROR: Invalid name");
    		return;
    	}
    	String query = "SELECT birthDate FROM user WHERE lastName = ? AND firstName = ?";
    	
    	try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    			PreparedStatement statement = connection.prepareStatement(query)) {
    		
    		statement.setString(1, name[0]);
    		statement.setString(2, name[1]);
    		ResultSet rs = statement.executeQuery();
    		
    		if (rs.next()) {
    			LocalDate birthDate = rs.getDate("birthDate").toLocalDate();
    			int age = Period.between(birthDate, LocalDate.now()).getYears();
    			conn.send("AGE/" + age);
    			System.out.println(age);
    		} else {
    			conn.send("ERROR: User not found");
    		}
    	} catch (SQLException e) {
    		conn.send("ERROR: Database issue");
    		e.printStackTrace();
    	}
    }
        
//	   _____  .____    .____     
//	  /  _  \ |    |   |    |    
//	 /  /_\  \|    |   |    |    
//	/    |    \    |___|    |___ 
//	\____|__  /_______ \_______ \
//	        \/        \/       \/

    private void RegisterUser(WebSocket conn, String firstName, String lastName, String gender, String birthDate, String email, String password) {
    	String checkQuery = "SELECT id FROM user WHERE email = ?";
    	String insertQuery = "INSERT INTO user (firstName, lastName, gender, birthDate, email, password) VALUES (?, ?, ?, ?, ?, ?)";
    	
    	try (Connection connDb = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    			PreparedStatement checkStmt = connDb.prepareStatement(checkQuery)) {
    		
    		checkStmt.setString(1, email);
    		ResultSet rs = checkStmt.executeQuery();
    		
    		if (rs.next()) {
    			conn.send("ERROR: User already exists");
    			return;
    		}
    		
    		try (PreparedStatement insertStmt = connDb.prepareStatement(insertQuery)) {
    			insertStmt.setString(1, firstName);
    			insertStmt.setString(2, lastName);
    			insertStmt.setString(3, gender);
    			insertStmt.setString(4, birthDate);
    			insertStmt.setString(5, email);
    			insertStmt.setString(6, password);
    			
    			int rows = insertStmt.executeUpdate();
    			conn.send(rows > 0 ? "SUCCESS" : "ERROR: Registration failed");
    		}
    	} catch (SQLException e) {
    		conn.send("ERROR: Database issue");
    		e.printStackTrace();
    	}
    }
    
    private void Logout(WebSocket conn) {
    	String email = socketEmails.remove(conn);
        if (email != null) {
            userSockets.remove(email);
            String threadId = emailToThreadId.remove(email);
            if (threadId != null) {
                Thread userThread = userThreads.remove(threadId);
                if (userThread != null) {
                    userThread.interrupt();
                    System.out.println("Stopped thread for user: " + email);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            serverInstance = new WS_Data_Server(HOST, PORT);
            serverInstance.start();
            System.out.println("WebSocket server running on ws://" + HOST + ":" + PORT);
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    public void stopServer() {
        try {
            stop();
            System.out.println("WebSocket server stopped");
        } catch (Exception e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
}
