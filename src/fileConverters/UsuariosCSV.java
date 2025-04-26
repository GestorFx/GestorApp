package fileConverters;

import connection.ConnectionBD;
import models.Usuario;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class UsuariosCSV {

    //Fill ArrayList with Usuario objects
    public ArrayList<Usuario> getAllUsuarios() {
        ArrayList<Usuario> misUsuarios = new ArrayList<>();
        String query = "SELECT * FROM Usuarios";

        try (Statement stmt = ConnectionBD.getStmt();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("email")
                );
                misUsuarios.add(usuario);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return misUsuarios;
    }

    //Export to CSV the ArrayList made of Usuario objects
    private static void exportUsuariosCSV(ArrayList<Usuario> misUsuarios){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("usuarios.csv"));
            writer.write("Id,Nombre,Apellido,Email \n");

            for (Usuario usuario:misUsuarios){
                writer.write(usuario.getId() + "," + usuario.getNombre() + "," +
                        usuario.getApellido() + "," + usuario.getEmail() + "\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
