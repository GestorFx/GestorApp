package controllers;

import models.Usuario;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class UsuariosController {


    //Export to CSV the ArrayList made of Usuario objects
    private static void exportUsuariosCSV(ArrayList<Usuario> misUsuarios){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("usuarios.csv"));
            writer.write("Id,Nombre,Apellido,Email,Calle,Ciudad,Código Postal \n");

            for (Usuario usuario:misUsuarios){
                writer.write(usuario.getId() + "," + usuario.getNombre() + "," +
                        usuario.getApellido() + "," + usuario.getEmail() +
                        usuario.getDireccion().getCalle() + "," +
                        usuario.getDireccion().getCiudad() + "," +
                        usuario.getDireccion().getCodigoPostal() + "," + "\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
