import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Auth } from '../auth'; // Importación corregida
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './auth.html', // Nombre corregido a 'auth.html'
  styleUrl: './auth.css'      // Nombre corregido a 'auth.css'
})
export class AuthComponent {
  email = '';
  pwd = '';
  pwd2 = '';
  isLoginMode = true;
  mensaje = '';

  constructor(private auth: Auth, private router: Router) { } // Inyección corregida

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.mensaje = '';
  }

  onSubmit() {
    if (this.isLoginMode) {
      this.auth.login(this.email, this.pwd).subscribe({
        next: (token) => {
          this.auth.saveToken(token);
          this.mensaje = '¡Bienvenido! Redirigiendo...';
          setTimeout(() => this.router.navigate(['/espectaculos']), 1500);
        },
        error: () => this.mensaje = 'Error: Email o contraseña incorrectos'
      });
    } else {
      this.auth.registrar(this.email, this.pwd, this.pwd2).subscribe({
        next: (token) => {
          this.auth.saveToken(token);
          this.mensaje = 'Cuenta creada con éxito. Redirigiendo...';
          setTimeout(() => this.router.navigate(['/espectaculos']), 1500);
        },
        error: (err) => {
          this.mensaje = err.status === 409 ? 'El usuario ya existe' : 'Error en el registro';
        }
      });
    }
  }
}