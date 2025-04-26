package models;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import services.ConnectionBD;


public class Pedido {
    private int id;
    private int usuarioId;
    private int productoId;
    private int cantidad;
    private LocalDate fecha;

    public Pedido(int id, int usuarioId, int productoId, int cantidad, LocalDate fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public Pedido() {

    }

    // Getters y Setters con validación básica
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("ID de usuario no válido");
        }
        this.usuarioId = usuarioId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        if (productoId <= 0) {
            throw new IllegalArgumentException("ID de producto no válido");
        }
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.fecha = fecha;
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
        if (id == 0) {
            insert(conn);
        } else {
            update(conn);
        }
    }

    private void insert(Connection conn) throws SQLException {
        String sql = "INSERT INTO Pedidos (usuario_id, producto_id, cantidad, fecha) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, productoId);
            stmt.setInt(3, cantidad);
            stmt.setDate(4, Date.valueOf(fecha));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            }
        }
    }

    private void update(Connection conn) throws SQLException {
        String sql = "UPDATE Pedidos SET usuario_id = ?, producto_id = ?, cantidad = ?, fecha = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, productoId);
            stmt.setInt(3, cantidad);
            stmt.setDate(4, Date.valueOf(fecha));
            stmt.setInt(5, id);
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
            ConnectionBD.closeConnection();
        }
    }

    public void delete(Connection conn) throws SQLException {
        String sql = "DELETE FROM Pedidos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public static void deleteByUsuarioId(Connection conn, int usuarioId) throws SQLException {
        String sql = "DELETE FROM Pedidos WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        }
    }

    public static void deleteByUsuarioId(int usuarioId) throws SQLException {
        Connection conn = ConnectionBD.getConn();
        conn.setAutoCommit(false);
        try {
            deleteByUsuarioId(conn, usuarioId);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Métodos estáticos de consulta
    public static Pedido findById(int id) throws SQLException {
        String sql = "SELECT p.*, pr.nombre as producto_nombre, pr.precio as producto_precio " +
                "FROM Pedidos p " +
                "JOIN Productos pr ON p.producto_id = pr.id " +
                "WHERE p.id = ?";
        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return fromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static List<Pedido> findByUsuarioId(int usuarioId) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, pr.nombre as producto_nombre, pr.precio as producto_precio " +
                "FROM Pedidos p " +
                "JOIN Productos pr ON p.producto_id = pr.id " +
                "WHERE p.usuario_id = ?";
        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(fromResultSet(rs));
                }
            }
        }
        return pedidos;
    }

    public static List<Pedido> findAll() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, pr.nombre as producto_nombre, pr.precio as producto_precio, " +
                "u.nombre as usuario_nombre, u.apellido as usuario_apellido " +
                "FROM Pedidos p " +
                "JOIN Productos pr ON p.producto_id = pr.id " +
                "JOIN Usuarios u ON p.usuario_id = u.id";
        try (Statement stmt = ConnectionBD.getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pedidos.add(fromResultSet(rs));
            }
        }
        return pedidos;
    }

    private static Pedido fromResultSet(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        pedido.setUsuarioId(rs.getInt("usuario_id"));
        pedido.setProductoId(rs.getInt("producto_id"));
        pedido.setCantidad(rs.getInt("cantidad"));
        pedido.setFecha(rs.getDate("fecha").toLocalDate());
        return pedido;
    }

    // equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id == pedido.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", fecha=" + fecha +
                '}';
    }
}