import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Auth } from '../auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class AuthComponent {
  email = '';
  pwd = '';
  pwd2 = '';
  isLoginMode = true;
  mensaje = '';

  constructor(private auth: Auth, private router: Router) { }

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.mensaje = '';
  }

  private redirigirPostLogin() {
    // Miramos si el usuario dejó un carrito a medias
    const compraToken = sessionStorage.getItem('compraToken');

    if (compraToken && compraToken !== 'null' && compraToken !== 'undefined' && compraToken !== '') {
      // Si hay entradas reservadas, le mandamos directo a la caja[cite: 2]
      this.router.navigate(['/comprar']);
    } else {
      // Si no, a la pantalla principal
      this.router.navigate(['/espectaculos']);
    }
  }

  onSubmit() {
    if (this.isLoginMode) {
      this.auth.login(this.email, this.pwd).subscribe({
        next: (token) => {
          this.auth.saveToken(token);
          this.mensaje = '¡Bienvenido! Accediendo a su pedido...';

          // Retardo para que el usuario vea el mensaje de bienvenida
          setTimeout(() => this.redirigirPostLogin(), 1500);
        },
        error: () => this.mensaje = 'Error: Email o contraseña incorrectos'
      });
    } else {
      this.auth.registrar(this.email, this.pwd, this.pwd2).subscribe({
        next: (token) => {
          this.auth.saveToken(token);
          this.mensaje = 'Cuenta creada con éxito. Redirigiendo...';

          setTimeout(() => this.redirigirPostLogin(), 1500);
        },
        error: (err) => {
          this.mensaje = err.status === 409 ? 'El usuario ya existe' : 'Error en el registro';
        }
      });
    }
  }
}