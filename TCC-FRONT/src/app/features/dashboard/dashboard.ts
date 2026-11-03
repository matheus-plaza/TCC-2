import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { Evolucao, PontoEvolucao, ResumoPainel, Safra } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { Ajuda } from '../../shared/ajuda';
import { EstadoVazio } from '../../shared/estado-vazio';
import { classeCultura, diferencaComSinal } from '../../shared/formato';
import { DataBrPipe, formatarData } from '../../shared/pipes/data-br.pipe';
import { FaixaDosePipe } from '../../shared/pipes/faixa-dose.pipe';
import { formatarNumero, NumeroPipe } from '../../shared/pipes/numero.pipe';
import { TalhaoBusca } from '../../shared/talhao-busca';

// Slot 1 da paleta categórica validada (validate_palette.js, modo claro). Cada gráfico tem
// uma única série e um único eixo: indicadores de escalas diferentes viram gráficos separados.
const SERIE = '#2a78d6';
// Slot 2 da mesma paleta: a colhida, ao lado da esperada, em ordem fixa
const SERIE_2 = '#eb6834';
const SUPERFICIE = '#ffffff';
const GRADE = '#e6ebe6';
const TEXTO_SECUNDARIO = '#5f6f63';

interface IndicadorGrafico {
  chave: keyof Pick<PontoEvolucao, 'v' | 'm' | 'mo' | 'p' | 'k'> | 'phCacl2' | 'phAgua';
  titulo: string;
  unidade: string;
  casas: number;
}

const INDICADORES: IndicadorGrafico[] = [
  { chave: 'v', titulo: 'Saturação por bases (V%)', unidade: '%', casas: 1 },
  { chave: 'phCacl2', titulo: 'pH (CaCl₂)', unidade: '', casas: 2 },
  { chave: 'phAgua', titulo: 'pH (H₂O)', unidade: '', casas: 2 },
  { chave: 'm', titulo: 'Saturação por alumínio (m%)', unidade: '%', casas: 1 },
  { chave: 'mo', titulo: 'Matéria orgânica', unidade: 'g/dm³', casas: 1 },
  { chave: 'p', titulo: 'Fósforo (P)', unidade: 'mg/dm³', casas: 1 },
  { chave: 'k', titulo: 'Potássio (K⁺)', unidade: 'cmolc/dm³', casas: 2 },
];

/** Os dois métodos de pH não são comparáveis: cada um tem a sua série. */
function valorDe(ponto: PontoEvolucao, chave: IndicadorGrafico['chave']): number | null {
  if (chave === 'phCacl2') {
    return ponto.metodoPh === 'CACL2' ? ponto.ph : null;
  }
  if (chave === 'phAgua') {
    return ponto.metodoPh === 'AGUA' ? ponto.ph : null;
  }
  return ponto[chave];
}

function rotuloDaSafra(s: Safra): string {
  const [ano, mes] = (s.dataColheita ?? s.dataAdocao).split('-');
  return `${s.cultura} ${mes}/${ano.substring(2)}`;
}

function serieDeBarras(rotulo: string, dados: (number | null)[], cor: string) {
  return {
    data: dados,
    label: rotulo,
    backgroundColor: cor,
    hoverBackgroundColor: cor,
    borderColor: SUPERFICIE,
    borderWidth: 2,
    borderRadius: 4,
    borderSkipped: 'bottom' as const,
    maxBarThickness: 28,
  };
}

function opcoesDeBarras(formato: (valor: number) => string, comLegenda: boolean): ChartConfiguration<'bar'>['options'] {
  return {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: { display: comLegenda, position: 'top', align: 'end', labels: { color: TEXTO_SECUNDARIO, boxWidth: 12, boxHeight: 12 } },
      tooltip: { callbacks: { label: (item) => `${item.dataset.label}: ${formato(item.parsed.y ?? 0)}` } },
    },
    scales: {
      x: { grid: { display: false }, ticks: { color: TEXTO_SECUNDARIO } },
      y: {
        beginAtZero: true,
        grid: { color: GRADE },
        border: { display: false },
        ticks: { color: TEXTO_SECUNDARIO, maxTicksLimit: 5, callback: (valor) => formatarNumero(Number(valor), 2, 0) },
      },
    },
  };
}

