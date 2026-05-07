package edu.esi.ds.esientradas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class Token {
    public static final long DURACION_RESERVA_MILLIS = 600000L;

    @Id
    @Column(length = 36)
    private String valor;
    private Long hora;
    private String sessionId;
    private String emailUsuario;

    @OneToMany(fetch = FetchType.EAGER) // EAGER para que cargue las entradas al recuperar el token
    @JoinColumn(name = "token_valor")
    private List<Entrada> entradas = new ArrayList<>();

    public Token() {
        this.valor = UUID.randomUUID().toString();
        this.hora = System.currentTimeMillis();
    }

    // --- GETTERS Y SETTERS ---
    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Long getHora() {
        return hora;
    }

    public void setHora(Long hora) {
        this.hora = hora;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public List<Entrada> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<Entrada> entradas) {
        this.entradas = entradas;
    }

    // Reemplaza setEntrada por addEntrada para manejar la lista
    public void addEntrada(Entrada entrada) {
        this.entradas.add(entrada);
    }

    public Long getHoraExpiracion() {
        if (this.hora == null) {
            return null;
        }
        return this.hora + DURACION_RESERVA_MILLIS;
    }

    public Long getTiempoRestanteMillis() {
        return getTiempoRestanteMillis(System.currentTimeMillis());
    }

    Long getTiempoRestanteMillis(long ahora) {
        Long horaExpiracion = getHoraExpiracion();
        if (horaExpiracion == null) {
            return null;
        }

        return Math.max(horaExpiracion - ahora, 0L);
    }

    public Long getTiempoRestanteSegundos() {
        return getTiempoRestanteSegundos(System.currentTimeMillis());
    }

    Long getTiempoRestanteSegundos(long ahora) {
        Long tiempoRestanteMillis = getTiempoRestanteMillis(ahora);
        if (tiempoRestanteMillis == null) {
            return null;
        }

        return (tiempoRestanteMillis + 999L) / 1000L;
    }

    public boolean isCaducado() {
        return isCaducado(System.currentTimeMillis());
    }

    boolean isCaducado(long ahora) {
        Long tiempoRestanteMillis = getTiempoRestanteMillis(ahora);
        return tiempoRestanteMillis != null && tiempoRestanteMillis == 0L;
    }
}