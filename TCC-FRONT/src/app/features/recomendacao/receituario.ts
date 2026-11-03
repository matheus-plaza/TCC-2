import { Component, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { Recomendacao } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { SistemaService } from '../../core/services/sistema.service';
import { confirmar } from '../../shared/confirmacao-dialog';
import { baixarArquivo, classeCultura, ROTULO_MANEJO } from '../../shared/formato';
import { DataBrPipe } from '../../shared/pipes/data-br.pipe';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { RecomendacaoProposta } from './recomendacao-proposta';
import { SafraCard } from './safra-card';

/** Visualizador do receituário registrado: adoção (RF06) e exportação em PDF (RF08). */
@Component({
  selector: 'app-receituario',
  imports: [RouterLink, MatIconModule, MatButtonModule, RecomendacaoProposta, SafraCard, NumeroPipe, DataBrPipe],
  template: `
    <a class="voltar" routerLink="/recomendacoes"><mat-icon>arrow_back</mat-icon> Recomendações</a>

    @if (recomendacao.valor(); as r) {
      <header class="pagina-cabecalho">
        <div>
          <h1>{{ r.status === 'ADOTADA' ? 'Receituário agronômico' : 'Simulação' }} nº {{ r.id }}</h1>
          <p>{{ r.talhao?.identificacao }} · {{ r.talhao?.propriedadeNome }} · {{ r.talhao?.produtorNome }}</p>
          @if (r.status === 'ADOTADA') {
            <p>Adotado pelo engenheiro agrônomo {{ r.responsavel?.nome ?? 'responsável' }} em {{ r.dataAdocao | dataBr }}</p>
          } @else {
            <p>Ainda não adotada por um engenheiro agrônomo: não vale como receituário.</p>
          }
        </div>
        <div class="acoes">
          @if (r.status === 'ADOTADA') {
            <button mat-flat-button (click)="exportar(r)" [disabled]="exportando()">
              <mat-icon>picture_as_pdf</mat-icon> Exportar receituário (PDF)
            </button>
          } @else if (auth.agronomo()) {
            <button mat-stroked-button (click)="descartar(r)"><mat-icon>delete</mat-icon> Descartar</button>
            <button mat-flat-button (click)="adotar(r)"><mat-icon>task_alt</mat-icon> Adotar recomendação</button>
          }
        </div>
      </header>

      <section class="cartao">
        <div class="meta">
          <div>
            <span>Status</span>
            <strong>
              @if (r.status === 'ADOTADA') {
                <span class="selo-status adotada"><mat-icon>task_alt</mat-icon> Adotada</span>
              } @else {
                <span class="selo-status simulacao"><mat-icon>edit_note</mat-icon> Simulação</span>
              }
            </strong>
          </div>
          <div><span>{{ r.status === 'ADOTADA' ? 'Adotada em' : 'Gerada em' }}</span><strong>{{ r.dataAdocao ?? r.dataGeracao | dataBr }}</strong></div>
          <div><span>Cultura alvo</span><strong><span [class]="classeCultura(r.culturaAlvo.nome)">{{ r.culturaAlvo.nome }}</span></strong></div>
          <div><span>Antecessora</span><strong>{{ r.antecessora }}</strong></div>
          <div><span>Manejo</span><strong>{{ rotuloManejo[r.sistemaManejo] }}</strong></div>
          <div><span>Rendimento esperado</span><strong>{{ r.expectativaRendimentoTonHa | numero: 2 : 0 }} t/ha</strong></div>
          <div><span>Laudo</span><strong><a [routerLink]="['/laudos', r.analiseSoloId]">{{ r.laudo.dataAnalise | dataBr }}</a></strong></div>
          <div>
            <span>Responsável técnico</span>
            <strong>
              {{ r.responsavel?.nome ?? '—' }}
              @if (r.responsavel?.registroProfissional) { <span class="sub">CREA {{ r.responsavel?.registroProfissional }}</span> }
            </strong>
          </div>
        </div>
      </section>

      @if (r.status === 'ADOTADA') {
        <app-safra-card [recomendacaoId]="r.id!" />
      }

      <!-- A faixa do topo já avisa quando a versão vigente não foi conferida; aqui fica o caso de um registro antigo -->
      @if (!r.versaoParametros.conferidaManual && !sistema.parametrosNaoConferidos()) {
        <div class="aviso alerta">
          <mat-icon>science</mat-icon>
          <div>
            Calculada com a versão {{ r.versaoParametros.codigo }} das tabelas, ainda não conferida com o Manual NEPAR.
            As doses são ilustrativas e não devem ser aplicadas em campo.
          </div>
        </div>
      }

      <section class="cartao">
        <app-recomendacao-proposta [recomendacao]="r" />
      </section>

      <p class="nota-legal">
        Instrumento de apoio ao cálculo. Não substitui a avaliação técnica presencial e a assinatura de um engenheiro agrônomo.
      </p>
    }
  `,
  styles: `
    .nota-legal { margin: 0; font-size: 0.8rem; color: var(--texto-suave); }
  `,
})
export class Receituario {
  readonly id = input.required<string>();

  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  protected readonly auth = inject(AuthService);
  protected readonly sistema = inject(SistemaService);
  protected readonly classeCultura = classeCultura;
  protected readonly rotuloManejo = ROTULO_MANEJO;
  protected readonly exportando = signal(false);

  private readonly idNumerico = computed(() => Number(this.id()));

  protected readonly recomendacao = carga(() => this.idNumerico(), (id) => this.api.recomendacao(id), null as Recomendacao | null);

  protected exportar(r: Recomendacao): void {
    this.exportando.set(true);
    this.api.receituarioPdf(r.id!).subscribe({
      next: (pdf) => {
        baixarArquivo(pdf, `receituario-${r.id}.pdf`);
        this.exportando.set(false);
      },
      error: () => this.exportando.set(false),
    });
  }

  protected adotar(r: Recomendacao): void {
    confirmar(this.dialog, {
      titulo: 'Adotar esta recomendação?',
      mensagem:
        'Ela entra no histórico do talhão e passa a definir o crédito ou débito de nitrogênio da próxima safra. Depois de adotada, não pode ser alterada nem excluída.',
      confirmar: 'Adotar',
    }).subscribe((ok) => {
      if (ok) {
        this.api.adotar(r.id!).subscribe(() => {
          this.snack.open('Recomendação adotada', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
          this.recomendacao.recarregar();
        });
      }
    });
  }

  protected descartar(r: Recomendacao): void {
    confirmar(this.dialog, {
      titulo: 'Descartar simulação',
      mensagem: 'A simulação salva será excluída. O laudo continua registrado.',
      confirmar: 'Descartar',
    }).subscribe((ok) => {
      if (ok) {
        this.api.excluirRecomendacao(r.id!).subscribe(() => {
          this.snack.open('Simulação descartada', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
          this.router.navigate(['/recomendacoes']);
        });
      }
    });
  }
}
