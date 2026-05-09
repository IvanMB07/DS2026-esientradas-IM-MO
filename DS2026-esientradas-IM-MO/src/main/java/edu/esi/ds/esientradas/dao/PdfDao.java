package edu.esi.ds.esientradas.dao;

import edu.esi.ds.esientradas.model.PdfEntidad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * nombre_clase: PdfDao
 * parametros_clave: PdfEntidad, Long
 * funcion: persistencia y recuperación de documentos PDF en formato binario
 * flujo_en_el_que_participa: generación de entradas, descarga de recibos y
 * almacenamiento de justificantes
 * comunicacion: PdfService, EmailService, CompraService
 */
@Repository
public interface PdfDao extends CrudRepository<PdfEntidad, Long> {
}