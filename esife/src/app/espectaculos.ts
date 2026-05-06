import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EspectaculosService {

  constructor(private http: HttpClient) { }

  getEscenarios() {
    return this.http.get<any[]>('http://localhost:8080/busqueda/getEscenarios');
  }

  // Búsqueda de espectáculos por texto y/o fecha
  searchEspectaculos(query?: string, fecha?: string) {
    let params: any = {};
    if (query) params.q = query;
    if (fecha) params.fecha = fecha;
    const qs = Object.keys(params).map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`).join('&');
    const url = `http://localhost:8080/busqueda/search${qs ? ('?' + qs) : ''}`;
    return this.http.get<any[]>(url);
  }

  getEspectaculos(escenario: any) {
    return this.http.get<any[]>(
      `http://localhost:8080/busqueda/getEspectaculos/${escenario.id}`
    );
  }

  getNumeroDeEntradas(espectaculo: any) {
    return this.http.get<any[]>(
      `http://localhost:8080/busqueda/getNumeroDeEntradas?espectaculoId=${espectaculo.id}`
    );
  }

  getEntradasLibres(espectaculo: any) {
    return this.http.get<any>(
      `http://localhost:8080/busqueda/getEntradasLibres?espectaculoId=${espectaculo.id}`
    );
  }

  getNumeroDeEntradasComoDto(espectaculo: any) {
    return this.http.get<any>(
      `http://localhost:8080/busqueda/getNumeroDeEntradasComoDto?espectaculoId=${espectaculo.id}`
    );
  }

  getToken() {
    return this.http.get<any>(
      'http://localhost:8080/auth/getToken',
      { withCredentials: true }
    );
  }

  // Búsqueda de espectáculos por texto y/o fecha
  searchEspectaculos(query?: string, fecha?: string) {
    let params: any = {};
    if (query) params.q = query;
    if (fecha) params.fecha = fecha;
    const qs = Object.keys(params).map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`).join('&');
    const url = `http://localhost:8080/busqueda/search${qs ? ('?' + qs) : ''}`;
    return this.http.get<any[]>(url);
  }

}