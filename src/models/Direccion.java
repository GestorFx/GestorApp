package models;

import java.sql.*;
import java.util.Objects;
import services.ConnectionBD;

public class Direccion {
    private int usuarioId;
    private String calle;
    private String ciudad;
    private String codigoPostal;

    // Getters y Setters
    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        if (calle == null || calle.trim().isEmpty()) {
            throw new IllegalArgumentException("La calle no puede estar vacía");
        }
        this.calle = calle.trim();
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        if (ciudad == null || ciudad.trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad no puede estar vacía");
        }
        this.ciudad = ciudad.trim();
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.trim().isEmpty()) {
            throw new IllegalArgumentException("El código postal no puede estar vacío");
        }
        this.codigoPostal = codigoPostal.trim();
    }

    // Métodos de persistencia
    public void save() throws SQLException {
        Connection conn = ConnectionBD.getConn();
        conn.setAutoCommit(false);
        try {
            save(conn);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void save(Connection conn) throws SQLException {
        if (exists(conn)) {
            update(conn);
        } else {
            insert(conn);
        }
    }

    private boolean exists(Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM Direcciones WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insert(Connection conn) throws SQLException {
        String sql = "INSERT INTO Direcciones (usuario_id, calle, ciudad, codigo_postal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, calle);
            stmt.setString(3, ciudad);
            stmt.setString(4, codigoPostal);
            stmt.executeUpdate();
        }
    }

    private void update(Connection conn) throws SQLException {
        String sql = "UPDATE Direcciones SET calle = ?, ciudad = ?, codigo_postal = ? WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, calle);
            stmt.setString(2, ciudad);
            stmt.setString(3, codigoPostal);
            stmt.setInt(4, usuarioId);
            stmt.executeUpdate();
        }
    }

    public void delete() throws SQLException {
        Connection conn = ConnectionBD.getConn();
        conn.setAutoCommit(false);
        try {
            delete(conn);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void delete(Connection conn) throws SQLException {
        String sql = "DELETE FROM Direcciones WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        }
    }

    // Métodos estáticos de consulta
    public static Direccion findByUsuarioId(int usuarioId) throws SQLException {
        String sql = "SELECT * FROM Direcciones WHERE usuario_id = ?";
        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Direccion direccion = new Direccion();
                    direccion.setUsuarioId(usuarioId);
                    direccion.setCalle(rs.getString("calle"));
                    direccion.setCiudad(rs.getString("ciudad"));
                    direccion.setCodigoPostal(rs.getString("codigo_postal"));
                    return direccion;
                }
            }
        }
        return null;
    }

    // equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Direccion direccion = (Direccion) o;
        return usuarioId == direccion.usuarioId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId);
    }

    @Override
    public String toString() {
        return "Direccion{" +
                "usuarioId=" + usuarioId +
                ", calle='" + calle + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", codigoPostal='" + codigoPostal + '\'' +
                '}';
    }
}