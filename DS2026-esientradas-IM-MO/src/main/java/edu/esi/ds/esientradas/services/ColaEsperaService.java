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

    /**
     * nombre_metodo: espectaculoTieneCola
     * parametros: espectaculoId
     * funcion: determina si un espectaculo pertenece al escenario configurado con
     * cola de espera
     * flujo_en_el_que_participa: decision de activar logica de cola en reservas
     * comunicacion: EspectaculoDao.findById
     */
    public boolean espectaculoTieneCola(Long espectaculoId) {
        Optional<Espectaculo> espectaculo = espectaculoDao.findById(espectaculoId);
        if (espectaculo.isEmpty() || espectaculo.get().getEscenario() == null) {
            return false;
        }
        return ESCENARIO_COLA_ID.equals(espectaculo.get().getEscenario().getId());
    }

    /**
     * nombre_metodo: unirseACola
     * parametros: espectaculoId, emailUsuario, compraTokenPreferido
     * funcion: inserta al usuario en cola si no estaba ya y devuelve su posicion
     * actual
     * flujo_en_el_que_participa: registro de espera cuando no hay entradas libres
     * comunicacion: ColaEsperaDao, EspectaculoDao, normalizarToken,
     * calcularPosicion
     */
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

    /**
     * nombre_metodo: getEstadoCola
     * parametros: espectaculoId, emailUsuario
     * funcion: devuelve estado resumido de cola (en cola, posicion, token asignado)
     * flujo_en_el_que_participa: consulta periodica del usuario en pantalla de
     * espera
     * comunicacion: ColaEsperaDao.findFirst..., calcularPosicion
     */
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

    /**
     * nombre_metodo: procesarColaSiAplica
     * parametros: espectaculoId
     * funcion: atiende cola asignando entradas libres a usuarios en orden de
     * llegada
     * flujo_en_el_que_participa: reasignacion automatica tras
     * cancelaciones/caducidades
     * comunicacion: EntradaDao, ColaEsperaDao, TokenDao, resolverTokenParaCola
     */
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

    /**
     * nombre_metodo: resolverTokenParaCola
     * parametros: item
     * funcion: reutiliza token preferido compatible o crea uno nuevo para la
     * asignacion
     * flujo_en_el_que_participa: puente entre cola de espera y carrito de compra
     * comunicacion: TokenDao.findById/save
     */
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

    /**
     * nombre_metodo: calcularPosicion
     * parametros: item
     * funcion: calcula posicion del usuario dentro de la cola ordenada
     * flujo_en_el_que_participa: feedback de estado para el usuario en espera
     * comunicacion:
     * ColaEsperaDao.findByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc
     */
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

    /**
     * nombre_metodo: normalizarToken
     * parametros: compraToken
     * funcion: limpia valores vacios o marcadores de frontend para tratarlos como
     * nulos
     * flujo_en_el_que_participa: validacion de entrada antes de buscar/reusar token
     * comunicacion: ningun componente externo (utilidad interna)
     */
    private String normalizarToken(String compraToken) {
        if (compraToken == null || compraToken.isBlank() || "null".equals(compraToken)
                || "undefined".equals(compraToken)) {
            return null;
        }
        return compraToken;
    }
}
