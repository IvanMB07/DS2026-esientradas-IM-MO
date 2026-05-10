import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
/**
 * nombre_clase: Pagos
 * funcion: servicio para preparación y confirmación de pagos con Stripe
 * flujo_en_el_que_participa: proceso de compra de entradas
 * comunicacion: backend de entradas, Stripe API
 */
export class Pagos {

  constructor(private http: HttpClient) { }

  /**
   * nombre_metodo: prepararPago
   * parametros: info
   * funcion: prepara datos de pago para Stripe
   * flujo_en_el_que_participa: proceso de pago
   */
  prepararPago(info: any) {
    return this.http.post('http://localhost:8080/pagos/prepararPago', info, { responseType: 'text' });
  }

  /**
   * nombre_metodo: confirmarPago
   * parametros: tokenPrerreserva, tokenUsuario
   * funcion: confirma el pago después de autenticación con Stripe
   * flujo_en_el_que_participa: confirmación de compra
   */
  confirmarPago(tokenPrerreserva: string, tokenUsuario: string) {
    return this.http.post(`http://localhost:8080/pagos/confirmar?tokenPrerreserva=${tokenPrerreserva}&tokenUsuario=${tokenUsuario}`, {}, { responseType: 'text' });
  }
}
