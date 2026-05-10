package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: RegisterRequest
 * funcion: objeto de transferencia para registro
 * flujo_en_el_que_participa: registro de usuario
 * comunicacion: controladores y servicios
 */
public class RegisterRequest {
    @NotBlank
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100)
    private String email;
    @NotBlank
    @Size(min = 8, max = 128)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$", message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, incluir un número y un símbolo (@#$%^&+=!._-)")
    private String pwd1;
    @NotBlank
    @Size(min = 8, max = 128)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$", message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, incluir un número y un símbolo (@#$%^&+=!._-)")
    private String pwd2;

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd1() {
        return pwd1;
    }

    public void setPwd1(String pwd1) {
        this.pwd1 = pwd1;
    }

    public String getPwd2() {
        return pwd2;
    }

    public void setPwd2(String pwd2) {
        this.pwd2 = pwd2;
    }
}