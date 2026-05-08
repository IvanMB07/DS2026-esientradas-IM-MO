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

export interface Escenario {
  id?: number | null;
  nombre: string;
  descripcion: string;
}

export interface Espectaculo {
  id?: number;
  artista: string;
  fecha: string;
  escenario: Escenario;
}

export interface EspectaculoEscenarioItem {
  id: number;
  artista: string;
  fecha: string;
  escenario: string;
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
  activeTab: 'usuarios' | 'escenarios' | 'espectaculos' = 'usuarios';

  // Modal de cambiar rol
  showChangeRoleModal = false;
  selectedUser: AdminUser | null = null;
  newRole = '';
  isChangingRole = false;

  // Modal de eliminar
  showDeleteModal = false;
  userToDelete: AdminUser | null = null;
  isDeleting = false;

  // Formulario de escenario
  showEscenarioModal = false;
  nuevoEscenario: Escenario = { nombre: '', descripcion: '' };
  isInsertingEscenario = false;

  // Formulario de espectáculo
  showEspectaculoModal = false;
  nuevoEspectaculo: Espectaculo = { artista: '', fecha: '', escenario: { id: null, nombre: '', descripcion: '' } };
  isInsertingEspectaculo = false;
  escenarios: Escenario[] = [];
  escenarioSeleccionadoId: number | null = null;
  espectaculosPorEscenario: { [escenarioId: number]: EspectaculoEscenarioItem[] } = {};
  escenarioExpandidoId: number | null = null;
  cargandoEspectaculosEscenarioId: number | null = null;

  private usersUrl = 'http://localhost:8081/users';
  private escenarioUrl = 'http://localhost:8080/escenarios';
  private espectaculoUrl = 'http://localhost:8080/espectaculos';
  private busquedaUrl = 'http://localhost:8080/busqueda';

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
        this.loadEscenarios();
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

  loadEscenarios(): void {
    this.http.get<Escenario[]>(`${this.busquedaUrl}/getEscenarios`).subscribe({
      next: (escenarios) => {
        this.escenarios = escenarios;
      },
      error: (err) => {
        console.error('Error al cargar escenarios:', err);
      }
    });
  }

