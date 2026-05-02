package edu.esi.ds.esientradas.dao;

import edu.esi.ds.esientradas.model.PdfEntidad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfDao extends CrudRepository<PdfEntidad, Long> {
}
