import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EspectaculosService } from '../espectaculos';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Auth } from '../auth';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-espectaculos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './espectaculos.html',
  styleUrl: './espectaculos.css',
})
export class Espectaculos implements OnInit {

  escenarios: any[] = [];
  escenariosFiltrados: any[] = [];
  entradasSeleccionadas: any[] = [];
  // Búsqueda
  searchQuery: string = '';
  searchDate: string = '';
  private allEspectaculosCache: any[] = [];
  tiempoRestanteSegundos: number | null = null;
  fechaExpiracionReserva: number | null = null;
  private contadorReservaId: ReturnType<typeof setInterval> | null = null;

  constructor(
    private espectaculosService: EspectaculosService,
    private auth: Auth,
    private router: Router,
    private http: HttpClient,
    private cd: ChangeDetectorRef
  ) { }

  ngOnDestroy() {
    this.detenerContadorReserva();
  }

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

  private construirUrlResumen(compraToken: string, userToken: string | null) {
    let url = `http://localhost:8080/reservas/resumen?compraToken=${compraToken}`;
    if (userToken) url += `&userToken=${userToken}`;
    return url;
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
    this.entradasSeleccionadas = this.getCarrito();
    this.loadEscenariosConEspectaculos();

    const compraToken = this.getCompraToken();
    const userToken = this.auth.getToken();

    if (!compraToken) return;

    this.http.get<any>(this.construirUrlResumen(compraToken, userToken)).subscribe({
      next: (res) => {
        const entradasBackend = res.entradas || [];
        this.configurarContadorReserva(res);

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
        this.detenerContadorReserva();
      }
    });

    this.loadAllEspectaculos().subscribe({ next: () => { }, error: () => { } });
  }

  onSearch() {
    const q = this.searchQuery?.trim().toLowerCase() || '';
    const fecha = this.searchDate || '';

    this.escenariosFiltrados = this.escenarios
      .map(escenario => ({
        ...escenario,
        espectaculos: (escenario.espectaculos || []).filter((espectaculo: any) =>
          this.matchesSearch(espectaculo, escenario, q, fecha)
        )
      }))
      .filter(escenario => !q && !fecha ? true : escenario.espectaculos.length > 0);
  }

  private loadEscenariosConEspectaculos() {
    this.espectaculosService.getEscenarios().pipe(
      switchMap((escenarios: any[]) => {
        if (!escenarios || escenarios.length === 0) {
          this.escenarios = [];
          this.escenariosFiltrados = [];
          return of([]);
        }

        const calls = escenarios.map(escenario =>
          this.espectaculosService.getEspectaculos(escenario).pipe(
            catchError(() => of([]))
          )
        );

        return forkJoin(calls).pipe(
          switchMap((listas: any[]) => {
            this.escenarios = escenarios.map((escenario, index) => ({
              ...escenario,
              espectaculos: listas[index] || []
            }));
            this.escenariosFiltrados = [...this.escenarios];
            return of(this.escenarios);
          })
        );
      })
    ).subscribe({
      error: (err) => {
        console.error('Error cargando escenarios', err);
        this.escenarios = [];
        this.escenariosFiltrados = [];
      }
    });
  }

  // Build a cached list of all espectaculos across escenarios
  private loadAllEspectaculos() {
    if (this.allEspectaculosCache.length > 0) return of(this.allEspectaculosCache);
    return this.espectaculosService.getEscenarios().pipe(
      switchMap((escenarios: any[]) => {
        if (!escenarios || escenarios.length === 0) return of([]);
        const calls = escenarios.map(es => this.espectaculosService.getEspectaculos(es).pipe(catchError(() => of([]))));
        return forkJoin(calls).pipe(
          switchMap((lists: any[]) => {
            const merged = [] as any[];
            lists.forEach((l, idx) => {
              const escenario = escenarios[idx];
              (l || []).forEach((esp: any) => merged.push({ ...esp, escenario: escenario.nombre }));
            });
            this.allEspectaculosCache = merged;
            return of(merged);
          })
        );
      })
    );
  }

  private matchesSearch(espectaculo: any, escenario: any, q: string, fecha: string): boolean {
    const text = [espectaculo.artista, espectaculo.nombre, escenario?.nombre]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
    const textMatch = !q || text.includes(q);

    if (!fecha) {
      return textMatch;
    }

    const selectedDate = new Date(fecha).toDateString();
    const espectaculoDate = new Date(espectaculo.fecha).toDateString();
    return textMatch && selectedDate === espectaculoDate;
  }

  clearSearch() {
    this.searchQuery = '';
    this.searchDate = '';
    this.escenariosFiltrados = [...this.escenarios];
  }

  getEscenarios() {
    if (this.escenarios.length === 0) {
      this.loadEscenariosConEspectaculos();
    }
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
          this.configurarContadorReserva({ horaExpiracion: Date.now() + (10 * 60 * 1000), tiempoRestanteSegundos: 600 });
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

  volverAEspectaculos() {
    this.detenerContadorReserva();
    this.router.navigate(['/espectaculos']);
  }
}