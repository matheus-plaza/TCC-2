import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { of, switchMap } from 'rxjs';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Cultura, Laudo, Recomendacao, RecomendacaoRequest, SistemaManejo, SugestaoAntecessora } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { Ajuda } from '../../shared/ajuda';
import { confirmar } from '../../shared/confirmacao-dialog';
import { EstadoVazio } from '../../shared/estado-vazio';
import { classeCultura, ROTULO_METODO_PH } from '../../shared/formato';
import { DataBrPipe } from '../../shared/pipes/data-br.pipe';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { TalhaoBusca } from '../../shared/talhao-busca';
import { aplicarErrosDeCampos, digitos, erroDeFormato } from '../../shared/validacao';
import { RecomendacaoProposta } from './recomendacao-proposta';

/** Valor do seletor para "Pousio/Palhada", enviado à API como antecessora nula. */
const POUSIO = 0;

/** Valores do seletor para a cultura anterior que não está cadastrada: enviam só o grupo. */
const OUTRA_GRAMINEA = -1;
const OUTRA_LEGUMINOSA = -2;

/**
 * Tela B e Tela C. A simulação (RF05) roda em memória, sem gravar. Ao adotar (RF06), o
 * cenário é registrado e recalculado no servidor, e só então muda para ADOTADA.
 */
@Component({
  selector: 'app-recomendacao-wizard',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TalhaoBusca,
    EstadoVazio,
    Ajuda,
    RecomendacaoProposta,
    NumeroPipe,
    DataBrPipe,
  ],
  templateUrl: './recomendacao-wizard.html',
  styleUrl: './recomendacao-wizard.scss',
})
export class RecomendacaoWizard {
  /** Laudo pré-selecionado pela URL, vindo da lista de laudos ou do painel. */
  readonly laudoIdUrl = input<string | undefined>(undefined, { alias: 'laudoId' });

  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  protected readonly auth = inject(AuthService);
  protected readonly classeCultura = classeCultura;
  protected readonly rotuloMetodoPh = ROTULO_METODO_PH;
  protected readonly POUSIO = POUSIO;
  protected readonly OUTRA_GRAMINEA = OUTRA_GRAMINEA;
  protected readonly OUTRA_LEGUMINOSA = OUTRA_LEGUMINOSA;

  protected readonly etapa = signal<'cenario' | 'resultado'>('cenario');
  protected readonly talhaoId = signal<number | null>(null);
  protected readonly laudoId = signal<number | null>(null);

  protected readonly culturas = carga(() => true, () => this.api.culturas(), [] as Cultura[]);
  /** A gessagem usa o laudo de 20-40 cm: a base da recomendação é sempre o de 0-20 cm. */
  protected readonly laudos = carga(
    () => this.talhaoId(),
    (talhaoId) => this.api.laudos({ talhaoId, profundidadeCm: '0-20' }),
    [] as Laudo[],
  );
  protected readonly sugestao = carga(
    () => this.talhaoId(),
    (id) => this.api.sugestaoAntecessora(id),
    null as SugestaoAntecessora | null,
  );

  protected readonly laudo = computed(() => this.laudos.valor().find((l) => l.id === this.laudoId()) ?? null);

  protected readonly form = this.fb.group({
    culturaAlvoId: [null as number | null, Validators.required],
    culturaAnteriorId: [POUSIO as number, Validators.required],
    sistemaManejo: ['PLANTIO_DIRETO' as SistemaManejo, Validators.required],
    expectativaRendimentoTonHa: [null as number | null, [Validators.required, Validators.min(0.01), digitos(3, 2)]],
    prntCalcario: [100 as number | null, [Validators.required, Validators.min(1), Validators.max(200), digitos(3, 2)]],
  });
  protected readonly erroDeFormato = erroDeFormato;

  protected readonly simulando = signal(false);
  protected readonly adotando = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erros = signal<string[]>([]);
  protected readonly pendencias = signal<string[]>([]);
  protected readonly resultado = signal<Recomendacao | null>(null);

