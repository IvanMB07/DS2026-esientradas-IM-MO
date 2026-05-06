package edu.esi.ds.esiusuarios.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * QrService - Generador de códigos QR
 * Responsabilidad única: Generar códigos QR para tickets usando ZXing
 * Formato: PNG de 300x300 píxeles
 */
@Service
public class QrService {

    private static final int QR_SIZE = 300;
    private static final String IMAGE_FORMAT = "png";

    /**
     * Genera un código QR a partir de datos
     * 
     * @param datos Datos a codificar en el QR (ej: ID de entrada, número de ticket)
     * @return byte[] Imagen del código QR en formato PNG
     * @throws IllegalArgumentException si los datos son null
     */
    public byte[] generarCodigoQr(String datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del QR no pueden ser null");
        }

        // Permitir cadenas vacías o con solo espacios - serán codificadas como tales
        String datosLimpios = datos.trim();
        if (datosLimpios.isEmpty()) {
            datosLimpios = "VACIO"; // Codificar algo en lugar de nada
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(datosLimpios, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, pngOutputStream);

            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el código QR: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un código QR en formato Base64
     * Útil para enviar por correo o mostrar en HTML
     * 
     * @param datos Datos a codificar
     * @return String Código QR en Base64 (formato: data:image/png;base64,...)
     */
    public String generarCodigoQrBase64(String datos) {
        byte[] qrBytes = generarCodigoQr(datos);
        String base64 = Base64.getEncoder().encodeToString(qrBytes);
        return "data:image/png;base64," + base64;
    }

    /**
     * Construye un string con los datos de las entradas para codificar en el QR
     * Formato: ID1|artista1|precio1#ID2|artista2|precio2#...
     * El precio se limpia quitando el símbolo €
     * 
     * @param entradasData Datos de las entradas
     * @return String Datos formateados para el QR
     */
    public String construirDatosQr(java.util.List<java.util.Map<String, String>> entradasData) {
        if (entradasData == null || entradasData.isEmpty()) {
            return "COMPRA-VACIA";
        }

        StringBuilder datoQr = new StringBuilder();
        for (int i = 0; i < entradasData.size(); i++) {
            if (i > 0) {
                datoQr.append("#");
            }
            java.util.Map<String, String> entrada = entradasData.get(i);
            String precio = entrada.getOrDefault("precio", "0");
            // Limpiar el precio: remover €, espacios y caracteres especiales
            String precioLimpio = precio.replace("€", "").replace(" ", "").trim();

            datoQr.append(entrada.getOrDefault("id", ""))
                    .append("|")
                    .append(entrada.getOrDefault("artista", ""))
                    .append("|")
                    .append(precioLimpio);
        }

        return datoQr.toString();
    }
}
