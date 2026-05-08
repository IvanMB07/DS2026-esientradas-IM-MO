import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Auth } from '../auth';

export interface UserProfile {
  email: string;
  name: string;
  role: string;
}

@Component({
  selector: 'app-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  isLoading = true;
  mensaje = '';
  tipoMensaje: 'success' | 'error' | 'info' = 'info';

  // Modal de cambiar contraseña
  showChangePasswordModal = false;
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  isChangingPassword = false;
  showPassword = false;

  // Modal de borrar cuenta
  showDeleteAccountModal = false;
  deleteAccountPassword = '';
  isDeleting = false;
  deleteConfirmation = '';

  private usersUrl = 'http://localhost:8081/users';

  constructor(
    private http: HttpClient,
    private auth: Auth,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    const email = sessionStorage.getItem('userEmail');
    const token = this.auth.getToken();

    if (!email || !token) {
      this.mostrarMensaje('No autenticado', 'error');
      this.router.navigate(['/auth']);
      return;
    }

    this.http.post<UserProfile>(
      `${this.usersUrl}/profile`,
      { email, token }
    ).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar perfil:', err);
        this.mostrarMensaje('Error al cargar el perfil', 'error');
        this.isLoading = false;
      }
    });
  }

  abrirChangePasswordModal(): void {
    this.showChangePasswordModal = true;
    this.limpiarChangePassword();
  }

  cerrarChangePasswordModal(): void {
    this.showChangePasswordModal = false;
    this.limpiarChangePassword();
  }

  private limpiarChangePassword(): void {
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  // Copia de la validación robusta usada en AuthComponent
  private esPasswordRobusta(password: string): boolean {
    const regex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\S+$).{8,}$/;
    return regex.test(password);
  }

  cambiarContrasena(): void {
    // Validaciones de frontend alineadas con el flujo de registro/reset
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.mostrarMensaje('Por favor, completa los campos de contraseña', 'error');
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.mostrarMensaje('Error: Las contraseñas nuevas no coinciden', 'error');
      return;
    }

    if (!this.esPasswordRobusta(this.newPassword)) {
      this.mostrarMensaje('Error: La contraseña debe tener al menos 8 caracteres, una mayúscula, incluir un número y un símbolo (@#$%^&+=!._-)', 'error');
      return;
    }

    this.isChangingPassword = true;
    const email = sessionStorage.getItem('userEmail');
    const token = this.auth.getToken();

    this.http.post(
      `${this.usersUrl}/profile/change-password`,
      {
        email,
        token,
        currentPwd: this.currentPassword,
        newPwd: this.newPassword
      }
    ).subscribe({
      next: (res: any) => {
        const backendMsg = res?.mensaje || res?.message || 'Contraseña cambiada exitosamente';
        this.mostrarMensaje(backendMsg, 'success');
        this.cerrarChangePasswordModal();
        this.isChangingPassword = false;
      },
      error: (err) => {
        console.error('Error al cambiar contraseña:', err);
        const backendMsg = err?.error?.mensaje || err?.error?.message || (typeof err?.error === 'string' ? err.error : null) || 'Error al cambiar la contraseña';
        this.mostrarMensaje(backendMsg, 'error');
        this.isChangingPassword = false;
      }
    });
  }

  abrirDeleteAccountModal(): void {
    this.showDeleteAccountModal = true;
    this.limpiarDeleteAccount();
  }

  cerrarDeleteAccountModal(): void {
    this.showDeleteAccountModal = false;
    this.limpiarDeleteAccount();
  }

  private limpiarDeleteAccount(): void {
    this.deleteAccountPassword = '';
    this.deleteConfirmation = '';
  }

  borrarCuenta(): void {
    if (!this.deleteAccountPassword) {
      this.mostrarMensaje('Por favor, ingresa tu contraseña', 'error');
      return;
    }

    if (this.deleteConfirmation !== 'BORRAR') {
      this.mostrarMensaje('Por favor, escribe BORRAR para confirmar', 'error');
      return;
    }

    this.isDeleting = true;
    const email = sessionStorage.getItem('userEmail');
    const token = this.auth.getToken();

    this.http.post(
      `${this.usersUrl}/profile/delete-account`,
      { email, token }
    ).subscribe({
      next: () => {
        this.mostrarMensaje('Cuenta eliminada. Redirigiendo...', 'success');
        setTimeout(() => {
          this.auth.logout();
        }, 2000);
        this.isDeleting = false;
      },
      error: (err) => {
        console.error('Error al borrar cuenta:', err);
        this.mostrarMensaje(err.error?.mensaje || 'Error al borrar la cuenta', 'error');
        this.isDeleting = false;
      }
    });
  }

  logout(): void {
    this.auth.logout();
  }

  private mostrarMensaje(msg: string, tipo: 'success' | 'error' | 'info' = 'info'): void {
    this.mensaje = msg;
    this.tipoMensaje = tipo;
    setTimeout(() => {
      this.mensaje = '';
    }, 5000);
  }
}
