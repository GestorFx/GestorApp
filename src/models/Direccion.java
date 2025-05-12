package models;

import java.sql.*;
import java.util.Objects;
import services.ConnectionBD;

public class Direccion {
    private int usuarioId;
    private String calle;
    private String ciudad;
    private String codigoPostal;

    public Direccion() {}

    public Direccion(int usuarioId, String calle, String ciudad, String codigoPostal) {
        this.usuarioId = usuarioId;
        setCalle(calle);
        setCiudad(ciudad);
        setCodigoPostal(codigoPostal);
    }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) {
        if (calle == null || calle.trim().isEmpty()) throw new IllegalArgumentException("La calle no puede estar vacía");
        this.calle = calle.trim();
    }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) {
        if (ciudad == null || ciudad.trim().isEmpty()) throw new IllegalArgumentException("La ciudad no puede estar vacía");
        this.ciudad = ciudad.trim();
    }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.trim().isEmpty() || !codigoPostal.matches("\\d{5}")) {
            throw new IllegalArgumentException("El código postal no puede estar vacío y debe tener 5 dígitos.");
        }
        this.codigoPostal = codigoPostal.trim();
    }

    public void save(Connection conn) throws SQLException {
        if (usuarioId <= 0) throw new SQLException("ID de usuario no válido para guardar dirección.");
        // System.out.println("Direccion.save(conn): Verificando si existe dirección para usuario ID: " + usuarioId);
        if (exists(conn, this.usuarioId)) { // Pass usuarioId to exists
            // System.out.println("Direccion.save(conn): Actualizando dirección para usuario ID: " + usuarioId);
            update(conn);
        } else {
            // System.out.println("Direccion.save(conn): Insertando dirección para usuario ID: " + usuarioId);
            insert(conn);
        }
    }

    public void save() throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            save(conn);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ex) { ex.printStackTrace(); }
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    private boolean exists(Connection conn, int uId) throws SQLException {
        String sql = "SELECT 1 FROM Direcciones WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insert(Connection conn) throws SQLException {
        String sql = "INSERT INTO Direcciones (usuario_id, calle, ciudad, codigo_postal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.usuarioId);
            stmt.setString(2, this.calle);
            stmt.setString(3, this.ciudad);
            stmt.setString(4, this.codigoPostal);
            stmt.executeUpdate();
        }
    }

    private void update(Connection conn) throws SQLException {
        String sql = "UPDATE Direcciones SET calle = ?, ciudad = ?, codigo_postal = ? WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.calle);
            stmt.setString(2, this.ciudad);
            stmt.setString(3, this.codigoPostal);
            stmt.setInt(4, this.usuarioId);
            stmt.executeUpdate();
        }
    }

    public static void deleteByUsuarioId(Connection conn, int uId) throws SQLException {
        System.out.println("Direccion.deleteByUsuarioId (static): Intentando borrar dirección para Usuario ID: " + uId);
        String sql = "DELETE FROM Direcciones WHERE usuario_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Direccion.deleteByUsuarioId (static): Filas afectadas en Direcciones: " + affectedRows + " para Usuario ID: " + uId);
            if (affectedRows == 0) {
                System.out.println("Direccion.deleteByUsuarioId (static): No se encontró/borró dirección para Usuario ID: " + uId);
            }
        } catch (SQLException e) {
            System.err.println("Direccion.deleteByUsuarioId (static): SQLException al borrar dirección para Usuario ID: " + uId + ". Mensaje: " + e.getMessage());
            throw e;
        }
    }

    public void delete() throws SQLException {
        if (this.usuarioId <= 0) throw new SQLException("ID de usuario no válido para borrar dirección.");
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            Direccion.deleteByUsuarioId(conn, this.usuarioId);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ex) { ex.printStackTrace(); }
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }


    public static Direccion findByUsuarioId(int usuarioId) throws SQLException {
        // System.out.println("Direccion.findByUsuarioId: Buscando dirección para usuario ID: " + usuarioId);
        String sql = "SELECT * FROM Direcciones WHERE usuario_id = ?";
        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Direccion direccion = new Direccion();
                    direccion.setUsuarioId(rs.getInt("usuario_id"));
                    direccion.setCalle(rs.getString("calle"));
                    direccion.setCiudad(rs.getString("ciudad"));
                    direccion.setCodigoPostal(rs.getString("codigo_postal"));
                    // System.out.println("Direccion.findByUsuarioId: Dirección encontrada para usuario ID: " + usuarioId);
                    return direccion;
                } else {
                    // System.out.println("Direccion.findByUsuarioId: No se encontró dirección para usuario ID: " + usuarioId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Direccion.findByUsuarioId: SQLException para usuario ID " + usuarioId + ": " + e.getMessage());
            throw e;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Direccion direccion = (Direccion) o;
        return usuarioId == direccion.usuarioId;
    }
    @Override
    public int hashCode() { return Objects.hash(usuarioId); }
    @Override
    public String toString() { return calle + ", " + ciudad + ", " + codigoPostal; }
}