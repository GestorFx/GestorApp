package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import services.ConnectionBD;

public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private int categoriaId;
    private Categoria categoria; // Relación opcional

    // Getters y Setters con validación
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID no puede ser negativo");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("Precio no puede ser negativo");
        }
        this.precio = precio;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        if (categoriaId <= 0) {
            throw new IllegalArgumentException("ID de categoría no válido");
        }
        this.categoriaId = categoriaId;
    }

    public Categoria getCategoria() throws SQLException {
        if (categoria == null && categoriaId > 0) {
            this.categoria = Categoria.findById(categoriaId);
        }
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
        if (categoria != null) {
            this.categoriaId = categoria.getId();
        }
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
            ConnectionBD.closeConnection();
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
        String sql = "INSERT INTO Productos (nombre, precio, categoria_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setDouble(2, precio);
            stmt.setInt(3, categoriaId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            }
        }
    }

    private void update(Connection conn) throws SQLException {
        String sql = "UPDATE Productos SET nombre = ?, precio = ?, categoria_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setDouble(2, precio);
            stmt.setInt(3, categoriaId);
            stmt.setInt(4, id);
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
        // Primero verificar si hay pedidos asociados
        if (tienePedidosAsociados(conn)) {
            throw new SQLException("No se puede eliminar el producto porque tiene pedidos asociados");
        }

        String sql = "DELETE FROM Productos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private boolean tienePedidosAsociados(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Pedidos WHERE producto_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Métodos estáticos de consulta
    public static Producto findById(int id) throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM Productos p " +
                "LEFT JOIN Categorias c ON p.categoria_id = c.id " +
                "WHERE p.id = ?";
        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return fromResultSet(rs);
                }
            }
        }
        finally {
            ConnectionBD.closeConnection();
        }
        return null;
    }

    public static List<Producto> findByCategoriaId(int categoriaId) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM Productos p " +
                "LEFT JOIN Categorias c ON p.categoria_id = c.id " +
                "WHERE p.categoria_id = ?";
        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(fromResultSet(rs));
                }
            }
        }
        finally {
            ConnectionBD.closeConnection();
        }
        return productos;
    }

    public static List<Producto> findAll() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM Productos p " +
                "LEFT JOIN Categorias c ON p.categoria_id = c.id";
        try (Statement stmt = ConnectionBD.getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(fromResultSet(rs));
            }
        }
        finally {
            ConnectionBD.closeConnection();
        }
        return productos;
    }

    private static Producto fromResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setPrecio(rs.getDouble("precio"));
        producto.setCategoriaId(rs.getInt("categoria_id"));

        // Cargar categoría si existe
        if (rs.getString("categoria_nombre") != null) {
            Categoria categoria = new Categoria();
            categoria.setId(rs.getInt("categoria_id"));
            categoria.setNombre(rs.getString("categoria_nombre"));
            producto.setCategoria(categoria);
        }

        return producto;
    }

    // equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return id == producto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", categoriaId=" + categoriaId +
                '}';
    }
}