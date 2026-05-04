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
        // 1. Obtener el token de compra
        Token token = this.tokenDao.findById(compraToken).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token de compra no válido"));

        // 2. Validar identidad del usuario (checkToken extrae el email del token de
        // sesión)
        String emailActual = null;
        if (userToken != null && !userToken.isEmpty() && !userToken.equals("null") && !userToken.equals("undefined")) {
            emailActual = usuariosService.checkToken(userToken);
        }

        // 3. [SEGURIDAD] Verificar que el usuario tiene permiso para modificar este
        // token
        // Principio: "Record Ownership" - el usuario solo puede modificar sus propias
        // entradas
        if (token.getEmailUsuario() != null) {
            // El token está vinculado a un email específico (usuario registrado)
            if (emailActual == null) {
                // Intenta cancelar un token de otro usuario sin estar autenticado
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Debes iniciar sesión para cancelar entradas de una compra existente");
            }
            if (!emailActual.equals(token.getEmailUsuario())) {
                // El usuario autenticado no es el dueño del token - ATAQUE IDOR BLOQUEADO
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para cancelar entradas de otro usuario");
            }
        }
        // Si token.getEmailUsuario() es null (carrito anónimo), permitir cancelación

        // 4. Verificar que la entrada pertenece a este token
        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if (!token.getEntradas().contains(entrada)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esta entrada no pertenece a tu carrito");
        }

        // 5. Cancelar la entrada (ahora validada como segura)
        token.getEntradas().removeIf(e -> e.getId().equals(idEntrada));
        this.tokenDao.save(token);
        this.entradaDao.updateEstado(idEntrada, Estado.DISPONIBLE);
    }

    public Token getResumenCompra(String tokenValor, String userToken) {
        // 1. Buscamos el carrito
        Token token = this.tokenDao.findById(tokenValor).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        // 2. Validamos el token de usuario SIEMPRE que venga algo
        String emailActual = null;
        if (userToken != null && !userToken.isEmpty() && !userToken.equals("null") && !userToken.equals("undefined")) {
            // Aquí es donde se hace la llamada al otro backend
            emailActual = usuariosService.checkToken(userToken);
        }

        // --- LÓGICA DE CONTROL DE ACCESO ---
        if (token.getEmailUsuario() != null) {
            if (emailActual == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión para ver este carrito");
            }
            if (!emailActual.equals(token.getEmailUsuario())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este carrito pertenece a otro usuario");
            }
        } else if (emailActual != null) {
            // Vinculación automática si el carrito era anónimo
            token.setEmailUsuario(emailActual);
            this.tokenDao.save(token);
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