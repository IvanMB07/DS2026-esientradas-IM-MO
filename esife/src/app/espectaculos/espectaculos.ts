import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { EspectaculosService } from '../espectaculos';
import { Router } from '@angular/router';

@Component({
  selector: 'app-espectaculos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './espectaculos.html',
  styleUrl: './espectaculos.css',
})
export class Espectaculos {

  escenarios: any = [];

  constructor(private espectaculosService: EspectaculosService, private router: Router) { }
  
  getEscenarios() {
    // Si ya hay escenarios, los ocultamos (toggle)
    if (this.escenarios && this.escenarios.length > 0) {
      this.escenarios = [];
      return;
    }
    this.espectaculosService.getEscenarios().subscribe(
      (response) => {
        this.escenarios = response;
      },
      (error) => {
        console.error('Error al obtener los escenarios:', error);
      }
    );
  }

  getEspectaculos(escenario: any) {
    // Si ya tiene espectáculos cargados, los ocultamos
    if (escenario.espectaculos && escenario.espectaculos.length > 0) {
      escenario.espectaculos = [];
      return;
    }
    this.espectaculosService.getEspectaculos(escenario).subscribe(
      (response: any) => {
        escenario.espectaculos = response;
      },
      (error: any) => {
        console.error('Error al obtener los espectáculos:', error);
      }
    );
  }

  getEntradasLibres(espectaculo: any) {
    this.espectaculosService.getEntradasLibres(espectaculo).subscribe(
      (response: any) => {
        espectaculo.entradasLibres = response;
      },
      (error: any) => {
        console.error('Error al obtener las entradas:', error);
      }
    );
  }

  /*getNumeroDeEntradas(espectaculo: any) {
    this.espectaculosService.getNumeroDeEntradasComoDto(espectaculo).subscribe(
      (response: any) => {
        espectaculo.resumen = response;
      },
      (error) => {
        console.error('Error al obtener las entradas:', error);
      }
    );
  }*/

  getNumeroDeEntradas(espectaculo: any) {
    // Si ya tiene entradas cargadas, ocultamos la lista y el resumen
    if (espectaculo.entradas && espectaculo.entradas.length > 0) {
      espectaculo.entradas = [];
      espectaculo.resumen = null; // Esto ocultará el resumen en el HTML
      return;
    }
    //Cargamos el DTO con los contadores (Total, Libres, etc.)
    this.espectaculosService.getNumeroDeEntradasComoDto(espectaculo).subscribe(
      (response: any) => {
        espectaculo.resumen = response; //Guardamos el DTO en 'resumen'
      }
    );

    //Cargamos la lista de entradas para el *ngFor
    //Usamos el método que devuelve List<Entrada> del controlador
    this.espectaculosService.getNumeroDeEntradas(espectaculo).subscribe(
      (response: any) => {
        espectaculo.entradas = response; // Esto activa el *ngFor del HTML
      },
      (error) => console.error('Error:', error)
    );
  }

  irAComprarEntradas(){
    this.router.navigate(['/comprar']);
  }

}