  constructor() {
    // Laudo vindo da URL define o talhão e pré-seleciona o laudo
    effect(() => {
      const id = Number(this.laudoIdUrl());
      if (id) {
        untracked(() =>
          this.api.laudo(id).subscribe((l) => {
            this.talhaoId.set(l.talhaoId);
            this.laudoId.set(l.id);
          }),
        );
      }
    });

    // Sem seleção válida, usa o laudo mais recente do talhão
    effect(() => {
      const lista = this.laudos.valor();
      const atual = untracked(this.laudoId);
      if (!this.laudos.carregando() && !lista.some((l) => l.id === atual)) {
        this.laudoId.set(lista[0]?.id ?? null);
      }
    });

    // Crédito/débito (RF06): a última cultura adotada no talhão vira a antecessora sugerida
    effect(() => {
      const sugestao = this.sugestao.valor();
      this.form.controls.culturaAnteriorId.setValue(sugestao?.cultura.id ?? POUSIO);
    });
  }

  protected simular(): void {
    const req = this.requisicao();
    if (!req) {
      this.form.markAllAsTouched();
      return;
    }
    this.simulando.set(true);
    this.limparMensagens();
    this.api.simular(req).subscribe({
      next: (r) => {
        this.resultado.set(r);
        this.etapa.set('resultado');
        this.simulando.set(false);
        window.scrollTo({ top: 0 });
      },
      error: (e) => {
        this.apresentarErro(e);
        this.simulando.set(false);
      },
    });
  }

  protected voltar(): void {
    this.etapa.set('cenario');
    this.limparMensagens();
  }

  protected salvarSimulacao(): void {
    const req = this.requisicao();
    if (!req) {
      return;
    }
    this.salvando.set(true);
    this.limparMensagens();
    this.api.registrarSimulacao(req).subscribe({
      next: (r) => {
        this.snack.open('Simulação salva', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
        this.router.navigate(['/recomendacoes', r.id]);
      },
      error: (e) => {
        this.apresentarErro(e);
        this.salvando.set(false);
      },
    });
  }

  protected adotar(): void {
    const req = this.requisicao();
    if (!req) {
      return;
    }
    confirmar(this.dialog, {
      titulo: 'Adotar esta recomendação?',
      mensagem:
        'Ela entra no histórico do talhão e passa a definir o crédito ou débito de nitrogênio da próxima safra. Depois de adotada, não pode ser alterada nem excluída.',
      confirmar: 'Adotar',
    }).subscribe((ok) => {
      if (!ok) {
        return;
      }
      this.adotando.set(true);
      this.limparMensagens();
      this.api
        .registrarSimulacao(req)
        .pipe(switchMap((r) => (r.id === null ? of(r) : this.api.adotar(r.id))))
        .subscribe({
          next: (r) => {
            this.snack.open('Recomendação adotada', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
            this.router.navigate(['/recomendacoes', r.id]);
          },
          error: (e) => {
            this.apresentarErro(e);
            this.adotando.set(false);
          },
        });
    });
  }

  private requisicao(): RecomendacaoRequest | null {
    const laudoId = this.laudoId();
    if (!laudoId || this.form.invalid) {
      return null;
    }
    const v = this.form.getRawValue();
    return {
      analiseSoloId: laudoId,
      culturaAlvoId: v.culturaAlvoId!,
      culturaAnteriorId: v.culturaAnteriorId! > 0 ? v.culturaAnteriorId : null,
      grupoAnterior:
        v.culturaAnteriorId === OUTRA_GRAMINEA ? 'GRAMINEA' : v.culturaAnteriorId === OUTRA_LEGUMINOSA ? 'LEGUMINOSA' : null,
      sistemaManejo: v.sistemaManejo!,
      expectativaRendimentoTonHa: v.expectativaRendimentoTonHa!,
      prntCalcario: v.prntCalcario!,
    };
  }

  private apresentarErro(erro: unknown): void {
    aplicarErrosDeCampos(erro, this.form);
    const mensagens = mensagensDeErro(erro);
    const problema = (erro as { error?: { title?: string } }).error;
    if (problema?.title === 'Parâmetros NEPAR não cadastrados') {
      this.pendencias.set(mensagens);
    } else {
      this.erros.set(mensagens);
    }
    // A explicação fica no topo; o botão que a provocou está no fim da tela
    window.scrollTo({ top: 0 });
  }

  private limparMensagens(): void {
    this.erros.set([]);
    this.pendencias.set([]);
  }
}
