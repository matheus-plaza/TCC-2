import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { TermoGlossario } from '../../core/glossario';
import { FaixaDose, Recomendacao } from '../../core/models/api.models';
import { Ajuda } from '../../shared/ajuda';
import { classeNivel } from '../../shared/formato';
import { formatarFaixa } from '../../shared/pipes/faixa-dose.pipe';
import { formatarNumero, NumeroPipe } from '../../shared/pipes/numero.pipe';

interface Dose {
  sigla: string;
  nome: string;
  porHa: FaixaDose | null;
  unidade: 't' | 'kg';
  detalhe?: string | null;
  naoAvaliado?: boolean;
  ajuda?: TermoGlossario;
}

function fixa(valor: number | null): FaixaDose | null {
  return valor === null ? null : { min: valor, max: valor };
}

/**
 * Tela C: a recomendação proposta, com o diagnóstico do solo e as doses por hectare e para
 * a área do talhão. Usada no resultado da simulação e no receituário registrado.
 */
@Component({
  selector: 'app-recomendacao-proposta',
  imports: [NgTemplateOutlet, MatIconModule, MatExpansionModule, Ajuda, NumeroPipe],
  template: `
    @let r = recomendacao();

    <section class="bloco">
      <h3>Diagnóstico do solo</h3>
      <div class="diagnostico">
        <div class="medidor">
          <div class="medidor-topo">
            <span>Saturação por bases (V%) <app-ajuda termo="V" /></span>
            <strong>{{ r.resumoQuimico.v1Porcentagem | numero: 1 }}%</strong>
          </div>
          <div class="trilho" role="img"
               [attr.aria-label]="'V% atual ' + (r.resumoQuimico.v1Porcentagem | numero: 1) + '%, alvo ' + (r.calagem.v2AlvoPorcentagem | numero: 1) + '%'">
            <div class="preenchido" [class.ok]="!r.calagem.necessario" [style.width.%]="limitar(r.resumoQuimico.v1Porcentagem)"></div>
            <div class="alvo" [style.left.%]="limitar(r.calagem.v2AlvoPorcentagem)">
              <span>alvo {{ r.calagem.v2AlvoPorcentagem | numero: 1 : 0 }}%</span>
            </div>
          </div>
          <p class="explicacao">
            {{ r.calagem.justificativa }}
          </p>
        </div>
        <div class="indicadores">
          <div class="indicador">
            <span class="rotulo">CTC Total (T) <app-ajuda termo="CTC" /></span>
            <strong>{{ r.resumoQuimico.ctcTotal | numero }}</strong> <small>cmolc/dm³</small>
          </div>
          <div class="indicador">
            <span class="rotulo">Soma de bases <app-ajuda termo="SB" /></span>
            <strong>{{ r.resumoQuimico.somaBases | numero }}</strong> <small>cmolc/dm³</small>
          </div>
          <div class="indicador">
            <span class="rotulo">Saturação por Al <app-ajuda termo="m" /></span>
            <strong>{{ r.resumoQuimico.mPorcentagem | numero: 1 }}%</strong>
          </div>
          <div class="indicador">
            <span class="rotulo">Fósforo no solo</span>
            <span [class]="classeNivel(r.adubacao.classeFosforo)">{{ r.adubacao.classeFosforo }}</span>
          </div>
          <div class="indicador">
            <span class="rotulo">Potássio no solo</span>
            <span [class]="classeNivel(r.adubacao.classePotassio)">{{ r.adubacao.classePotassio }}</span>
          </div>
          <div class="indicador">
            <span class="rotulo">Matéria orgânica</span>
            <span [class]="classeNivel(r.adubacao.classeMateriaOrganica)">{{ r.adubacao.classeMateriaOrganica }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="bloco">
      <div class="cabecalho-bloco">
        <h3>1. Correção do solo</h3>
        @if (area(); as a) {
          <span class="area"><mat-icon>crop_free</mat-icon> totais para {{ a | numero: 2 : 0 }} ha</span>
        }
      </div>
      <div class="corretivos">
        @for (d of corretivos(); track d.sigla) {
          <ng-container [ngTemplateOutlet]="cartao" [ngTemplateOutletContext]="{ $implicit: d, grande: true }" />
        }
      </div>
    </section>

    <section class="bloco">
      <h3>2. Adubação recomendada (NPK)</h3>
      <p class="texto-suave">O manual indica as doses em faixas. A definição dentro da faixa cabe ao agrônomo.</p>
      <div class="nutrientes">
        @for (d of nutrientes(); track d.sigla) {
          <ng-container [ngTemplateOutlet]="cartao" [ngTemplateOutletContext]="{ $implicit: d, grande: false }" />
        }
      </div>
    </section>

    <section class="bloco">
      <h3>3. Micronutrientes</h3>
      <div class="nutrientes">
        @for (d of micronutrientes(); track d.sigla) {
          <ng-container [ngTemplateOutlet]="cartao" [ngTemplateOutletContext]="{ $implicit: d, grande: false }" />
        }
      </div>
    </section>

    @if (r.avisos.length) {
      <div class="aviso">
        <mat-icon>info</mat-icon>
        <div>
          <strong>Observações do cálculo</strong>
          <ul>
            @for (a of r.avisos; track $index) {
              <li>{{ a }}</li>
            }
          </ul>
        </div>
      </div>
    }

    <mat-expansion-panel class="memoria">
      <mat-expansion-panel-header>
        <mat-panel-title><mat-icon>functions</mat-icon>&nbsp; Memória de cálculo (passo a passo)</mat-panel-title>
      </mat-expansion-panel-header>
      <ol>
        @for (linha of r.memoriaCalculo; track $index) {
          <li>{{ linha }}</li>
        }
      </ol>
      <p class="versao">Tabelas paramétricas: versão {{ r.versaoParametros.codigo }}</p>
    </mat-expansion-panel>

    <ng-template #cartao let-d let-grande="grande">
      <div class="dose" [class.grande]="grande" [class.zero]="d.porHa?.max === 0" [class.na]="d.naoAvaliado">
        <div class="dose-topo">
          <span class="sigla" [attr.data-sigla]="d.sigla">{{ d.sigla }}</span>
          <span class="dose-nome">{{ d.nome }}
            @if (d.ajuda) { <app-ajuda [termo]="d.ajuda" /> }
          </span>
        </div>
        @if (d.naoAvaliado) {
          <div class="dose-estado"><mat-icon>remove_circle_outline</mat-icon> Não avaliado</div>
        } @else if (d.porHa?.max === 0) {
          <div class="dose-valor">0,00 <small>{{ d.unidade }}/ha</small></div>
          <div class="dose-estado"><mat-icon>check_circle</mat-icon> Não necessário</div>
        } @else {
          <div class="dose-valor">{{ valor(d) }} <small>{{ d.unidade }}/ha</small></div>
          @if (total(d); as t) {
            <div class="dose-total">{{ t }} {{ d.unidade }} no talhão</div>
          }
        }
        @if (d.detalhe) {
          <div class="dose-detalhe">{{ d.detalhe }}</div>
        }
      </div>
    </ng-template>
  `,
  styleUrl: './recomendacao-proposta.scss',
})
export class RecomendacaoProposta {
  readonly recomendacao = input.required<Recomendacao>();

