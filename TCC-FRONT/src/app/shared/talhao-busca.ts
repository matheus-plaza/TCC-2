import { Component, computed, effect, inject, input, model, signal } from '@angular/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Talhao } from '../core/models/api.models';
import { ApiService } from '../core/services/api.service';
import { carga } from '../core/services/carga';
import { normalizar } from './formato';
import { NumeroPipe } from './pipes/numero.pipe';

interface Grupo {
  chave: number;
  rotulo: string;
  talhoes: Talhao[];
}

function descrever(t: Talhao): string {
  return `${t.identificacao} · ${t.propriedadeNome} · ${t.produtorNome}`;
}

/**
 * Campo único de busca de talhão: digite parte do nome do produtor, da propriedade,
 * do município ou do talhão. A lista já vem restrita ao que o usuário pode ver.
 */
@Component({
  selector: 'app-talhao-busca',
  imports: [MatFormFieldModule, MatInputModule, MatAutocompleteModule, MatIconModule, MatButtonModule, NumeroPipe],
  template: `
    <mat-form-field class="campo">
      <mat-label>{{ rotulo() }}</mat-label>
      <mat-icon matPrefix>search</mat-icon>
      <input
        matInput
        [matAutocomplete]="auto"
        [value]="valorCampo()"
        (input)="texto.set($any($event.target).value)"
        (focus)="aoFocar($event)"
        (blur)="editando.set(false)"
        placeholder="Produtor, propriedade ou talhão"
        [required]="obrigatorio()"
      />
      @if (talhaoId() !== null && permitirTodos()) {
        <button matIconSuffix mat-icon-button type="button" aria-label="Limpar talhão" (click)="talhaoId.set(null)">
          <mat-icon>close</mat-icon>
        </button>
      }
      @if (!talhoes.carregando() && talhoes.valor().length === 0) {
        <mat-hint>Nenhum talhão cadastrado</mat-hint>
      }
      <mat-autocomplete
        #auto="matAutocomplete"
        [displayWith]="exibir"
        (optionSelected)="talhaoId.set($event.option.value)"
        class="painel-talhoes"
      >
        @if (permitirTodos()) {
          <mat-option [value]="null"><mat-icon>select_all</mat-icon> Todos os talhões</mat-option>
        }
        @for (g of grupos(); track g.chave) {
          <mat-optgroup [label]="g.rotulo">
            @for (t of g.talhoes; track t.id) {
              <mat-option [value]="t.id">
                <span class="opcao">
                  <span>{{ t.identificacao }}</span>
                  <small>{{ t.areaHectares | numero: 2 : 0 }} ha</small>
                </span>
              </mat-option>
            }
          </mat-optgroup>
        } @empty {
          @if (texto()) {
            <mat-option disabled>Nenhum talhão encontrado para "{{ texto() }}"</mat-option>
          }
        }
      </mat-autocomplete>
    </mat-form-field>
  `,
  styles: `
    :host { display: block; min-width: 0; }
    .campo { width: 100%; }
    .opcao { display: flex; justify-content: space-between; gap: 16px; width: 100%; }
    .opcao small { color: var(--texto-suave); }
  `,
})
export class TalhaoBusca {
  private readonly api = inject(ApiService);

  readonly talhaoId = model<number | null>(null);
  readonly rotulo = input('Talhão');
  readonly obrigatorio = input(false);
  /** Exibe "Todos os talhões" e o botão de limpar (uso como filtro). */
  readonly permitirTodos = input(false);

  protected readonly texto = signal('');
  protected readonly editando = signal(false);

  protected readonly talhoes = carga(() => true, () => this.api.talhoes(), [] as Talhao[]);

  private readonly selecionado = computed(() => this.talhoes.valor().find((t) => t.id === this.talhaoId()));

  protected readonly valorCampo = computed(() => {
    if (this.editando()) {
      return this.texto();
    }
    const t = this.selecionado();
    return t ? descrever(t) : '';
  });

  protected readonly grupos = computed<Grupo[]>(() => {
    const termo = normalizar(this.texto().trim());
    const mapa = new Map<number, Grupo>();
    for (const t of this.talhoes.valor()) {
      const alvo = normalizar(`${t.identificacao} ${t.propriedadeNome} ${t.municipio} ${t.produtorNome}`);
      if (termo && !termo.split(/\s+/).every((parte) => alvo.includes(parte))) {
        continue;
      }
      let g = mapa.get(t.propriedadeId);
      if (!g) {
        g = { chave: t.propriedadeId, rotulo: `${t.produtorNome}: ${t.propriedadeNome} (${t.municipio})`, talhoes: [] };
        mapa.set(t.propriedadeId, g);
      }
      g.talhoes.push(t);
    }
    return [...mapa.values()];
  });

  protected readonly exibir = (id: number | null): string => {
    const t = this.talhoes.valor().find((x) => x.id === id);
    return t ? descrever(t) : '';
  };

  constructor() {
    // Talhão que o usuário não pode ver (link antigo, por exemplo) é descartado
    effect(() => {
      const id = this.talhaoId();
      if (id !== null && !this.talhoes.carregando() && this.talhoes.valor().length
          && !this.talhoes.valor().some((t) => t.id === id)) {
        this.talhaoId.set(null);
      }
    });
  }

  protected aoFocar(evento: FocusEvent): void {
    this.texto.set('');
    this.editando.set(true);
    (evento.target as HTMLInputElement).select();
  }
}
