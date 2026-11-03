import { Routes } from '@angular/router';

export const ROTAS: Routes = [
  { path: '', loadComponent: () => import('./laudos').then((m) => m.Laudos) },
  { path: 'novo', loadComponent: () => import('./laudo-form').then((m) => m.LaudoForm) },
  { path: ':id', loadComponent: () => import('./laudo-detalhe').then((m) => m.LaudoDetalhe) },
];
