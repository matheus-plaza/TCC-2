import { Component, computed, inject, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { Laudo, Recomendacao } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { Ajuda } from '../../shared/ajuda';
import { confirmar } from '../../shared/confirmacao-dialog';
import { classeCultura, classeNivel } from '../../shared/formato';
import { DataBrPipe } from '../../shared/pipes/data-br.pipe';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';

/** RF04: indicadores calculados e enquadramento dos teores em classes de fertilidade. */
@Component({
  selector: 'app-laudo-detalhe',
  imports: [RouterLink, MatIconModule, MatButtonModule, Ajuda, NumeroPipe, DataBrPipe],
  template: `
    <a class="voltar" routerLink="/laudos"><mat-icon>arrow_back</mat-icon> Laudos de solo</a>

    @if (laudo.valor(); as l) {
      <header class="pagina-cabecalho">
        <div>
          <h1>Laudo de {{ l.dataAnalise | dataBr }} ({{ l.profundidadeCm }} cm)</h1>
          <p>{{ l.talhaoIdentificacao }} · {{ l.propriedadeNome }} · {{ l.produtorNome }}</p>
        </div>
        <div class="acoes">
          <a mat-stroked-button [routerLink]="['/propriedades/talhoes', l.talhaoId, 'historico']">
            <mat-icon>history</mat-icon> Histórico do talhão
          </a>
          <button mat-stroked-button (click)="excluir(l)"><mat-icon>delete</mat-icon> Excluir</button>
          @if (!auth.produtor() && l.profundidadeCm === '0-20') {
            <a mat-flat-button routerLink="/recomendacoes/nova" [queryParams]="{ laudoId: l.id }">
              <mat-icon>calculate</mat-icon> Gerar recomendação
            </a>
          }
        </div>
      </header>

      @if (l.profundidadeCm === '20-40') {
        <div class="aviso">
          <mat-icon>info</mat-icon>
          <div>
            Laudo da camada de 20-40 cm: é usado para avaliar a gessagem nas recomendações deste talhão. A calagem e a
            adubação partem do laudo de 0-20 cm.
          </div>
        </div>
      }

      <section class="cartao">
        <h2><mat-icon>insights</mat-icon> Valores calculados</h2>
        <div class="indicadores">
          <div><span>Soma de Bases (SB) <app-ajuda termo="SB" /></span><strong>{{ l.indicadores.somaBases | numero }}</strong></div>
          <div><span>CTC Efetiva (t) <app-ajuda termo="t" /></span><strong>{{ l.indicadores.ctcEfetiva | numero }}</strong></div>
          <div><span>CTC Total (T) <app-ajuda termo="CTC" /></span><strong>{{ l.indicadores.ctcTotal | numero }}</strong></div>
          <div><span>Saturação por Bases (V%) <app-ajuda termo="V" /></span><strong>{{ l.indicadores.saturacaoBases | numero: 1 }}%</strong></div>
          <div><span>Saturação por Al (m%) <app-ajuda termo="m" /></span><strong>{{ l.indicadores.saturacaoAluminio | numero: 1 }}%</strong></div>
        </div>
      </section>

      <section class="cartao">
        <h2><mat-icon>rule</mat-icon> Interpretação da fertilidade</h2>
        <div class="tabela-container">
          <table class="lista">
            <thead>
              <tr>
                <th>Atributo</th>
                <th class="numero">Valor</th>
                <th>Unidade</th>
                <th>Classe</th>
              </tr>
            </thead>
            <tbody>
              @for (i of l.interpretacao; track i.atributo) {
                <tr>
                  <td class="principal" data-rotulo="Atributo">{{ i.rotulo }}</td>
                  <td class="numero" data-rotulo="Valor">{{ i.valor | numero: 3 : 1 }}</td>
                  <td data-rotulo="Unidade">{{ i.unidade || '—' }}</td>
                  <td data-rotulo="Classe">
                    @if (i.classe) {
                      <span [class]="classeNivel(i.classe)">{{ i.classe }}</span>
                    } @else {
                      <span class="texto-suave">Sem faixa cadastrada</span>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        <p class="nota">
          Argila: {{ l.argilaPorcentagem | numero: 2 : 0 }}%. A classe de fósforo considera a argila e a de potássio
          considera a CTC. Faixas da versão vigente das tabelas paramétricas.
        </p>
      </section>

      @if (recomendacoes.valor().length) {
        <section class="cartao">
          <h2><mat-icon>assignment</mat-icon> Recomendações deste laudo</h2>
          <ul class="recomendacoes">
            @for (r of recomendacoes.valor(); track r.id) {
              <li>
                <a [routerLink]="['/recomendacoes', r.id]">
                  <span [class]="classeCultura(r.culturaAlvo.nome)">{{ r.culturaAlvo.nome }}</span>
                  <span class="sub">{{ r.dataAdocao ?? r.dataGeracao | dataBr }}</span>
                  @if (r.status === 'ADOTADA') {
                    <span class="selo-status adotada">Adotada</span>
                  } @else {
                    <span class="selo-status simulacao">Simulação</span>
                  }
                  <mat-icon class="seta">chevron_right</mat-icon>
                </a>
              </li>
            }
          </ul>
        </section>
      }
    }
  `,
  styles: `
    .indicadores {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 12px 20px;
    }
    .indicadores div { display: flex; flex-direction: column; gap: 2px; }
    .indicadores span { font-size: 0.8rem; color: var(--texto-suave); }
    .indicadores strong { font-size: 1.4rem; font-weight: 600; color: var(--verde-900); font-variant-numeric: tabular-nums; }
    .nota { margin: 12px 0 0; font-size: 0.82rem; color: var(--texto-suave); }
    .recomendacoes { list-style: none; margin: 0; padding: 0; }
    .recomendacoes li + li { border-top: 1px solid var(--borda); }
    .recomendacoes a {
      display: flex; align-items: center; gap: 12px; padding: 10px 4px; border-radius: 8px;
      color: inherit; text-decoration: none;
    }
    .recomendacoes a:hover { background: var(--superficie-2); }
    .recomendacoes .sub { flex: 1; }
    .seta { color: var(--texto-suave); }
  `,
})
export class LaudoDetalhe {
  readonly id = input.required<string>();

  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  protected readonly auth = inject(AuthService);
  protected readonly classeNivel = classeNivel;
  protected readonly classeCultura = classeCultura;

  private readonly idNumerico = computed(() => Number(this.id()));

  protected readonly laudo = carga(() => this.idNumerico(), (id) => this.api.laudo(id), null as Laudo | null);
  protected readonly recomendacoes = carga(
    () => this.idNumerico(),
    (laudoId) => this.api.recomendacoes({ laudoId }),
    [] as Recomendacao[],
  );

  protected excluir(l: Laudo): void {
    confirmar(this.dialog, {
      titulo: 'Excluir laudo',
      mensagem: 'Excluir este laudo? Um laudo usado em recomendações não pode ser excluído, para preservar o histórico.',
      confirmar: 'Excluir',
    }).subscribe((ok) => {
      if (ok) {
        this.api.excluirLaudo(l.id).subscribe(() => {
          this.snack.open('Laudo excluído', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
          this.router.navigate(['/laudos']);
        });
      }
    });
  }
}
