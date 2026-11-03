import { Component, computed, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { ParametrosVigentes } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { carga } from '../../core/services/carga';
import { classeNivel } from '../../shared/formato';
import { formatarFaixa } from '../../shared/pipes/faixa-dose.pipe';
import { formatarNumero, NumeroPipe } from '../../shared/pipes/numero.pipe';

type Faixa = ParametrosVigentes['faixas'][number];
type RegraNpk = ParametrosVigentes['npk'][number];

interface TabelaNpk {
  titulo: string;
  rotuloLinhas: string;
  colunas: string[];
  linhas: { rotulo: string; celulas: string[] }[];
}

function faixaTexto(min: number | null, max: number | null): string {
  if (min === null && max === null) {
    return 'Qualquer';
  }
  if (max === null) {
    return `a partir de ${formatarNumero(min, 3, 0)}`;
  }
  if (min === null) {
    return `abaixo de ${formatarNumero(max, 3, 0)}`;
  }
  return `${formatarNumero(min, 3, 0)} a < ${formatarNumero(max, 3, 0)}`;
}

function celulaNpk(r: RegraNpk): string {
  return r.doseMin === null || r.doseMax === null ? 'Inviável' : formatarFaixa({ min: r.doseMin, max: r.doseMax });
}

function rotuloDaLinha(r: RegraNpk): string {
  return [r.grupoAntecessor ? `Anterior: ${r.grupoAntecessor}` : null, r.classe].filter(Boolean).join(' · ');
}

function rotuloDasLinhas(r: RegraNpk): string {
  if (r.nutriente !== 'N') {
    return 'No solo';
  }
  return r.classe ? 'Cultura anterior · M.O. no solo' : 'Cultura anterior';
}

const ORDEM_NUTRIENTES = ['N', 'P₂O₅', 'K₂O'];
const ORDEM_CLASSES = ['Muito baixo', 'Baixo', 'Médio', 'Alto', 'Muito alto', 'Condição a evitar'];

/** Mesma ordem das tabelas do livro, para facilitar a conferência. */
function ordemDoManual(a: RegraNpk, b: RegraNpk): number {
  return (
    ORDEM_NUTRIENTES.indexOf(a.nutriente) - ORDEM_NUTRIENTES.indexOf(b.nutriente) ||
    (a.grupoAntecessor ?? '').localeCompare(b.grupoAntecessor ?? '') ||
    ORDEM_CLASSES.indexOf(a.classe ?? '') - ORDEM_CLASSES.indexOf(b.classe ?? '') ||
    (a.rendimentoMin ?? 0) - (b.rendimentoMin ?? 0)
  );
}

/** Consulta das tabelas paramétricas vigentes: é o que define as recomendações (RNF02, RNF06). */
@Component({
  selector: 'app-parametros',
  imports: [MatIconModule, MatTabsModule, NumeroPipe],
  template: `
    <header class="pagina-cabecalho">
      <div>
        <h1>Tabelas NEPAR</h1>
        <p>Valores em uso pelo motor de cálculo. As faixas incluem o mínimo e excluem o máximo.</p>
      </div>
    </header>

    @if (parametros.valor(); as p) {
      @if (p.versao; as v) {
        <div class="aviso" [class.alerta]="!v.conferidaManual">
          <mat-icon>{{ v.conferidaManual ? 'verified' : 'science' }}</mat-icon>
          <div>
            <strong>Versão {{ v.codigo }}</strong>
            <p>{{ v.descricao }}</p>
          </div>
        </div>
      }

      <section class="cartao">
        <mat-tab-group>
          <mat-tab label="Culturas e correção">
            <div class="aba">
              <h3>Culturas</h3>
              <div class="tabela-container">
                <table class="lista">
                  <thead>
                    <tr>
                      <th>Cultura</th>
                      <th>Grupo</th>
                      <th class="numero">Aplicar calcário se V% abaixo de</th>
                      <th class="numero">Elevar V% para</th>
                      <th class="numero">pH ideal</th>
                    </tr>
                  </thead>
                  <tbody>
                    @for (c of p.culturas; track c.id) {
                      <tr>
                        <td class="principal" data-rotulo="Cultura">{{ c.nome }}</td>
                        <td data-rotulo="Grupo">{{ c.grupo === 'LEGUMINOSA' ? 'Leguminosa' : 'Gramínea' }}</td>
                        <td class="numero" data-rotulo="Aplicar calcário se V% abaixo de">{{ c.vMinimo | numero: 1 }}</td>
                        <td class="numero" data-rotulo="Elevar V% para">{{ c.vAlvo | numero: 1 }}</td>
                        <td class="numero" data-rotulo="pH ideal">{{ c.phMinIdeal | numero }} a {{ c.phMaxIdeal | numero }}</td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>

              <h3>Aplicação do calcário por manejo</h3>
              <div class="tabela-container">
                <table class="lista">
                  <thead>
                    <tr><th>Manejo</th><th>Aplicação</th></tr>
                  </thead>
                  <tbody>
                    @for (c of p.calagem; track $index) {
                      <tr>
                        <td class="principal" data-rotulo="Manejo">{{ c.manejo }}</td>
                        <td data-rotulo="Aplicação">{{ c.observacao }}</td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>

              @if (p.gessagem; as g) {
                <h3>Gessagem (camada de 20-40 cm)</h3>
                <p class="regra">
                  Indicada se a saturação por alumínio for maior que {{ g.limiteMPorcentagem | numero: 1 : 0 }}%
                  ou se Ca²⁺ for menor que {{ g.limiteCaCmolc | numero: 1 }} cmolc/dm³.
                </p>
                <div class="tabela-container">
                  <table class="lista">
                    <thead>
                      <tr><th>Argila (%)</th><th class="numero">Gesso (kg/ha)</th></tr>
                    </thead>
                    <tbody>
                      @for (d of g.doses; track $index) {
                        <tr>
                          <td data-rotulo="Argila (%)">{{ faixa(d.argilaMin, d.argilaMax) }}</td>
                          <td class="numero" data-rotulo="Gesso (kg/ha)">{{ d.doseKgHa | numero: 0 }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </div>
          </mat-tab>

          <mat-tab label="Classes de fertilidade">
            <div class="aba">
              @for (grupo of faixasPorAtributo(); track grupo.rotulo) {
                <h3>{{ grupo.rotulo }} @if (grupo.unidade) { <small>({{ grupo.unidade }})</small> }</h3>
                <div class="tabela-container">
                  <table class="lista">
                    <thead>
                      <tr>
                        @if (grupo.temCriterio) { <th>Argila (%)</th> }
                        <th>Teor</th>
                        <th>Classe</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (f of grupo.faixas; track $index) {
                        <tr>
                          @if (grupo.temCriterio) {
                            <td data-rotulo="Argila (%)">{{ faixa(f.criterioMin, f.criterioMax) }}</td>
                          }
                          <td data-rotulo="Teor">{{ faixa(f.teorMin, f.teorMax) }}</td>
                          <td data-rotulo="Classe"><span [class]="classeNivel(f.classe)">{{ f.classe }}</span></td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </div>
          </mat-tab>

          <mat-tab label="Doses NPK">
            <div class="aba">
              <p class="regra">
                Doses em kg/ha, por produtividade esperada em t/ha. P₂O₅ é lido pela classe de fósforo e K₂O pela de potássio.
                N é lido pela cultura anterior e, no trigo, também pela classe de matéria orgânica. A soja não recebe N mineral.
              </p>
              @for (t of tabelasNpk(); track t.titulo) {
                <h3>{{ t.titulo }}</h3>
                <div class="tabela-container">
                  <table class="lista">
                    <thead>
                      <tr>
                        <th>{{ t.rotuloLinhas }}</th>
                        @for (c of t.colunas; track c) {
                          <th class="numero">{{ c }} t/ha</th>
                        }
                      </tr>
                    </thead>
                    <tbody>
                      @for (l of t.linhas; track l.rotulo) {
                        <tr>
                          <td class="principal" [attr.data-rotulo]="t.rotuloLinhas">{{ l.rotulo }}</td>
                          @for (celula of l.celulas; track $index) {
                            <td class="numero" [attr.data-rotulo]="t.colunas[$index] + ' t/ha'">{{ celula }}</td>
                          }
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </div>
          </mat-tab>

          <mat-tab label="Micronutrientes">
            <div class="aba">
              <p class="regra">O manual traz dose apenas para a soja. Abaixo do nível crítico (classe Alto), aplica-se a dose da classe.</p>
              <div class="tabela-container">
                <table class="lista">
                  <thead>
                    <tr><th>Cultura</th><th>Nutriente</th><th>Classe no solo</th><th class="numero">Dose (kg/ha)</th></tr>
                  </thead>
                  <tbody>
                    @for (m of p.micronutrientes; track $index) {
                      <tr>
                        <td class="principal" data-rotulo="Cultura">{{ m.cultura }}</td>
                        <td data-rotulo="Nutriente">{{ m.nutriente }}</td>
                        <td data-rotulo="Classe no solo"><span [class]="classeNivel(m.classe)">{{ m.classe }}</span></td>
                        <td class="numero" data-rotulo="Dose (kg/ha)">{{ m.doseKgHa | numero: 1 }}</td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </div>
          </mat-tab>
        </mat-tab-group>
      </section>
    }
  `,
  styles: `
    .aba { padding: 16px 2px 4px; }
    h3 { margin: 20px 0 8px; font-size: 0.95rem; font-weight: 600; color: var(--verde-900); }
    h3:first-child { margin-top: 0; }
    h3 small { font-weight: 400; color: var(--texto-suave); }
    .regra { margin: 0 0 8px; font-size: 0.9rem; line-height: 1.5; }
  `,
})
export class Parametros {
  private readonly api = inject(ApiService);
  protected readonly classeNivel = classeNivel;
  protected readonly faixa = faixaTexto;

  protected readonly parametros = carga(() => true, () => this.api.parametrosVigentes(), null as ParametrosVigentes | null);

  protected readonly faixasPorAtributo = computed(() => {
    const grupos = new Map<string, Faixa[]>();
    for (const f of this.parametros.valor()?.faixas ?? []) {
      grupos.set(f.atributo, [...(grupos.get(f.atributo) ?? []), f]);
    }
    return [...grupos.values()].map((faixas) => ({
      rotulo: faixas[0].rotulo,
      unidade: faixas[0].unidade,
      faixas,
      temCriterio: faixas.some((f) => f.criterioMin !== null || f.criterioMax !== null),
    }));
  });

  /** Remonta cada tabela do manual, uma por cultura e nutriente, com a produtividade nas colunas. */
  protected readonly tabelasNpk = computed<TabelaNpk[]>(() => {
    const p = this.parametros.valor();
    const regras = (p?.culturas ?? []).flatMap((c) => (p?.npk ?? []).filter((r) => r.cultura === c.nome).sort(ordemDoManual));
    const tabelas = new Map<string, TabelaNpk>();
    for (const r of regras) {
      const titulo = `${r.cultura}: ${r.nutriente}`;
      const tabela = tabelas.get(titulo) ?? { titulo, rotuloLinhas: rotuloDasLinhas(r), colunas: [], linhas: [] };
      tabelas.set(titulo, tabela);
      const coluna = faixaTexto(r.rendimentoMin, r.rendimentoMax);
      if (!tabela.colunas.includes(coluna)) {
        tabela.colunas.push(coluna);
      }
      const rotulo = rotuloDaLinha(r);
      let linha = tabela.linhas.find((l) => l.rotulo === rotulo);
      if (!linha) {
        linha = { rotulo, celulas: [] };
        tabela.linhas.push(linha);
      }
      linha.celulas[tabela.colunas.indexOf(coluna)] = celulaNpk(r);
    }
    return [...tabelas.values()];
  });
}
