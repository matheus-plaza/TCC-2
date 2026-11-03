import { Component, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { HistoricoTalhao } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { EstadoVazio } from '../../shared/estado-vazio';
import { classeCultura, diferencaComSinal, ROTULO_METODO_PH } from '../../shared/formato';
import { DataBrPipe } from '../../shared/pipes/data-br.pipe';
import { FaixaDosePipe } from '../../shared/pipes/faixa-dose.pipe';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';

/** RF07: linha do tempo de laudos e recomendações adotadas de um talhão, por período. */
@Component({
  selector: 'app-historico-talhao',
  imports: [RouterLink, MatFormFieldModule, MatInputModule, MatIconModule, MatButtonModule, EstadoVazio, NumeroPipe, FaixaDosePipe, DataBrPipe],
  template: `
    @if (historico.valor(); as h) {
      <a class="voltar" [routerLink]="['/propriedades', h.talhao.propriedadeId]">
        <mat-icon>arrow_back</mat-icon> {{ h.talhao.propriedadeNome }}
      </a>
      <header class="pagina-cabecalho">
        <div>
          <h1>Histórico de {{ h.talhao.identificacao }}</h1>
          <p>{{ h.talhao.propriedadeNome }} · {{ h.talhao.produtorNome }} · {{ h.talhao.areaHectares | numero: 2 : 0 }} ha</p>
        </div>
        <div class="acoes">
          <a mat-stroked-button routerLink="/dashboard" [queryParams]="{ talhaoId: h.talhao.id }">
            <mat-icon>monitoring</mat-icon> Ver evolução
          </a>
          <a mat-flat-button routerLink="/laudos/novo" [queryParams]="{ talhaoId: h.talhao.id }">
            <mat-icon>add</mat-icon> Registrar laudo
          </a>
        </div>
      </header>

      <section class="cartao">
        <div class="periodo">
          <mat-form-field>
            <mat-label>De</mat-label>
            <input matInput type="date" [value]="inicio()" (change)="inicio.set($any($event.target).value)" />
          </mat-form-field>
          <mat-form-field>
            <mat-label>Até</mat-label>
            <input matInput type="date" [value]="fim()" (change)="fim.set($any($event.target).value)" />
          </mat-form-field>
        </div>

        @if (h.eventos.length) {
          <ol class="linha-do-tempo">
            @for (e of h.eventos; track e.tipo + e.referenciaId) {
              <li [class.adotada]="e.tipo !== 'LAUDO'">
                <span class="marcador"><mat-icon>{{ iconeDoEvento[e.tipo] }}</mat-icon></span>
                <a class="evento" [routerLink]="e.tipo === 'LAUDO' ? ['/laudos', e.referenciaId] : ['/recomendacoes', e.referenciaId]">
                  <span class="cabecalho-evento">
                    <strong>{{ e.titulo }}</strong>
                    <span class="sub">{{ e.data | dataBr }}</span>
                  </span>
                  @if (e.laudo; as l) {
                    <span class="valores">
                      <span>pH ({{ rotuloMetodoPh[l.metodoPh] }}) <b>{{ l.ph | numero: 2 }}</b></span>
                      <span>V% <b>{{ l.v | numero: 1 }}</b></span>
                      <span>m% <b>{{ l.m | numero: 1 }}</b></span>
                      <span>M.O. <b>{{ l.mo | numero: 1 }}</b></span>
                      <span>P <b>{{ l.p | numero: 1 }}</b></span>
                      <span>K <b>{{ l.k | numero: 2 }}</b></span>
                    </span>
                  }
                  @if (e.doses; as d) {
                    <span class="valores">
                      <span [class]="classeCultura(d.cultura)">{{ d.cultura }}</span>
                      <span>Calcário <b>{{ d.calcarioTonHa | numero }} t/ha</b></span>
                      <span>Gesso <b>{{ d.gessoKgHa | numero: 0 }} kg/ha</b></span>
                      <span>N <b>{{ d.nKgHa | faixaDose }}</b></span>
                      <span>P₂O₅ <b>{{ d.p2o5KgHa | faixaDose }}</b></span>
                      <span>K₂O <b>{{ d.k2oKgHa | faixaDose }}</b></span>
                    </span>
                    @if (e.detalhe) {
                      <span class="sub">{{ e.detalhe }}</span>
                    }
                  }
                  @if (e.safra; as s) {
                    <span class="valores">
                      <span>Esperada <b>{{ s.produtividadeEsperadaTonHa | numero: 2 : 0 }} t/ha</b></span>
                      <span>Colhida <b>{{ s.produtividadeColhidaTonHa | numero: 2 : 0 }} t/ha</b></span>
                      <span>Diferença <b>{{ diferenca(s.diferencaPercentual) }}</b></span>
                      @if (s.custoAdubacaoReais !== null) {
                        <span>Gasto com adubação <b>R$ {{ s.custoAdubacaoReais | numero: 2 }}</b></span>
                      }
                    </span>
                  }
                </a>
              </li>
            }
          </ol>
        } @else if (!historico.carregando()) {
          <app-estado-vazio
            icone="history"
            titulo="Nada neste período"
            [texto]="inicio() || fim() ? 'Amplie o período para ver mais eventos.' : 'Este talhão ainda não tem laudos, recomendações adotadas nem colheitas.'"
          />
        }
      </section>
    }
  `,
  styles: `
    .periodo { display: grid; grid-template-columns: repeat(2, minmax(0, 200px)); gap: 0 16px; }
    .periodo mat-form-field { width: 100%; }
    .linha-do-tempo { list-style: none; margin: 8px 0 0; padding: 0; }
    .linha-do-tempo li { position: relative; display: flex; gap: 14px; padding-bottom: 18px; }
    .linha-do-tempo li:not(:last-child)::before {
      content: ''; position: absolute; left: 17px; top: 36px; bottom: 0; width: 2px; background: var(--borda);
    }
    .marcador {
      display: grid; place-items: center; flex: none; width: 36px; height: 36px; border-radius: 50%;
      background: var(--azul-50); color: var(--azul-600);
    }
    .marcador mat-icon { font-size: 20px; width: 20px; height: 20px; }
    .adotada .marcador { background: var(--verde-100); color: var(--verde-800); }
    .evento {
      display: flex; flex-direction: column; gap: 6px; flex: 1; min-width: 0; padding: 6px 10px; margin: -6px 0 0;
      border-radius: var(--raio-pequeno); color: inherit; text-decoration: none;
    }
    .evento:hover { background: var(--superficie-2); }
    .cabecalho-evento { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 4px 12px; }
    .cabecalho-evento strong { font-weight: 600; }
    .valores { display: flex; flex-wrap: wrap; align-items: center; gap: 4px 14px; font-size: 0.85rem; color: var(--texto-suave); }
    .valores b { font-weight: 600; color: var(--texto); font-variant-numeric: tabular-nums; }
  `,
})
export class HistoricoTalhaoTela {
  readonly id = input.required<string>();

  protected readonly rotuloMetodoPh = ROTULO_METODO_PH;
  protected readonly diferenca = diferencaComSinal;
  protected readonly iconeDoEvento = { LAUDO: 'science', RECOMENDACAO_ADOTADA: 'task_alt', COLHEITA: 'agriculture' } as const;

  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly classeCultura = classeCultura;

  protected readonly inicio = signal('');
  protected readonly fim = signal('');

  private readonly consulta = computed(() => ({ id: Number(this.id()), inicio: this.inicio(), fim: this.fim() }));

  protected readonly historico = carga(
    () => this.consulta(),
    (c) => this.api.historicoTalhao(c.id, c.inicio, c.fim),
    null as HistoricoTalhao | null,
  );
}
