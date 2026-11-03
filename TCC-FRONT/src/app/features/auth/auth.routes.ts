import { inject } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/** Quem já está autenticado não precisa ver as telas de acesso. */
const somenteVisitante = () => {
  return inject(AuthService).autenticado() ? inject(Router).createUrlTree(['/dashboard']) : true;
};

export const ROTAS: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', canActivate: [somenteVisitante], loadComponent: () => import('./login').then((m) => m.Login) },
  { path: 'registro', canActivate: [somenteVisitante], loadComponent: () => import('./registro').then((m) => m.Registro) },
];
