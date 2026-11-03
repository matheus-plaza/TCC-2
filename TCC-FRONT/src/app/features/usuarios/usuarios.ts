import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Perfil, Usuario, Vinculo } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { confirmar } from '../../shared/confirmacao-dialog';
import { EstadoVazio } from '../../shared/estado-vazio';
import { iniciais, ROTULO_PERFIL } from '../../shared/formato';
import { UsuarioFormDialog } from './usuario-form-dialog';

/**
 * Contas e vínculos (RNF05), conforme o perfil: o administrador gerencia todos, o agrônomo
 * cadastra seus produtores e o produtor autoriza os agrônomos que podem ver seus dados.
 */
@Component({
  selector: 'app-usuarios',
  imports: [ReactiveFormsModule, MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, EstadoVazio],
  template: `
    <header class="pagina-cabecalho">
      <div>
        <h1>{{ titulo() }}</h1>
        <p>{{ descricao() }}</p>
      </div>
      @if (!auth.produtor()) {
        <div class="acoes">
          <button mat-flat-button (click)="novo()">
            <mat-icon>person_add</mat-icon> {{ auth.admin() ? 'Novo usuário' : 'Cadastrar produtor' }}
          </button>
        </div>
      }
    </header>

    @if (auth.produtor()) {
      <section class="cartao">
        <h2><mat-icon>verified_user</mat-icon> Autorizar um agrônomo</h2>
        <p class="texto-suave ajuda-texto">
          O agrônomo autorizado passa a ver suas propriedades e laudos e pode emitir recomendações para os seus talhões.
        </p>
        <form class="autorizar" [formGroup]="formAutorizacao" (ngSubmit)="autorizar()">
          <mat-form-field>
            <mat-label>E-mail do agrônomo</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="off" />
            <mat-error>Informe um e-mail válido</mat-error>
          </mat-form-field>
          <button mat-flat-button type="submit">Autorizar</button>
        </form>
        @if (erroVinculo(); as mensagem) {
          <div class="aviso erro" role="alert"><mat-icon>error</mat-icon> {{ mensagem }}</div>
        }
      </section>

      <section class="cartao">
        <h2><mat-icon>groups</mat-icon> Agrônomos autorizados</h2>
        @if (vinculos.valor().length) {
          <ul class="pessoas">
            @for (v of vinculos.valor(); track v.agronomo.id) {
              <li>
                <span class="avatar">{{ iniciais(v.agronomo.nome) }}</span>
                <span class="dados">
                  <strong>{{ v.agronomo.nome }}</strong>
                  <span class="sub">{{ v.agronomo.email }}@if (v.agronomo.registroProfissional) { · CREA {{ v.agronomo.registroProfissional }} }</span>
                </span>
                <button mat-button (click)="removerVinculo(v)">Remover</button>
              </li>
            }
          </ul>
        } @else if (!vinculos.carregando()) {
          <app-estado-vazio icone="groups" titulo="Nenhum agrônomo autorizado" texto="Informe o e-mail do seu agrônomo acima." />
        }
      </section>
    } @else {
      <section class="cartao">
        @if (auth.admin()) {
          <mat-form-field class="filtro-perfil">
            <mat-label>Perfil</mat-label>
            <mat-select [value]="filtroPerfil()" (valueChange)="filtroPerfil.set($event)">
              <mat-option [value]="null">Todos</mat-option>
              @for (p of perfis; track p) {
                <mat-option [value]="p">{{ rotuloPerfil[p] }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }

        @if (usuarios.valor().length) {
          <ul class="pessoas">
            @for (u of usuarios.valor(); track u.id) {
              <li>
                <span class="avatar" [class.produtor]="u.perfil === 'PRODUTOR'">{{ iniciais(u.nome) }}</span>
                <span class="dados">
                  <strong>{{ u.nome }}</strong>
                  <span class="sub">{{ u.email }}@if (u.registroProfissional) { · CREA {{ u.registroProfissional }} }</span>
                </span>
                <span class="selo">{{ rotuloPerfil[u.perfil] }}</span>
                @if (auth.admin()) {
                  <button mat-icon-button (click)="editar(u)" [attr.aria-label]="'Editar ' + u.nome"><mat-icon>edit</mat-icon></button>
                  @if (u.id !== auth.usuario()?.id) {
                    <button mat-icon-button (click)="excluir(u)" [attr.aria-label]="'Excluir ' + u.nome"><mat-icon>delete</mat-icon></button>
                  }
                } @else {
                  <button mat-button (click)="desvincularProdutor(u)">Remover vínculo</button>
                }
              </li>
            }
          </ul>
        } @else if (!usuarios.carregando()) {
          <app-estado-vazio
            icone="group"
            [titulo]="auth.admin() ? 'Nenhum usuário encontrado' : 'Nenhum produtor vinculado'"
            [texto]="auth.admin() ? 'Ajuste o filtro ou cadastre um usuário.' : 'Cadastre um produtor ou peça que ele autorize você pelo seu e-mail.'"
          >
            @if (auth.agronomo()) {
              <button mat-flat-button (click)="novo()">Cadastrar produtor</button>
            }
          </app-estado-vazio>
        }
      </section>

      @if (auth.admin()) {
        <section class="cartao">
          <h2><mat-icon>link</mat-icon> Vínculos agrônomo-produtor</h2>
          <form class="vincular" [formGroup]="formVinculo" (ngSubmit)="vincular()">
            <mat-form-field>
              <mat-label>Agrônomo</mat-label>
              <mat-select formControlName="agronomoId">
                @for (a of agronomos(); track a.id) {
                  <mat-option [value]="a.id">{{ a.nome }}</mat-option>
                }
              </mat-select>
              <mat-error>Selecione o agrônomo</mat-error>
            </mat-form-field>
            <mat-form-field>
              <mat-label>Produtor</mat-label>
              <mat-select formControlName="produtorId">
                @for (p of produtores(); track p.id) {
                  <mat-option [value]="p.id">{{ p.nome }}</mat-option>
                }
              </mat-select>
              <mat-error>Selecione o produtor</mat-error>
            </mat-form-field>
            <button mat-flat-button type="submit">Vincular</button>
          </form>
          @if (erroVinculo(); as mensagem) {
            <div class="aviso erro" role="alert"><mat-icon>error</mat-icon> {{ mensagem }}</div>
          }
          @if (vinculos.valor().length) {
            <ul class="pessoas">
              @for (v of vinculos.valor(); track v.agronomo.id + '-' + v.produtor.id) {
                <li>
                  <span class="dados">
                    <strong>{{ v.agronomo.nome }}</strong>
                    <span class="sub">atende {{ v.produtor.nome }}</span>
                  </span>
                  <button mat-button (click)="removerVinculo(v)">Remover</button>
                </li>
              }
            </ul>
          }
        </section>
      }
    }
  `,
  styles: `
    .ajuda-texto { margin: -6px 0 12px; font-size: 0.88rem; }
    .autorizar { display: grid; grid-template-columns: minmax(0, 420px) auto; gap: 0 12px; align-items: start; }
    .autorizar button, .vincular button { margin-top: 8px; height: 44px; }
    .vincular { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)) auto; gap: 0 12px; align-items: start; }
    .vincular mat-form-field, .autorizar mat-form-field { width: 100%; }
    .filtro-perfil { max-width: 240px; width: 100%; }
    .pessoas { list-style: none; margin: 0; padding: 0; }
    .pessoas li { display: flex; align-items: center; gap: 12px; padding: 10px 2px; }
    .pessoas li + li { border-top: 1px solid var(--borda); }
    .dados { display: flex; flex-direction: column; flex: 1; min-width: 0; }
    .dados strong { font-weight: 500; }
    .dados .sub { overflow-wrap: anywhere; }
    @media (max-width: 599.98px) {
      .autorizar, .vincular { grid-template-columns: minmax(0, 1fr); }
      .autorizar button, .vincular button { margin: 0 0 12px; }
    }
  `,
})
export class Usuarios {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);
  protected readonly iniciais = iniciais;
  protected readonly rotuloPerfil = ROTULO_PERFIL;
  protected readonly perfis: Perfil[] = ['ADMIN', 'AGRONOMO', 'PRODUTOR'];

  protected readonly filtroPerfil = signal<Perfil | null>(null);
  protected readonly erroVinculo = signal<string | null>(null);

  protected readonly usuarios = carga(
    () => (this.auth.produtor() ? null : { perfil: this.filtroPerfil() }),
    (p) => this.api.usuarios(p.perfil),
    [] as Usuario[],
  );
  /** O administrador escolhe os dois lados do vínculo, independente do filtro da lista. */
  private readonly todos = carga(
    () => (this.auth.admin() ? true : null),
    () => this.api.usuarios(),
    [] as Usuario[],
  );
  protected readonly vinculos = carga(
    () => (this.auth.agronomo() ? null : true),
    () => this.api.vinculos(),
    [] as Vinculo[],
  );

  protected readonly agronomos = computed(() => this.todos.valor().filter((u) => u.perfil === 'AGRONOMO'));
  protected readonly produtores = computed(() => this.todos.valor().filter((u) => u.perfil === 'PRODUTOR'));

  protected readonly titulo = computed(() =>
    this.auth.admin() ? 'Usuários' : this.auth.agronomo() ? 'Meus produtores' : 'Meus agrônomos',
  );
  protected readonly descricao = computed(() =>
    this.auth.admin()
      ? 'Contas do sistema e vínculos entre agrônomos e produtores.'
      : this.auth.agronomo()
        ? 'Produtores vinculados a você. Só os dados deles ficam visíveis na sua conta.'
        : 'Agrônomos que podem ver seus dados e emitir recomendações para seus talhões.',
  );

  protected readonly formAutorizacao = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });
  protected readonly formVinculo = this.fb.group({
    agronomoId: [null as number | null, Validators.required],
    produtorId: [null as number | null, Validators.required],
  });

  protected novo(): void {
    const perfis: Perfil[] = this.auth.admin() ? ['PRODUTOR', 'AGRONOMO', 'ADMIN'] : ['PRODUTOR'];
    this.dialog
      .open(UsuarioFormDialog, { width: '480px', data: { usuario: null, perfis } })
      .afterClosed()
      .subscribe((criado?: Usuario) => {
        if (criado) {
          this.snack.open(`${criado.nome} cadastrado(a)`, 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
          this.recarregar();
        }
      });
  }

  protected editar(usuario: Usuario): void {
    this.dialog
      .open(UsuarioFormDialog, { width: '480px', data: { usuario, perfis: [usuario.perfil] } })
      .afterClosed()
      .subscribe((salvo?: Usuario) => {
        if (salvo) {
          this.recarregar();
        }
      });
  }

  protected excluir(usuario: Usuario): void {
    confirmar(this.dialog, {
      titulo: 'Excluir usuário',
      mensagem: `Excluir a conta de ${usuario.nome}? Um produtor com propriedades cadastradas não pode ser excluído.`,
      confirmar: 'Excluir',
    }).subscribe((ok) => {
      if (ok) {
        this.api.excluirUsuario(usuario.id).subscribe(() => this.recarregar());
      }
    });
  }

  protected autorizar(): void {
    if (this.formAutorizacao.invalid) {
      this.formAutorizacao.markAllAsTouched();
      return;
    }
    this.criarVinculo({ agronomoEmail: this.formAutorizacao.getRawValue().email }, () => this.formAutorizacao.reset());
  }

  protected vincular(): void {
    if (this.formVinculo.invalid) {
      this.formVinculo.markAllAsTouched();
      return;
    }
    const v = this.formVinculo.getRawValue();
    this.criarVinculo({ agronomoId: v.agronomoId!, produtorId: v.produtorId! }, () => this.formVinculo.reset());
  }

  protected removerVinculo(vinculo: Vinculo): void {
    this.confirmarRemocao(vinculo.agronomo.id, vinculo.produtor.id, `${vinculo.agronomo.nome} deixa de ver os dados de ${vinculo.produtor.nome}.`);
  }

  protected desvincularProdutor(produtor: Usuario): void {
    const agronomoId = this.auth.usuario()!.id;
    this.confirmarRemocao(agronomoId, produtor.id, `Você deixa de ver os dados de ${produtor.nome}.`);
  }

  private criarVinculo(req: { agronomoEmail?: string; agronomoId?: number; produtorId?: number }, aoConcluir: () => void): void {
    this.erroVinculo.set(null);
    this.api.vincular(req).subscribe({
      next: () => {
        this.snack.open('Vínculo criado', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
        aoConcluir();
        this.recarregar();
      },
      error: (e) => this.erroVinculo.set(mensagensDeErro(e).join(' ')),
    });
  }

  private confirmarRemocao(agronomoId: number, produtorId: number, mensagem: string): void {
    confirmar(this.dialog, { titulo: 'Remover vínculo', mensagem, confirmar: 'Remover' }).subscribe((ok) => {
      if (ok) {
        this.api.desvincular(agronomoId, produtorId).subscribe(() => this.recarregar());
      }
    });
  }

  private recarregar(): void {
    this.usuarios.recarregar();
    this.todos.recarregar();
    this.vinculos.recarregar();
  }
}
