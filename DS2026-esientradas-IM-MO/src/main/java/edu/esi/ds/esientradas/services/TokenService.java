package edu.esi.ds.esientradas.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Token;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private ColaEsperaService colaEsperaService;

    private static final long EXPIRATION_TIME_MILLIS = Token.DURACION_RESERVA_MILLIS;

    @Transactional
    public void liberarEntradasExpiredToken(Token token) {
        if (token != null && token.getEntradas() != null) {
            logger.info("[AUDITORÍA] Expirando token de compra: {}. Liberando entradas asociadas.", token.getValor());

            Set<Long> espectaculosLiberados = new HashSet<>();

            for (Entrada entrada : token.getEntradas()) {
                if (entrada.getEstado() == Estado.RESERVADA) {
                    entrada.setEstado(Estado.DISPONIBLE);
                    entradaDao.save(entrada);
                    if (entrada.getEspectaculo() != null) {
                        espectaculosLiberados.add(entrada.getEspectaculo().getId());
                    }
                }
            }

            token.getEntradas().clear();
            tokenDao.delete(token);

            for (Long espectaculoId : espectaculosLiberados) {
                colaEsperaService.procesarColaSiAplica(espectaculoId);
            }
        }
    }

    @Scheduled(fixedRate = 60000) // Revisión cada minuto
    public void liberarEntradasReservadas() {
        long tiempoLimite = System.currentTimeMillis() - EXPIRATION_TIME_MILLIS;
        List<Token> tokensCaducados = tokenDao.findByHoraBefore(tiempoLimite);

        if (!tokensCaducados.isEmpty()) {
            logger.info("Iniciando limpieza automática de {} carritos caducados.", tokensCaducados.size());
            for (Token token : tokensCaducados) {
                liberarEntradasExpiredToken(token);
            }
        }
    }
}