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
    // private int stock;
    private int categoriaId;
    private String categoriaNombre;

    public Producto() {}

    public Producto(int id, String nombre, double precio, int categoriaId) {
        this.id = id;
        setNombre(nombre);
        setPrecio(precio);
        // setStock(stock);
        setCategoriaId(categoriaId);
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    // public int getStock() { return stock; }
    public int getCategoriaId() { return categoriaId; }
    public String getCategoriaNombre() { return categoriaNombre; }

    public void setId(int id) { this.id = id; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = precio;
    }

    // public void setStock(int stock) {
    //     if (stock < 0) {
    //         throw new IllegalArgumentException("El stock no puede ser negativo.");
    //     }
    //     this.stock = stock;
    // }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public void save() throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (this.categoriaId > 0) {
                Categoria categoriaExistente = Categoria.findById(this.categoriaId);
                if (categoriaExistente == null) {
                    throw new SQLException("La categoría con ID " + this.categoriaId + " no existe.");
                }
            }

            if (this.id == 0) {
                insert(conn);
            } else {
                update(conn);
            }
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

    private void insert(Connection conn) throws SQLException {
        String sql = "INSERT INTO Productos (nombre, precio, categoria_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.nombre);
            stmt.setDouble(2, this.precio);
            // stmt.setInt(3, this.stock);
            if (this.categoriaId > 0) {
                stmt.setInt(3, this.categoriaId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
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
            stmt.setString(1, this.nombre);
            stmt.setDouble(2, this.precio);
            // stmt.setInt(3, this.stock);
            if (this.categoriaId > 0) {
                stmt.setInt(3, this.categoriaId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, this.id);
            stmt.executeUpdate();
        }
    }

    public void delete() throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        if (this.id == 0) throw new IllegalStateException("ID de producto no válido para borrar.");
        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            Pedido.deleteByProductoId(conn, this.id);

            String sql = "DELETE FROM Productos WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, this.id);
                stmt.executeUpdate();
            }
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

    public static Producto findById(int id) throws SQLException {
        String sql = "SELECT p.id, p.nombre, p.precio, p.categoria_id, c.nombre as nombre_categoria " +
                "FROM Productos p " +
                "LEFT JOIN Categorias c ON p.categoria_id = c.id WHERE p.id = ?";
        Producto producto = null;
        System.out.println("DEBUG: Producto.findById(" + id + ") - Iniciando búsqueda.");

        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("DEBUG: Producto.findById(" + id + ") - Conexión obtenida ("+ conn.hashCode() +"), Statement preparado.");
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("DEBUG: Producto.findById(" + id + ") - Query ejecutada, procesando ResultSet.");
                if (rs.next()) {
                    producto = fromResultSet(rs);
                    System.out.println("DEBUG: Producto.findById(" + id + ") - Producto ENCONTRADO: " + producto.getNombre());
                } else {
                    System.out.println("DEBUG: Producto.findById(" + id + ") - Producto NO encontrado.");
                }
            }
        }
        catch (SQLException e) {
            System.err.println("ERROR: Producto.findById(" + id + ") - SQLException: " + e.getMessage());
            // e.printStackTrace();
            throw e;
        }
        System.out.println("DEBUG: Producto.findById(" + id + ") - Finalizado. Producto devuelto: " + (producto != null ? producto.getNombre() : "null"));
        return producto;
    }

    public static List<Producto> findAll() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as nombre_categoria FROM Productos p " +
                "LEFT JOIN Categorias c ON p.categoria_id = c.id ORDER BY p.nombre ASC";
        try (Connection conn = ConnectionBD.getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(fromResultSet(rs));
            }
        }
        return productos;
    }

    private static Producto fromResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setPrecio(rs.getDouble("precio"));
        // producto.setStock(rs.getInt("stock"));

        int catId = rs.getInt("categoria_id");
        if (rs.wasNull()) {
            producto.setCategoriaId(0);
        } else {
            producto.setCategoriaId(catId);
        }

        try {
            ResultSetMetaData metaData = rs.getMetaData();
            boolean found = false;
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if ("nombre_categoria".equalsIgnoreCase(metaData.getColumnLabel(i))) {
                    found = true;
                    break;
                }
            }
            if (found) {
                producto.setCategoriaNombre(rs.getString("nombre_categoria"));
            } else {
                producto.setCategoriaNombre(null);
            }
        } catch (SQLException e) {
            producto.setCategoriaNombre(null);
        }
        return producto;
    }

    public static List<Producto> findByCategoriaId(int categoriaId) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as nombre_categoria FROM Productos p " +
                "LEFT JOIN Categorias c ON p.categoria_id = c.id WHERE p.categoria_id = ? ORDER BY p.nombre ASC";
        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(fromResultSet(rs));
                }
            }
        }
        return productos;
    }

    public static int countByCategoriaId(Connection conn, int categoriaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Productos WHERE categoria_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return nombre + " - " + precio + "€" + (categoriaNombre != null ? " [" + categoriaNombre + "]" : "");
    }

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
}