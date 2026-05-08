import { Component, OnDestroy, OnInit } from '@angular/core';
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
export class AuthComponent implements OnInit, OnDestroy {
  // Campos comunes
  email = '';
  pwd = '';
  pwd2 = '';
  mensaje = '';
  isLoading = false;
  isAccountBlocked = false;
  blockedMessage = '';
  blockedEmail: string | null = null;
  blockedUntil: string | null = null;
  retryAfterSeconds = 0;

  // Modo actual de autenticación
  authMode: AuthMode = 'login';

  // Control de visibilidad de contraseña
  showPassword = false;

  // Campos para recuperación de contraseña
  resetToken = '';
  nuevaPassword = '';
  confirmarPassword = '';

  private emailStatusTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly blockStateStorageKey = 'auth-login-block-state';

  constructor(private auth: Auth, private router: Router) { }

  ngOnInit() {
    this.restoreBlockState();
  }

  ngOnDestroy() {
    if (this.emailStatusTimer) {
      clearTimeout(this.emailStatusTimer);
      this.emailStatusTimer = null;
    }
  }

  // Cambiar modo de autenticación
  setMode(mode: AuthMode) {
    this.authMode = mode;
    this.limpiarFormulario(true);
    this.mensaje = '';
    this.showPassword = false; // Resetear el ojo al cambiar de modo

    if (mode === 'login' || mode === 'forgot-password') {
      this.scheduleBlockStatusCheck();
    }
  }

  private limpiarFormulario(preserveEmail = false) {
    if (!preserveEmail) {
      this.email = '';
    }
    this.pwd = '';
    this.pwd2 = '';
    this.resetToken = '';
    this.nuevaPassword = '';
    this.confirmarPassword = '';
  }

  private restoreBlockState() {
    const rawState = sessionStorage.getItem(this.blockStateStorageKey);
    if (!rawState) {
      return;
    }

    try {
      const state = JSON.parse(rawState) as { email?: string; blockedUntil?: string | null };
      if (!state.email || !state.blockedUntil) {
        this.clearStoredBlockState();
        return;
      }

      const remaining = Math.ceil((new Date(state.blockedUntil).getTime() - Date.now()) / 1000);
      if (!Number.isFinite(remaining) || remaining <= 0) {
        this.clearStoredBlockState();
        return;
      }

      this.email = state.email;
      this.isAccountBlocked = true;
      this.blockedEmail = state.email;
      this.blockedUntil = state.blockedUntil;
      this.retryAfterSeconds = remaining;
      this.blockedMessage = this.buildBlockedMessage();
      this.mensaje = this.blockedMessage;
      this.startCountdown();
    } catch {
      this.clearStoredBlockState();
    }
  }

  private persistBlockState() {
    if (!this.blockedEmail || !this.blockedUntil) {
      this.clearStoredBlockState();
      return;
    }

    sessionStorage.setItem(
      this.blockStateStorageKey,
      JSON.stringify({ email: this.blockedEmail, blockedUntil: this.blockedUntil })
    );
  }

  private clearStoredBlockState() {
    sessionStorage.removeItem(this.blockStateStorageKey);
  }

  onEmailChange(value: string) {
    this.email = value;

    if (this.emailStatusTimer) {
      clearTimeout(this.emailStatusTimer);
    }

    if (this.authMode === 'login' || this.authMode === 'forgot-password') {
      if (this.esEmailValido(value)) {
        this.emailStatusTimer = setTimeout(() => this.refreshBlockStatus(), 250);
      }
    }
  }

