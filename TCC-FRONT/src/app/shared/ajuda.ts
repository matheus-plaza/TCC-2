import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { GLOSSARIO, TermoGlossario } from '../core/glossario';

/** Ícone "?" com a explicação de um termo técnico em linguagem simples. */
@Component({
  selector: 'app-ajuda',
  imports: [MatIconModule, MatTooltipModule],
  template: `
    <button
      type="button"
      class="ajuda"
      [matTooltip]="texto()"
      matTooltipClass="tooltip-ajuda"
      [attr.aria-label]="'O que é: ' + texto()"
      (click)="$event.stopPropagation()"
    >
      <mat-icon>help</mat-icon>
    </button>
  `,
  styles: `
    :host { display: inline-flex; vertical-align: middle; }
    .ajuda {
      display: inline-flex; padding: 0; margin: 0 0 0 2px; border: 0; background: none;
      color: var(--texto-suave); cursor: help; border-radius: 50%;
    }
    .ajuda:hover, .ajuda:focus-visible { color: var(--verde-700); }
    mat-icon { font-size: 16px; width: 16px; height: 16px; }
  `,
})
export class Ajuda {
  readonly termo = input.required<TermoGlossario>();
  protected readonly texto = computed(() => GLOSSARIO[this.termo()]);
}
