package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Direccion;
import models.Usuario;

import java.sql.SQLException;

public class DialogoUsuarioController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private TextField emailField;
    @FXML private TextField calleField;
    @FXML private TextField ciudadField;
    @FXML private TextField cpField;

    private Stage dialogStage;
    private Usuario usuario;
    private MainController mainController;
    private UsuariosController usuariosController;

    @FXML
    public void initialize() {
        usuariosController = new UsuariosController();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            nombreField.setText(usuario.getNombre());
            apellidoField.setText(usuario.getApellido());
            emailField.setText(usuario.getEmail());
            try {
                Direccion dir = usuario.getDireccion();
                if (dir != null) {
                    calleField.setText(dir.getCalle());
                    ciudadField.setText(dir.getCiudad());
                    cpField.setText(dir.getCodigoPostal());
                } else {
                    calleField.clear();
                    ciudadField.clear();
                    cpField.clear();
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo cargar la dirección: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            // Limpiar todos los campos si es un nuevo usuario
            nombreField.clear();
            apellidoField.clear();
            emailField.clear();
            calleField.clear();
            ciudadField.clear();
            cpField.clear();
        }
    }

    @FXML
    private void handleGuardar() {
        if (isInputValid()) {
            boolean esNuevo = (this.usuario == null);
            if (esNuevo) {
                this.usuario = new Usuario();
            }


            try {
                this.usuario.setNombre(nombreField.getText());
                this.usuario.setApellido(apellidoField.getText());
                this.usuario.setEmail(emailField.getText());
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Datos de Usuario Inválidos", e.getMessage(), Alert.AlertType.ERROR);
                return;
            }


            Direccion direccion = null;
            boolean hayDatosDireccion = !calleField.getText().trim().isEmpty() ||
                    !ciudadField.getText().trim().isEmpty() ||
                    !cpField.getText().trim().isEmpty();

            if (hayDatosDireccion) {
                try {
                    if (!esNuevo && this.usuario.getId() != null) {
                        direccion = this.usuario.getDireccion();
                    }
                    if (direccion == null) {
                        direccion = new Direccion();
                    }

                    direccion.setCalle(calleField.getText());
                    direccion.setCiudad(ciudadField.getText());
                    direccion.setCodigoPostal(cpField.getText());

                } catch (IllegalArgumentException e) {
                    mostrarAlerta("Datos de Dirección Inválidos", e.getMessage(), Alert.AlertType.ERROR);
                    return;
                } catch (SQLException e) {
                    mostrarAlerta("Error al Acceder a Dirección", "No se pudo obtener la dirección existente: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                    return;
                }
            }
            this.usuario.setDireccion(direccion);


            try {
                this.usuario.save();

                String mensajeExito = esNuevo ? "Usuario creado correctamente." : "Usuario modificado correctamente.";
                mostrarAlerta("Éxito", mensajeExito, Alert.AlertType.INFORMATION);
                dialogStage.close();

                if (mainController != null) {
                    mainController.refrescarTablaActual();
                }

            } catch (SQLException e) {
                mostrarAlerta("Error de Base de Datos", "No se pudo guardar el usuario: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Datos Inválidos", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMsg = "";
        if (nombreField.getText() == null || nombreField.getText().trim().isEmpty()) {
            errorMsg += "El nombre es obligatorio.\n";
        }

        String email = emailField.getText();
        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                errorMsg += "El formato del email no es válido.\n";
            }
        }

        boolean calleVacia = calleField.getText() == null || calleField.getText().trim().isEmpty();
        boolean ciudadVacia = ciudadField.getText() == null || ciudadField.getText().trim().isEmpty();
        boolean cpVacio = cpField.getText() == null || cpField.getText().trim().isEmpty();

        if ((!calleVacia || !ciudadVacia || !cpVacio) && (calleVacia || ciudadVacia || cpVacio)) {
            errorMsg += "Si proporciona datos de dirección, todos los campos (Calle, Ciudad, CP) son obligatorios.\n";
        }

        if (!cpVacio && !cpField.getText().matches("\\d{5}")) {
            errorMsg += "El código postal debe tener 5 dígitos.\n";
        }

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
        if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }
}