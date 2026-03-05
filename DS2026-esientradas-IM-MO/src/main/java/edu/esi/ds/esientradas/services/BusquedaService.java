package edu.esi.ds.esientradas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.dto.DtoEntradas;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Estado;

@Service
public class BusquedaService {

    @Autowired
    private EscenarioDao dao;

    @Autowired
    private EspectaculoDao espectaculoDao;

    @Autowired
    private EntradaDao entradaDao;

    //@Autowired
    //private EntradaDao entradaDao;

    public List<Escenario> getEscenarios() {
        return this.dao.findAll();
    }

    public List<Espectaculo> getEspectaculos(@RequestParam String artista) {
       return this.espectaculoDao.findByArtista(artista);
    }

    public List<Entrada> getEntradas(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoId(espectaculoId);
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
            ((Number) arr[3]).intValue()
        );
        return dto;
    }

}
