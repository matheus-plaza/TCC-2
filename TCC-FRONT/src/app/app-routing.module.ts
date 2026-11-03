import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Shell } from './shared/navbar/shell';

/** Cada funcionalidade é carregada sob demanda (lazy loading). */
const routes: Routes = [
  { path: 'auth', loadChildren: () => import('./features/auth/auth.routes').then((m) => m.ROTAS) },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadChildren: () => import('./features/dashboard/dashboard.routes').then((m) => m.ROTAS) },
      { path: 'propriedades', loadChildren: () => import('./features/propriedades/propriedades.routes').then((m) => m.ROTAS) },
      { path: 'laudos', loadChildren: () => import('./features/laudos/laudos.routes').then((m) => m.ROTAS) },
      { path: 'recomendacoes', loadChildren: () => import('./features/recomendacao/recomendacao.routes').then((m) => m.ROTAS) },
      { path: 'usuarios', loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.ROTAS) },
      {
        path: 'parametros',
        canActivate: [roleGuard],
        data: { perfis: ['ADMIN', 'AGRONOMO'] },
        loadChildren: () => import('./features/parametros/parametros.routes').then((m) => m.ROTAS),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { bindToComponentInputs: true, scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
