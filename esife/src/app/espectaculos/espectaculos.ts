import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { EspectaculosService } from '../espectaculos';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Auth } from '../auth';

@Component({
  selector: 'app-espectaculos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './espectaculos.html',
  styleUrl: './espectaculos.css',
})
export class Espectaculos implements OnInit {

  escenarios: any[] = [];
  entradasSeleccionadas: any[] = [];

  constructor(
    private espectaculosService: EspectaculosService,
    private auth: Auth,
    private router: Router,
    private http: HttpClient
  ) { }

  private getCompraToken(): string | null {
    const t = sessionStorage.getItem('compraToken');
    return (t === 'null' || t === 'undefined' || t === '') ? null : t;
  }

  private saveCompraToken(token: string) {
    sessionStorage.setItem('compraToken', token);
  }

  private getCarrito(): any[] {
    const carritoGuardado = sessionStorage.getItem('carrito');
    return carritoGuardado ? JSON.parse(carritoGuardado) : [];
  }

  private saveCarrito(carrito: any[]) {
    sessionStorage.setItem('carrito', JSON.stringify(carrito));
  }

  ngOnInit() {
    this.entradasSeleccionadas = this.getCarrito();

    const compraToken = this.getCompraToken();
    const userToken = this.auth.getToken();

    if (!compraToken) return;

    let url = `http://localhost:8080/reservas/resumen?compraToken=${compraToken}`;
    if (userToken) url += `&userToken=${userToken}`;

    this.http.get<any>(url).subscribe({
      next: (res) => {
        const entradasBackend = res.entradas || [];

        this.entradasSeleccionadas = entradasBackend.map((entrada: any) => {
          const entradaGuardada = this.entradasSeleccionadas.find(e => e.id === entrada.id);
          return {
            ...entrada,
            espectaculo: entrada.espectaculo || entradaGuardada?.espectaculo
          };
        });

        this.saveCarrito(this.entradasSeleccionadas);
      },
      error: () => {
        this.saveCompraToken('');
        this.saveCarrito([]);
      }
    });
  }

  getEscenarios() {
    if (this.escenarios.length > 0) {
      this.escenarios = [];
      return;
    }

    // 🔧 FIX línea 99
    this.espectaculosService.getEscenarios()
      .subscribe((res: any) => this.escenarios = res);
  }

  getEspectaculos(escenario: any) {
    if (escenario.espectaculos?.length) {
      escenario.espectaculos = [];
      return;
    }

    this.espectaculosService.getEspectaculos(escenario)
      .subscribe((res: any) => escenario.espectaculos = res);
  }

  getNumeroDeEntradas(espectaculo: any) {
    if (espectaculo.entradasAgrupadas) {
      espectaculo.entradasAgrupadas = null;
      espectaculo.resumen = null;
      return;
    }

    this.espectaculosService.getNumeroDeEntradasComoDto(espectaculo)
      .subscribe((res: any) => espectaculo.resumen = res);

    this.espectaculosService.getNumeroDeEntradas(espectaculo).subscribe((entradas: any[]) => {
      espectaculo.entradasAgrupadas = entradas.reduce((acc: any, entrada: any) => {
        const grupo = entrada.zona ? `Zona: ${entrada.zona}` : 'Asientos Numerados';

        if (!acc[grupo]) acc[grupo] = [];
        acc[grupo].push(entrada);
        return acc;
      }, {});
    });
  }

  toggleSeleccion(espectaculo: any, entrada: any) {
    const compraToken = this.getCompraToken();
    const userToken = this.auth.getToken();

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
      this.http.post('http://localhost:8080/reservas/cancelar', {
        idEntrada: entrada.id.toString(),
        compraToken,
        userToken
      }).subscribe(() => {
        entrada.estado = 'DISPONIBLE';
        this.entradasSeleccionadas.splice(index, 1);
        this.saveCarrito(this.entradasSeleccionadas);
      });
    } else {
      this.http.post('http://localhost:8080/reservas/seleccionar', {
        idEntrada: entrada.id.toString(),
        compraToken,
        userToken
      }, { responseType: 'text' }).subscribe({
        next: (nuevoToken: string) => {
          this.saveCompraToken(nuevoToken);
          entrada.estado = 'PRERRESERVADA';
          this.entradasSeleccionadas.push(entradaConEspectaculo);
          this.saveCarrito(this.entradasSeleccionadas);
        },
        error: (err) =>
          alert("No se pudo reservar: " + (err.error || "No disponible"))
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
    const token = this.getCompraToken();

    if (!token || this.entradasSeleccionadas.length === 0) {
      alert("Selecciona al menos una entrada");
      return;
    }

    this.saveCarrito(this.entradasSeleccionadas);

    const usuarioIdentificado = this.auth.getToken();

    this.router.navigate([usuarioIdentificado ? '/comprar' : '/auth']);
  }
}