import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from './auth';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
/**
 * nombre_clase: App
 * funcion: componente raíz de la aplicación Angular
 * flujo_en_el_que_participa: renderizado de la interfaz principal
 * comunicacion: Auth service, routing
 */
export class App {
  isLoggedIn$: Observable<string | null>;
  isAdmin$: Observable<boolean>;

  constructor(public auth: Auth) {
    this.isLoggedIn$ = this.auth.token$;
    this.isAdmin$ = this.auth.token$.pipe(
      switchMap(token => token ? this.auth.getRole() : of('')),
      map(role => role === 'ADMIN'),
      catchError(() => of(false))
    );
  }

  /**
   * nombre_metodo: logout
   * parametros: ninguno
   * funcion: cierra la sesión del usuario
   * flujo_en_el_que_participa: logout
   */
  logout() {
    this.auth.logout();
  }
}