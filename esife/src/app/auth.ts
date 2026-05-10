import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';

export interface LoginStatusResponse {
  email: string;
  blocked: boolean;
  attempts: number;
  blockedUntil: string | null;
  retryAfterSeconds: number;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
/**
 * nombre_clase: Auth
 * funcion: servicio de autenticación para login, registro y gestión de tokens
 * flujo_en_el_que_participa: autenticación de usuarios en el frontend
 * comunicacion: backend de usuarios
 */
export class Auth {
  private usersUrl = 'http://localhost:8081/users';
  private reservasUrl = 'http://localhost:8080/reservas';

  // BehaviorSubject para que Angular observe los cambios de token
  private tokenSubject = new BehaviorSubject<string | null>(this.getTokenFromStorage());
  public token$ = this.tokenSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) { }

  login(email: string, pwd: string): Observable<string> {
    return this.http.post(`${this.usersUrl}/login`, { email, pwd }, { responseType: 'text' });
  }

  registrar(email: string, pwd1: string, pwd2: string): Observable<string> {
    return this.http.post(`${this.usersUrl}/register`, { email, pwd1, pwd2 }, { responseType: 'text' });
  }

  // Recuperación de contraseña: solicitar token
  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.usersUrl}/forgot-password`, { email });
  }

  getLoginStatus(email: string): Observable<LoginStatusResponse> {
    return this.http.get<LoginStatusResponse>(`${this.usersUrl}/login-status?email=${encodeURIComponent(email)}`);
  }

  // Recuperación de contraseña: resetear con token
  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.usersUrl}/reset-password`, { token, pwd: newPassword });
  }

  saveToken(token: string, email: string = '') {
    sessionStorage.setItem('userToken', token);
    if (email) {
      sessionStorage.setItem('userEmail', email);
    }
    this.tokenSubject.next(token); // Notificar a los observadores
  }

  private getTokenFromStorage(): string | null {
    const token = sessionStorage.getItem('userToken');
    if (!token || token === 'null' || token === 'undefined' || token === '') {
      return null;
    }
    return token;
  }

  // MÉTODO CORREGIDO: Validación estricta de identidad
  getToken(): string | null {
    return this.getTokenFromStorage();
  }

  // Recuperar carritos pendientes del usuario
  getCarritosUsuario(): Observable<any[]> {
    const userToken = this.getToken();
    if (!userToken) {
      return new Observable(observer => observer.error('No user token'));
    }
    return this.http.get<any[]>(`${this.reservasUrl}/carritos-usuario?userToken=${userToken}`);
  }

  getRole(): Observable<string> {
    const token = this.getToken();
    if (!token) {
      return of('');
    }

    return this.http.post<{ role: string }>(`${this.usersUrl}/get-role`, { token })
      .pipe(map((response) => response?.role || ''));
  }

  logout() {
    const email = sessionStorage.getItem('userEmail');
    const token = this.getToken();

    // Si tenemos email y token, notificamos al backend para invalidar
    if (email && token) {
      this.http.post(`${this.usersUrl}/logout`, { email, token }).subscribe({
        next: () => {
          console.log('Logout realizado en el servidor');
          this.limpiarLocal();
        },
        error: (err) => {
          console.error('Error al hacer logout en servidor:', err);
          // Incluso si falla, limpiamos localmente
          this.limpiarLocal();
        }
      });
    } else {
      this.limpiarLocal();
    }
  }

  private limpiarLocal() {
    sessionStorage.removeItem('userToken');
    sessionStorage.removeItem('userEmail');
    sessionStorage.removeItem('compraToken');
    sessionStorage.removeItem('carrito');
    //sessionStorage.removeItem('compraToken');
    //sessionStorage.removeItem('carrito');

    // Actualizar el BehaviorSubject para notificar a los observadores
    this.tokenSubject.next(null);

    this.router.navigate(['/auth']);
  }
}