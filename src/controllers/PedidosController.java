package controllers;

import services.ConnectionBD;
import models.Pedido;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;

public class PedidosController {

    //Fill ArrayList with Pedido objects
    public ArrayList<Pedido> getAllPedidos() throws SQLException {
        ArrayList<Pedido> misPedidos = new ArrayList<>();
        String query = "SELECT * FROM Pedidos";

        Connection conn = ConnectionBD.getConn();
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement(query)){
             ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Pedido pedido = new Pedido(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getInt("producto_id"),
                        rs.getInt("cantidad"),

                        //Change type of date in the Constructor
                        rs.getDate("fecha").toLocalDate()
                );
                misPedidos.add(pedido);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return misPedidos;
    }

    //Export to XML the ArrayList with Pedido objects
    private static void exportPedidosXML(ArrayList<Pedido> misPedidos){
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        try {
            XMLEventWriter writer = xmlOutputFactory.createXMLEventWriter(new FileOutputStream("pedidos.xml"));

            //Every element starts this way
            writer.add(eventFactory.createStartDocument());
            writer.add(eventFactory.createDTD("\n"));

            writer.add(eventFactory.createStartElement("", "", "pedidos"));
            writer.add(eventFactory.createDTD("\n"));

            //For every pedido in the database
            for (Pedido pedido:misPedidos){
                writer.add(eventFactory.createStartElement("","","pedido"));
                writer.add(eventFactory.createDTD("\n"));

                writer.add(eventFactory.createStartElement("","","id"));
                writer.add(eventFactory.createCharacters(String.valueOf(pedido.getId())));
                writer.add(eventFactory.createEndElement("","","id"));
                writer.add(eventFactory.createDTD("\n"));

                writer.add(eventFactory.createStartElement("","","usuario_id"));
                writer.add(eventFactory.createCharacters(String.valueOf(pedido.getUsuarioId())));
                writer.add(eventFactory.createEndElement("","","usuario_id"));
                writer.add(eventFactory.createDTD("\n"));

                writer.add(eventFactory.createStartElement("","","producto_id"));
                writer.add(eventFactory.createCharacters(String.valueOf(pedido.getProductoId())));
                writer.add(eventFactory.createEndElement("","","producto_id"));
                writer.add(eventFactory.createDTD("\n"));

                writer.add(eventFactory.createStartElement("","","cantidad"));
                writer.add(eventFactory.createCharacters(String.valueOf(pedido.getCantidad())));
                writer.add(eventFactory.createEndElement("","","cantidad"));
                writer.add(eventFactory.createDTD("\n"));

                writer.add(eventFactory.createStartElement("","","fecha"));
                writer.add(eventFactory.createCharacters(String.valueOf(pedido.getFecha())));
                writer.add(eventFactory.createEndElement("","","fecha"));
                writer.add(eventFactory.createDTD("\n"));

                writer.add(eventFactory.createEndElement("","","pedido"));
                writer.add(eventFactory.createDTD("\n"));
            }

            //All elements end this way
            writer.add(eventFactory.createEndElement("","","pedidos"));
            writer.add(eventFactory.createEndDocument());
            writer.flush();
            writer.close();

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
