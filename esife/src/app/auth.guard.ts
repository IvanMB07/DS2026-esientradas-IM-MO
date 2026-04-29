import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { Auth } from './auth';

export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(Auth);
    const router = inject(Router);

    // Si hay token, dejamos pasar
    if (authService.getToken()) {
        return true;
    }

    // Si no hay token, guardamos a dónde quería ir el usuario y lo mandamos al login
    router.navigate(['/auth']);
    return false;
};