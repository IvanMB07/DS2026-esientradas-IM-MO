import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { EspectaculosService } from '../espectaculos';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-espectaculos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './espectaculos.html',
  styleUrl: './espectaculos.css',
})
export class Espectaculos {
  escenarios: any = [];
  entradasSeleccionadas: any[] = []; // Nuestro "carrito" temporal

  constructor(private espectaculosService: EspectaculosService, private router: Router, private http: HttpClient) { }

  private getCompraToken(): string | null {
    return localStorage.getItem('compraToken') || sessionStorage.getItem('compraToken');
  }

  private saveCompraToken(token: string) {
    localStorage.setItem('compraToken', token);
    sessionStorage.setItem('compraToken', token);
  }

  private getCarrito(): any[] {
    const carritoGuardado = localStorage.getItem('carrito') || sessionStorage.getItem('carrito');
    return carritoGuardado ? JSON.parse(carritoGuardado) : [];
  }

  private saveCarrito(carrito: any[]) {
    localStorage.setItem('carrito', JSON.stringify(carrito));
    sessionStorage.setItem('carrito', JSON.stringify(carrito));
  }

  ngOnInit() {
    this.entradasSeleccionadas = this.getCarrito();

    const token = this.getCompraToken();
    if (token) {
      // Al cargar, preguntamos al backend qué tenemos ya reservado
      this.http.get<any>(`http://localhost:8080/reservas/resumen?compraToken=${token}`)
        .subscribe(res => {
          const entradasBackend = res.entradas || [];

          // Mezclamos el resumen del backend con lo que ya guardamos para no perder el nombre del espectáculo
          this.entradasSeleccionadas = entradasBackend.map((entrada: any) => {
            const entradaGuardada = this.entradasSeleccionadas.find((item: any) => item.id === entrada.id);
            return {
              ...entrada,
              espectaculo: entrada.espectaculo || entradaGuardada?.espectaculo
            };
          });

          // Si el backend no devolvió nada, mantenemos lo que ya estaba persistido
          if (this.entradasSeleccionadas.length === 0) {
            this.entradasSeleccionadas = this.getCarrito();
          }

          this.saveCarrito(this.entradasSeleccionadas);
        });
    }
  }

  getEscenarios() {
    if (this.escenarios && this.escenarios.length > 0) {
      this.escenarios = [];
      return;
    }
    this.espectaculosService.getEscenarios().subscribe(response => this.escenarios = response);
  }

  getEspectaculos(escenario: any) {
    if (escenario.espectaculos && escenario.espectaculos.length > 0) {
      escenario.espectaculos = [];
      return;
    }
    this.espectaculosService.getEspectaculos(escenario).subscribe(response => escenario.espectaculos = response);
  }

  getNumeroDeEntradas(espectaculo: any) {
    if (espectaculo.entradasAgrupadas) {
      espectaculo.entradasAgrupadas = null;
      espectaculo.resumen = null;
      return;
    }

    this.espectaculosService.getNumeroDeEntradasComoDto(espectaculo).subscribe(res => espectaculo.resumen = res);

    this.espectaculosService.getNumeroDeEntradas(espectaculo).subscribe((entradas: any) => {
      // Agrupamos las entradas por Zona o por "Asientos" si no hay zona definida
      espectaculo.entradasAgrupadas = entradas.reduce((acc: any, entrada: any) => {
        const grupo = entrada.zona ? `Zona: ${entrada.zona}` : 'Asientos Numerados';
        if (!acc[grupo]) acc[grupo] = [];
        acc[grupo].push(entrada);
        return acc;
      }, {});
    });
  }

  // Lógica para seleccionar/deseleccionar entradas
  // En espectaculos.ts, actualizamos toggleSeleccion
  toggleSeleccion(espectaculo: any, entrada: any) {
    const compraToken = this.getCompraToken();
    const index = this.entradasSeleccionadas.findIndex(e => e.id === entrada.id);
    const entradaConEspectaculo = {
      ...entrada,
      espectaculo: {
        id: espectaculo.id,
        artista: espectaculo.artista,
        fecha: espectaculo.fecha,
        nombre: espectaculo.nombre
      }
    };

    if (index !== -1) {
      // CASO A: La entrada YA está seleccionada -> LIBERAR
      this.http.post('http://localhost:8080/reservas/cancelar', {
        idEntrada: entrada.id,
        compraToken: compraToken
      }, { responseType: 'text' }).subscribe({
        next: () => {
          entrada.estado = 'DISPONIBLE'; // Cambiamos estado visual
          this.entradasSeleccionadas.splice(index, 1); // Quitamos del carrito
          this.saveCarrito(this.entradasSeleccionadas);
        },
        error: (err: any) => alert("Error al liberar la entrada: " + err.error)
      });

    } else {
      // CASO B: La entrada NO está seleccionada -> RESERVAR (Tu código actual)
      this.http.post('http://localhost:8080/reservas/seleccionar', {
        idEntrada: entrada.id,
        compraToken: compraToken
      }, { responseType: 'text' }).subscribe({
        next: (nuevoToken: any) => {
          this.saveCompraToken(nuevoToken);
          entrada.estado = 'PRERRESERVADA';
          this.entradasSeleccionadas.push(entradaConEspectaculo);
          this.saveCarrito(this.entradasSeleccionadas);
        },
        error: (err: any) => alert("No se pudo reservar: " + err.error)
      });
    }
  }

  estaSeleccionada(entrada: any): boolean {
    return this.entradasSeleccionadas.some(e => e.id === entrada.id);
  }

  getTotal(): number {
    return this.entradasSeleccionadas.reduce((acc, e) => acc + (e.precio / 100), 0);
  }

  irAComprar() {
    // Verificamos que el token exista antes de irnos
    const token = this.getCompraToken();
    if (!token || this.entradasSeleccionadas.length === 0) {
      alert("Selecciona al menos una entrada");
      return;
    }

    this.saveCarrito(this.entradasSeleccionadas);

    if (this.espectaculosService.getToken()) {
      this.router.navigate(['/comprar']);
    } else {
      // Al ir a auth, el token sigue en sessionStorage, así que estamos bien.
      this.router.navigate(['/auth']);
    }
  }
}