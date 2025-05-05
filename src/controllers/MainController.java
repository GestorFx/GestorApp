package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Usuario;
import models.Pedido;
import services.ConnectionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class MainController {

    @FXML private ToggleButton usuariosBtn;
    @FXML private ToggleButton pedidosBtn;
    @FXML private TableView<Object> tablaDatos;

    // Columnas para Usuarios
    @FXML private TableColumn<Object, Integer> colId;
    @FXML private TableColumn<Object, String> colNombre;
    @FXML private TableColumn<Object, String> colApellido;
    @FXML private TableColumn<Object, String> colEmail;
    @FXML private TableColumn<Object, String> colCalle;
    @FXML private TableColumn<Object, String> colCiudad;
    @FXML private TableColumn<Object, String> colCP;

    // Columnas para Pedidos
    @FXML private TableColumn<Object, Integer> colPedidoId;
    @FXML private TableColumn<Object, Integer> colUsuarioId;
    @FXML private TableColumn<Object, Integer> colProductoId;
    @FXML private TableColumn<Object, Integer> colCantidad;
    @FXML private TableColumn<Object, LocalDate> colFecha;
    @FXML private TableColumn<Object, String> colProductoNombre;
    @FXML private TableColumn<Object, Double> colProductoPrecio;
    @FXML private TableColumn<Object, String> colUsuarioNombre;
    @FXML private TableColumn<Object, String> colUsuarioApellido;


    @FXML
    public void initialize() {

        System.out.println("tablaDatos: " + tablaDatos);
        System.out.println("colPedidoId: " + colPedidoId);
        if (colPedidoId == null) {
            colPedidoId = new TableColumn<>("Pedido ID");
        }
        if (colUsuarioId == null) {
            colUsuarioId = new TableColumn<>("Usuario ID");
        }
        if (colProductoId == null) {
            colProductoId = new TableColumn<>("Producto ID");
        }
        if (colCantidad == null) {
            colCantidad = new TableColumn<>("Cantidad");
        }
        if (colFecha == null) {
            colFecha = new TableColumn<>("Fecha");
        }
        if (colProductoNombre == null) {
            colProductoNombre = new TableColumn<>("Producto");
        }
        if (colProductoPrecio == null) {
            colProductoPrecio = new TableColumn<>("Precio");
        }
        if (colUsuarioNombre == null) {
            colUsuarioNombre = new TableColumn<>("Nombre Usuario");
        }
        if (colUsuarioApellido == null) {
            colUsuarioApellido = new TableColumn<>("Apellido Usuario");
        }
    

        usuariosBtn.setOnAction(event -> cargarUsuarios());
        pedidosBtn.setOnAction(event -> cargarPedidos());
    }

    private void cargarUsuarios() {
        tablaDatos.getColumns().clear();
        tablaDatos.getItems().clear();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colCalle.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    if (cellData.getValue() instanceof Usuario) {
                        Usuario usuario = (Usuario) cellData.getValue();
                        return usuario.getDireccion() != null ? usuario.getDireccion().getCalle() : "";
                    }
                    return "";
                }
        ));
        colCiudad.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    if (cellData.getValue() instanceof Usuario) {
                        Usuario usuario = (Usuario) cellData.getValue();
                        return usuario.getDireccion() != null ? usuario.getDireccion().getCiudad() : "";
                    }
                    return "";
                }
        ));
        colCP.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    if (cellData.getValue() instanceof Usuario) {
                        Usuario usuario = (Usuario) cellData.getValue();
                        return usuario.getDireccion() != null ? usuario.getDireccion().getCodigoPostal() : "";
                    }
                    return "";
                }
        ));

        tablaDatos.getColumns().addAll(colId, colNombre, colApellido, colEmail, colCalle, colCiudad, colCP);

        ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
        String query = "SELECT u.id, u.nombre, u.apellido, u.email, d.calle, d.ciudad, d.codigo_postal " +
                "FROM Usuarios u " +
                "LEFT JOIN Direcciones d ON u.id = d.usuario_id";

        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("email")
                );
                usuarios.add(usuario);
            }

            tablaDatos.setItems(FXCollections.observableArrayList(usuarios));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarPedidos() {
        tablaDatos.getColumns().clear();
        tablaDatos.getItems().clear();

        // Configurar columnas de pedidos
        colPedidoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuarioId.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
        colProductoId.setCellValueFactory(new PropertyValueFactory<>("productoId"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colProductoNombre.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colProductoPrecio.setCellValueFactory(new PropertyValueFactory<>("productoPrecio"));
        colUsuarioNombre.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colUsuarioApellido.setCellValueFactory(new PropertyValueFactory<>("usuarioApellido"));

        tablaDatos.getColumns().addAll(colPedidoId, colUsuarioId, colProductoId, colCantidad, colFecha, colProductoNombre, colProductoPrecio, colUsuarioNombre, colUsuarioApellido);

        ObservableList<Pedido> pedidos = FXCollections.observableArrayList();

        String query = "SELECT p.id AS pedido_id, p.usuario_id, p.producto_id, p.cantidad, p.fecha, " +
                "pr.nombre AS producto_nombre, pr.precio AS producto_precio, " +
                "u.nombre AS usuario_nombre, u.apellido AS usuario_apellido " +
                "FROM pedidos p " +
                "JOIN productos pr ON p.producto_id = pr.id " +
                "JOIN usuarios u ON p.usuario_id = u.id";

        try (Connection conn = ConnectionBD.getConn();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pedidos.add(new Pedido(
                        rs.getInt("pedido_id"),
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

            tablaDatos.setItems(FXCollections.observableArrayList(pedidos.stream().map(p -> (Object) p).toList()));


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
