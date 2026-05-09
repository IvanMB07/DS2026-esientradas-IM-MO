package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
/**
 * nombre_clase: ColaEspera
 * parametros_clave: espectaculo, emailUsuario, compraTokenPreferido,
 * compraTokenAsignado, estado
 * funcion: registra el estado de espera de usuarios cuando no hay entradas
 * disponibles
 * flujo_en_el_que_participa: cola de asignacion automatica tras
 * cancelaciones/caducidades
 * comunicacion: ColaEsperaService, ColaEsperaDao, Token
 */
public class ColaEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    private Espectaculo espectaculo;

    private String emailUsuario;
    private String compraTokenPreferido;
    private String compraTokenAsignado;
    private Long horaSolicitud;

    @Enumerated(EnumType.STRING)
    private EstadoCola estado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Espectaculo getEspectaculo() {
        return espectaculo;
    }

    public void setEspectaculo(Espectaculo espectaculo) {
        this.espectaculo = espectaculo;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getCompraTokenPreferido() {
        return compraTokenPreferido;
    }

    public void setCompraTokenPreferido(String compraTokenPreferido) {
        this.compraTokenPreferido = compraTokenPreferido;
    }

    public String getCompraTokenAsignado() {
        return compraTokenAsignado;
    }

    public void setCompraTokenAsignado(String compraTokenAsignado) {
        this.compraTokenAsignado = compraTokenAsignado;
    }

    public Long getHoraSolicitud() {
        return horaSolicitud;
    }

    public void setHoraSolicitud(Long horaSolicitud) {
        this.horaSolicitud = horaSolicitud;
    }

    public EstadoCola getEstado() {
        return estado;
    }

    public void setEstado(EstadoCola estado) {
        this.estado = estado;
    }
}
