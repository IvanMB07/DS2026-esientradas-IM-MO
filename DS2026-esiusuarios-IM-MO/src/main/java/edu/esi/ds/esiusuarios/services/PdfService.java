package edu.esi.ds.esiusuarios.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * PdfService - Generador de documentos PDF
 * Responsabilidad única: Generar facturas y otros documentos en PDF
 * No tiene dependencias del modelo de esientradas
 */
@Service
/**
 * NOTA DE MANTENIMIENTO:
 * Clase en uso activo en el flujo principal de generación de facturas.
 */
public class PdfService {

    /**
     * nombre_metodo: generarFactura
     * parametros: email, entradasData
     * funcion: genera PDF de factura con datos de entradas
     * flujo_en_el_que_participa: generación de facturas
     */
    /**
     * Genera un PDF de factura con los datos proporcionados
     * 
     * @param email        Email del usuario
     * @param entradasData Lista de mapas con datos de entradas (artista, id,
     *                     precio)
     * @return byte[] Contenido del PDF
     * @throws IllegalStateException si no se puede generar el PDF
     */
    public byte[] generarFactura(String email, List<Map<String, String>> entradasData) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        List<Map<String, String>> entradas = entradasData == null ? List.of() : entradasData;
        byte[] pdfBytes = null;

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            if (writer == null) {
                throw new IllegalStateException("No se pudo instanciar PdfWriter");
            }

            document.open();

            document.add(new Paragraph("RESUMEN DE COMPRA - ESIENTRADAS"));
            document.add(new Paragraph("Usuario: " + (email == null ? "N/A" : email)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(3); // Columnas: Espectáculo, ID, Precio
            table.addCell("Espectáculo");
            table.addCell("ID Entrada");
            table.addCell("Precio");

            BigDecimal total = BigDecimal.ZERO;
            for (Map<String, String> entrada : entradas) {
                String artista = entrada.getOrDefault("artista", "N/A");
                String idEntrada = entrada.getOrDefault("id", "N/A");
                String precio = entrada.getOrDefault("precio", "0€");

                table.addCell(artista);
                table.addCell(idEntrada);
                table.addCell(precio);

                total = total.add(parsearPrecio(precio));
            }

            document.add(table);
            document.add(new Paragraph("Total pagado: " + total.setScale(2, RoundingMode.HALF_UP) + "€"));

            // IMPORTANTE: Cerrar el documento ANTES de obtener los bytes
            // En iText, los datos se escriben en el stream cuando se cierra el documento
            document.close();

            // Ahora obtener los bytes después de cerrar
            pdfBytes = out.toByteArray();

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new IllegalStateException("El PDF generado está vacío después de cerrar");
            }

            return pdfBytes;

        } catch (DocumentException e) {
            throw new IllegalStateException("Error al generar documento PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException("Error inesperado al generar PDF: " + e.getMessage(), e);
        } finally {
            // Cerrar el stream
            try {
                out.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar stream: " + e.getMessage());
            }
        }
    }

    /**
     * Parsea un string de precio y lo convierte a BigDecimal
     * Soporta formatos: "10€", "10.50€", "10,50€", etc.
     * 
     * @param precio String del precio
     * @return BigDecimal valor del precio o ZERO si no puede parsear
     */
    private BigDecimal parsearPrecio(String precio) {
        if (precio == null || precio.isBlank()) {
            return BigDecimal.ZERO;
        }

        String normalizado = precio.replace("€", "")
                .replace(" ", "")
                .replace(",", ".")
                .replaceAll("[^0-9.]", "");

        if (normalizado.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(normalizado);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
