import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Auth } from '../auth';

export interface AdminUser {
  email: string;
  name: string;
  role: string;
}

@Component({
  selector: 'app-admin',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  usuarios: AdminUser[] = [];
  isLoading = true;
  mensaje = '';
  tipoMensaje: 'success' | 'error' | 'info' = 'info';
  currentEmail = '';

  // Modal de cambiar rol
  showChangeRoleModal = false;
  selectedUser: AdminUser | null = null;
  newRole = '';
  isChangingRole = false;

  // Modal de eliminar
  showDeleteModal = false;
  userToDelete: AdminUser | null = null;
  isDeleting = false;

  private usersUrl = 'http://localhost:8081/users';

  constructor(
    private http: HttpClient,
    private auth: Auth,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.checkAdminAccess();
  }

  checkAdminAccess(): void {
    const email = sessionStorage.getItem('userEmail');
    const token = this.auth.getToken();

    if (!email || !token) {
      this.router.navigate(['/auth']);
      return;
    }

    this.currentEmail = email;

    this.http.post<{ email: string; role: string }>(
      `${this.usersUrl}/get-role`,
      { token }
    ).subscribe({
      next: (data) => {
        if (data.role !== 'ADMIN') {
          this.router.navigate(['/']);
          return;
        }
        this.loadUsuarios();
      },
      error: () => {
        this.router.navigate(['/auth']);
      }
    });
  }

  loadUsuarios(): void {
    this.isLoading = true;
    const email = sessionStorage.getItem('userEmail');
    const token = this.auth.getToken();

    this.http.post<AdminUser[]>(
      `${this.usersUrl}/admin/users`,
      { adminEmail: email, adminToken: token }
    ).subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
        this.mostrarMensaje('Error al cargar la lista de usuarios', 'error');
        this.isLoading = false;
      }
    });
  }

  abrirChangeRoleModal(usuario: AdminUser): void {
    this.selectedUser = usuario;
    this.newRole = usuario.role;
    this.showChangeRoleModal = true;
  }

  cerrarChangeRoleModal(): void {
    this.showChangeRoleModal = false;
    this.selectedUser = null;
    this.newRole = '';
  }

  cambiarRol(): void {
    if (!this.selectedUser || !this.newRole) {
      this.mostrarMensaje('Por favor, selecciona un rol', 'error');
      return;
    }

    if (this.newRole === this.selectedUser.role) {
      this.mostrarMensaje('El nuevo rol es igual al actual', 'error');
      return;
    }

    this.isChangingRole = true;
    const email = sessionStorage.getItem('userEmail');
    const token = this.auth.getToken();

    this.http.post(
      `${this.usersUrl}/change-role`,
      {
        adminEmail: email,
        adminToken: token,
        targetEmail: this.selectedUser.email,
        newRole: this.newRole
      }
    ).subscribe({
      next: () => {
        this.mostrarMensaje(`Rol de ${this.selectedUser?.email} cambiado a ${this.newRole}`, 'success');
        this.loadUsuarios();
        this.cerrarChangeRoleModal();
        this.isChangingRole = false;
      },
      error: (err) => {
        console.error('Error al cambiar rol:', err);
        this.mostrarMensaje('Error al cambiar el rol', 'error');
        this.isChangingRole = false;
      }
    });
  }

  abrirDeleteModal(usuario: AdminUser): void {
    if (!this.canDeleteUser(usuario)) {
      this.mostrarMensaje('No puedes eliminar tu propia cuenta ni la de otro administrador.', 'error');
      return;
    }

    this.userToDelete = usuario;
    this.showDeleteModal = true;
  }

  cerrarDeleteModal(): void {
    this.showDeleteModal = false;
    this.userToDelete = null;
  }

  eliminarUsuario(): void {
    if (!this.userToDelete) return;

    if (!this.canDeleteUser(this.userToDelete)) {
      this.mostrarMensaje('No puedes eliminar tu propia cuenta ni la de otro administrador.', 'error');
      this.cerrarDeleteModal();
      return;
    }

    this.isDeleting = true;
    const email = sessionStorage.getItem('userEmail');
    const token = this.auth.getToken();

    this.http.delete(
      `${this.usersUrl}/admin/users/${this.userToDelete.email}`,
      {
        body: {
          adminEmail: email,
          adminToken: token
        }
      }
    ).subscribe({
      next: () => {
        this.mostrarMensaje(`Usuario ${this.userToDelete?.email} eliminado`, 'success');
        this.loadUsuarios();
        this.cerrarDeleteModal();
        this.isDeleting = false;
      },
      error: (err) => {
        console.error('Error al eliminar usuario:', err);
        this.mostrarMensaje('Error al eliminar el usuario', 'error');
        this.isDeleting = false;
      }
    });
  }

  logout(): void {
    this.auth.logout();
  }

  canDeleteUser(usuario: AdminUser): boolean {
    return usuario.email !== this.currentEmail && usuario.role !== 'ADMIN';
  }

  private mostrarMensaje(msg: string, tipo: 'success' | 'error' | 'info' = 'info'): void {
    this.mensaje = msg;
    this.tipoMensaje = tipo;
    setTimeout(() => {
      this.mensaje = '';
    }, 5000);
  }
}
