import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { Laudo } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { EstadoVazio } from '../../shared/estado-vazio';
import { ROTULO_METODO_PH } from '../../shared/formato';
import { DataBrPipe } from '../../shared/pipes/data-br.pipe';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { TalhaoBusca } from '../../shared/talhao-busca';

/** UC07: consulta dos laudos, por talhão, período e profundidade. */
@Component({
  selector: 'app-laudos',
  imports: [
    RouterLink,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TalhaoBusca,
    EstadoVazio,
    NumeroPipe,
    DataBrPipe,
  ],
  template: `
    <header class="pagina-cabecalho">
      <div>
        <h1>Laudos de solo</h1>
        <p>Histórico das análises laboratoriais. Abra um laudo para ver a interpretação da fertilidade.</p>
      </div>
      <div class="acoes">
        <a mat-flat-button routerLink="/laudos/novo"><mat-icon>add</mat-icon> Registrar laudo</a>
      </div>
    </header>

    <section class="cartao">
      <div class="filtros-laudo">
        <app-talhao-busca class="filtro-talhao" [(talhaoId)]="talhaoId" [permitirTodos]="true" />
        <mat-form-field>
          <mat-label>Camada</mat-label>
          <mat-select [value]="profundidade()" (valueChange)="profundidade.set($event)">
            <mat-option value="">Todas</mat-option>
            <mat-option value="0-20">0-20 cm</mat-option>
            <mat-option value="20-40">20-40 cm</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>De</mat-label>
          <input matInput type="date" [value]="inicio()" (change)="inicio.set($any($event.target).value)" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Até</mat-label>
          <input matInput type="date" [value]="fim()" (change)="fim.set($any($event.target).value)" />
        </mat-form-field>
      </div>

      @if (laudos.valor().length) {
        <div class="tabela-container">
          <table class="lista">
            <thead>
              <tr>
                <th>Talhão</th>
                <th>Data</th>
                <th>Camada</th>
                <th class="numero">pH</th>
                <th class="numero">V%</th>
                <th class="numero">m%</th>
                <th class="numero">P</th>
                <th class="numero">K</th>
                <th class="numero">Argila %</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              @for (l of laudos.valor(); track l.id) {
                <tr class="clicavel" [routerLink]="['/laudos', l.id]">
                  <td class="principal" data-rotulo="Talhão">
                    {{ l.talhaoIdentificacao }}
                    <span class="sub">{{ l.propriedadeNome }}@if (!auth.produtor()) { · {{ l.produtorNome }} }</span>
                  </td>
                  <td data-rotulo="Data">{{ l.dataAnalise | dataBr }}</td>
                  <td data-rotulo="Camada">{{ l.profundidadeCm }} cm</td>
                  <td class="numero" data-rotulo="pH">{{ l.ph | numero: 2 }} <small>{{ rotuloMetodoPh[l.metodoPh] }}</small></td>
                  <td class="numero" data-rotulo="V%">{{ l.indicadores.saturacaoBases | numero: 1 }}</td>
                  <td class="numero" data-rotulo="m%">{{ l.indicadores.saturacaoAluminio | numero: 1 }}</td>
                  <td class="numero" data-rotulo="P">{{ l.pMehlich | numero: 1 }}</td>
                  <td class="numero" data-rotulo="K">{{ l.kCmolc | numero: 2 }}</td>
                  <td class="numero" data-rotulo="Argila %">{{ l.argilaPorcentagem | numero: 0 }}</td>
                  <td class="acoes-linha">
                    @if (!auth.produtor() && l.profundidadeCm === '0-20') {
                      <a mat-button routerLink="/recomendacoes/nova" [queryParams]="{ laudoId: l.id }" (click)="$event.stopPropagation()">
                        <mat-icon>calculate</mat-icon> Recomendar
                      </a>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else if (!laudos.carregando()) {
        <app-estado-vazio
          icone="science"
          [titulo]="comFiltro() ? 'Nenhum laudo com esses filtros' : 'Nenhum laudo registrado'"
          [texto]="comFiltro() ? 'Ajuste os filtros para ver outros laudos.' : 'Registre o primeiro laudo de solo a partir do resultado do laboratório.'"
        >
          @if (!comFiltro()) {
            <a mat-flat-button routerLink="/laudos/novo">Registrar laudo</a>
          }
        </app-estado-vazio>
      }
    </section>
  `,
  styles: `
    .filtros-laudo {
      display: grid;
      grid-template-columns: minmax(260px, 2fr) repeat(3, minmax(130px, 1fr));
      gap: 4px 16px;
      align-items: start;
    }
    .filtros-laudo mat-form-field { width: 100%; }
    @media (max-width: 860px) {
      .filtros-laudo { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      .filtro-talhao { grid-column: 1 / -1; }
    }
  `,
})
export class Laudos {
  /** Filtro por talhão vindo da URL. */
  readonly talhaoIdUrl = input<string | undefined>(undefined, { alias: 'talhaoId' });

  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly rotuloMetodoPh = ROTULO_METODO_PH;

  protected readonly talhaoId = signal<number | null>(null);
  protected readonly profundidade = signal('');
  protected readonly inicio = signal('');
  protected readonly fim = signal('');

  protected readonly comFiltro = computed(
    () => this.talhaoId() !== null || !!this.profundidade() || !!this.inicio() || !!this.fim(),
  );

  private readonly filtro = computed(() => ({
    talhaoId: this.talhaoId(),
    profundidadeCm: this.profundidade(),
    inicio: this.inicio(),
    fim: this.fim(),
  }));

  protected readonly laudos = carga(() => this.filtro(), (f) => this.api.laudos(f), [] as Laudo[]);

  constructor() {
    effect(() => {
      const daUrl = Number(this.talhaoIdUrl());
      if (daUrl) {
        untracked(() => this.talhaoId.set(daUrl));
      }
    });
  }
}
