import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
/**
 * nombre_clase: EspectaculosService
 * funcion: servicio para obtener información de espectáculos y escenarios
 * flujo_en_el_que_participa: búsqueda y visualización de espectáculos
 * comunicacion: backend de entradas
 */
export class EspectaculosService {

  constructor(private http: HttpClient) { }

  /**
   * nombre_metodo: getEscenarios
   * parametros: ninguno
   * funcion: obtiene lista de escenarios disponibles
   * flujo_en_el_que_participa: búsqueda de espectáculos
   */
  getEscenarios() {
    return this.http.get<any[]>('http://localhost:8080/busqueda/getEscenarios');
  }

  /**
   * nombre_metodo: searchEspectaculos
   * parametros: query, fecha
   * funcion: busca espectáculos por texto y/o fecha
   * flujo_en_el_que_participa: búsqueda de espectáculos
   */
  // Búsqueda de espectáculos por texto y/o fecha
  searchEspectaculos(query?: string, fecha?: string) {
    let params: any = {};
    if (query) params.q = query;
    if (fecha) params.fecha = fecha;
    const qs = Object.keys(params).map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`).join('&');
    const url = `http://localhost:8080/busqueda/search${qs ? ('?' + qs) : ''}`;
    return this.http.get<any[]>(url);
  }

  /**
   * nombre_metodo: getEspectaculos
   * parametros: escenario
   * funcion: obtiene espectáculos de un escenario específico
   * flujo_en_el_que_participa: búsqueda de espectáculos
   */
  getEspectaculos(escenario: any) {
    return this.http.get<any[]>(
      `http://localhost:8080/busqueda/getEspectaculos/${escenario.id}`
    );
  }

  /**
   * nombre_metodo: getNumeroDeEntradas
   * parametros: espectaculo
   * funcion: obtiene número total de entradas de un espectáculo
   * flujo_en_el_que_participa: búsqueda de disponibilidad
   */
  getNumeroDeEntradas(espectaculo: any) {
    return this.http.get<any[]>(
      `http://localhost:8080/busqueda/getNumeroDeEntradas?espectaculoId=${espectaculo.id}`
    );
  }

  /**
   * nombre_metodo: getEntradasLibres
   * parametros: espectaculo
   * funcion: obtiene número de entradas disponibles
   * flujo_en_el_que_participa: búsqueda de disponibilidad
   */
  getEntradasLibres(espectaculo: any) {
    return this.http.get<any>(
      `http://localhost:8080/busqueda/getEntradasLibres?espectaculoId=${espectaculo.id}`
    );
  }

  /**
   * nombre_metodo: getNumeroDeEntradasComoDto
   * parametros: espectaculo
   * funcion: obtiene información de entradas como DTO
   * flujo_en_el_que_participa: búsqueda de disponibilidad
   */
  getNumeroDeEntradasComoDto(espectaculo: any) {
    return this.http.get<any>(
      `http://localhost:8080/busqueda/getNumeroDeEntradasComoDto?espectaculoId=${espectaculo.id}`
    );
  }

  /**
   * nombre_metodo: getToken
   * parametros: ninguno
   * funcion: obtiene token de autenticación del backend
   * flujo_en_el_que_participa: autenticación
   */
  getToken() {
    return this.http.get<any>(
      'http://localhost:8080/auth/getToken',
      { withCredentials: true }
    );
  }

  unirseCola(espectaculoId: number, compraToken: string | null, userToken: string) {
    return this.http.post<any>('http://localhost:8080/reservas/cola/unirse', {
      espectaculoId,
      compraToken,
      userToken
    });
  }

  estadoCola(espectaculoId: number, userToken: string) {
    return this.http.get<any>(
      `http://localhost:8080/reservas/cola/estado?espectaculoId=${espectaculoId}&userToken=${encodeURIComponent(userToken)}`
    );
  }

}