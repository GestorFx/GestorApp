package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import services.ConnectionBD;

public class Categoria {
    private int id;
    private String nombre;

    public Categoria() {}

    public Categoria(int id, String nombre) {
        this.id = id;
        setNombre(nombre);
    }

    public Categoria(String nombre) {
        setNombre(nombre);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public void save() throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (this.id == 0) {
                insert(conn);
            } else {
                update(conn);
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(originalAutoCommit);
        }
    }

    private void insert(Connection conn) throws SQLException {
        String sql = "INSERT INTO Categorias (nombre) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.nombre);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            }
        }
    }

    private void update(Connection conn) throws SQLException {
        String sql = "UPDATE Categorias SET nombre = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.nombre);
            stmt.setInt(2, this.id);
            stmt.executeUpdate();
        }
    }

    public void delete() throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        if (this.id == 0) throw new IllegalStateException("No se puede borrar una categoría sin ID.");

        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (Producto.countByCategoriaId(conn, this.id) > 0) {
                throw new SQLException("No se puede borrar la categoría porque tiene productos asociados.");
            }

            String sql = "DELETE FROM Categorias WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, this.id);
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(originalAutoCommit);
        }
    }

    public static Categoria findById(int id) throws SQLException {
        String sql = "SELECT * FROM Categorias WHERE id = ?";
        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id"), rs.getString("nombre"));
                }
            }
        }
        return null;
    }

    public static List<Categoria> findAll() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM Categorias ORDER BY nombre";
        try (Connection conn = ConnectionBD.getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return categorias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return id == categoria.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}