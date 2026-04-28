import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Auth { // Nombre de clase corregido a 'Auth'
  private url = 'http://localhost:8081/users';

  constructor(private http: HttpClient) { }

  login(email: string, pwd: string): Observable<string> {
    return this.http.post(`${this.url}/login`, { email, pwd }, { responseType: 'text' });
  }

  registrar(email: string, pwd1: string, pwd2: string): Observable<string> {
    return this.http.post(`${this.url}/register`, { email, pwd1, pwd2 }, { responseType: 'text' });
  }

  saveToken(token: string) {
    localStorage.setItem('userToken', token);
  }

  getToken() {
    return localStorage.getItem('userToken');
  }

  logout() {
    localStorage.removeItem('userToken');
  }
}