package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import services.ConnectionBD;

public class Categoria {
    private int id;
    private String nombre;
    private List<Producto> productos; // Relación con productos

    // Constructores
    public Categoria() {
        this.productos = new ArrayList<>();
    }

    public Categoria(String nombre) {
        this();
        this.nombre = nombre;
    }

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

    public List<Producto> getProductos() throws SQLException {
        if (productos == null && id > 0) {
            this.productos = Producto.findByCategoriaId(id);
        }
        return new ArrayList<>(productos);
    }

    public void setProductos(List<Producto> productos) {
        this.productos = (productos != null) ? new ArrayList<>(productos) : new ArrayList<>();
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
        String sql = "INSERT INTO Categorias (nombre) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
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
            stmt.setString(1, nombre);
            stmt.setInt(2, id);
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
        // Verificar si hay productos asociados
        if (tieneProductosAsociados(conn)) {
            throw new SQLException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        String sql = "DELETE FROM Categorias WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private boolean tieneProductosAsociados(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Productos WHERE categoria_id = ?";
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
    public static Categoria findById(int id) throws SQLException {
        String sql = "SELECT * FROM Categorias WHERE id = ?";
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

    public static List<Categoria> findAll() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM Categorias";
        try (Statement stmt = ConnectionBD.getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categorias.add(fromResultSet(rs));
            }
        }
        return categorias;
    }

    public static List<Categoria> findByNombre(String nombreBusqueda) throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM Categorias WHERE nombre LIKE ?";
        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setString(1, "%" + nombreBusqueda + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categorias.add(fromResultSet(rs));
                }
            }
        }
        return categorias;
    }

    private static Categoria fromResultSet(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setId(rs.getInt("id"));
        categoria.setNombre(rs.getString("nombre"));
        return categoria;
    }

    // equals, hashCode y toString
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