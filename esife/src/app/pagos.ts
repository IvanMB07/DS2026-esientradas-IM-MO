import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class Pagos {

  constructor(private http: HttpClient) { }

  prepararPago(info: any) {
    return this.http.post('http://localhost:8080/pagos/prepararPago', info, { responseType: 'text' });
  }

  confirmarPago(tokenPrerreserva: string, tokenUsuario: string) {
    return this.http.post(`http://localhost:8080/pagos/confirmar?tokenPrerreserva=${tokenPrerreserva}&tokenUsuario=${tokenUsuario}`, {}, { responseType: 'text' });
  }
}
