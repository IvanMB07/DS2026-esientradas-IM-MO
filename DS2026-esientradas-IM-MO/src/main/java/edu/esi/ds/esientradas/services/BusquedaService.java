package edu.esi.ds.esientradas.services;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.dto.DtoEntradas;
import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Precisa;

@Service
public class BusquedaService {

    @Autowired
    private EscenarioDao dao;

    @Autowired
    private EspectaculoDao espectaculoDao;

    @Autowired
    private EntradaDao entradaDao;

    // @Autowired
    // private EntradaDao entradaDao;

    /**
     * nombre_metodo: getEscenarios
     * parametros: ninguno
     * funcion: recupera todos los escenarios disponibles
     * flujo_en_el_que_participa: carga de filtros y catalogo inicial
     * comunicacion: EscenarioDao.findAll
     */
    public List<Escenario> getEscenarios() {
        return this.dao.findAll();
    }

    /**
     * nombre_metodo: getEspectaculos
     * parametros: artista
     * funcion: busca espectaculos por nombre de artista
     * flujo_en_el_que_participa: busqueda textual en catalogo
     * comunicacion: EspectaculoDao.findByArtista
     */
    public List<Espectaculo> getEspectaculos(@RequestParam String artista) {
        return this.espectaculoDao.findByArtista(artista);
    }

    /**
     * nombre_metodo: getEntradas
     * parametros: espectaculoId
     * funcion: devuelve entradas del espectaculo mapeadas a estructura publica
     * legacy
     * flujo_en_el_que_participa: compatibilidad con consumidores antiguos
     * comunicacion: EntradaDao.findByEspectaculoId, toPublicEntryMap
     */
    public List<Map<String, Object>> getEntradas(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoId(espectaculoId).stream()
                .map(this::toPublicEntryMap)
                .toList();
    }

    /**
     * nombre_metodo: getEntradasResponse
     * parametros: espectaculoId
     * funcion: devuelve entradas del espectaculo con DTO tipado
     * flujo_en_el_que_participa: respuesta segura de endpoints de busqueda
     * comunicacion: EntradaDao.findByEspectaculoId, toEntradaResponse
     */
    public List<edu.esi.ds.esientradas.dto.EntradaResponse> getEntradasResponse(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoId(espectaculoId).stream()
                .map(this::toEntradaResponse)
                .toList();
    }

    /**
     * nombre_metodo: getEspectaculos
     * parametros: idEscenario
     * funcion: lista los espectaculos de un escenario
     * flujo_en_el_que_participa: navegacion por recinto en frontend
     * comunicacion: EspectaculoDao.findByEscenarioId
     */
    public List<Espectaculo> getEspectaculos(Long idEscenario) {
        return this.espectaculoDao.findByEscenarioId(idEscenario);
    }

    /**
     * nombre_metodo: getNumeroDeEntradas
     * parametros: espectaculoId
     * funcion: obtiene numero total de entradas asociadas a un espectaculo
     * flujo_en_el_que_participa: metrica de aforo total
     * comunicacion: EntradaDao.countByEspectaculoId
     */
    public Integer getNumeroDeEntradas(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoId(espectaculoId);
    }

    /**
     * nombre_metodo: getEntradasLibres
     * parametros: espectaculoId
     * funcion: obtiene numero de entradas disponibles para reservar
     * flujo_en_el_que_participa: decision de compra y disponibilidad en tiempo real
     * comunicacion: EntradaDao.countByEspectaculoIdAndEstado
     */
    public Integer getEntradasLibres(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    /**
     * nombre_metodo: getNumeroDeEntradasComoDto
     * parametros: espectaculoId
     * funcion: transforma agregados de base de datos a DtoEntradas
     * flujo_en_el_que_participa: endpoints de resumen estadistico
     * comunicacion: EntradaDao.getNumeroDeEntradasComoDto
     */
    public Object getNumeroDeEntradasComoDto(Long espectaculoId) {
        Object o = this.entradaDao.getNumeroDeEntradasComoDto(espectaculoId);
        Object[] arr = (Object[]) o;
        DtoEntradas dto = new DtoEntradas(
                ((Number) arr[0]).intValue(),
                ((Number) arr[1]).intValue(),
                ((Number) arr[2]).intValue(),
                ((Number) arr[3]).intValue());
        return dto;
    }

    /**
     * nombre_metodo: toPublicEntryMap
     * parametros: entrada
     * funcion: convierte entidad Entrada en mapa publico legacy
     * flujo_en_el_que_participa: serializacion compatible hacia clientes anteriores
     * comunicacion: modelos DeZona/Precisa para campos especificos
     */
    private Map<String, Object> toPublicEntryMap(Entrada entrada) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entrada.getId());
        map.put("precio", entrada.getPrecio());
        map.put("estado", entrada.getEstado());

        if (entrada instanceof DeZona ez) {
            map.put("zona", ez.getZona());
        } else if (entrada instanceof Precisa ep) {
            map.put("fila", ep.getFila());
            map.put("columna", ep.getColumna());
            map.put("planta", ep.getPlanta());
        }

        return map;
    }

    /**
     * nombre_metodo: toEntradaResponse
     * parametros: entrada
     * funcion: convierte entidad Entrada en DTO tipado para API segura
     * flujo_en_el_que_participa: respuesta estandarizada en endpoints de busqueda
     * comunicacion: modelos DeZona/Precisa y dto.EntradaResponse
     */
    private edu.esi.ds.esientradas.dto.EntradaResponse toEntradaResponse(Entrada entrada) {
        edu.esi.ds.esientradas.dto.EntradaResponse response = new edu.esi.ds.esientradas.dto.EntradaResponse();
        response.setId(entrada.getId());
        response.setPrecio(entrada.getPrecio());
        response.setEstado(entrada.getEstado().toString());

        if (entrada instanceof DeZona ez) {
            response.setZona(ez.getZona());
        } else if (entrada instanceof Precisa ep) {
            response.setFila(ep.getFila());
            response.setColumna(ep.getColumna());
            response.setPlanta(ep.getPlanta());
        }

        return response;
    }

}