  // VALIDACIONES PRIVADAS
  private esEmailValido(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  // AuthComponent.ts
  private esPasswordRobusta(password: string): boolean {
    // Debe coincidir exactamente con el Backend para evitar confusiones
    const regex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\S+$).{8,}$/;
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

  private scheduleBlockStatusCheck() {
    if (!this.email || !this.esEmailValido(this.email)) {
      this.clearBlockState();
      return;
    }

    this.refreshBlockStatus();
  }

  private refreshBlockStatus() {
    if (!this.email || !this.esEmailValido(this.email)) {
      this.clearBlockState();
      return;
    }

    this.auth.getLoginStatus(this.email).subscribe({
      next: (status) => this.applyBlockStatus(status),
      error: () => this.clearBlockState()
    });
  }

  private applyBlockStatus(status: { blocked: boolean; blockedUntil?: string | null; retryAfterSeconds?: number; }) {
    this.isAccountBlocked = Boolean(status.blocked);
    this.blockedEmail = this.email;
    this.blockedUntil = status.blockedUntil ?? null;
    this.retryAfterSeconds = status.retryAfterSeconds ?? 0;

    if (this.isAccountBlocked) {
      this.blockedMessage = this.buildBlockedMessage();
      this.mensaje = this.blockedMessage;
      this.persistBlockState();
      this.startCountdown();
      return;
    }

    this.blockedMessage = '';
    this.blockedEmail = null;
    this.blockedUntil = null;
    this.retryAfterSeconds = 0;
    this.clearStoredBlockState();
  }

  private clearBlockState() {
    this.isAccountBlocked = false;
    this.blockedMessage = '';
    this.blockedEmail = null;
    this.blockedUntil = null;
    this.retryAfterSeconds = 0;
    this.clearStoredBlockState();

    if (this.emailStatusTimer) {
      clearTimeout(this.emailStatusTimer);
      this.emailStatusTimer = null;
    }
  }

  private buildBlockedMessage() {
    if (this.retryAfterSeconds > 0) {
      return `Cuenta bloqueada temporalmente. Intenta de nuevo en ${this.retryAfterSeconds} segundos.`;
    }

    return 'Cuenta bloqueada temporalmente. Intenta de nuevo más tarde.';
  }

  private normalizeBlockError(err: any) {
    const errorBody = err?.error;

    if (errorBody && typeof errorBody === 'object') {
      return {
        blocked: Boolean(errorBody.blocked ?? true),
        blockedUntil: errorBody.blockedUntil ?? null,
        retryAfterSeconds: Number(errorBody.retryAfterSeconds ?? 0),
      };
    }

    const retryAfterHeader = Number(err?.headers?.get?.('Retry-After') ?? 0);

    return {
      blocked: true,
      blockedUntil: null,
      retryAfterSeconds: Number.isFinite(retryAfterHeader) ? retryAfterHeader : 0,
    };
  }

  private startCountdown() {
    if (!this.blockedUntil) {
      return;
    }

    if (this.emailStatusTimer) {
      clearTimeout(this.emailStatusTimer);
    }

    const tick = () => {
      if (!this.blockedUntil) {
        this.clearBlockState();
        return;
      }

      const remaining = Math.max(0, Math.ceil((new Date(this.blockedUntil).getTime() - Date.now()) / 1000));
      this.retryAfterSeconds = remaining;

      if (remaining <= 0) {
        this.clearBlockState();
        if (this.authMode === 'login' || this.authMode === 'forgot-password') {
          this.refreshBlockStatus();
        }
        return;
      }

      this.blockedMessage = this.buildBlockedMessage();
      this.mensaje = this.blockedMessage;
      this.persistBlockState();
      this.emailStatusTimer = setTimeout(tick, 1000);
    };

    this.emailStatusTimer = setTimeout(tick, 1000);
  }

  // LOGIN
  onLoginSubmit() {
    if (this.isAccountBlocked) {
      this.mensaje = this.blockedMessage || 'Cuenta bloqueada temporalmente';
      return;
    }

    if (!this.email || !this.pwd) {
      this.mensaje = 'Error: Por favor completa todos los campos';
      return;
    }

    this.isLoading = true;
    this.auth.login(this.email, this.pwd).subscribe({
      next: (token) => {
        this.clearBlockState();
        this.auth.saveToken(token, this.email);
        this.mensaje = '✓ ¡Bienvenido! Accediendo a su pedido...';
        setTimeout(() => this.redirigirPostLogin(), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 429) {
          this.applyBlockStatus(this.normalizeBlockError(err));
          this.mensaje = this.blockedMessage || 'Error: Cuenta bloqueada temporalmente';
          return;
        }

        this.clearBlockState();
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
      this.mensaje = 'Error: La contraseña debe tener al menos 8 caracteres, una mayúsculua, incluir un número y un símbolo (@$!%*?&)';
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
    if (this.isAccountBlocked) {
      this.mensaje = this.blockedMessage || 'Cuenta bloqueada temporalmente';
      return;
    }

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
        if (err.status === 429) {
          this.applyBlockStatus(this.normalizeBlockError(err));
          this.mensaje = this.blockedMessage || 'Error: Demasiadas solicitudes';
          return;
        }

        this.clearBlockState();
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