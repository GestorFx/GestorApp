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

    public Usuario() {
        this.pedidos = new ArrayList<>();
    }

    public Usuario(String nombre, String apellido, String email) {
        this();
        setNombre(nombre);
        setApellido(apellido);
        setEmail(email);
    }

    public Usuario(Integer id, String nombre, String apellido, String email) {
        this(nombre, apellido, email);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = (apellido != null) ? apellido.trim() : null; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            if (email.trim().isEmpty()) {
                this.email = null;
            } else {
                throw new IllegalArgumentException("El email debe ser válido o estar vacío/nulo");
            }
        } else {
            this.email = (email != null && !email.trim().isEmpty()) ? email.trim().toLowerCase() : null;
        }
    }

    public Direccion getDireccion() throws SQLException {
        if (direccion == null && id != null && id > 0) {
            this.direccion = Direccion.findByUsuarioId(id);
        }
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
        if (this.direccion != null && this.id != null && this.id > 0) {
            this.direccion.setUsuarioId(this.id);
        }
    }

    public List<Pedido> getPedidos() throws SQLException {
        if (this.pedidos.isEmpty() && id != null && id > 0) {
            this.pedidos = Pedido.findByUsuarioId(id);
        }
        return new ArrayList<>(this.pedidos);
    }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = (pedidos != null) ? new ArrayList<>(pedidos) : new ArrayList<>(); }

    public String getCalle() throws SQLException { Direccion d = getDireccion(); return d != null ? d.getCalle() : ""; }
    public String getCiudad() throws SQLException { Direccion d = getDireccion(); return d != null ? d.getCiudad() : ""; }
    public String getCodigoPostal() throws SQLException { Direccion d = getDireccion(); return d != null ? d.getCodigoPostal() : ""; }


    public void save() throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (id == null || id == 0) {
                insert(conn);
            } else {
                update(conn);
            }

            if (this.direccion != null) {
                if (this.id == null) throw new SQLException("Usuario ID no establecido después de insert/update para guardar dirección.");
                this.direccion.setUsuarioId(this.id);
                this.direccion.save(conn);
            } else {
                if (this.id != null) {
                    Direccion.deleteByUsuarioId(conn, this.id);
                }
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
        String sql = "INSERT INTO Usuarios (nombre, apellido, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, email);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    this.id = rs.getInt(1);
                } else {
                    throw new SQLException("Fallo al insertar usuario, no se obtuvo ID.");
                }
            }
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
    }

    public void delete() throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;

        if (this.id == null || this.id <= 0) {
            System.err.println("Usuario.delete: Intento de borrar Usuario sin ID válido (ID: " + this.id + ").");
            throw new IllegalStateException("No se puede borrar un usuario sin un ID válido.");
        }

        System.out.println("Usuario.delete: ***** INICIANDO BORRADO para Usuario ID: " + this.id + " *****");

        try {
            conn = ConnectionBD.getConn();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            System.out.println("Usuario.delete: Conexión obtenida, autoCommit=false para Usuario ID: " + this.id);

            System.out.println("Usuario.delete: Intentando borrar pedidos para Usuario ID: " + this.id);
            Pedido.deleteByUsuarioId(conn, this.id);
            System.out.println("Usuario.delete: Pedidos (si los había) borrados para Usuario ID: " + this.id);

            System.out.println("Usuario.delete: Intentando borrar dirección para Usuario ID: " + this.id);
            Direccion.deleteByUsuarioId(conn, this.id);
            System.out.println("Usuario.delete: Dirección (si la había) borrada para Usuario ID: " + this.id);

            String sqlUsuario = "DELETE FROM Usuarios WHERE id = ?";
            System.out.println("Usuario.delete: Ejecutando SQL: " + sqlUsuario + " para Usuario ID: " + this.id);
            try (PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario)) {
                stmtUsuario.setInt(1, this.id);
                int affectedRows = stmtUsuario.executeUpdate();
                System.out.println("Usuario.delete: Filas afectadas por DELETE en Usuarios: " + affectedRows + " para Usuario ID: " + this.id);
                if (affectedRows == 0) {
                    System.err.println("Usuario.delete: ADVERTENCIA: El borrado del Usuario ID " + this.id + " no afectó a ninguna fila.");
                }
            }

            conn.commit();
            System.out.println("Usuario.delete: COMMIT realizado para el borrado del Usuario ID: " + this.id);

        } catch (SQLException e) {
            System.err.println("Usuario.delete: SQLException durante el borrado del Usuario ID: " + this.id + ". Mensaje: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Usuario.delete: Intentando ROLLBACK para Usuario ID: " + this.id);
                    conn.rollback();
                    System.err.println("Usuario.delete: ROLLBACK realizado para Usuario ID: " + this.id);
                } catch (SQLException exRollback) {
                    System.err.println("Usuario.delete: Error durante el ROLLBACK para Usuario ID: " + this.id + ". Mensaje: " + exRollback.getMessage());
                    exRollback.printStackTrace();
                }
            }
            throw e;
        } finally {
            System.out.println("Usuario.delete: Bloque finally alcanzado para Usuario ID: " + this.id);
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        System.out.println("Usuario.delete: Restaurando autoCommit a " + originalAutoCommit + " para Usuario ID: " + this.id);
                        conn.setAutoCommit(originalAutoCommit);
                        System.out.println("Usuario.delete: Cerrando conexión para Usuario ID: " + this.id);
                        conn.close();
                        System.out.println("Usuario.delete: Conexión cerrada para Usuario ID: " + this.id);
                    } else {
                        System.out.println("Usuario.delete: Conexión ya estaba cerrada en finally para Usuario ID: " + this.id);
                    }
                } catch (SQLException exFinal) {
                    System.err.println("Usuario.delete: SQLException en bloque finally para Usuario ID: " + this.id + ". Mensaje: " + exFinal.getMessage());
                    exFinal.printStackTrace();
                }
            }
        }
    }

    public static Usuario findById(int id) throws SQLException {
        System.out.println("Usuario.findById: Buscando usuario con ID: " + id);
        String sql = "SELECT * FROM Usuarios WHERE id = ?";
        Usuario usuario = null;
        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("email")
                    );
                    System.out.println("Usuario.findById: Usuario ENCONTRADO con ID: " + id + ", Nombre: " + usuario.getNombre());
                } else {
                    System.out.println("Usuario.findById: NO se encontró usuario con ID: " + id);
                }
            }
        } catch (SQLException e) {
            System.err.println("Usuario.findById: SQLException al buscar usuario con ID: " + id + ". Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return usuario;
    }

    public static List<Usuario> findAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM Usuarios";
        try (Connection conn = ConnectionBD.getConn();
             Statement stmt = conn.createStatement();
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
        return usuarios;
    }

    public static Usuario findByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) return null;
        String sql = "SELECT * FROM Usuarios WHERE email = ?";
        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
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
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return nombre + " " + (apellido != null ? apellido : "") + (email != null ? " (" + email + ")" : ""); }
}