package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * nombre_clase: EntradaData
 * funcion: objeto de transferencia para datos de entrada
 * flujo_en_el_que_participa: procesamiento de compras
 * comunicacion: controladores y servicios
 */
public class EntradaData {
    @NotNull(message = "El ID de entrada es obligatorio")
    private Long id;

    @NotBlank(message = "El artista es obligatorio")
    @Size(max = 255)
    private String artista;

    @NotNull(message = "El precio es obligatorio")
    private BigDecimal precio;

    public EntradaData() {
    }

    public EntradaData(Long id, String artista, BigDecimal precio) {
        this.id = id;
        this.artista = artista;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
