package services;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/tienda";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection conn = null;

    // Constructor privado para evitar instanciación
    private ConnectionBD() {}

    /**
     * Establece una conexión a la base de datos
     * @return Connection objeto de conexión
     * @throws SQLException si ocurre un error al conectar
     */
    public static Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                // Registrar el driver (opcional desde JDBC 4.0)
                Class.forName("com.mysql.jdbc.Driver");

                // Establecer conexión con parámetros adicionales
                String connectionURL = URL + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                conn = DriverManager.getConnection(connectionURL, USER, PASSWORD);
                conn.setAutoCommit(true); // Por defecto autocommit=true
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver no encontrado", e);
            }
        }
        return conn;
    }

    /**
     * Cierra la conexión a la base de datos
     */
    public static void closeConnection() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionBD.class.getName())
                        .log(Level.SEVERE, "Error al cerrar la conexión", ex);
            } finally {
                conn = null;
            }
        }
    }


    public static void closeResources(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionBD.class.getName())
                    .log(Level.SEVERE, "Error al cerrar recursos", ex);
        }
    }
}