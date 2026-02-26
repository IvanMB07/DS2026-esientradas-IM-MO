package edu.esi.ds.esientradas.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;


@Entity
public class Token {
    @Id 
    @Column(length = 36)
    private String valor;
    private Long hora;
    private String sessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="entrada_id", referencedColumnName = "id")
    private Entrada entrada;

    public Token(){
        this.valor = UUID.randomUUID().toString();
        this.hora = System.currentTimeMillis();
    }

    // --- GETTERS ---
    
    public String getValor() {
        return valor;
    }

    public Long getHora() {
        return hora;
    }

    public String getSessionId() {
        return sessionId;
    }

    // --- SETTERS ---

    public void setValor(String valor) {
        this.valor = valor;
    }

    public void setHora(Long hora) {
        this.hora = hora;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }
}