/** UC10: painel de indicadores e evolução da fertilidade do talhão. */
@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    BaseChartDirective,
    MatIconModule,
    MatButtonModule,
    MatExpansionModule,
    TalhaoBusca,
    EstadoVazio,
    Ajuda,
    NumeroPipe,
    FaixaDosePipe,
    DataBrPipe,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  /** Talhão aberto por um link direto (detalhe da propriedade, por exemplo). */
  readonly talhaoIdUrl = input<string | undefined>(undefined, { alias: 'talhaoId' });

  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly classeCultura = classeCultura;
  protected readonly valorDe = valorDe;
  protected readonly diferenca = diferencaComSinal;

  protected readonly resumo = carga(() => true, () => this.api.resumoPainel(), null as ResumoPainel | null);
  protected readonly talhaoId = signal<number | null>(null);
  protected readonly evolucao = carga(() => this.talhaoId(), (id) => this.api.evolucao(id), null as Evolucao | null);

  protected readonly vazio = computed(() => {
    const r = this.resumo.valor();
    return r !== null && r.propriedades === 0;
  });

  /** Um método de pH que o talhão nunca usou não ganha gráfico nem coluna. */
  protected readonly indicadores = computed(() => {
    const pontos = this.evolucao.valor()?.pontos ?? [];
    return INDICADORES.filter((i) => pontos.some((p) => valorDe(p, i.chave) !== null));
  });

  protected readonly graficos = computed(() => {
    const pontos = this.evolucao.valor()?.pontos ?? [];
    const rotulos = pontos.map((p) => {
      const [ano, mes] = p.data.split('-');
      return `${mes}/${ano.substring(2)}`;
    });
    return this.indicadores().map((indicador) => ({
      ...indicador,
      ultimo: pontos.map((p) => valorDe(p, indicador.chave)).filter((v) => v !== null).pop() ?? null,
      dados: {
        labels: rotulos,
        datasets: [
          {
            data: pontos.map((p) => valorDe(p, indicador.chave)),
            spanGaps: true,
            label: indicador.titulo,
            borderColor: SERIE,
            backgroundColor: SERIE,
            borderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6,
            pointBorderColor: SUPERFICIE,
            pointBorderWidth: 2,
            tension: 0,
          },
        ],
      } satisfies ChartConfiguration<'line'>['data'],
      opcoes: this.opcoesDoGrafico(indicador),
    }));
  });

  /** Esperada e colhida são a mesma medida: ficam lado a lado, no mesmo eixo. */
  protected readonly graficoColheita = computed(() => {
    const safras = (this.evolucao.valor()?.safras ?? []).filter((s) => s.produtividadeColhidaTonHa !== null);
    return {
      possui: safras.length > 0,
      dados: {
        labels: safras.map(rotuloDaSafra),
        datasets: [
          serieDeBarras('Esperada', safras.map((s) => s.produtividadeEsperadaTonHa), SERIE),
          serieDeBarras('Colhida', safras.map((s) => s.produtividadeColhidaTonHa), SERIE_2),
        ],
      } satisfies ChartConfiguration<'bar'>['data'],
      opcoes: opcoesDeBarras((valor) => `${formatarNumero(valor, 2, 0)} t/ha`, true),
    };
  });

  /** O gasto é comparado por hectare, para valer também entre talhões de áreas diferentes. */
  protected readonly graficoGasto = computed(() => {
    const safras = (this.evolucao.valor()?.safras ?? []).filter((s) => s.custoPorHectareReais !== null);
    return {
      possui: safras.length > 0,
      dados: {
        labels: safras.map(rotuloDaSafra),
        datasets: [serieDeBarras('Gasto por hectare', safras.map((s) => s.custoPorHectareReais), SERIE)],
      } satisfies ChartConfiguration<'bar'>['data'],
      opcoes: opcoesDeBarras((valor) => `R$ ${formatarNumero(valor, 2)}`, false),
    };
  });

  constructor() {
    effect(() => {
      const daUrl = Number(this.talhaoIdUrl());
      if (daUrl) {
        this.talhaoId.set(daUrl);
        return;
      }
      // Sem link direto, abre no talhão com o laudo mais recente
      const recentes = this.resumo.valor()?.laudosRecentes ?? [];
      if (recentes.length && untracked(this.talhaoId) === null) {
        this.talhaoId.set(recentes[0].talhaoId);
      }
    });
  }

  private opcoesDoGrafico(indicador: IndicadorGrafico): ChartConfiguration<'line'>['options'] {
    return {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { display: false },
        tooltip: {
          displayColors: false,
          callbacks: {
            title: (itens) => {
              const ponto = this.evolucao.valor()?.pontos[itens[0].dataIndex];
              return ponto ? formatarData(ponto.data) : '';
            },
            label: (item) => `${indicador.titulo}: ${formatarNumero(item.parsed.y, indicador.casas)} ${indicador.unidade}`.trim(),
          },
        },
      },
      scales: {
        x: { grid: { display: false }, ticks: { color: TEXTO_SECUNDARIO } },
        y: {
          grid: { color: GRADE },
          border: { display: false },
          grace: '10%',
          ticks: {
            color: TEXTO_SECUNDARIO,
            maxTicksLimit: 5,
            callback: (valor) => formatarNumero(Number(valor), indicador.casas, 0),
          },
        },
      },
    };
  }
}
