import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { Propriedade } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { EstadoVazio } from '../../shared/estado-vazio';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { PropriedadeFormDialog } from './propriedade-form-dialog';

/** RF02: propriedades rurais visíveis ao usuário. */
@Component({
  selector: 'app-propriedades',
  imports: [RouterLink, MatIconModule, MatButtonModule, EstadoVazio, NumeroPipe],
  template: `
    <header class="pagina-cabecalho">
      <div>
        <h1>Propriedades e talhões</h1>
        <p>
          @if (auth.produtor()) {
            Suas propriedades rurais no Paraná e os talhões de cada uma.
          } @else {
            Propriedades rurais dos produtores {{ auth.agronomo() ? 'vinculados a você' : 'do sistema' }}.
          }
        </p>
      </div>
      <div class="acoes">
        <button mat-flat-button (click)="nova()"><mat-icon>add</mat-icon> Nova propriedade</button>
      </div>
    </header>

    @if (propriedades.valor().length) {
      <div class="lista-cartoes">
        @for (p of propriedades.valor(); track p.id) {
          <a class="cartao propriedade" [routerLink]="['/propriedades', p.id]">
            <span class="icone"><mat-icon>agriculture</mat-icon></span>
            <span class="dados">
              <strong>{{ p.nome }}</strong>
              <span class="sub">{{ p.municipio }}/{{ p.uf }}</span>
              @if (!auth.produtor()) {
                <span class="sub">{{ p.produtorNome }}</span>
              }
            </span>
            <span class="totais">
              <strong>{{ p.quantidadeTalhoes }}</strong> {{ p.quantidadeTalhoes === 1 ? 'talhão' : 'talhões' }}
              <span class="sub">{{ p.areaTotalHectares | numero: 1 : 0 }} ha</span>
            </span>
          </a>
        }
      </div>
    } @else if (!propriedades.carregando()) {
      <section class="cartao">
        <app-estado-vazio
          icone="agriculture"
          titulo="Nenhuma propriedade cadastrada"
          texto="Cadastre a propriedade e depois os talhões que recebem os laudos de solo."
        >
          <button mat-flat-button (click)="nova()">Cadastrar propriedade</button>
        </app-estado-vazio>
      </section>
    }
  `,
  styles: `
    .propriedade {
      display: flex; align-items: center; gap: 14px; margin: 0;
      color: inherit; text-decoration: none; transition: border-color 0.15s;
    }
    .propriedade:hover { border-color: var(--verde-500); }
    .icone {
      display: grid; place-items: center; flex: none; width: 44px; height: 44px; border-radius: 12px;
      background: var(--verde-50); color: var(--verde-700);
    }
    .dados { display: flex; flex-direction: column; flex: 1; min-width: 0; }
    .dados strong { font-weight: 600; overflow-wrap: anywhere; }
    .totais { text-align: right; font-size: 0.85rem; white-space: nowrap; }
    .totais strong { font-size: 1.1rem; font-weight: 600; }
  `,
})
export class Propriedades {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  protected readonly auth = inject(AuthService);

  protected readonly propriedades = carga(() => true, () => this.api.propriedades(), [] as Propriedade[]);

  protected nova(): void {
    this.dialog
      .open(PropriedadeFormDialog, { width: '480px', data: null })
      .afterClosed()
      .subscribe((criada?: Propriedade) => {
        if (criada) {
          this.snack.open('Propriedade cadastrada. Agora cadastre os talhões.', 'Fechar', { duration: 5000, panelClass: 'snack-ok' });
          this.router.navigate(['/propriedades', criada.id]);
        }
      });
  }
}
