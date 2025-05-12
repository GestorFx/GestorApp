package controllers;

import models.Producto;
import java.sql.SQLException;
import java.util.List;

public class ProductosController {

    public List<Producto> getAllProductos() throws SQLException {
        return Producto.findAll();
    }

    public Producto getProductoById(int id) throws SQLException {
        return Producto.findById(id);
    }
}