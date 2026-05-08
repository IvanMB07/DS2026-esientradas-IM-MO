package edu.esi.ds.esientradas.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.ColaEsperaDao;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.ColaEspera;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.EstadoCola;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class ColaEsperaService {

    private static final Long ESCENARIO_COLA_ID = 6L;

    @Autowired
    private ColaEsperaDao colaEsperaDao;

    @Autowired
    private EspectaculoDao espectaculoDao;

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TokenDao tokenDao;

    public boolean espectaculoTieneCola(Long espectaculoId) {
        Optional<Espectaculo> espectaculo = espectaculoDao.findById(espectaculoId);
        if (espectaculo.isEmpty() || espectaculo.get().getEscenario() == null) {
            return false;
        }
        return ESCENARIO_COLA_ID.equals(espectaculo.get().getEscenario().getId());
    }

    @Transactional
    public Integer unirseACola(Long espectaculoId, String emailUsuario, String compraTokenPreferido) {
        if (!espectaculoTieneCola(espectaculoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Este espectáculo no tiene cola de espera habilitada");
        }
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para entrar en la cola de espera");
        }

        ColaEspera existente = colaEsperaDao.findFirstByEspectaculoIdAndEmailUsuarioAndEstado(
                espectaculoId, emailUsuario, EstadoCola.EN_COLA);

        if (existente != null) {
            return calcularPosicion(existente);
        }

        Espectaculo espectaculo = espectaculoDao.findById(espectaculoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espectáculo no encontrado"));

        ColaEspera item = new ColaEspera();
        item.setEspectaculo(espectaculo);
        item.setEmailUsuario(emailUsuario);
        item.setEstado(EstadoCola.EN_COLA);
        item.setHoraSolicitud(System.currentTimeMillis());
        item.setCompraTokenPreferido(normalizarToken(compraTokenPreferido));

        colaEsperaDao.save(item);
        return calcularPosicion(item);
    }

    public Map<String, Object> getEstadoCola(Long espectaculoId, String emailUsuario) {
        Map<String, Object> estado = new HashMap<>();
        estado.put("espectaculoId", espectaculoId);

        ColaEspera enCola = colaEsperaDao.findFirstByEspectaculoIdAndEmailUsuarioAndEstado(
                espectaculoId, emailUsuario, EstadoCola.EN_COLA);

        if (enCola != null) {
            estado.put("enCola", true);
            estado.put("posicion", calcularPosicion(enCola));
            estado.put("compraTokenAsignado", null);
            return estado;
        }

        ColaEspera atendida = colaEsperaDao
                .findFirstByEspectaculoIdAndEmailUsuarioAndEstadoOrderByHoraSolicitudDescIdDesc(
                        espectaculoId, emailUsuario, EstadoCola.ATENDIDA);

        estado.put("enCola", false);
        estado.put("posicion", 0);
        estado.put("compraTokenAsignado", atendida != null ? atendida.getCompraTokenAsignado() : null);
        return estado;
    }

    @Transactional
    public int procesarColaSiAplica(Long espectaculoId) {
        if (!espectaculoTieneCola(espectaculoId)) {
            return 0;
        }

        int atendidos = 0;
        while (true) {
            Entrada libre = entradaDao.findFirstByEspectaculoIdAndEstadoOrderByIdAsc(espectaculoId, Estado.DISPONIBLE);
            ColaEspera siguiente = colaEsperaDao.findFirstByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc(
                    espectaculoId, EstadoCola.EN_COLA);

            if (libre == null || siguiente == null) {
                break;
            }

            Token token = resolverTokenParaCola(siguiente);
            token.addEntrada(libre);
            tokenDao.save(token);

            libre.setEstado(Estado.RESERVADA);
            entradaDao.save(libre);

            siguiente.setEstado(EstadoCola.ATENDIDA);
            siguiente.setCompraTokenAsignado(token.getValor());
            colaEsperaDao.save(siguiente);

            atendidos++;
        }

        return atendidos;
    }

    private Token resolverTokenParaCola(ColaEspera item) {
        String tokenPreferido = normalizarToken(item.getCompraTokenPreferido());
        if (tokenPreferido != null) {
            Optional<Token> tokenExistente = tokenDao.findById(tokenPreferido);
            if (tokenExistente.isPresent()) {
                Token token = tokenExistente.get();
                if (token.getEmailUsuario() == null || item.getEmailUsuario().equals(token.getEmailUsuario())) {
                    if (token.getEmailUsuario() == null) {
                        token.setEmailUsuario(item.getEmailUsuario());
                        tokenDao.save(token);
                    }
                    return token;
                }
            }
        }

        Token tokenNuevo = new Token();
        tokenNuevo.setEmailUsuario(item.getEmailUsuario());
        return tokenDao.save(tokenNuevo);
    }

    private Integer calcularPosicion(ColaEspera item) {
        List<ColaEspera> cola = colaEsperaDao.findByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc(
                item.getEspectaculo().getId(), EstadoCola.EN_COLA);

        for (int i = 0; i < cola.size(); i++) {
            if (cola.get(i).getId().equals(item.getId())) {
                return i + 1;
            }
        }

        return 0;
    }

    private String normalizarToken(String compraToken) {
        if (compraToken == null || compraToken.isBlank() || "null".equals(compraToken)
                || "undefined".equals(compraToken)) {
            return null;
        }
        return compraToken;
    }
}
