import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Safra } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { diferencaComSinal, hojeIso } from '../../shared/formato';
import { DataBrPipe } from '../../shared/pipes/data-br.pipe';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { aplicarErrosDeCampos, digitos, erroDeFormato } from '../../shared/validacao';

/** Colheita e gasto com adubação da recomendação adotada, comparados com o esperado. */
@Component({
  selector: 'app-safra-card',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, NumeroPipe, DataBrPipe],
  template: `
    <section class="cartao">
      <h2><mat-icon>agriculture</mat-icon> Resultado da safra</h2>

      @if (safra.valor(); as s) {
        <div class="meta">
          <div><span>Produtividade esperada</span><strong>{{ s.produtividadeEsperadaTonHa | numero: 2 : 0 }} t/ha</strong></div>
          <div>
            <span>Produtividade colhida</span>
            <strong>
              @if (s.produtividadeColhidaTonHa !== null) {
                {{ s.produtividadeColhidaTonHa | numero: 2 : 0 }} t/ha
              } @else {
                Não registrada
              }
            </strong>
          </div>
          <div><span>Diferença</span><strong>{{ diferenca(s.diferencaPercentual) }}</strong></div>
          <div><span>Data da colheita</span><strong>{{ s.dataColheita ? (s.dataColheita | dataBr) : '—' }}</strong></div>
          <div>
            <span>Gasto com adubação</span>
            <strong>
              @if (s.custoAdubacaoReais !== null) {
                R$ {{ s.custoAdubacaoReais | numero: 2 }}
                <span class="sub">R$ {{ s.custoPorHectareReais | numero: 2 }} por hectare</span>
              } @else {
                Não registrado
              }
            </strong>
          </div>
        </div>

        @if (podeEditar()) {
          <form [formGroup]="form" (ngSubmit)="salvar()" class="formulario">
            <p class="descricao">
              Este registro fica à parte: a recomendação adotada não muda. Informe o gasto total com adubação neste talhão.
              Ele pode ser salvo antes da colheita.
            </p>
            <div class="campos">
              <mat-form-field>
                <mat-label>Produtividade colhida</mat-label>
                <input matInput type="number" inputmode="decimal" step="any" formControlName="produtividadeColhidaTonHa" />
                <span matTextSuffix class="unidade">t/ha</span>
                @if (erroProdutividade(); as mensagem) {
                  <mat-error>{{ mensagem }}</mat-error>
                }
              </mat-form-field>
              <mat-form-field>
                <mat-label>Data da colheita</mat-label>
                <input matInput type="date" formControlName="dataColheita" [min]="s.dataAdocao" [max]="hoje" />
                @if (form.controls.dataColheita.getError('servidor'); as mensagem) {
                  <mat-error>{{ mensagem }}</mat-error>
                }
              </mat-form-field>
              <mat-form-field>
                <mat-label>Gasto com adubação</mat-label>
                <span matTextPrefix class="unidade">R$&nbsp;</span>
                <input matInput type="number" inputmode="decimal" step="any" formControlName="custoAdubacaoReais" />
                @if (erroCusto(); as mensagem) {
                  <mat-error>{{ mensagem }}</mat-error>
                }
              </mat-form-field>
            </div>

            @if (erros().length) {
              <div class="aviso erro" role="alert">
                <mat-icon>error</mat-icon>
                <ul>
                  @for (e of erros(); track $index) {
                    <li>{{ e }}</li>
                  }
                </ul>
              </div>
            }

            <div class="acoes-form">
              <button mat-flat-button type="submit" [disabled]="salvando()"><mat-icon>save</mat-icon> Salvar resultado</button>
            </div>
          </form>
        }
      }
    </section>
  `,
  styles: `
    .formulario { margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--borda); }
    .descricao { margin: 0 0 12px; font-size: 0.85rem; color: var(--texto-suave); }
    .campos { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 4px 16px; }
    .campos mat-form-field { width: 100%; }
    .unidade { font-size: 0.8rem; color: var(--texto-suave); padding-left: 4px; }
    .sub { display: block; font-size: 0.8rem; font-weight: 400; color: var(--texto-suave); }
  `,
})
export class SafraCard {
  readonly recomendacaoId = input.required<number>();

  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly snack = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  protected readonly hoje = hojeIso();
  protected readonly diferenca = diferencaComSinal;
  protected readonly salvando = signal(false);
  protected readonly erros = signal<string[]>([]);
  /** O administrador acompanha, mas quem informa o resultado é o produtor ou o agrônomo. */
  protected readonly podeEditar = computed(() => this.auth.produtor() || this.auth.agronomo());

  protected readonly safra = carga(() => this.recomendacaoId(), (id) => this.api.safra(id), null as Safra | null);

  protected readonly form = this.fb.group({
    produtividadeColhidaTonHa: [null as number | null, [Validators.min(0.01), digitos(3, 2)]],
    dataColheita: [null as string | null],
    custoAdubacaoReais: [null as number | null, [Validators.min(0), digitos(10, 2)]],
  });

  constructor() {
    effect(() => {
      const s = this.safra.valor();
      if (s) {
        this.form.reset({
          produtividadeColhidaTonHa: s.produtividadeColhidaTonHa,
          dataColheita: s.dataColheita,
          custoAdubacaoReais: s.custoAdubacaoReais,
        });
      }
    });
  }

  protected erroProdutividade(): string | null {
    const controle = this.form.controls.produtividadeColhidaTonHa;
    return controle.hasError('min') ? 'Deve ser maior que zero' : erroDeFormato(controle);
  }

  protected erroCusto(): string | null {
    const controle = this.form.controls.custoAdubacaoReais;
    return controle.hasError('min') ? 'Não pode ser negativo' : erroDeFormato(controle);
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.salvando.set(true);
    this.erros.set([]);
    this.api
      .salvarSafra(this.recomendacaoId(), {
        produtividadeColhidaTonHa: v.produtividadeColhidaTonHa,
        dataColheita: v.dataColheita || null,
        custoAdubacaoReais: v.custoAdubacaoReais,
      })
      .subscribe({
        next: () => {
          this.snack.open('Resultado da safra salvo', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
          this.salvando.set(false);
          this.safra.recarregar();
        },
        error: (e) => {
          aplicarErrosDeCampos(e, this.form);
          this.erros.set(mensagensDeErro(e));
          this.salvando.set(false);
        },
      });
  }
}
