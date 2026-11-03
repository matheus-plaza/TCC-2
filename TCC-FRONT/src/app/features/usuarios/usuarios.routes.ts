import { Routes } from '@angular/router';

export const ROTAS: Routes = [{ path: '', loadComponent: () => import('./usuarios').then((m) => m.Usuarios) }];
