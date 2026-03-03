import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EspectaculosService {

  // El constructor inyecta la herramienta para hacer peticiones HTTP
  constructor(private http: HttpClient) { }

  getEscenarios() {
    return this.http.get('http://localhost:8080/busqueda/getEscenarios');
  }

  getEspectaculos(escenario: any) {
    return this.http.get(`http://localhost:8080/busqueda/getEspectaculos/${escenario.id}`);
  }

  getEntradas(espectaculo: any) {
    return this.http.get(`http://localhost:8080/busqueda/getEntradas?espectaculoId=${espectaculo.id}`);
  }
}