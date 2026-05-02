package edu.esi.ds.esientradas.services;

import java.util.UUID;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class ReservasService {

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private UsuariosService usuariosService;

    @Transactional
    public String seleccionarEntrada(Long idEntrada, String compraToken, String userToken) {
        String emailActual = null;
        if (userToken != null && !userToken.isEmpty() && !userToken.equals("null") && !userToken.equals("undefined")) {
            emailActual = usuariosService.checkToken(userToken);
        }

        Token token;
        if (compraToken == null || compraToken.isEmpty() || compraToken.equals("null")
                || compraToken.equals("undefined")) {
            token = new Token();
            token.setValor(UUID.randomUUID().toString());
            if (emailActual != null)
                token.setEmailUsuario(emailActual);
            // La fecha NO se asigna aquí, la pone MySQL por defecto
            this.tokenDao.save(token);
        } else {
            token = this.tokenDao.findById(compraToken).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proceso de compra no encontrado"));

            if (emailActual != null && token.getEmailUsuario() == null) {
                token.setEmailUsuario(emailActual);
                this.tokenDao.save(token);
            }
        }

        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if (entrada.getEstado() != Estado.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entrada ya reservada");
        }

        token.addEntrada(entrada);
        this.tokenDao.save(token);
        this.entradaDao.updateEstado(idEntrada, Estado.RESERVADA);

        return token.getValor();
    }

    @Transactional
    public void cancelarEntrada(Long idEntrada, String compraToken, String userToken) {
        Token token = this.tokenDao.findById(compraToken).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no válido"));

        token.getEntradas().removeIf(e -> e.getId().equals(idEntrada));
        this.tokenDao.save(token);
        this.entradaDao.updateEstado(idEntrada, Estado.DISPONIBLE);
    }

    public Token getResumenCompra(String tokenValor, String userToken) {
        Token token = this.tokenDao.findById(tokenValor).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión expirada"));

        String emailActual = null;

        // Obtener email del usuario actual si tiene userToken válido
        if (userToken != null && !userToken.isEmpty() && !userToken.equals("null")
                && !userToken.equals("undefined")) {
            emailActual = usuariosService.checkToken(userToken);
        }

        // CASO 1: Si el token no tiene emailUsuario asignado, asignarlo
        if (token.getEmailUsuario() == null) {
            if (emailActual != null) {
                token.setEmailUsuario(emailActual);
                this.tokenDao.save(token);
                System.out.println("[RESERVAS] Token vinculado a usuario: " + emailActual);
            }
        }
        // CASO 2: Si el token YA tiene emailUsuario, validar que sea el mismo usuario
        else if (emailActual != null && !emailActual.equals(token.getEmailUsuario())) {
            // El usuario intenta acceder a un token de otro usuario
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este carrito pertenece a otro usuario: " + token.getEmailUsuario());
        }

        return token;
    }

    @Transactional
    public void confirmarCompra(String compraToken, String userToken) {
        // Validar que el compraToken existe
        Token token = this.tokenDao.findById(compraToken).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token de compra no válido"));

        // Si hay userToken, validar que pertenece al usuario correcto
        if (userToken != null && !userToken.isEmpty() && !userToken.equals("null")
                && !userToken.equals("undefined")) {
            String emailActual = usuariosService.checkToken(userToken);
            if (emailActual != null && token.getEmailUsuario() != null
                    && !emailActual.equals(token.getEmailUsuario())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para confirmar esta compra");
            }
        }

        // Marcar todas las entradas como VENDIDA
        for (Entrada entrada : token.getEntradas()) {
            this.entradaDao.updateEstado(entrada.getId(), Estado.VENDIDA);
        }

        // Marcar el token como pagado (si tienes un campo de estado en Token)
        // token.setPagado(true);
        // this.tokenDao.save(token);
    }

    public List<Token> getCarritosDelUsuario(String userToken) {
        // Obtener email del usuario desde el userToken
        if (userToken == null || userToken.isEmpty() || userToken.equals("null")
                || userToken.equals("undefined")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de usuario no válido");
        }

        String emailUsuario = usuariosService.checkToken(userToken);
        if (emailUsuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de usuario expirado o inválido");
        }

        System.out.println("[RESERVAS] Buscando carritos para usuario: " + emailUsuario);

        // 1. Buscar carritos YA vinculados al usuario
        List<Token> carritos = this.tokenDao.findByEmailUsuarioOrderByHoraDesc(emailUsuario);

        System.out.println("[RESERVAS] Carritos vinculados a " + emailUsuario + ": " + carritos.size());

        // 2. Si no hay carritos vinculados, buscar carritos sin vinculación
        // (emailUsuario = null)
        // que probablemente el usuario creó antes de loguear
        if (carritos.isEmpty()) {
            System.out.println("[RESERVAS] Buscando carritos sin vinculación (emailUsuario = null)...");
            List<Token> unboundTokens = this.tokenDao.findRecentUnboundTokens();

            // Tomar el más reciente (primer elemento)
            if (!unboundTokens.isEmpty()) {
                Token tokenReciente = unboundTokens.get(0);

                // Verificar que tenga entradas
                if (tokenReciente.getEntradas() != null && !tokenReciente.getEntradas().isEmpty()) {
                    // Vincularlo al usuario actual
                    tokenReciente.setEmailUsuario(emailUsuario);
                    this.tokenDao.save(tokenReciente);

                    System.out.println("[RESERVAS] Token sin vinculación recuperado y vinculado a " + emailUsuario);
                    System.out.println("[RESERVAS] Token: " + tokenReciente.getValor() + ", Entradas: "
                            + tokenReciente.getEntradas().size());

                    carritos = List.of(tokenReciente);
                }
            }
        }

        // Filtrar solo los que tienen entradas
        List<Token> carritosConEntradas = carritos.stream()
                .filter(t -> t.getEntradas() != null && !t.getEntradas().isEmpty())
                .toList();

        System.out.println(
                "[RESERVAS] Total carritos con entradas para " + emailUsuario + ": " + carritosConEntradas.size());

        for (Token t : carritosConEntradas) {
            System.out.println("  - Token: " + t.getValor() + ", Entradas: " + t.getEntradas().size()
                    + ", Email: " + t.getEmailUsuario() + ", Hora: " + t.getHora());
        }

        return carritosConEntradas;
    }
}