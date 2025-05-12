package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionBD {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE_NAME = "tienda";

    private static final String URL_DB_BASE = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME;

    private static final String USER_DB = "root";
    private static final String PASSWORD_DB = "";

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("INFO: Driver MySQL JDBC cargado exitosamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR CRÍTICO: Driver MySQL JDBC no encontrado. Asegúrate de que el JAR del conector MySQL está en el classpath.");
            Logger.getLogger(ConnectionBD.class.getName()).log(Level.SEVERE, "Driver MySQL JDBC no encontrado", e);
            throw new RuntimeException("Driver MySQL JDBC no encontrado, la aplicación no puede continuar.", e);
        }
    }

    private ConnectionBD() {}

    public static Connection getConn() throws SQLException {
        String connectionUrl = URL_DB_BASE +
                "?useSSL=false" +
                "&serverTimezone=UTC" +
                "&allowPublicKeyRetrieval=true" +
                "&useUnicode=true" +
                "&characterEncoding=UTF-8";

        System.out.println("INFO: ConnectionBD.getConn() - Solicitando NUEVA conexión a: " + connectionUrl);
        Connection nuevaConexion = DriverManager.getConnection(connectionUrl, USER_DB, PASSWORD_DB);
        System.out.println("INFO: ConnectionBD.getConn() - NUEVA conexión establecida. AutoCommit por defecto: " + nuevaConexion.getAutoCommit());
        return nuevaConexion;
    }

    public static void closeQuietly(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(ConnectionBD.class.getName()).log(Level.WARNING, "Error al cerrar ResultSet", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                Logger.getLogger(ConnectionBD.class.getName()).log(Level.WARNING, "Error al cerrar Statement", e);
            }
        }
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("INFO: ConnectionBD.closeQuietly() - Conexión cerrada.");
                }
            } catch (SQLException e) {
                Logger.getLogger(ConnectionBD.class.getName()).log(Level.WARNING, "Error al cerrar Connection", e);
            }
        }
    }

    public static void closeQuietly(ResultSet rs, Statement stmt) {
        closeQuietly(rs, stmt, null);
    }
}