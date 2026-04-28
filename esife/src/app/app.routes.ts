import { Routes } from '@angular/router';
import { Compra } from './compra/compra';
import { AuthComponent } from './auth/auth';
import { Espectaculos } from './espectaculos/espectaculos'; // Asegúrate de que esta ruta sea correcta

export const routes: Routes = [
    // Pantalla de Login/Registro
    { path: "auth", component: AuthComponent },

    // Pantalla principal de eventos (A la que intentas redirigir)
    { path: "espectaculos", component: Espectaculos },

    // Pantalla de compra (añadimos el :id para saber qué entrada compramos)
    { path: "comprar/:id", component: Compra },

    // Si entran a la raíz, que les mande al login
    { path: "", redirectTo: "/auth", pathMatch: "full" },

    // Si escriben cualquier cosa rara, que les mande también al login
    { path: "**", redirectTo: "/auth" }
];