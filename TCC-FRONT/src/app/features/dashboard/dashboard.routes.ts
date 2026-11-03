import { Routes } from '@angular/router';

export const ROTAS: Routes = [{ path: '', loadComponent: () => import('./dashboard').then((m) => m.Dashboard) }];
