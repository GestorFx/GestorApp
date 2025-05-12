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

    private String productoNombre;
    private double productoPrecio;
    private String usuarioNombreCompleto;

    public Pedido() {}

    public Pedido(int id, int usuarioId, int productoId, int cantidad, LocalDate fecha) {
        this.id = id;
        setUsuarioId(usuarioId);
        setProductoId(productoId);
        setCantidad(cantidad);
        setFecha(fecha);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) {
        if (usuarioId <= 0) throw new IllegalArgumentException("ID de usuario no válido");
        this.usuarioId = usuarioId;
    }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) {
        if (productoId <= 0) throw new IllegalArgumentException("ID de producto no válido");
        this.productoId = productoId;
    }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        this.cantidad = cantidad;
    }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.fecha = fecha;
    }

    public String getProductoNombre() {
        if (productoNombre == null && productoId > 0 && (id > 0 || productoPrecio == 0.0)) {
            cargaProductoInfo();
        }
        return productoNombre;
    }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public double getProductoPrecio() {
        if (productoNombre == null && productoId > 0 && (id > 0 || productoPrecio == 0.0)) {
            cargaProductoInfo();
        }
        return productoPrecio;
    }
    public void setProductoPrecio(double productoPrecio) { this.productoPrecio = productoPrecio; }

    private void cargaProductoInfo() {
        System.out.println("Pedido.cargaProductoInfo: Cargando info para Producto ID: " + productoId + " (Pedido actual ID: " + this.id + ")");
        try {
            Producto p = Producto.findById(productoId);
            if (p != null) {
                this.productoNombre = p.getNombre();
                this.productoPrecio = p.getPrecio();
                System.out.println("Pedido.cargaProductoInfo: Info cargada para Producto ID: " + productoId + ", Nombre: " + this.productoNombre);
            } else {
                System.err.println("Pedido.cargaProductoInfo: Producto NO encontrado con ID: " + productoId);
            }
        } catch (SQLException e) {
            System.err.println("Pedido.cargaProductoInfo: Error cargando info de producto para pedido ID " + this.id + ", Producto ID: " + productoId);
            e.printStackTrace();
        }
    }

    public String getUsuarioNombreCompleto() {
        if (usuarioNombreCompleto == null && usuarioId > 0 && (id > 0 || usuarioNombreCompleto == null)) {
            System.out.println("Pedido.getUsuarioNombreCompleto: Cargando info para Usuario ID: " + usuarioId + " (Pedido actual ID: " + this.id + ")");
            try {
                Usuario u = Usuario.findById(this.usuarioId);
                if (u != null) {
                    this.usuarioNombreCompleto = u.getNombre() + (u.getApellido() != null ? " " + u.getApellido() : "");
                    System.out.println("Pedido.getUsuarioNombreCompleto: Info cargada para Usuario ID: " + usuarioId + ", Nombre: " + this.usuarioNombreCompleto);
                } else {
                    System.err.println("Pedido.getUsuarioNombreCompleto: Usuario NO encontrado con ID: " + usuarioId);
                }
            } catch (SQLException e) {
                System.err.println("Pedido.getUsuarioNombreCompleto: Error cargando info de usuario para pedido ID " + this.id + ", Usuario ID: " + usuarioId);
                e.printStackTrace();
            }
        }
        return usuarioNombreCompleto;
    }
    public void setUsuarioNombreCompleto(String usuarioNombreCompleto) { this.usuarioNombreCompleto = usuarioNombreCompleto; }

    public void save() throws SQLException {
        boolean originalAutoCommitState = true;
        System.out.println("DEBUG: Pedido.save - Iniciando guardado para Pedido (ID actual: " + this.id +
                ", UsuarioID: " + this.usuarioId + ", ProductoID: " + this.productoId + ")");


        try (Connection conn = ConnectionBD.getConn()) {

            System.out.println("DEBUG: Pedido.save - Conexión ("+ conn.hashCode() +") obtenida para la transacción. ¿Cerrada al inicio? " + conn.isClosed());
            originalAutoCommitState = conn.getAutoCommit();
            conn.setAutoCommit(false);
            System.out.println("DEBUG: Pedido.save - AutoCommit establecido a false.");

            System.out.println("DEBUG: Pedido.save - Validando Producto ID: " + this.productoId);
            Producto pExists = Producto.findById(this.productoId);
            if (pExists == null) {
                conn.rollback();
                System.err.println("ERROR: Pedido.save - Producto con ID " + this.productoId + " no existe. Rollback realizado.");
                throw new SQLException("El producto con ID " + this.productoId + " no existe y es requerido para el pedido.");
            }
            System.out.println("DEBUG: Pedido.save - Producto ID: " + this.productoId + " validado. ¿Conexión de Pedido.save ("+ conn.hashCode() +") cerrada ahora? " + conn.isClosed());

            System.out.println("DEBUG: Pedido.save - Validando Usuario ID: " + this.usuarioId);
            Usuario uExists = Usuario.findById(this.usuarioId);
            if (uExists == null) {
                conn.rollback();
                System.err.println("ERROR: Pedido.save - Usuario con ID " + this.usuarioId + " no existe. Rollback realizado.");
                throw new SQLException("El usuario con ID " + this.usuarioId + " no existe y es requerido para el pedido.");
            }
            System.out.println("DEBUG: Pedido.save - Usuario ID: " + this.usuarioId + " validado. ¿Conexión de Pedido.save ("+ conn.hashCode() +") cerrada ahora? " + conn.isClosed());

            if (this.id == 0) {
                System.out.println("DEBUG: Pedido.save - Insertando nuevo pedido.");
                insert(conn);
            } else {
                System.out.println("DEBUG: Pedido.save - Actualizando pedido existente ID: " + this.id);
                update(conn);
            }

            conn.commit();
            System.out.println("INFO: Pedido.save - Commit exitoso para Pedido ID: " + this.id);

        } catch (SQLException e) {
            System.err.println("ERROR: Pedido.save - SQLException durante el guardado del Pedido ID: " + this.id + ". Mensaje: " + e.getMessage());
            throw e;
        }
        System.out.println("DEBUG: Pedido.save - Método finalizado para Pedido ID: " + this.id);
    }

    private void insert(Connection conn) throws SQLException {
        String sql = "INSERT INTO Pedidos (usuario_id, producto_id, cantidad, fecha) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, this.usuarioId);
            stmt.setInt(2, this.productoId);
            stmt.setInt(3, this.cantidad);
            stmt.setDate(4, java.sql.Date.valueOf(this.fecha));

            System.out.println("DEBUG: Pedido.insert - Ejecutando PreparedStatement...");
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    this.id = rs.getInt(1);
                    System.out.println("INFO: Pedido.insert - Pedido insertado con nuevo ID: " + this.id);
                }
            }
        }
    }

    private void update(Connection conn) throws SQLException {
        String sql = "UPDATE Pedidos SET usuario_id = ?, producto_id = ?, cantidad = ?, fecha = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.usuarioId);
            stmt.setInt(2, this.productoId);
            stmt.setInt(3, this.cantidad);
            stmt.setDate(4, java.sql.Date.valueOf(this.fecha));
            stmt.setInt(5, this.id);

            System.out.println("DEBUG: Pedido.update - Ejecutando PreparedStatement para ID: " + this.id);
            int affectedRows = stmt.executeUpdate();
            System.out.println("INFO: Pedido.update - Filas afectadas: " + affectedRows);
        }
    }

    public void delete() throws SQLException {
        if (this.id == 0) throw new IllegalStateException("Pedido ID no válido para borrar.");
        Connection conn = null;
        boolean originalAutoCommit = true;
        System.out.println("Pedido.delete: Iniciando borrado para Pedido ID: " + this.id);
        try {
            conn = ConnectionBD.getConn();
            if (conn == null || conn.isClosed()) throw new SQLException("Conexión nula o cerrada al intentar borrar pedido.");
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String sql = "DELETE FROM Pedidos WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, this.id);
                int affectedRows = stmt.executeUpdate();
                System.out.println("Pedido.delete: Filas afectadas: " + affectedRows + " para Pedido ID: " + this.id);
            }
            if (!conn.isClosed()) conn.commit(); else throw new SQLException("Conexión cerrada antes del commit en delete.");
            System.out.println("Pedido.delete: Commit realizado para Pedido ID: " + this.id);
        } catch (SQLException e) {
            System.err.println("Pedido.delete: SQLException para Pedido ID: " + this.id + ". Mensaje: " + e.getMessage());
            if (conn != null && !conn.isClosed()) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Pedido.delete: Error en rollback: " + ex.getMessage()); }
            }
            throw e;
        } finally {
            if (conn != null && !conn.isClosed()) {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ex) { /* Ignorar error al restaurar autocommit si ya hay problemas */ }
                try { conn.close(); } catch (SQLException ex) { /* Ignorar error al cerrar si ya hay problemas */ }
            }
            System.out.println("Pedido.delete: Finalizado borrado para Pedido ID: " + this.id);
        }
    }

    public static void deleteByUsuarioId(Connection conn, int uId) throws SQLException {
        Objects.requireNonNull(conn, "La conexión no puede ser nula para deleteByUsuarioId");
        if (conn.isClosed()) throw new SQLException("La conexión está cerrada para deleteByUsuarioId");

        System.out.println("Pedido.deleteByUsuarioId (static): Intentando borrar pedidos para Usuario ID: " + uId);
        String sql = "DELETE FROM Pedidos WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Pedido.deleteByUsuarioId (static): Filas afectadas en Pedidos: " + affectedRows + " para Usuario ID: " + uId);
        } catch (SQLException e) {
            System.err.println("Pedido.deleteByUsuarioId (static): SQLException al borrar pedidos para Usuario ID: " + uId + ". Mensaje: " + e.getMessage());
            throw e;
        }
    }

    public static void deleteByProductoId(Connection conn, int pId) throws SQLException {
        Objects.requireNonNull(conn, "La conexión no puede ser nula para deleteByProductoId");
        if (conn.isClosed()) throw new SQLException("La conexión está cerrada para deleteByProductoId");

        System.out.println("Pedido.deleteByProductoId (static): Intentando borrar pedidos para Producto ID: " + pId);
        String sql = "DELETE FROM Pedidos WHERE producto_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Pedido.deleteByProductoId (static): Filas afectadas en Pedidos: " + affectedRows + " para Producto ID: " + pId);
        } catch (SQLException e) {
            System.err.println("Pedido.deleteByProductoId (static): SQLException al borrar pedidos para Producto ID: " + pId + ". Mensaje: " + e.getMessage());
            throw e;
        }
    }

    public static Pedido findById(int id) throws SQLException {
        String sql = "SELECT p.*, pr.nombre as producto_nombre, pr.precio as producto_precio, " +
                "u.nombre as usuario_nombre, u.apellido as usuario_apellido " +
                "FROM Pedidos p " +
                "JOIN Productos pr ON p.producto_id = pr.id " +
                "JOIN Usuarios u ON p.usuario_id = u.id " +
                "WHERE p.id = ?";
        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        }
        return null;
    }

    public static List<Pedido> findByUsuarioId(int usuarioId) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, pr.nombre as producto_nombre, pr.precio as producto_precio, " +
                "u.nombre as usuario_nombre, u.apellido as usuario_apellido " +
                "FROM Pedidos p " +
                "JOIN Productos pr ON p.producto_id = pr.id " +
                "JOIN Usuarios u ON p.usuario_id = u.id " +
                "WHERE p.usuario_id = ?";
        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) pedidos.add(fromResultSet(rs));
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
                "JOIN Usuarios u ON p.usuario_id = u.id ORDER BY p.fecha DESC, p.id DESC";
        try (Connection conn = ConnectionBD.getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) pedidos.add(fromResultSet(rs));
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
        pedido.setProductoNombre(rs.getString("producto_nombre"));
        pedido.setProductoPrecio(rs.getDouble("producto_precio"));
        String uNombre = rs.getString("usuario_nombre");
        String uApellido = rs.getString("usuario_apellido");
        pedido.setUsuarioNombreCompleto(uNombre + (uApellido != null && !uApellido.trim().isEmpty() ? " " + uApellido.trim() : ""));
        return pedido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id == pedido.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
    @Override
    public String toString() {
        return "ID:" + id + " Usr:" + (usuarioNombreCompleto!=null?usuarioNombreCompleto:usuarioId) + " Prod:" + (productoNombre!=null?productoNombre:productoId) + " Cant:" + cantidad + " Fecha:" + fecha;
    }
}