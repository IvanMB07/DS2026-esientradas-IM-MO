import { Component, OnInit } from '@angular/core';
import { Pagos } from '../pagos';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Auth } from '../auth';

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

  stripe = Stripe('pk_test_51T92li3gOJNjp26dAGHSKdNSIOYItMdsHRX9H7faPz7lA4aMItqNcJcdEEVtvnRhRuvL2LyH3iYDyctxx89hXwXF00UFwKEYL2'); // Aquí va tu clave pública de Stripe
  card: any;

  constructor(private service: Pagos, private http: HttpClient, private auth: Auth) { }

  private getCompraToken(): string | null {
    return sessionStorage.getItem('compraToken');
  }

  private getCarrito(): any[] {
    const carritoGuardado = sessionStorage.getItem('carrito');
    return carritoGuardado ? JSON.parse(carritoGuardado) : [];
  }

  ngOnInit() {
    this.cargarResumen();
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
          return {
            ...entrada,
            espectaculo: entrada.espectaculo || entradaCarrito?.espectaculo
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

    this.card.on("change", function (event: any) {
      document.querySelector("button")!.toggleAttribute("disabled", event.empty);
      document.querySelector("#card-error")!.textContent =
        event.error ? event.error.message : "";
    });

    let self = this;

    let form = document.getElementById("payment-form");
    form!.addEventListener("submit", function (event) {
      event.preventDefault();
      self.payWithCard();
    });

    form!.style.display = "block";
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
        alert("Pago realizado correctamente");
        // Aquí puedes redirigir o limpiar el carrito
      }
    }
  }
}