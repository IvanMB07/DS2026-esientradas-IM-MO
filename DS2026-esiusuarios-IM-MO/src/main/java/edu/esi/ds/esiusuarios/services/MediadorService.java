package edu.esi.ds.esiusuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * MediadorService - Orquestador centralizado de procesos de tickets
 * 
 * Responsabilidades:
 * - Coordinar la generación de facturas en PDF
 * - Coordinar la generación de códigos QR
 * - Gestionar el envío de correos electrónicos
 * - Facilitar la incorporación de nuevas funcionalidades
 * 
 * Beneficios de usar Mediator:
 * - Separación clara de responsabilidades
 * - Reducción del acoplamiento entre servicios
 * - Mayor escalabilidad y mantenibilidad
 * - Fácil extensión con nuevas funcionalidades
 */
@Service
public class MediadorService {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private QrService qrService;

    @Autowired
    private GmailService gmailService;

    /**
     * Procesa una compra completa: genera factura, QR y envía por correo
     * 
     * Flujo:
     * 1. Generar PDF de factura (CRÍTICO - debe retornarse siempre)
     * 2. Generar código QR del ticket (opcional - no bloquea)
     * 3. Enviar factura por correo electrónico (opcional - no bloquea)
     * 
     * @param email        Email del usuario
     * @param entradasData Lista de datos de entradas (artista, id, precio)
     * @return byte[] PDF generado
     */
    public byte[] procesarCompraCompleta(String email, List<Map<String, String>> entradasData) {
        // 1. CRÍTICO: Generar el PDF de la factura primero
        byte[] pdf = pdfService.generarFactura(email, entradasData);

        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException("El PDF generado está vacío o es nulo");
        }

        // 2. OPCIONAL: Generar código QR (si falla, no debe afectar al retorno del PDF)
        String qrBase64 = null;
        try {
            String datosQr = qrService.construirDatosQr(entradasData);
            qrBase64 = qrService.generarCodigoQrBase64(datosQr);
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo generar el QR: " + e.getMessage());
            // Continuar sin QR - el PDF se retorna igual
        }

        // 3. OPCIONAL: Enviar correo con la factura
        try {
            gmailService.sendFacturaEmail(email, pdf, qrBase64);
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo enviar el correo: " + e.getMessage());
            // Continuar - el PDF se retorna igual
        }

        return pdf;
    }

}
