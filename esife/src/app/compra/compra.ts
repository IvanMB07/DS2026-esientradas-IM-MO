import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { Pagos } from '../pagos';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Auth } from '../auth';
import { Router } from '@angular/router';

declare let Stripe: any;

@Component({
  selector: 'app-compra',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compra.html',
  styleUrl: './compra.css',
})
export class Compra implements OnInit {

  entradas: any[] = [];
  total: number = 0;
  compraExitosa: boolean = false;
  compraFallida: boolean = false;
  mensajeError: string = '';
  clientSecret?: string;
  tiempoRestanteSegundos: number | null = null;
  fechaExpiracionReserva: number | null = null;
  private contadorReservaId: ReturnType<typeof setInterval> | null = null;

  stripe: any = null; // se inicializa dinámicamente con la clave pública del backend
  card: any;

  constructor(private service: Pagos, private http: HttpClient, private auth: Auth, private router: Router, private cd: ChangeDetectorRef) { }

  ngOnDestroy() {
    this.detenerContadorReserva();
  }

  private getCompraToken(): string | null {
    return sessionStorage.getItem('compraToken');
  }

  private getCarrito(): any[] {
    const carritoGuardado = sessionStorage.getItem('carrito');
    return carritoGuardado ? JSON.parse(carritoGuardado) : [];
  }

  getDisplayName(e: any): string | null {
    return e?.espectaculo?.nombre || e?.espectaculo?.artista || e?.artista || e?.nombre || null;
  }

  getTiempoRestanteTexto(): string {
    if (this.tiempoRestanteSegundos === null) {
      return '';
    }

    const minutos = Math.floor(this.tiempoRestanteSegundos / 60);
    const segundos = this.tiempoRestanteSegundos % 60;
    return `${minutos}:${segundos.toString().padStart(2, '0')}`;
  }

  getReservaExpirada(): boolean {
    return this.tiempoRestanteSegundos !== null && this.tiempoRestanteSegundos <= 0;
  }

  ngOnInit() {
    // Inicializar Stripe solicitando la clave pública al backend
    this.http.get('http://localhost:8080/pagos/publicKey', { responseType: 'text' }).subscribe({
      next: (pk) => {
        if (pk) {
          try {
            this.stripe = Stripe(pk);
          } catch (e) {
            console.error('Error inicializando Stripe con la clave pública:', e);
          }
        } else {
          console.warn('No hay clave pública de Stripe configurada en el backend.');
        }
        this.cargarResumen();
      },
      error: (err) => {
        console.error('Error obteniendo la clave pública de Stripe:', err);
        // Aun así cargamos el resumen para no bloquear la vista
        this.cargarResumen();
      }
    });
  }

  cargarResumen() {
    const compraToken = this.getCompraToken();
    const userToken = this.auth.getToken();
    const carrito = this.getCarrito();

    if (!compraToken) {
      this.entradas = carrito;
      this.total = this.entradas.reduce((acc, e) => acc + (e.precio / 100), 0);
      return;
    }

    // Construir URL con ambos tokens
    let url = `http://localhost:8080/reservas/resumen?compraToken=${compraToken}`;
    if (userToken) {
      url += `&userToken=${userToken}`;
    }

    this.http.get<any>(url).subscribe({
      next: (res) => {
        // Si el backend devuelve el objeto Token, las entradas están en res.entradas
        // Si devuelve directamente la lista, se queda como res
        const entradasResumen = res.entradas || res;

        this.configurarContadorReserva(res);

        this.entradas = entradasResumen.map((entrada: any) => {
          const entradaCarrito = carrito.find((item: any) => item.id === entrada.id);
          // Combinar espectáculo: usar el del carrito como base y mergear con el del backend
          const espectaculoCarrito = entradaCarrito?.espectaculo || {};
          const espectaculoBackend = entrada.espectaculo || {};
          return {
            ...entrada,
            espectaculo: {
              ...espectaculoCarrito,
              ...espectaculoBackend
            }
          };
        });

        // Calculamos el total recorriendo la lista de entradas
        this.total = this.entradas.reduce((acc, e) => acc + (e.precio / 100), 0);
      },
      error: (err) => console.error("Error al cargar el resumen", err)
    });
  }

  private configurarContadorReserva(res: any) {
    const ahora = Date.now();
    const tiempoRestanteSegundos = typeof res?.tiempoRestanteSegundos === 'number'
      ? res.tiempoRestanteSegundos
      : typeof res?.tiempoRestanteMillis === 'number'
        ? Math.max(Math.ceil(res.tiempoRestanteMillis / 1000), 0)
        : null;

    const fechaExpiracion = typeof res?.horaExpiracion === 'number'
      ? res.horaExpiracion
      : tiempoRestanteSegundos !== null
        ? ahora + (tiempoRestanteSegundos * 1000)
        : null;

    this.fechaExpiracionReserva = fechaExpiracion;
    this.tiempoRestanteSegundos = tiempoRestanteSegundos;

    this.detenerContadorReserva();

    if (this.fechaExpiracionReserva !== null) {
      this.actualizarContadorReserva();
      this.contadorReservaId = setInterval(() => this.actualizarContadorReserva(), 1000);
    }
  }

