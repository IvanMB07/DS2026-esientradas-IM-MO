package edu.esi.ds.esientradas.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import edu.esi.ds.esientradas.model.Entrada;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public byte[] generarFactura(String email, List<Entrada> entradas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();
        document.add(new Paragraph("RESUMEN DE COMPRA - ESIENTRADAS"));
        document.add(new Paragraph("Usuario: " + email));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3); // Columnas: Espectáculo, ID, Precio
        table.addCell("Espectáculo");
        table.addCell("ID Entrada");
        table.addCell("Precio");

        long total = 0;
        for (Entrada e : entradas) {
            table.addCell(e.getEspectaculo().getArtista());
            table.addCell(e.getId().toString());
            table.addCell((e.getPrecio() / 100.0) + "€");
            total += e.getPrecio();
        }
        document.add(table);
        document.add(new Paragraph("Total pagado: " + (total / 100.0) + "€"));

        document.close();
        return out.toByteArray();
    }
}