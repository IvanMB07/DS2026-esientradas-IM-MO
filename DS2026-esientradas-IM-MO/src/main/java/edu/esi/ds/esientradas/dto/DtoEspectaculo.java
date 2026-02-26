package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

public class DtoEspectaculo {

    private Long id;
    private String artista;
    private LocalDateTime fecha;
    private String escenario;

    // --- Getters ---
    public Long getId(){
        return id;
    }

    public String getArtista() {
        return artista;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEscenario() {
        return escenario;
    }

    // --- Setters ---
    public void setId(Long id){
        this.id = id;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setEscenario(String escenario) {
        this.escenario = escenario;
    }
}