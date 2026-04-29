import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private url = 'http://localhost:8081/users';

  constructor(private http: HttpClient, private router: Router) { }

  login(email: string, pwd: string): Observable<string> {
    return this.http.post(`${this.url}/login`, { email, pwd }, { responseType: 'text' });
  }

  registrar(email: string, pwd1: string, pwd2: string): Observable<string> {
    return this.http.post(`${this.url}/register`, { email, pwd1, pwd2 }, { responseType: 'text' });
  }

  saveToken(token: string) {
    localStorage.setItem('userToken', token);
  }

  // MÉTODO CORREGIDO: Validación estricta de identidad
  getToken(): string | null {
    const token = localStorage.getItem('userToken');
    if (!token || token === 'null' || token === 'undefined' || token === '') {
      return null;
    }
    return token;
  }

  logout() {
    localStorage.removeItem('userToken');
    localStorage.removeItem('compraToken');
    sessionStorage.removeItem('compraToken');
    localStorage.removeItem('carrito');
    this.router.navigate(['/auth']);
  }
}