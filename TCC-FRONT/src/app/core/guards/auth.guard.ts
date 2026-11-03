import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Exige sessão ativa; sem ela, vai ao login e guarda a tela pedida para o retorno. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.autenticado()) {
    return true;
  }
  return inject(Router).createUrlTree(['/auth/login'], { queryParams: { retorno: state.url } });
};
