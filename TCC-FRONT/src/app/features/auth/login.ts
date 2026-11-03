import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { ContaDemonstracao } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { SistemaService } from '../../core/services/sistema.service';
import { ROTULO_PERFIL } from '../../shared/formato';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <section class="apresentacao">
      <div class="marca">
        <span class="marca-icone"><mat-icon>eco</mat-icon></span>
        <span class="marca-nome">AgroMotor</span>
      </div>
      <div>
        <h1>Calagem e adubação do laudo ao receituário do agrônomo</h1>
        <p>
          Interprete laudos de solo e gere recomendações para soja, milho e trigo com base no Manual de Adubação e
          Calagem para o Estado do Paraná.
        </p>
      </div>
      <span class="rodape">Trabalho de Conclusão de Curso · UTFPR Santa Helena</span>
    </section>

    <main class="conteudo">
      <div class="formulario">
        <h2>Entrar</h2>
        <p class="sub-titulo">Use o e-mail e a senha da sua conta.</p>

        <form [formGroup]="form" (ngSubmit)="entrar()">
          <mat-form-field>
            <mat-label>E-mail</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="username" />
            @if (form.controls.email.hasError('email')) {
              <mat-error>E-mail inválido</mat-error>
            } @else {
              <mat-error>Informe o e-mail</mat-error>
            }
          </mat-form-field>
          <mat-form-field>
            <mat-label>Senha</mat-label>
            <input matInput type="password" formControlName="senha" autocomplete="current-password" />
            <mat-error>Informe a senha</mat-error>
          </mat-form-field>

          @if (erro(); as mensagem) {
            <div class="aviso erro" role="alert"><mat-icon>error</mat-icon> {{ mensagem }}</div>
          }

          <button mat-flat-button type="submit" class="botao-bloco enviar" [disabled]="enviando()">Entrar</button>
        </form>

        <p class="troca">Ainda não tem conta? <a routerLink="/auth/registro">Criar conta</a></p>

        @if (sistema.info()?.contasDemonstracao?.length) {
          <section class="demonstracao">
            <h3><mat-icon>science</mat-icon> Modo demonstração</h3>
            <p>Dados fictícios. Entre com uma conta de exemplo:</p>
            <div class="contas">
              @for (conta of sistema.info()!.contasDemonstracao; track conta.email) {
                <button mat-stroked-button type="button" (click)="entrarComo(conta)" [disabled]="enviando()">
                  {{ rotuloPerfil[conta.perfil] }}
                </button>
              }
            </div>
          </section>
        }
      </div>
    </main>
  `,
  styleUrl: './auth.scss',
})
export class Login {
  /** Tela pedida antes do login, recebida pela URL. */
  readonly retorno = input<string>();

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly sistema = inject(SistemaService);
  protected readonly rotuloPerfil = ROTULO_PERFIL;

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required],
  });

  protected entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { email, senha } = this.form.getRawValue();
    this.autenticar(email, senha);
  }

  protected entrarComo(conta: ContaDemonstracao): void {
    this.form.setValue({ email: conta.email, senha: conta.senha });
    this.autenticar(conta.email, conta.senha);
  }

  private autenticar(email: string, senha: string): void {
    this.enviando.set(true);
    this.erro.set(null);
    this.auth.login(email, senha).subscribe({
      next: () => this.router.navigateByUrl(this.retorno() ?? '/dashboard'),
      error: (e) => {
        this.erro.set(mensagensDeErro(e).join(' '));
        this.enviando.set(false);
      },
    });
  }
}
