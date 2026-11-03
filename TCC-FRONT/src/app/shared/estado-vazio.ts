import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Estado vazio amigável: ícone, título, explicação e ações (conteúdo projetado). */
@Component({
  selector: 'app-estado-vazio',
  imports: [MatIconModule],
  template: `
    <div class="vazio">
      <div class="icone"><mat-icon>{{ icone() }}</mat-icon></div>
      <h3>{{ titulo() }}</h3>
      @if (texto()) {
        <p>{{ texto() }}</p>
      }
      <div class="acoes"><ng-content /></div>
    </div>
  `,
  styles: `
    .vazio { display: flex; flex-direction: column; align-items: center; text-align: center; padding: 40px 16px; }
    .icone {
      width: 64px; height: 64px; border-radius: 50%; display: grid; place-items: center;
      background: var(--verde-50); color: var(--verde-700); margin-bottom: 12px;
    }
    .icone mat-icon { font-size: 32px; width: 32px; height: 32px; }
    h3 { margin: 0 0 4px; font-size: 1.05rem; font-weight: 600; color: var(--texto); }
    p { margin: 0; max-width: 440px; color: var(--texto-suave); line-height: 1.5; }
    .acoes { display: flex; gap: 8px; flex-wrap: wrap; justify-content: center; margin-top: 16px; }
    .acoes:empty { display: none; }
  `,
})
export class EstadoVazio {
  readonly icone = input('inbox');
  readonly titulo = input.required<string>();
  readonly texto = input<string>();
}
