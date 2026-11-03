import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const ROTAS: Routes = [
  { path: '', loadComponent: () => import('./recomendacoes').then((m) => m.Recomendacoes) },
  {
    path: 'nova',
    canActivate: [roleGuard],
    data: { perfis: ['ADMIN', 'AGRONOMO'] },
    loadComponent: () => import('./recomendacao-wizard').then((m) => m.RecomendacaoWizard),
  },
  { path: ':id', loadComponent: () => import('./receituario').then((m) => m.Receituario) },
];
