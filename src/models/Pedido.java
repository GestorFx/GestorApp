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
    private String usuarioNombre;
    private String usuarioApellido;

    // Constructor actualizado con nuevos atributos
    public Pedido(int id, int usuarioId, int productoId, int cantidad, LocalDate fecha, String productoNombre, double productoPrecio, String usuarioNombre, String usuarioApellido) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.productoNombre = productoNombre;
        this.productoPrecio = productoPrecio;
        this.usuarioNombre = usuarioNombre;
        this.usuarioApellido = usuarioApellido;
    }

    public Pedido() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public double getProductoPrecio() { return productoPrecio; }
    public void setProductoPrecio(double productoPrecio) { this.productoPrecio = productoPrecio; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getUsuarioApellido() { return usuarioApellido; }
    public void setUsuarioApellido(String usuarioApellido) { this.usuarioApellido = usuarioApellido; }

    public static List<Pedido> findByUsuarioId(Integer usuarioId) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, pr.nombre AS producto_nombre, pr.precio AS producto_precio, " +
                "u.nombre AS usuario_nombre, u.apellido AS usuario_apellido " +
                "FROM Pedidos p " +
                "JOIN Productos pr ON p.producto_id = pr.id " +
                "JOIN Usuarios u ON p.usuario_id = u.id " +
                "WHERE p.usuario_id = ?";

        try (PreparedStatement stmt = ConnectionBD.getConn().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(new Pedido(
                            rs.getInt("id"),
                            rs.getInt("usuario_id"),
                            rs.getInt("producto_id"),
                            rs.getInt("cantidad"),
                            rs.getDate("fecha").toLocalDate(),
                            rs.getString("producto_nombre"),
                            rs.getDouble("producto_precio"),
                            rs.getString("usuario_nombre"),
                            rs.getString("usuario_apellido")
                    ));
                }
            }
        }
        return pedidos;
    }


    public static void deleteByUsuarioId(Connection conn, Integer usuarioId) throws SQLException {
        String sql = "DELETE FROM Pedidos WHERE usuario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        }
    }



    // Método estático corregido para extraer información completa desde la base de datos
    public static List<Pedido> findAll() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, pr.nombre AS producto_nombre, pr.precio AS producto_precio, " +
                "u.nombre AS usuario_nombre, u.apellido AS usuario_apellido " +
                "FROM Pedidos p " +
                "JOIN Productos pr ON p.producto_id = pr.id " +
                "JOIN Usuarios u ON p.usuario_id = u.id";

        try (Statement stmt = ConnectionBD.getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pedidos.add(new Pedido(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getInt("producto_id"),
                        rs.getInt("cantidad"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getString("producto_nombre"),
                        rs.getDouble("producto_precio"),
                        rs.getString("usuario_nombre"),
                        rs.getString("usuario_apellido")
                ));
            }
        }
        return pedidos;
    }
}
