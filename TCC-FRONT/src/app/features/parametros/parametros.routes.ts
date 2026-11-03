import { Routes } from '@angular/router';

export const ROTAS: Routes = [{ path: '', loadComponent: () => import('./parametros').then((m) => m.Parametros) }];
