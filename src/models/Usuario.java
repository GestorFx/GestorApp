package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import services.ConnectionBD;


public class Usuario {
    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private Direccion direccion;
    private List<Pedido> pedidos;

    // Constructores
    public Usuario() {
        this.pedidos = new ArrayList<>();
    }

    public Usuario(String nombre, String apellido, String email) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
    }

    public Usuario(Integer id, String nombre, String apellido, String email) {
        this(nombre, apellido, email);
        this.id = id;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = (apellido != null) ? apellido.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && !email.contains("@")) {
            throw new IllegalArgumentException("El email debe ser válido");
        }
        this.email = (email != null) ? email.trim().toLowerCase() : null;
    }

    public Direccion getDireccion() throws SQLException {
        if (direccion == null && id != null) {
            this.direccion = direccion.findByUsuarioId(id);
        }
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
        if (direccion != null && id != null) {
            direccion.setUsuarioId(id);
        }
    }

    public List<Pedido> getPedidos() throws SQLException {
        if (pedidos == null && id != null) {
            this.pedidos = Pedido.findByUsuarioId(id);
        }
        return new ArrayList<>(pedidos);
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = (pedidos != null) ? new ArrayList<>(pedidos) : new ArrayList<>();
    }

    // Métodos de negocio
    public void agregarPedido(Pedido pedido) {
        Objects.requireNonNull(pedido, "El pedido no puede ser nulo");
        if (!pedidos.contains(pedido)) {
            pedidos.add(pedido);
            pedido.setUsuarioId(id);
        }
    }

    public void removerPedido(Pedido pedido) {
        pedidos.remove(pedido);
    }

    // Métodos de persistencia
    public void save() throws SQLException {
        Connection conn = ConnectionBD.getConn();
        conn.setAutoCommit(false);

        try {
            if (id == null) {
                insert(conn);
            } else {
                update(conn);
            }

            if (direccion != null) {
                direccion.save(conn);
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            ConnectionBD.closeConnection();
        }
    }

    private void insert(Connection conn) throws SQLException {
        String sql = "INSERT INTO Usuarios (nombre, apellido, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, email);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            }
        }
        finally {
            ConnectionBD.closeConnection();
        }
    }

    private void update(Connection conn) throws SQLException {
        String sql = "UPDATE Usuarios SET nombre=?, apellido=?, email=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, email);
            stmt.setInt(4, id);
            stmt.executeUpdate();
        }
        finally {
            ConnectionBD.closeConnection();
        }
    }

    public void delete() throws SQLException {
        Connection conn = ConnectionBD.getConn();
        conn.setAutoCommit(false);

        try {
            Pedido.deleteByUsuarioId(conn, id);

            if (direccion != null) {
                direccion.delete(conn);
            }

            String sql = "DELETE FROM Usuarios WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            ConnectionBD.closeConnection();
        }
    }

    // Métodos estáticos de consulta
    public static Usuario findById(int id) throws SQLException {
        String sql = "SELECT * FROM Usuarios WHERE id=?";
        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("email")
                    );
                }
            }
        }
        finally {
            ConnectionBD.closeConnection();
        }
        return null;
    }

    public static List<Usuario> findAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM Usuarios";
        try (Statement stmt = ConnectionBD.getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("email")
                ));
            }
        }
        finally {
            ConnectionBD.closeConnection();
        }
        return usuarios;
    }

    // equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}