  private actualizarContadorReserva() {
    if (this.fechaExpiracionReserva === null) {
      return;
    }

    const restanteMs = this.fechaExpiracionReserva - Date.now();
    this.tiempoRestanteSegundos = Math.max(Math.ceil(restanteMs / 1000), 0);

    if (this.tiempoRestanteSegundos === 0) {
      this.detenerContadorReserva();
    }

    try {
      this.cd.detectChanges();
    } catch (e) { /* noop */ }
  }

  private detenerContadorReserva() {
    if (this.contadorReservaId !== null) {
      clearInterval(this.contadorReservaId);
      this.contadorReservaId = null;
    }
  }

  irAlPago() {
    let info = {
      centimos: Math.floor(this.total * 100)
    };

    this.service.prepararPago(info).subscribe({
      next: (response) => {
        this.clientSecret = response;
        this.showForm();
      },
      error: (error) => {
        console.error('Error al preparar el pago:', error);
      }
    });
  }

  showForm() {
    let elements = this.stripe.elements();

    let style = {
      base: {
        color: "#32325d",
        fontFamily: 'Arial, sans-serif',
        fontSmoothing: "antialiased",
        fontSize: "16px",
        "::placeholder": {
          color: "#32325d"
        }
      },
      invalid: {
        fontFamily: 'Arial, sans-serif',
        color: "#fa755a",
        iconColor: "#fa755a"
      }
    };

    this.card = elements.create("card", { style: style });
    this.card.mount("#card-element");
    this.card.on("change", (event: any) => {
      const submitBtn = document.getElementById('submit-button') as HTMLButtonElement | null;
      if (submitBtn) submitBtn.toggleAttribute('disabled', event.empty);
      const errEl = document.querySelector("#card-error");
      if (errEl) errEl.textContent = event.error ? event.error.message : "";
    });

    let self = this;

    let form = document.getElementById("payment-form");
    if (form) {
      const submitBtn = form.querySelector('button[type="submit"]') as HTMLButtonElement | null;
      form.addEventListener("submit", function (event) {
        event.preventDefault();
        self.payWithCard();
      });

      const cancel = document.getElementById('cancel-payment');
      if (cancel) {
        cancel.addEventListener('click', () => {
          form!.style.display = 'none';
        });
      }

      if (submitBtn) submitBtn.disabled = true;
      form.style.display = "block";
    } else {
      console.error('No se encontró el formulario de pago (#payment-form)');
    }
  }

  async payWithCard() {
    if (!this.clientSecret) {
      console.error("No hay clientSecret");
      return;
    }

    const result = await this.stripe.confirmCardPayment(this.clientSecret, {
      payment_method: {
        card: this.card
      }
    });

    if (result.error) {
      // Mostrar error al usuario
      const errorElement = document.getElementById("card-error");
      if (errorElement) {
        errorElement.textContent = result.error.message;
      }
    } else {
      if (result.paymentIntent.status === "succeeded") {
        // Pago confirmado por Stripe, ahora confirmar en el backend
        this.confirmarPagoEnBackend();
      }
    }
  }

  private confirmarPagoEnBackend() {
    const compraToken = this.getCompraToken();
    const userToken = this.auth.getToken();

    if (!compraToken) {
      console.error("No hay compraToken");
      return;
    }

    if (!userToken) {
      console.error("No hay userToken");
      return;
    }

    // Llamar a /pagos/confirmar con el tokenPrerreserva
    this.service.confirmarPago(compraToken, userToken).subscribe({
      next: () => {
        // Limpiar datos
        sessionStorage.removeItem('compraToken');
        sessionStorage.removeItem('carrito');
        // Mostrar pantalla de éxito en la misma vista
        this.compraExitosa = true;
        // Forzar actualización de la vista para mostrar la pantalla de éxito inmediatamente
        try { this.cd.detectChanges(); } catch (e) { /* noop */ }
        const form = document.getElementById('payment-form');
        if (form) form.style.display = 'none';
      },
      error: (err) => {
        console.error("Error al confirmar el pago:", err);
        this.mensajeError = err?.error?.message || err?.error || "Error desconocido al procesar la compra";
        this.compraFallida = true;
        try { this.cd.detectChanges(); } catch (e) { /* noop */ }
        const form = document.getElementById('payment-form');
        if (form) form.style.display = 'none';
      }
    });
  }

  volverAEspectaculos() {
    this.detenerContadorReserva();
    this.router.navigate(['/espectaculos']);
  }

  reintentar() {
    // Restablecer estado de error/éxito y preparar para reintentar el pago
    this.compraFallida = false;
    this.mensajeError = '';
    this.compraExitosa = false;
    // Detener cualquier contador de reserva en curso y mostrar el formulario de pago
    this.detenerContadorReserva();
    const form = document.getElementById('payment-form');
    if (form) form.style.display = 'block';
  }
}