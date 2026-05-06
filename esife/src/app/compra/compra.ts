import { Component, OnInit } from '@angular/core';
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
  clientSecret?: string;

  stripe: any = null; // se inicializa dinámicamente con la clave pública del backend
  card: any;

  constructor(private service: Pagos, private http: HttpClient, private auth: Auth, private router: Router) { }

  private getCompraToken(): string | null {
    return sessionStorage.getItem('compraToken');
  }

  private getCarrito(): any[] {
    const carritoGuardado = sessionStorage.getItem('carrito');
    return carritoGuardado ? JSON.parse(carritoGuardado) : [];
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
        alert("¡Pago realizado correctamente! Entradas compradas.");
        // Limpiar datos
        sessionStorage.removeItem('compraToken');
        sessionStorage.removeItem('carrito');
        // Redirigir a la pantalla de espectáculos
        this.router.navigate(['/espectaculos']);
      },
      error: (err) => {
        console.error("Error al confirmar el pago:", err);
        alert("Error al procesar la compra: " + (err.error || "Error desconocido"));
      }
    });
  }
}