import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { Recomendacao, StatusRecomendacao } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { EstadoVazio } from '../../shared/estado-vazio';
import { classeCultura } from '../../shared/formato';
import { DataBrPipe } from '../../shared/pipes/data-br.pipe';
import { FaixaDosePipe } from '../../shared/pipes/faixa-dose.pipe';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { TalhaoBusca } from '../../shared/talhao-busca';

/** Recomendações registradas, por talhão, status e período. */
@Component({
  selector: 'app-recomendacoes',
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
    FaixaDosePipe,
    DataBrPipe,
  ],
  template: `
    <header class="pagina-cabecalho">
      <div>
        <h1>Recomendações</h1>
        <p>
          @if (auth.produtor()) {
            Receituários adotados pelos agrônomos vinculados a você.
          } @else {
            Recomendações adotadas e simulações salvas.
          }
        </p>
      </div>
      @if (!auth.produtor()) {
        <div class="acoes">
          <a mat-flat-button routerLink="/recomendacoes/nova"><mat-icon>calculate</mat-icon> Nova recomendação</a>
        </div>
      }
    </header>

    <section class="cartao">
      <div class="filtros-rec">
        <app-talhao-busca class="filtro-talhao" [(talhaoId)]="talhaoId" [permitirTodos]="true" />
        @if (!auth.produtor()) {
          <mat-form-field>
            <mat-label>Status</mat-label>
            <mat-select [value]="status()" (valueChange)="status.set($event)">
              <mat-option [value]="null">Todos</mat-option>
              <mat-option value="ADOTADA">Adotadas</mat-option>
              <mat-option value="SIMULACAO">Simulações</mat-option>
            </mat-select>
          </mat-form-field>
        }
        <mat-form-field>
          <mat-label>De</mat-label>
          <input matInput type="date" [value]="inicio()" (change)="inicio.set($any($event.target).value)" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Até</mat-label>
          <input matInput type="date" [value]="fim()" (change)="fim.set($any($event.target).value)" />
        </mat-form-field>
      </div>

      @if (recomendacoes.valor().length) {
        <div class="tabela-container">
          <table class="lista">
            <thead>
              <tr>
                <th>Talhão</th>
                <th>Data</th>
                <th>Cultura</th>
                <th>Status</th>
                <th class="numero">Calcário (t/ha)</th>
                <th class="numero">Gesso (kg/ha)</th>
                <th class="numero">N (kg/ha)</th>
                <th class="numero">P₂O₅ (kg/ha)</th>
                <th class="numero">K₂O (kg/ha)</th>
              </tr>
            </thead>
            <tbody>
              @for (r of recomendacoes.valor(); track r.id) {
                <tr class="clicavel" [routerLink]="['/recomendacoes', r.id]">
                  <td class="principal" data-rotulo="Talhão">
                    {{ r.talhao?.identificacao }}
                    <span class="sub">{{ r.talhao?.propriedadeNome }}@if (!auth.produtor()) { · {{ r.talhao?.produtorNome }} }</span>
                  </td>
                  <td data-rotulo="Data">{{ r.dataAdocao ?? r.dataGeracao | dataBr }}</td>
                  <td data-rotulo="Cultura"><span [class]="classeCultura(r.culturaAlvo.nome)">{{ r.culturaAlvo.nome }}</span></td>
                  <td data-rotulo="Status">
                    @if (r.status === 'ADOTADA') {
                      <span class="selo-status adotada">Adotada</span>
                    } @else {
                      <span class="selo-status simulacao">Simulação</span>
                    }
                  </td>
                  <td class="numero" data-rotulo="Calcário (t/ha)">{{ r.calagem.necessidadeCalcarioTonHa | numero }}</td>
                  <td class="numero" data-rotulo="Gesso (kg/ha)">{{ r.gessagem.doseKgHa | numero: 0 }}</td>
                  <td class="numero" data-rotulo="N (kg/ha)">{{ r.adubacao.doseNkgHa | faixaDose }}</td>
                  <td class="numero" data-rotulo="P₂O₅ (kg/ha)">{{ r.adubacao.doseP2O5kgHa | faixaDose }}</td>
                  <td class="numero" data-rotulo="K₂O (kg/ha)">{{ r.adubacao.doseK2OkgHa | faixaDose }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else if (!recomendacoes.carregando()) {
        <app-estado-vazio
          icone="assignment"
          [titulo]="comFiltro() ? 'Nenhuma recomendação com esses filtros' : 'Nenhuma recomendação registrada'"
          [texto]="auth.produtor() ? 'Quando um agrônomo adotar uma recomendação para seus talhões, ela aparece aqui.' : 'Simule uma recomendação a partir de um laudo de solo.'"
        >
          @if (!auth.produtor() && !comFiltro()) {
            <a mat-flat-button routerLink="/recomendacoes/nova">Nova recomendação</a>
          }
        </app-estado-vazio>
      }
    </section>
  `,
  styles: `
    .filtros-rec {
      display: grid;
      grid-template-columns: minmax(260px, 2fr) repeat(auto-fit, minmax(130px, 1fr));
      gap: 4px 16px;
      align-items: start;
    }
    .filtros-rec mat-form-field { width: 100%; }
    @media (max-width: 860px) {
      .filtros-rec { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      .filtro-talhao { grid-column: 1 / -1; }
    }
  `,
})
export class Recomendacoes {
  /** Filtro por status vindo da URL (atalhos do painel). */
  readonly statusUrl = input<StatusRecomendacao | undefined>(undefined, { alias: 'status' });

  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly classeCultura = classeCultura;

  protected readonly talhaoId = signal<number | null>(null);
  protected readonly status = signal<StatusRecomendacao | null>(null);
  protected readonly inicio = signal('');
  protected readonly fim = signal('');

  protected readonly comFiltro = computed(
    () => this.talhaoId() !== null || this.status() !== null || !!this.inicio() || !!this.fim(),
  );

  private readonly filtro = computed(() => ({
    talhaoId: this.talhaoId(),
    status: this.status(),
    inicio: this.inicio(),
    fim: this.fim(),
  }));

  protected readonly recomendacoes = carga(() => this.filtro(), (f) => this.api.recomendacoes(f), [] as Recomendacao[]);

  constructor() {
    effect(() => {
      const daUrl = this.statusUrl();
      if (daUrl) {
        untracked(() => this.status.set(daUrl));
      }
    });
  }
}
