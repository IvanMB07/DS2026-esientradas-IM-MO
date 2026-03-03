import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { EspectaculosService } from '../espectaculos';

@Component({
  selector: 'app-espectaculos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './espectaculos.html',
  styleUrl: './espectaculos.css',
})
export class Espectaculos {

  escenarios: any = [];

  constructor(private espectaculosService: EspectaculosService) { }

  getEscenarios() {
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
    this.espectaculosService.getEspectaculos(escenario).subscribe(
      (response: any) => {
        escenario.espectaculos = response;
      },
      (error: any) => {
        console.error('Error al obtener los espectáculos:', error);
      }
    );
  }

  getEntradas(espectaculo: any) {
    this.espectaculosService.getEntradas(espectaculo).subscribe(
      (response: any) => {
        espectaculo.entradas = response;
      },
      (error: any) => {
        console.error('Error al obtener las entradas:', error);
      }
    );
  }

}
