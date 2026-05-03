import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Auth } from '../auth';
import { Router } from '@angular/router';

type AuthMode = 'login' | 'register' | 'forgot-password' | 'reset-password';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class AuthComponent {
  // Campos comunes
  email = '';
  pwd = '';
  pwd2 = '';
  mensaje = '';
  isLoading = false;

  // Modo actual de autenticación
  authMode: AuthMode = 'login';

  // Control de visibilidad de contraseña
  showPassword = false;

  // Campos para recuperación de contraseña
  resetToken = '';
  nuevaPassword = '';
  confirmarPassword = '';

  constructor(private auth: Auth, private router: Router) { }

  // Cambiar modo de autenticación
  setMode(mode: AuthMode) {
    this.authMode = mode;
    this.limpiarFormulario();
    this.mensaje = '';
    this.showPassword = false; // Resetear el ojo al cambiar de modo
  }

  private limpiarFormulario() {
    this.email = '';
    this.pwd = '';
    this.pwd2 = '';
    this.resetToken = '';
    this.nuevaPassword = '';
    this.confirmarPassword = '';
  }

  // VALIDACIONES PRIVADAS
  private esEmailValido(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  private esPasswordRobusta(password: string): boolean {
    // He añadido el punto (.) y otros símbolos a la lista de permitidos
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&._-])[A-Za-z\d@$!%*?&._-]{8,}$/;
    return regex.test(password);
  }
  private redirigirPostLogin() {
    // Primero, intentar recuperar carritos pendientes del usuario
    this.auth.getCarritosUsuario().subscribe({
      next: (carritos) => {
        if (carritos && carritos.length > 0) {
          // Usar el carrito más reciente (primer elemento, ya ordenados por backend)
          const carritoActual = carritos[0];
          sessionStorage.setItem('compraToken', carritoActual.valor);
          console.log('Carrito sincronizado desde servidor:', carritoActual.valor);
          this.redirigirAlDestino();
        } else {
          // No hay carritos previos, ir a espectáculos
          this.redirigirAlDestino();
        }
      },
      error: (err) => {
        console.warn('No se pudieron recuperar carritos previos:', err);
        this.redirigirAlDestino();
      }
    });
  }

  private redirigirAlDestino() {
    const compraToken = sessionStorage.getItem('compraToken');
    if (compraToken && compraToken !== 'null' && compraToken !== 'undefined' && compraToken !== '') {
      this.router.navigate(['/comprar']);
    } else {
      this.router.navigate(['/espectaculos']);
    }
  }

  // LOGIN
  onLoginSubmit() {
    if (!this.email || !this.pwd) {
      this.mensaje = 'Error: Por favor completa todos los campos';
      return;
    }

    this.isLoading = true;
    this.auth.login(this.email, this.pwd).subscribe({
      next: (token) => {
        this.auth.saveToken(token, this.email);
        this.mensaje = '✓ ¡Bienvenido! Accediendo a su pedido...';
        setTimeout(() => this.redirigirPostLogin(), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        this.mensaje = 'Error: Email o contraseña incorrectos';
      }
    });
  }

  // REGISTER
  onRegisterSubmit() {
    if (!this.email || !this.pwd || !this.pwd2) {
      this.mensaje = 'Error: Por favor completa todos los campos';
      return;
    }

    if (!this.esEmailValido(this.email)) {
      this.mensaje = 'Error: El formato del email no es válido (ejemplo@dominio.com)';
      return;
    }

    if (!this.esPasswordRobusta(this.pwd)) {
      this.mensaje = 'Error: La contraseña debe tener al menos 8 caracteres, incluir un número y un símbolo (@$!%*?&)';
      return;
    }

    if (this.pwd !== this.pwd2) {
      this.mensaje = 'Error: Las contraseñas no coinciden';
      return;
    }

    this.isLoading = true;
    this.auth.registrar(this.email, this.pwd, this.pwd2).subscribe({
      next: (token) => {
        this.auth.saveToken(token, this.email);
        this.mensaje = '✓ Cuenta creada con éxito. Redirigiendo...';
        setTimeout(() => this.redirigirPostLogin(), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 409) {
          this.mensaje = 'Error: El usuario ya existe';
        } else if (err.status === 422) {
          this.mensaje = 'Error: ' + (err.error.message || 'La contraseña no cumple los requisitos del servidor');
        } else {
          this.mensaje = 'Error en el registro';
        }
      }
    });
  }

  // FORGOT PASSWORD
  onForgotPasswordSubmit() {
    if (!this.email) {
      this.mensaje = 'Error: Por favor ingresa tu email';
      return;
    }

    if (!this.esEmailValido(this.email)) {
      this.mensaje = 'Error: Ingresa un email con formato correcto';
      return;
    }

    this.isLoading = true;
    this.auth.forgotPassword(this.email).subscribe({
      next: () => {
        this.isLoading = false;
        this.mensaje = '✓ Te enviamos un email con el token de recuperación. Revisa tu bandeja de entrada.';
        this.email = '';

        // Mostrar opción para ingresar el token en 3 segundos
        setTimeout(() => {
          this.setMode('reset-password');
        }, 3000);
      },
      error: (err) => {
        this.isLoading = false;
        this.mensaje = 'Error: No pudimos procesar tu solicitud';
      }
    });
  }

  // RESET PASSWORD
  onResetPasswordSubmit() {
    if (!this.resetToken || !this.nuevaPassword || !this.confirmarPassword) {
      this.mensaje = 'Error: Por favor completa todos los campos';
      return;
    }

    if (this.nuevaPassword !== this.confirmarPassword) {
      this.mensaje = 'Error: Las contraseñas nuevas no coinciden';
      return;
    }

    if (!this.esPasswordRobusta(this.nuevaPassword)) {
      this.mensaje = 'Error: La nueva contraseña debe tener al menos 8 caracteres, un número y un símbolo';
      return;
    }

    this.isLoading = true;
    this.auth.resetPassword(this.resetToken, this.nuevaPassword).subscribe({
      next: () => {
        this.isLoading = false;
        this.mensaje = '✓ Contraseña actualizada correctamente. Redirigiendo a login...';
        setTimeout(() => {
          this.setMode('login');
          this.mensaje = 'Ahora puedes iniciar sesión con tu nueva contraseña';
        }, 2000);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 410) {
          this.mensaje = 'Error: El token de recuperación ha expirado. Solicita uno nuevo.';
        } else {
          this.mensaje = 'Error: El token es inválido o no pudimos actualizar tu contraseña';
        }
      }
    });
  }

  onSubmit() {
    switch (this.authMode) {
      case 'login':
        this.onLoginSubmit();
        break;
      case 'register':
        this.onRegisterSubmit();
        break;
      case 'forgot-password':
        this.onForgotPasswordSubmit();
        break;
      case 'reset-password':
        this.onResetPasswordSubmit();
        break;
    }
  }
}