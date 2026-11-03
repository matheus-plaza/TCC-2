import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Perfil } from '../models/api.models';
import { AuthService } from '../services/auth.service';

/** Restringe a rota aos perfis em {@code data.perfis}. A API sempre revalida a permissão. */
export const roleGuard: CanActivateFn = (route) => {
  const perfis = (route.data['perfis'] ?? []) as Perfil[];
  if (inject(AuthService).temPerfil(perfis)) {
    return true;
  }
  return inject(Router).createUrlTree(['/dashboard']);
};
