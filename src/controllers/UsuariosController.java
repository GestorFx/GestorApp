package controllers;

import models.Direccion;
import models.Usuario;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UsuariosController {



    public List<Usuario> getAllUsuarios() throws SQLException {
        return Usuario.findAll();
    }

    public Usuario getUsuarioById(int id) throws SQLException {
        return Usuario.findById(id);
    }

    public void crearUsuario(Usuario usuario, Direccion direccion) throws SQLException {
        if (usuario == null) throw new IllegalArgumentException("El usuario no puede ser nulo.");
        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty() && Usuario.findByEmail(usuario.getEmail()) != null) {
            throw new SQLException("El email '" + usuario.getEmail() + "' ya está registrado.");
        }
        usuario.setDireccion(direccion);
        usuario.save();
    }

    public void modificarUsuario(Usuario usuario, Direccion direccion) throws SQLException {
        if (usuario == null || usuario.getId() == null) throw new IllegalArgumentException("El usuario a modificar no puede ser nulo y debe tener ID.");

        Usuario existente = Usuario.findById(usuario.getId());
        if (existente == null) throw new SQLException("Usuario con ID " + usuario.getId() + " no encontrado para modificar.");

        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty() && !usuario.getEmail().equalsIgnoreCase(existente.getEmail())) {
            Usuario usuarioConNuevoEmail = Usuario.findByEmail(usuario.getEmail());
            if (usuarioConNuevoEmail != null && !usuarioConNuevoEmail.getId().equals(existente.getId())) {
                throw new SQLException("El nuevo email '" + usuario.getEmail() + "' ya está registrado para otro usuario.");
            }
        }

        existente.setNombre(usuario.getNombre());
        existente.setApellido(usuario.getApellido());
        existente.setEmail(usuario.getEmail());
        existente.setDireccion(direccion);

        existente.save();
    }

    public void borrarUsuario(int usuarioId) throws SQLException {
        System.out.println("UsuariosController: Iniciando borrado para Usuario ID: " + usuarioId);
        Usuario usuario = null;
        try {
            System.out.println("UsuariosController: Intentando buscar Usuario con ID: " + usuarioId + " usando Usuario.findById().");
            usuario = Usuario.findById(usuarioId);

            if (usuario != null) {
                System.out.println("UsuariosController: Usuario encontrado (ID: " + usuario.getId() + ", Nombre: " + usuario.getNombre() + "). Llamando a usuario.delete().");
                usuario.delete();
                System.out.println("UsuariosController: usuario.delete() completado para ID: " + usuarioId);
            } else {
                System.err.println("UsuariosController: Usuario.findById(" + usuarioId + ") devolvió NULL. No se puede llamar a delete().");
                throw new SQLException("Usuario con ID " + usuarioId + " no encontrado para borrar (findById devolvió null).");
            }
        } catch (SQLException e) {
            System.err.println("UsuariosController: SQLException durante el borrado del Usuario ID: " + usuarioId + ". Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("UsuariosController: Exception general durante el borrado del Usuario ID: " + usuarioId + ". Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error inesperado en UsuariosController al borrar (ID: " + usuarioId + "): " + e.getMessage(), e);
        }
    }

    public List<Usuario> buscarUsuarios(String criterio) throws SQLException {
        String critLower = criterio.toLowerCase().trim();
        if (critLower.isEmpty()) return getAllUsuarios();
        return Usuario.findAll().stream()
                .filter(u -> String.valueOf(u.getId()).equals(critLower) ||
                        (u.getNombre() != null && u.getNombre().toLowerCase().contains(critLower)) ||
                        (u.getApellido() != null && u.getApellido().toLowerCase().contains(critLower)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(critLower)))
                .collect(Collectors.toList());
    }

    public void exportUsuariosToCSV(List<Usuario> usuarios, String filePath) throws IOException, SQLException {
        if (usuarios == null || usuarios.isEmpty()) {
            System.out.println("No hay usuarios para exportar.");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Id,Nombre,Apellido,Email,Calle,Ciudad,Codigo_Postal\n");
            for (Usuario usuario : usuarios) {
                Direccion dir = usuario.getDireccion();
                writer.write(usuario.getId() + ",");
                writer.write(escapeCsv(usuario.getNombre()) + ",");
                writer.write(escapeCsv(usuario.getApellido()) + ",");
                writer.write(escapeCsv(usuario.getEmail()) + ",");
                writer.write(escapeCsv(dir != null ? dir.getCalle() : "") + ",");
                writer.write(escapeCsv(dir != null ? dir.getCiudad() : "") + ",");
                writer.write(escapeCsv(dir != null ? dir.getCodigoPostal() : "") + "\n");
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}