import { Component } from '@angular/core';
import { Pagos } from '../pagos';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

declare let Stripe: any;

@Component({
  selector: 'app-compra',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compra.html',
  styleUrl: './compra.css',
})
export class Compra {

  importe: number = 20;
  clientSecret?: string; // La ? quiere decir que puede tener valor o no (undefined)
  stripe = Stripe('pk_test_51T92li3gOJNjp26dAGHSKdNSIOYItMdsHRX9H7faPz7lA4aMItqNcJcdEEVtvnRhRuvL2LyH3iYDyctxx89hXwXF00UFwKEYL2'); // Aquí va tu clave pública de Stripe

  constructor(private service: Pagos){}

  irAlPago() {
    let info = {
      centimos: Math.floor(this.importe.valueOf() * 100)
    }
    this.service.prepararPago(info).subscribe(
      (response) => {
      this.clientSecret = response;
      // Aquí puedes redirigir al usuario a la página de pago de Stripe utilizando el clientSecret recibido
    }, (error) => {
      console.error('Error al preparar el pago:', error);
      // Aquí puedes mostrar un mensaje de error al usuario o manejar el error de alguna otra manera
    });
  }

showForm() {
 let elements = this.stripe.elements()
 let style = {
 base: {
 color: "#32325d", fontFamily: 'Arial, sans-serif',
 fontSmoothing: "antialiased", fontSize: "16px",
 "::placeholder": {
 color: "#32325d"
 }
 },invalid: {
 fontFamily: 'Arial, sans-serif', color: "#fa755a",
 iconColor: "#fa755a"
 }
 }
 let card = elements.create("card", { style : style })
 card.mount("#card-element")
 card.on("change", function(event : any) {
 document.querySelector("button")!.disabled = event.empty;
 document.querySelector("#card-error")!.textContent =
event.error ? event.error.message : "";
 });
 let self = this
 let form = document.getElementById("payment-form");
 form!.addEventListener("submit", function(event) {
 event.preventDefault();
 self.payWithCard(card);
 });
 form!.style.display = "block"
}
  payWithCard(card: any) { //Añadir del pdf
    throw new Error('Method not implemented.');
  }

}

// Para utilizar el Stripe en el frontend con angular, con nmp 