  protected readonly classeNivel = classeNivel;
  protected readonly area = computed(() => this.recomendacao().talhao?.areaHectares ?? null);

  protected readonly corretivos = computed<Dose[]>(() => {
    const r = this.recomendacao();
    return [
      {
        sigla: 'Cal',
        nome: `Calcário (PRNT ${formatarNumero(r.prntCalcario, 1, 0)}%)`,
        porHa: fixa(r.calagem.necessidadeCalcarioTonHa),
        unidade: 't',
        detalhe: r.calagem.observacao,
        ajuda: 'PRNT',
      },
      {
        sigla: 'Ges',
        nome: 'Gesso agrícola',
        porHa: fixa(r.gessagem.doseKgHa),
        unidade: 'kg',
        detalhe: r.gessagem.justificativa,
        naoAvaliado: !r.gessagem.avaliada,
        ajuda: 'gesso',
      },
    ];
  });

  protected readonly nutrientes = computed<Dose[]>(() => {
    const { adubacao: a } = this.recomendacao();
    return [
      { sigla: 'N', nome: 'Nitrogênio (N)', porHa: a.doseNkgHa, unidade: 'kg', detalhe: a.justificativaN },
      { sigla: 'P', nome: 'Fósforo (P₂O₅)', porHa: a.doseP2O5kgHa, unidade: 'kg', detalhe: a.justificativaP },
      { sigla: 'K', nome: 'Potássio (K₂O)', porHa: a.doseK2OkgHa, unidade: 'kg', detalhe: a.justificativaK },
    ];
  });

  protected readonly micronutrientes = computed<Dose[]>(() =>
    this.recomendacao().micronutrientes.map((m) => ({
      sigla: m.simbolo,
      nome: m.nutriente,
      porHa: fixa(m.doseKgHa),
      unidade: 'kg',
      detalhe: m.justificativa,
      naoAvaliado: !m.avaliado,
    })),
  );

  protected valor(d: Dose): string {
    return d.porHa === null ? '' : formatarFaixa(d.porHa, 2);
  }

  /** Total para a área do talhão: a conta é só de apresentação, sobre a dose já calculada. */
  protected total(d: Dose): string | null {
    const area = this.area();
    if (d.porHa === null || !area) {
      return null;
    }
    const casas = d.unidade === 't' ? 2 : 0;
    return formatarFaixa({ min: d.porHa.min * area, max: d.porHa.max * area }, casas);
  }

  protected limitar(v: number): number {
    return Math.max(0, Math.min(100, v));
  }
}
