import { Routes } from '@angular/router';
import { Compra } from './compra/compra';
import { AuthComponent } from './auth/auth';
import { Espectaculos } from './espectaculos/espectaculos';
import { authGuard } from './auth.guard';
import { ProfileComponent } from './profile/profile.component';
import { AdminComponent } from './admin/admin.component';

export const routes: Routes = [
    // Pantalla de Login/Registro
    { path: "auth", component: AuthComponent },

    // Pantalla principal (ahora accesible sin login para ver y elegir)
    { path: "espectaculos", component: Espectaculos },

    // Perfil del usuario (requiere autenticaci\u00f3n)
    {
        path: "profile",
        component: ProfileComponent,
        canActivate: [authGuard]
    },

    // Panel de administraci\u00f3n (requiere autenticaci\u00f3n)
    {
        path: "admin",
        component: AdminComponent,
        canActivate: [authGuard]
    },

    // El proceso de pago SIEMPRE requiere estar logueado[cite: 2]
    {
        path: "comprar",
        component: Compra,
        canActivate: [authGuard]
    },

    // Por defecto mandamos a la cartelera para que el usuario vea qué hay
    { path: "", redirectTo: "/espectaculos", pathMatch: "full" },

    // Cualquier otra cosa, a la cartelera
    { path: "**", redirectTo: "/espectaculos" }
];