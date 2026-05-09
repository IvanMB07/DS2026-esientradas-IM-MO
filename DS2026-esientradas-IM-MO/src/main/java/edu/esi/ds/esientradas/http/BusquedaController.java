package edu.esi.ds.esientradas.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.dto.DtoEntradas;
import edu.esi.ds.esientradas.dto.DtoEspectaculo;
import edu.esi.ds.esientradas.dto.EntradaResponse;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.services.BusquedaService;

@RestController
@RequestMapping("/busqueda")
@CrossOrigin(origins = "http://localhost:4200") // Limitado al frontend Angular local.
public class BusquedaController {

    @Autowired
    private BusquedaService service;

    /**
     * nombre_metodo: getEntradas
     * parametros: espectaculoId
     * funcion: devuelve el listado de entradas de un espectaculo en formato de
     * respuesta publica
     * flujo_en_el_que_participa: exploracion de disponibilidad antes de reservar
     */
    @GetMapping("/getEntradas")
    public List<EntradaResponse> getEntradas(@RequestParam Long espectaculoId) {
        return this.service.getEntradasResponse(espectaculoId);
    }

    /**
     * nombre_metodo: getNumeroDeEntradas
     * parametros: espectaculoId
     * funcion: devuelve entradas de espectaculo reutilizando el mismo contrato de
     * respuesta
     * flujo_en_el_que_participa: compatibilidad con consumidores legacy del
     * frontend
     */
    @GetMapping("/getNumeroDeEntradas")
    public List<EntradaResponse> getNumeroDeEntradas(@RequestParam Long espectaculoId) {
        return this.service.getEntradasResponse(espectaculoId);
    }

    /**
     * nombre_metodo: getNumeroDeEntradasComoDto
     * parametros: espectaculoId
     * funcion: devuelve un DTO agregado con metricas de entradas del espectaculo
     * flujo_en_el_que_participa: visualizaciones resumidas de
     * capacidad/disponibilidad
     */
    @GetMapping("/getNumeroDeEntradasComoDto")
    public DtoEntradas getNumeroDeEntradasComoDto(@RequestParam Long espectaculoId) {
        DtoEntradas dto = (DtoEntradas) this.service.getNumeroDeEntradasComoDto(espectaculoId);
        return dto;
    }

    /**
     * nombre_metodo: getEntradasLibres
     * parametros: espectaculoId
     * funcion: obtiene el numero de entradas libres de un espectaculo
     * flujo_en_el_que_participa: decision de compra en pantalla de detalle
     */
    @GetMapping("/getEntradasLibres")
    public Integer getEntradasLibres(@RequestParam Long espectaculoId) {
        return this.service.getEntradasLibres(espectaculoId);
    }

    /**
     * nombre_metodo: getEspectaculos
     * parametros: artista
     * funcion: busca espectaculos por artista y los transforma a DTO de salida
     * flujo_en_el_que_participa: busqueda principal de catalogo
     */
    @GetMapping("/getEspectaculos")
    public List<DtoEspectaculo> getEspectaculos(@RequestParam String artista) {
        List<Espectaculo> espectaculos = this.service.getEspectaculos(artista);

        List<DtoEspectaculo> dtos = espectaculos.stream().map(e -> {
            DtoEspectaculo dto = new DtoEspectaculo();
            dto.setId(e.getId());
            dto.setArtista(e.getArtista());
            dto.setFecha(e.getFecha());
            dto.setEscenario(e.getEscenario().getNombre());
            return dto;
        }).toList();
        return dtos;
    }

    /**
     * nombre_metodo: getEspectaculos
     * parametros: idEscenario
     * funcion: lista espectaculos asociados a un escenario concreto
     * flujo_en_el_que_participa: navegacion por escenario en catalogo
     */
    @GetMapping("/getEspectaculos/{idEscenario}")
    public List<DtoEspectaculo> getEspectaculos(@PathVariable Long idEscenario) {
        List<Espectaculo> espectaculos = this.service.getEspectaculos(idEscenario);

        List<DtoEspectaculo> dtos = espectaculos.stream().map(e -> {
            DtoEspectaculo dto = new DtoEspectaculo();
            dto.setId(e.getId());
            dto.setArtista(e.getArtista());
            dto.setFecha(e.getFecha());
            dto.setEscenario(e.getEscenario().getNombre());
            return dto;
        }).toList();
        return dtos;
    }

    /**
     * nombre_metodo: getEscenarios
     * parametros: ninguno
     * funcion: devuelve todos los escenarios disponibles
     * flujo_en_el_que_participa: filtros de busqueda y seleccion de recinto
     */
    @GetMapping("/getEscenarios")
    public List<Escenario> getEscenarios() {
        return this.service.getEscenarios();
    }

}