  // Métodos de usuario
  abrirChangeRoleModal(usuario: AdminUser): void {
    if (!this.canChangeUserRole(usuario)) {
      this.mostrarMensaje('No puedes cambiar tu propio rol.', 'error');
      return;
    }

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

    if (!this.canChangeUserRole(this.selectedUser)) {
      this.mostrarMensaje('No puedes cambiar tu propio rol.', 'error');
      this.cerrarChangeRoleModal();
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

  canDeleteUser(usuario: AdminUser): boolean {
    return usuario.email !== this.currentEmail && usuario.role !== 'ADMIN';
  }

  canChangeUserRole(usuario: AdminUser): boolean {
    return usuario.email !== this.currentEmail;
  }

  // Métodos de escenario
  abrirEscenarioModal(): void {
    this.nuevoEscenario = { nombre: '', descripcion: '' };
    this.showEscenarioModal = true;
  }

  cerrarEscenarioModal(): void {
    this.showEscenarioModal = false;
    this.nuevoEscenario = { nombre: '', descripcion: '' };
  }

  insertarEscenario(): void {
    if (!this.nuevoEscenario.nombre || !this.nuevoEscenario.nombre.trim()) {
      this.mostrarMensaje('El nombre del escenario es requerido', 'error');
      return;
    }

    if (!this.nuevoEscenario.descripcion || !this.nuevoEscenario.descripcion.trim()) {
      this.mostrarMensaje('La descripción del escenario es requerida', 'error');
      return;
    }

    this.isInsertingEscenario = true;
    const token = this.auth.getToken();

    this.http.post(
      `${this.escenarioUrl}/insertar?userToken=${token}`,
      this.nuevoEscenario
    ).subscribe({
      next: () => {
        this.mostrarMensaje('Escenario insertado exitosamente', 'success');
        this.cerrarEscenarioModal();
        this.loadEscenarios();
        this.isInsertingEscenario = false;
      },
      error: (err) => {
        console.error('Error al insertar escenario:', err);
        const errorMsg = err.error?.message || 'Error al insertar el escenario';
        this.mostrarMensaje(errorMsg, 'error');
        this.isInsertingEscenario = false;
      }
    });
  }

  eliminarEscenario(escenario: Escenario): void {
    if (!confirm(`¿Estás seguro de que deseas eliminar el escenario "${escenario.nombre}"? Esta acción no se puede deshacer.`)) {
      return;
    }

    const token = this.auth.getToken();
    this.http.delete(
      `${this.escenarioUrl}/eliminar/${escenario.id}?userToken=${token}`
    ).subscribe({
      next: () => {
        this.mostrarMensaje(`Escenario "${escenario.nombre}" eliminado exitosamente`, 'success');
        if (escenario.id && this.escenarioExpandidoId === escenario.id) {
          this.escenarioExpandidoId = null;
        }
        this.loadEscenarios();
      },
      error: (err) => {
        console.error('Error al eliminar escenario:', err);
        const errorMsg = this.extraerMensajeError(err, 'Error al eliminar el escenario');
        if (errorMsg.toLowerCase().includes('espectáculos asociados')) {
          this.mostrarMensaje(
            'No se puede eliminar el escenario porque tiene espectáculos asociados. Expande el escenario y elimina primero esos espectáculos.',
            'error'
          );
          return;
        }
        this.mostrarMensaje(errorMsg, 'error');
      }
    });
  }

  toggleEspectaculosEscenario(escenarioId: number | null | undefined): void {
    if (!escenarioId) {
      this.mostrarMensaje('El escenario seleccionado no es válido', 'error');
      return;
    }

    if (this.escenarioExpandidoId === escenarioId) {
      this.escenarioExpandidoId = null;
      return;
    }

    this.escenarioExpandidoId = escenarioId;
    this.cargarEspectaculosDeEscenario(escenarioId);
  }

  cargarEspectaculosDeEscenario(escenarioId: number): void {
    this.cargandoEspectaculosEscenarioId = escenarioId;

    this.http.get<EspectaculoEscenarioItem[]>(`${this.busquedaUrl}/getEspectaculos/${escenarioId}`).subscribe({
      next: (espectaculos) => {
        this.espectaculosPorEscenario[escenarioId] = espectaculos;
        this.cargandoEspectaculosEscenarioId = null;
      },
      error: (err) => {
        console.error('Error al cargar espectáculos por escenario:', err);
        this.espectaculosPorEscenario[escenarioId] = [];
        this.cargandoEspectaculosEscenarioId = null;
        this.mostrarMensaje(this.extraerMensajeError(err, 'No se pudieron cargar los espectáculos del escenario'), 'error');
      }
    });
  }

  // Métodos de espectáculo
  abrirEspectaculoModal(): void {
    this.nuevoEspectaculo = { artista: '', fecha: '', escenario: { id: null, nombre: '', descripcion: '' } };
    this.escenarioSeleccionadoId = null;
    this.showEspectaculoModal = true;
  }

  cerrarEspectaculoModal(): void {
    this.showEspectaculoModal = false;
    this.nuevoEspectaculo = { artista: '', fecha: '', escenario: { id: null, nombre: '', descripcion: '' } };
    this.escenarioSeleccionadoId = null;
  }

  insertarEspectaculo(): void {
    if (!this.nuevoEspectaculo.artista || !this.nuevoEspectaculo.artista.trim()) {
      this.mostrarMensaje('El nombre del artista es requerido', 'error');
      return;
    }

    if (!this.nuevoEspectaculo.fecha) {
      this.mostrarMensaje('La fecha del espectáculo es requerida', 'error');
      return;
    }

    const escenarioId = Number(this.escenarioSeleccionadoId);
    if (!Number.isInteger(escenarioId) || escenarioId <= 0) {
      this.mostrarMensaje('Debe seleccionar un escenario', 'error');
      return;
    }

    this.isInsertingEspectaculo = true;
    const token = this.auth.getToken();

    // Crear objeto con estructura correcta para el servidor
    const espectaculoData = {
      artista: this.nuevoEspectaculo.artista,
      fecha: this.nuevoEspectaculo.fecha + 'T00:00:00', // Agregar hora si no existe
      escenario: {
        id: escenarioId
      }
    };

    this.http.post(
      `${this.espectaculoUrl}/insertar?userToken=${token}`,
      espectaculoData
    ).subscribe({
      next: () => {
        this.mostrarMensaje('Espectáculo insertado exitosamente', 'success');
        this.cerrarEspectaculoModal();
        this.isInsertingEspectaculo = false;
      },
      error: (err) => {
        console.error('Error al insertar espectáculo:', err);
        const errorMsg = err.error?.message || 'Error al insertar el espectáculo';
        this.mostrarMensaje(errorMsg, 'error');
        this.isInsertingEspectaculo = false;
      }
    });
  }

  eliminarEspectaculo(id: number | undefined, escenarioId?: number | null, artista?: string): void {
    if (!id) {
      this.mostrarMensaje('ID de espectáculo no válido', 'error');
      return;
    }

    const textoEspectaculo = artista ? ` "${artista}"` : '';
    if (!confirm(`¿Estás seguro de que deseas eliminar el espectáculo${textoEspectaculo}? Esta acción no se puede deshacer.`)) {
      return;
    }

    const token = this.auth.getToken();
    this.http.delete(
      `${this.espectaculoUrl}/eliminar/${id}?userToken=${token}`
    ).subscribe({
      next: () => {
        this.mostrarMensaje('Espectáculo eliminado exitosamente', 'success');
        if (escenarioId) {
          this.cargarEspectaculosDeEscenario(escenarioId);
        }
      },
      error: (err) => {
        console.error('Error al eliminar espectáculo:', err);
        const errorMsg = this.extraerMensajeError(err, 'Error al eliminar el espectáculo');
        this.mostrarMensaje(errorMsg, 'error');
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

  private extraerMensajeError(err: any, fallback: string): string {
    if (!err) {
      return fallback;
    }

    if (typeof err.error === 'string' && err.error.trim()) {
      return err.error;
    }

    if (err.error?.message) {
      return err.error.message;
    }

    if (err.error?.error) {
      return err.error.error;
    }

    if (err.message) {
      return err.message;
    }

    return fallback;
  }
}
