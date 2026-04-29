import { Routes } from '@angular/router';
import { Compra } from './compra/compra';
import { AuthComponent } from './auth/auth';
import { Espectaculos } from './espectaculos/espectaculos';
import { authGuard } from './auth.guard';

export const routes: Routes = [
    // Pantalla de Login/Registro
    { path: "auth", component: AuthComponent },

    // Pantalla principal (ahora accesible sin login para ver y elegir)
    { path: "espectaculos", component: Espectaculos },

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