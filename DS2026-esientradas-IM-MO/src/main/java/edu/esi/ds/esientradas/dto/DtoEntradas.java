package edu.esi.ds.esientradas.dto;

public record DtoEntradas(
    Integer total,
    Integer libres,
    Integer reservadas,
    Integer vendidas) {

        public Integer getTotal() {
            return total;
        }
        public Integer getLibres() {
            return libres;
        }
        public Integer getReservadas() {
            return reservadas;
        }
        public Integer getVendidas() {
            return vendidas;
        }     

}
