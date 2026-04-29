import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { Auth } from './auth';

export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(Auth);
    const router = inject(Router);

    // Si getToken() devuelve null (porque es 'null' string o vacío), bloqueamos
    if (authService.getToken()) {
        return true;
    }

    // Si no hay identidad, al login
    router.navigate(['/auth']);
    return false;
};