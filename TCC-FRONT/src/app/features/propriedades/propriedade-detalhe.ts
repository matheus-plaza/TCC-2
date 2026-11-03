import { Component, computed, inject, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { Propriedade, Talhao } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { carga } from '../../core/services/carga';
import { confirmar } from '../../shared/confirmacao-dialog';
import { EstadoVazio } from '../../shared/estado-vazio';
import { ROTULO_TEXTURA } from '../../shared/formato';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { PropriedadeFormDialog } from './propriedade-form-dialog';
import { TalhaoFormDialog } from './talhao-form-dialog';

/** RF02: talhões de uma propriedade, com os atalhos de laudo e histórico. */
@Component({
  selector: 'app-propriedade-detalhe',
  imports: [RouterLink, MatIconModule, MatButtonModule, MatMenuModule, MatTooltipModule, EstadoVazio, NumeroPipe],
  template: `
    <a class="voltar" routerLink="/propriedades"><mat-icon>arrow_back</mat-icon> Propriedades</a>

    @if (propriedade.valor(); as p) {
      <header class="pagina-cabecalho">
        <div>
          <h1>{{ p.nome }}</h1>
          <p>{{ p.municipio }}/{{ p.uf }} · {{ p.produtorNome }}</p>
        </div>
        <div class="acoes">
          <button mat-stroked-button (click)="editar(p)"><mat-icon>edit</mat-icon> Editar</button>
          <button mat-stroked-button (click)="excluir(p)"><mat-icon>delete</mat-icon> Excluir</button>
          <button mat-flat-button (click)="abrirTalhao(null)"><mat-icon>add</mat-icon> Novo talhão</button>
        </div>
      </header>

      <section class="cartao">
        <div class="meta">
          <div><span>Talhões</span><strong>{{ p.quantidadeTalhoes }}</strong></div>
          <div><span>Área total</span><strong>{{ p.areaTotalHectares | numero: 2 : 0 }} ha</strong></div>
          <div><span>Produtor</span><strong>{{ p.produtorNome }}</strong></div>
        </div>
      </section>

      <section class="cartao">
        <h2><mat-icon>grid_on</mat-icon> Talhões</h2>
        @if (talhoes.valor().length) {
          <div class="tabela-container">
            <table class="lista">
              <thead>
                <tr>
                  <th>Identificação</th>
                  <th class="numero">Área (ha)</th>
                  <th>Textura</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                @for (t of talhoes.valor(); track t.id) {
                  <tr>
                    <td class="principal" data-rotulo="Identificação">{{ t.identificacao }}</td>
                    <td class="numero" data-rotulo="Área (ha)">{{ t.areaHectares | numero: 2 : 0 }}</td>
                    <td data-rotulo="Textura">{{ textura(t) }}</td>
                    <td class="acoes-linha">
                      <a mat-button routerLink="/laudos/novo" [queryParams]="{ talhaoId: t.id }">
                        <mat-icon>science</mat-icon> Registrar laudo
                      </a>
                      <a mat-button [routerLink]="['/propriedades/talhoes', t.id, 'historico']">
                        <mat-icon>history</mat-icon> Histórico
                      </a>
                      <button mat-icon-button [matMenuTriggerFor]="menu" [attr.aria-label]="'Mais ações de ' + t.identificacao">
                        <mat-icon>more_vert</mat-icon>
                      </button>
                      <mat-menu #menu="matMenu">
                        <a mat-menu-item routerLink="/dashboard" [queryParams]="{ talhaoId: t.id }">
                          <mat-icon>monitoring</mat-icon> Ver evolução
                        </a>
                        <button mat-menu-item (click)="abrirTalhao(t)"><mat-icon>edit</mat-icon> Editar</button>
                        <button mat-menu-item (click)="excluirTalhao(t)"><mat-icon>delete</mat-icon> Excluir</button>
                      </mat-menu>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        } @else if (!talhoes.carregando()) {
          <app-estado-vazio
            icone="grid_on"
            titulo="Nenhum talhão nesta propriedade"
            texto="Cada talhão guarda seus próprios laudos e recomendações."
          >
            <button mat-flat-button (click)="abrirTalhao(null)">Cadastrar talhão</button>
          </app-estado-vazio>
        }
      </section>
    }
  `,
})
export class PropriedadeDetalhe {
  readonly id = input.required<string>();

  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  private readonly idNumerico = computed(() => Number(this.id()));

  protected readonly propriedade = carga(() => this.idNumerico(), (id) => this.api.propriedade(id), null as Propriedade | null);
  protected readonly talhoes = carga(() => this.idNumerico(), (id) => this.api.talhoes(id), [] as Talhao[]);

  protected textura(t: Talhao): string {
    return t.texturaPredominante ? ROTULO_TEXTURA[t.texturaPredominante] : 'Não informada';
  }

  protected editar(p: Propriedade): void {
    this.dialog
      .open(PropriedadeFormDialog, { width: '480px', data: p })
      .afterClosed()
      .subscribe((salva?: Propriedade) => {
        if (salva) {
          this.propriedade.recarregar();
        }
      });
  }

  protected excluir(p: Propriedade): void {
    confirmar(this.dialog, {
      titulo: 'Excluir propriedade',
      mensagem: `Excluir "${p.nome}"? Só é possível depois de excluir os talhões.`,
      confirmar: 'Excluir',
    }).subscribe((ok) => {
      if (ok) {
        this.api.excluirPropriedade(p.id).subscribe(() => {
          this.snack.open('Propriedade excluída', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
          this.router.navigate(['/propriedades']);
        });
      }
    });
  }

  protected abrirTalhao(talhao: Talhao | null): void {
    this.dialog
      .open(TalhaoFormDialog, { width: '440px', data: { propriedadeId: this.idNumerico(), talhao } })
      .afterClosed()
      .subscribe((salvo?: Talhao) => {
        if (salvo) {
          this.atualizarListas();
        }
      });
  }

  protected excluirTalhao(t: Talhao): void {
    confirmar(this.dialog, {
      titulo: 'Excluir talhão',
      mensagem: `Excluir "${t.identificacao}"? Só é possível se ele não tiver laudos.`,
      confirmar: 'Excluir',
    }).subscribe((ok) => {
      if (ok) {
        this.api.excluirTalhao(t.id).subscribe(() => this.atualizarListas());
      }
    });
  }

  private atualizarListas(): void {
    this.talhoes.recarregar();
    this.propriedade.recarregar();
  }
}
