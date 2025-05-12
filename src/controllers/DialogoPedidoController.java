package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Pedido;
import models.Producto;
import models.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DialogoPedidoController {

    @FXML private ComboBox<Usuario> usuarioComboBox;
    @FXML private ComboBox<Producto> productoComboBox;
    @FXML private TextField cantidadField;
    @FXML private DatePicker fechaPicker;

    private Stage dialogStage;
    private Pedido pedido;
    private boolean guardadoExitoso = false;
    private MainController mainController;

    private PedidosController pedidosController;
    private UsuariosController usuariosController;
    private ProductosController productosController;

    @FXML
    public void initialize() {
        pedidosController = new PedidosController();
        usuariosController = new UsuariosController();
        productosController = new ProductosController();

        configurarComboBoxes();
        cargarDatosComboBoxes();

        fechaPicker.setValue(LocalDate.now());
    }

    private void configurarComboBoxes() {
        usuarioComboBox.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario == null ? null : usuario.getNombre() + " " + (usuario.getApellido() != null ? usuario.getApellido() : "") + " (ID: " + usuario.getId() + ")";
            }
            @Override
            public Usuario fromString(String string) { return null; }
        });

        productoComboBox.setConverter(new StringConverter<Producto>() {
            @Override
            public String toString(Producto producto) {
                return producto == null ? null : producto.getNombre() + " (ID: " + producto.getId() + ")";
            }
            @Override
            public Producto fromString(String string) { return null; }
        });
    }

    private void cargarDatosComboBoxes() {
        try {
            List<Usuario> listaUsuarios = usuariosController.getAllUsuarios();
            usuarioComboBox.setItems(FXCollections.observableArrayList(listaUsuarios));

            List<Producto> listaProductos = productosController.getAllProductos();
            productoComboBox.setItems(FXCollections.observableArrayList(listaProductos));
        } catch (SQLException e) {
            mostrarAlerta("Error de Carga", "No se pudieron cargar los datos para los desplegables: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        if (pedido != null) {
            if (pedido.getUsuarioId() > 0) {
                try {
                    Usuario u = usuariosController.getUsuarioById(pedido.getUsuarioId());
                    usuarioComboBox.setValue(u);
                } catch (SQLException e) { /* Ignorar si no se encuentra */ }
            }
            if (pedido.getProductoId() > 0) {
                try {
                    Producto p = productosController.getProductoById(pedido.getProductoId());
                    productoComboBox.setValue(p);
                } catch (SQLException e) { /* Ignorar si no se encuentra */ }
            }
            cantidadField.setText(String.valueOf(pedido.getCantidad()));
            fechaPicker.setValue(pedido.getFecha());
        } else {
            fechaPicker.setValue(LocalDate.now());
            cantidadField.setText("1");
        }
    }

    @FXML
    private void handleGuardar() {
        if (isInputValid()) {
            boolean esNuevo = (this.pedido == null);
            if (esNuevo) {
                this.pedido = new Pedido();
            }

            Usuario selectedUsuario = usuarioComboBox.getSelectionModel().getSelectedItem();
            Producto selectedProducto = productoComboBox.getSelectionModel().getSelectedItem();

            this.pedido.setUsuarioId(selectedUsuario.getId());
            this.pedido.setProductoId(selectedProducto.getId());
            this.pedido.setCantidad(Integer.parseInt(cantidadField.getText()));
            this.pedido.setFecha(fechaPicker.getValue());

            try {
                if (esNuevo) {
                    pedidosController.crearPedido(this.pedido);
                } else {
                    pedidosController.modificarPedido(this.pedido);
                }
                guardadoExitoso = true;
                dialogStage.close();
                if (mainController != null) {
                    mainController.refrescarTablaActual();
                }
            } catch (SQLException e) {
                mostrarAlerta("Error de Base de Datos", "No se pudo guardar el pedido: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Datos Inválidos", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMsg = "";
        if (usuarioComboBox.getSelectionModel().getSelectedItem() == null) errorMsg += "Debe seleccionar un usuario.\n";
        if (productoComboBox.getSelectionModel().getSelectedItem() == null) errorMsg += "Debe seleccionar un producto.\n";
        if (cantidadField.getText() == null || cantidadField.getText().trim().isEmpty()) {
            errorMsg += "La cantidad es obligatoria.\n";
        } else {
            try {
                int cantidad = Integer.parseInt(cantidadField.getText());
                if (cantidad <= 0) errorMsg += "La cantidad debe ser un número positivo.\n";
            } catch (NumberFormatException e) {
                errorMsg += "La cantidad debe ser un número válido.\n";
            }
        }
        if (fechaPicker.getValue() == null) errorMsg += "Debe seleccionar una fecha.\n";

        if (errorMsg.isEmpty()) {
            return true;
        } else {
            mostrarAlerta("Campos Inválidos", errorMsg, Alert.AlertType.ERROR);
            return false;
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }
}