package edu.esi.ds.esientradas.services;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public List<Escenario> getEscenarios() {
        return this.dao.findAll();
    }

    public List<Espectaculo> getEspectaculos(@RequestParam String artista) {
        return this.espectaculoDao.findByArtista(artista);
    }

    public List<Map<String, Object>> getEntradas(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoId(espectaculoId).stream()
                .map(this::toPublicEntryMap)
                .toList();
    }

    public List<edu.esi.ds.esientradas.dto.EntradaResponse> getEntradasResponse(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoId(espectaculoId).stream()
                .map(this::toEntradaResponse)
                .toList();
    }

    public List<Espectaculo> getEspectaculos(Long idEscenario) {
        return this.espectaculoDao.findByEscenarioId(idEscenario);
    }

    public Integer getNumeroDeEntradas(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoId(espectaculoId);
    }

    public Integer getEntradasLibres(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

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