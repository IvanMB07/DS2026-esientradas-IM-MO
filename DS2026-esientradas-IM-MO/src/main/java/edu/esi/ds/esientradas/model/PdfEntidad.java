package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
/**
 * nombre_clase: PdfEntidad
 * parametros_clave: id, emailUsuario, fechaGeneracion, contenido
 * funcion: persistencia local del comprobante PDF generado tras pago
 * flujo_en_el_que_participa: registro historico de factura post-compra
 * comunicacion: PagosService, PdfDao
 */
public class PdfEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emailUsuario;
    private LocalDateTime fechaGeneracion;

    @Lob // Para guardar el contenido del PDF si fuera necesario, o la ruta
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    public PdfEntidad() {
        this.fechaGeneracion = LocalDateTime.now();
    }

    // Getters y Setters...
    public void setEmailUsuario(String email) {
        this.emailUsuario = email;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }
}