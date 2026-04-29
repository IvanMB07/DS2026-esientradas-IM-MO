import { Routes } from '@angular/router';
import { Compra } from './compra/compra';
import { AuthComponent } from './auth/auth';
import { Espectaculos } from './espectaculos/espectaculos';
import { authGuard } from './auth.guard';

export const routes: Routes = [
    // Pantalla de Login/Registro
    { path: "auth", component: AuthComponent },

    // Pantalla principal de eventos (A la que intentas redirigir)
    { path: "espectaculos", component: Espectaculos },

    // Solo se puede entrar a comprar si el authGuard devuelve true
    {
        path: "comprar",
        component: Compra,
        canActivate: [authGuard]
    },

    // Si entran a la raíz, que les mande al login
    { path: "", redirectTo: "/auth", pathMatch: "full" },

    // Si escriben cualquier cosa rara, que les mande también al login
    { path: "**", redirectTo: "/auth" }
];
