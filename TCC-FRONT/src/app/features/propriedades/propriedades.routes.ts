import { Routes } from '@angular/router';

export const ROTAS: Routes = [
  { path: '', loadComponent: () => import('./propriedades').then((m) => m.Propriedades) },
  {
    path: 'talhoes/:id/historico',
    loadComponent: () => import('./historico-talhao').then((m) => m.HistoricoTalhaoTela),
  },
  { path: ':id', loadComponent: () => import('./propriedade-detalhe').then((m) => m.PropriedadeDetalhe) },
];
