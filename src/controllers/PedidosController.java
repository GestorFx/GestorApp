package controllers;

import models.Pedido;
import models.Producto;
import models.Usuario;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PedidosController {

    public List<Pedido> getAllPedidos() throws SQLException {
        return Pedido.findAll();
    }

    public Pedido getPedidoById(int id) throws SQLException {
        return Pedido.findById(id);
    }

    public void crearPedido(Pedido pedido) throws SQLException {
        if (pedido == null) throw new IllegalArgumentException("El pedido no puede ser nulo.");
        pedido.save();
    }

    public void modificarPedido(Pedido pedido) throws SQLException {
        if (pedido == null) throw new IllegalArgumentException("El pedido a modificar no puede ser nulo.");
        if (Pedido.findById(pedido.getId()) == null) {
            throw new SQLException("Pedido con ID " + pedido.getId() + " no encontrado para modificar.");
        }
        pedido.save();
    }

    public void borrarPedido(int pedidoId) throws SQLException {
        Pedido pedido = Pedido.findById(pedidoId);
        if (pedido != null) {
            pedido.delete();
        } else {
            throw new SQLException("Pedido con ID " + pedidoId + " no encontrado para borrar.");
        }
    }

    public List<Pedido> buscarPedidos(String criterio) throws SQLException {
        String critLower = criterio.toLowerCase().trim();
        if (critLower.isEmpty()) return getAllPedidos();
        return Pedido.findAll().stream()
                .filter(p -> String.valueOf(p.getId()).equals(critLower) ||
                        (p.getProductoNombre() != null && p.getProductoNombre().toLowerCase().contains(critLower)) ||
                        (p.getUsuarioNombreCompleto() != null && p.getUsuarioNombreCompleto().toLowerCase().contains(critLower)) ||
                        (p.getFecha() != null && p.getFecha().toString().contains(critLower)))
                .collect(Collectors.toList());
    }

    public void exportPedidosToXML(List<Pedido> pedidos, String filePath) throws XMLStreamException, FileNotFoundException, SQLException {
        if (pedidos == null || pedidos.isEmpty()) {
            System.out.println("No hay pedidos para exportar.");
            return;
        }

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEventWriter writer = null;

        try {
            writer = xmlOutputFactory.createXMLEventWriter(new FileOutputStream(filePath), "UTF-8");
            writer.add(eventFactory.createStartDocument("UTF-8", "1.0"));
            writer.add(eventFactory.createDTD("\n"));
            writer.add(eventFactory.createStartElement("", "", "pedidos"));
            writer.add(eventFactory.createDTD("\n"));

            for (Pedido pedido : pedidos) {
                writer.add(eventFactory.createDTD("\t"));
                writer.add(eventFactory.createStartElement("", "", "pedido"));
                writer.add(eventFactory.createDTD("\n"));

                createXmlElement(writer, eventFactory, "id", String.valueOf(pedido.getId()), 2);
                createXmlElement(writer, eventFactory, "usuario_id", String.valueOf(pedido.getUsuarioId()), 2);
                createXmlElement(writer, eventFactory, "producto_id", String.valueOf(pedido.getProductoId()), 2);
                createXmlElement(writer, eventFactory, "cantidad", String.valueOf(pedido.getCantidad()), 2);
                createXmlElement(writer, eventFactory, "fecha", pedido.getFecha().toString(), 2);
                createXmlElement(writer, eventFactory, "producto_nombre", pedido.getProductoNombre(), 2);
                createXmlElement(writer, eventFactory, "producto_precio", String.valueOf(pedido.getProductoPrecio()), 2);
                createXmlElement(writer, eventFactory, "usuario_nombre_completo", pedido.getUsuarioNombreCompleto(), 2);

                writer.add(eventFactory.createDTD("\t"));
                writer.add(eventFactory.createEndElement("", "", "pedido"));
                writer.add(eventFactory.createDTD("\n"));
            }

            writer.add(eventFactory.createEndElement("", "", "pedidos"));
            writer.add(eventFactory.createDTD("\n"));
            writer.add(eventFactory.createEndDocument());
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    private void createXmlElement(XMLEventWriter writer, XMLEventFactory eventFactory, String elementName, String value, int indentLevel) throws XMLStreamException {
        String indent = "\t".repeat(indentLevel);
        writer.add(eventFactory.createDTD(indent));
        writer.add(eventFactory.createStartElement("", "", elementName));
        if (value != null) {
            writer.add(eventFactory.createCharacters(value));
        }
        writer.add(eventFactory.createEndElement("", "", elementName));
        writer.add(eventFactory.createDTD("\n"));
    }
}