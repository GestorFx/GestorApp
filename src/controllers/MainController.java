package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Pedido;
import models.Usuario;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {

    @FXML private ToggleButton usuariosBtn;
    @FXML private ToggleButton pedidosBtn;
    @FXML private Button insertarBtn;
    @FXML private Button modificarBtn;
    @FXML private Button borrarBtn;
    @FXML private Button csvBtn;
    @FXML private Button xmlBtn;
    @FXML private TextField buscarField;
    @FXML private Button buscarBtn;
    @FXML private TableView<Object> dataTable;

    private UsuariosController usuariosController;
    private PedidosController pedidosController;

    private ObservableList<Usuario> listaCompletaUsuarios = FXCollections.observableArrayList();
    private ObservableList<Pedido> listaCompletaPedidos = FXCollections.observableArrayList();

    private ObservableList<Usuario> listaUsuariosParaTabla = FXCollections.observableArrayList();
    private ObservableList<Pedido> listaPedidosParaTabla = FXCollections.observableArrayList();

    private enum VistaActual { USUARIOS, PEDIDOS }
    private VistaActual vistaActual = VistaActual.USUARIOS;

    @FXML
    public void initialize() {
        usuariosController = new UsuariosController();
        pedidosController = new PedidosController();

        usuariosBtn.setOnAction(e -> cambiarVista(VistaActual.USUARIOS));
        pedidosBtn.setOnAction(e -> cambiarVista(VistaActual.PEDIDOS));

        buscarField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                if (vistaActual == VistaActual.USUARIOS) {
                    listaUsuariosParaTabla.setAll(listaCompletaUsuarios);
                } else {
                    listaPedidosParaTabla.setAll(listaCompletaPedidos);
                }
            }
        });

        buscarField.setOnAction(this::handleBuscar);
        buscarBtn.setOnAction(this::handleBuscar);

        cambiarVista(VistaActual.USUARIOS);
    }

    private void cambiarVista(VistaActual nuevaVista) {
        this.vistaActual = nuevaVista;
        buscarField.setText("");

        if (vistaActual == VistaActual.USUARIOS) {
            usuariosBtn.setSelected(true);
            pedidosBtn.setSelected(false);
            setupColumnasUsuarios();
            cargarYAlmacenarTodosLosUsuarios();
            csvBtn.setDisable(false);
            xmlBtn.setDisable(true);
            insertarBtn.setText("NUEVO USUARIO");
            modificarBtn.setText("MODIFICAR USUARIO");
            borrarBtn.setText("BORRAR USUARIO");
            buscarField.setPromptText("Buscar Usuarios por ID, Nombre, Email...");
        } else {
            pedidosBtn.setSelected(true);
            usuariosBtn.setSelected(false);
            setupColumnasPedidos();
            cargarYAlmacenarTodosLosPedidos();
            csvBtn.setDisable(true);
            xmlBtn.setDisable(false);
            insertarBtn.setText("NUEVO PEDIDO");
            modificarBtn.setText("MODIFICAR PEDIDO");
            borrarBtn.setText("BORRAR PEDIDO");
            buscarField.setPromptText("Buscar Pedidos por ID, Producto, Usuario, Fecha...");
        }
    }

    @SuppressWarnings("unchecked")
    private void setupColumnasUsuarios() {
        dataTable.getColumns().clear();
        TableColumn<Object, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Object, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Object, String> apellidosCol = new TableColumn<>("Apellidos");
        apellidosCol.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        TableColumn<Object, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Object, String> calleCol = new TableColumn<>("Calle");
        calleCol.setCellValueFactory(new PropertyValueFactory<>("calle"));
        TableColumn<Object, String> ciudadCol = new TableColumn<>("Ciudad");
        ciudadCol.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        TableColumn<Object, String> cpCol = new TableColumn<>("CP");
        cpCol.setCellValueFactory(new PropertyValueFactory<>("codigoPostal"));

        idCol.setPrefWidth(50);
        nombreCol.setPrefWidth(120);
        apellidosCol.setPrefWidth(120);
        emailCol.setPrefWidth(180);
        calleCol.setPrefWidth(150);
        ciudadCol.setPrefWidth(100);
        cpCol.setPrefWidth(70);

        dataTable.getColumns().addAll(idCol, nombreCol, apellidosCol, emailCol, calleCol, ciudadCol, cpCol);
        dataTable.setItems((ObservableList<Object>) (ObservableList<?>) listaUsuariosParaTabla);
    }

    @SuppressWarnings("unchecked")
    private void setupColumnasPedidos() {
        dataTable.getColumns().clear();
        TableColumn<Object, Integer> idCol = new TableColumn<>("ID Pedido");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Object, String> usuarioCol = new TableColumn<>("Usuario");
        usuarioCol.setCellValueFactory(new PropertyValueFactory<>("usuarioNombreCompleto"));
        TableColumn<Object, String> productoCol = new TableColumn<>("Producto");
        productoCol.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        TableColumn<Object, Double> precioCol = new TableColumn<>("Precio Unit.");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("productoPrecio"));
        TableColumn<Object, Integer> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<Object, LocalDate> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        idCol.setPrefWidth(80);
        usuarioCol.setPrefWidth(180);
        productoCol.setPrefWidth(180);
        precioCol.setPrefWidth(100);
        cantidadCol.setPrefWidth(80);
        fechaCol.setPrefWidth(100);

        dataTable.getColumns().addAll(idCol, usuarioCol, productoCol, precioCol, cantidadCol, fechaCol);
        dataTable.setItems((ObservableList<Object>) (ObservableList<?>) listaPedidosParaTabla);
    }

    private void cargarYAlmacenarTodosLosUsuarios() {
        try {
            List<Usuario> usuariosDesdeBD = usuariosController.getAllUsuarios();
            listaCompletaUsuarios.setAll(usuariosDesdeBD);
            listaUsuariosParaTabla.setAll(listaCompletaUsuarios);
        } catch (SQLException e) {
            mostrarError("Error al cargar usuarios", e.getMessage());
            e.printStackTrace();
            listaCompletaUsuarios.clear();
            listaUsuariosParaTabla.clear();
        }
    }

    private void cargarYAlmacenarTodosLosPedidos() {
        try {
            List<Pedido> pedidosDesdeBD = pedidosController.getAllPedidos();
            listaCompletaPedidos.setAll(pedidosDesdeBD);
            listaPedidosParaTabla.setAll(listaCompletaPedidos);
        } catch (SQLException e) {
            mostrarError("Error al cargar pedidos", e.getMessage());
            e.printStackTrace();
            listaCompletaPedidos.clear();
            listaPedidosParaTabla.clear();
        }
    }

    private boolean usuarioCoincideConFiltro(Usuario usuario, String filtroEnMinusculas) {
        try {
            if (String.valueOf(usuario.getId()).contains(filtroEnMinusculas)) return true;
            if (usuario.getNombre() != null && usuario.getNombre().toLowerCase().contains(filtroEnMinusculas)) return true;
            if (usuario.getApellido() != null && usuario.getApellido().toLowerCase().contains(filtroEnMinusculas)) return true;
            if (usuario.getEmail() != null && usuario.getEmail().toLowerCase().contains(filtroEnMinusculas)) return true;
            if (usuario.getCalle() != null && usuario.getCalle().toLowerCase().contains(filtroEnMinusculas)) return true;
            if (usuario.getCiudad() != null && usuario.getCiudad().toLowerCase().contains(filtroEnMinusculas)) return true;
            if (usuario.getCodigoPostal() != null && usuario.getCodigoPostal().toLowerCase().contains(filtroEnMinusculas)) return true;
            return false;
        } catch (SQLException e) {
            System.err.println("SQLException al acceder a datos del usuario ID: " + (usuario != null ? usuario.getId() : "null") + " durante el filtrado: " + e.getMessage());
            return false;
        }
    }

    private boolean pedidoCoincideConFiltro(Pedido pedido, String filtroEnMinusculas) {
        if (String.valueOf(pedido.getId()).contains(filtroEnMinusculas)) return true;
        if (pedido.getUsuarioNombreCompleto() != null && pedido.getUsuarioNombreCompleto().toLowerCase().contains(filtroEnMinusculas)) return true;
        if (pedido.getProductoNombre() != null && pedido.getProductoNombre().toLowerCase().contains(filtroEnMinusculas)) return true;
        if (pedido.getFecha() != null && pedido.getFecha().toString().contains(filtroEnMinusculas)) return true;
        return false;
    }

    @FXML
    void handleBuscar(ActionEvent event) {
        String criterio = buscarField.getText();

        if (criterio == null || criterio.trim().isEmpty()) {
            if (vistaActual == VistaActual.USUARIOS) {
                listaUsuariosParaTabla.setAll(listaCompletaUsuarios);
            } else {
                listaPedidosParaTabla.setAll(listaCompletaPedidos);
            }
        } else {
            String filtroEnMinusculas = criterio.toLowerCase().trim();
            if (vistaActual == VistaActual.USUARIOS) {
                if (listaCompletaUsuarios.isEmpty() && !criterio.isEmpty()) {
                    System.err.println("Advertencia: listaCompletaUsuarios vacía al intentar filtrar. Recargando...");
                    cargarYAlmacenarTodosLosUsuarios();
                }
                List<Usuario> usuariosFiltrados = listaCompletaUsuarios.stream()
                        .filter(usuario -> usuarioCoincideConFiltro(usuario, filtroEnMinusculas))
                        .collect(Collectors.toList());
                listaUsuariosParaTabla.setAll(usuariosFiltrados);
            } else {
                if (listaCompletaPedidos.isEmpty() && !criterio.isEmpty()) {
                    System.err.println("Advertencia: listaCompletaPedidos vacía al intentar filtrar. Recargando...");
                    cargarYAlmacenarTodosLosPedidos();
                }
                List<Pedido> pedidosFiltrados = listaCompletaPedidos.stream()
                        .filter(pedido -> pedidoCoincideConFiltro(pedido, filtroEnMinusculas))
                        .collect(Collectors.toList());
                listaPedidosParaTabla.setAll(pedidosFiltrados);
            }
        }
    }

    @FXML
    void handleInsertar(ActionEvent event) {
        if (vistaActual == VistaActual.USUARIOS) {
            mostrarDialogoUsuario(null);
        } else {
            mostrarDialogoPedido(null);
        }
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Object seleccionado = dataTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Ningún ítem seleccionado", "Por favor, seleccione un ítem de la tabla para modificar.");
            return;
        }
        if (vistaActual == VistaActual.USUARIOS && seleccionado instanceof Usuario) {
            mostrarDialogoUsuario((Usuario) seleccionado);
        } else if (vistaActual == VistaActual.PEDIDOS && seleccionado instanceof Pedido) {
            mostrarDialogoPedido((Pedido) seleccionado);
        }
    }

    @FXML
    void handleBorrar(ActionEvent event) {
        Object seleccionado = dataTable.getSelectionModel().getSelectedItem();
        String tipoElemento = "";
        String nombreElemento = "";

        if (seleccionado == null) {
            mostrarAlerta("Ningún ítem seleccionado", "Por favor, seleccione un ítem de la tabla para borrar.");
            return;
        }

        if (vistaActual == VistaActual.USUARIOS && seleccionado instanceof Usuario) {
            tipoElemento = "usuario";
            Usuario u = (Usuario) seleccionado;
            nombreElemento = u.getNombre() + (u.getApellido() != null ? " " + u.getApellido() : "");
        } else if (vistaActual == VistaActual.PEDIDOS && seleccionado instanceof Pedido) {
            tipoElemento = "pedido";
            Pedido p = (Pedido) seleccionado;
            nombreElemento = "ID " + p.getId();
        } else {
            tipoElemento = "ítem";
            nombreElemento = "[Elemento desconocido]";
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Borrado");
        confirmacion.setHeaderText("¿Está seguro de que desea borrar el " + tipoElemento + " seleccionado?");
        confirmacion.setContentText("Elemento: " + nombreElemento + "\nEsta acción no se puede deshacer.");

        ButtonType botonSi = new ButtonType("Sí", ButtonBar.ButtonData.OK_DONE);
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(botonSi, botonCancelar);

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == botonSi) {
            try {
                if (vistaActual == VistaActual.USUARIOS && seleccionado instanceof Usuario) {
                    Usuario usuarioABorrar = (Usuario) seleccionado;
                    usuariosController.borrarUsuario(usuarioABorrar.getId());
                    cargarYAlmacenarTodosLosUsuarios();
                    mostrarInformacion("Borrado Exitoso", "El usuario \"" + nombreElemento + "\" ha sido borrado correctamente.");

                } else if (vistaActual == VistaActual.PEDIDOS && seleccionado instanceof Pedido) {
                    Pedido pedidoABorrar = (Pedido) seleccionado;
                    pedidosController.borrarPedido(pedidoABorrar.getId());
                    cargarYAlmacenarTodosLosPedidos();
                    mostrarInformacion("Borrado Exitoso", "El pedido \"" + nombreElemento + "\" ha sido borrado correctamente.");
                }
            } catch (SQLException e) {
                mostrarError("Error al borrar", "No se pudo borrar el " + tipoElemento + ": " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                mostrarError("Error inesperado al borrar", "Ocurrió un error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleExportarCSV(ActionEvent event) {
        if (listaCompletaUsuarios.isEmpty()) {
            mostrarAlerta("Sin datos", "No hay usuarios para exportar.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar CSV de Usuarios");
        fileChooser.setInitialFileName("usuarios.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                usuariosController.exportUsuariosToCSV(listaCompletaUsuarios, file.getAbsolutePath());
                mostrarInformacion("Exportación CSV Exitosa", "Archivo guardado en: " + file.getAbsolutePath());
            } catch (IOException | SQLException e) {
                mostrarError("Error al exportar CSV", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleExportarXML(ActionEvent event) {
        if (listaCompletaPedidos.isEmpty()) {
            mostrarAlerta("Sin datos", "No hay pedidos para exportar.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar XML de Pedidos");
        fileChooser.setInitialFileName("pedidos.xml");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files (*.xml)", "*.xml"));
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                pedidosController.exportPedidosToXML(listaCompletaPedidos, file.getAbsolutePath());
                mostrarInformacion("Exportación XML Exitosa", "Archivo guardado en: " + file.getAbsolutePath());
            } catch (Exception e) {
                mostrarError("Error al exportar XML", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void mostrarDialogoUsuario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DialogoUsuario.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            DialogoUsuarioController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUsuario(usuario);
            controller.setMainController(this);

            dialogStage.showAndWait();
        } catch (IOException e) {
            mostrarError("Error al abrir diálogo de usuario", e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarDialogoPedido(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DialogoPedido.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(pedido == null ? "Nuevo Pedido" : "Editar Pedido");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            DialogoPedidoController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPedido(pedido);
            controller.setMainController(this);

            dialogStage.showAndWait();
        } catch (IOException e) {
            mostrarError("Error al abrir diálogo de pedido", e.getMessage());
            e.printStackTrace();
        }
    }

    public void refrescarTablaActual() {
        System.out.println("MainController: Refrescando tabla para vista: " + vistaActual);
        if (vistaActual == VistaActual.USUARIOS) {
            cargarYAlmacenarTodosLosUsuarios();
        } else {
            cargarYAlmacenarTodosLosPedidos();
        }
        String criterioActual = buscarField.getText();
        if (criterioActual != null && !criterioActual.trim().isEmpty()) {
            handleBuscar(null);
        }
    }

    private Stage getStage() {
        return (Stage) dataTable.getScene().getWindow();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}