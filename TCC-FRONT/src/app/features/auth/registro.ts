import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Perfil } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { aplicarErrosDeCampos, erroDeFormato } from '../../shared/validacao';

/** Registro público: produtor ou agrônomo. Administradores são criados por outro administrador. */
@Component({
  selector: 'app-registro',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
  ],
  template: `
    <section class="apresentacao">
      <div class="marca">
        <span class="marca-icone"><mat-icon>eco</mat-icon></span>
        <span class="marca-nome">AgroMotor</span>
      </div>
      <div>
        <h1>Histórico do solo e recomendações em um só lugar</h1>
        <p>
          O produtor acompanha a evolução dos talhões e os receituários adotados pelo agrônomo. O agrônomo interpreta os laudos e adota as
          recomendações dos produtores vinculados a ele.
        </p>
      </div>
      <span class="rodape">Trabalho de Conclusão de Curso · UTFPR Santa Helena</span>
    </section>

    <main class="conteudo">
      <div class="formulario">
        <h2>Criar conta</h2>
        <p class="sub-titulo">Escolha como você vai usar o sistema.</p>

        <form [formGroup]="form" (ngSubmit)="criar()">
          <span class="rotulo-campo">Eu sou</span>
          <mat-button-toggle-group formControlName="perfil" class="perfis" aria-label="Perfil">
            <mat-button-toggle value="PRODUTOR">Produtor rural</mat-button-toggle>
            <mat-button-toggle value="AGRONOMO">Agrônomo</mat-button-toggle>
          </mat-button-toggle-group>

          <mat-form-field>
            <mat-label>Nome completo</mat-label>
            <input matInput formControlName="nome" autocomplete="name" />
            <mat-error>{{ erroDeFormato(form.controls.nome) ?? 'Informe o nome' }}</mat-error>
          </mat-form-field>
          <mat-form-field>
            <mat-label>E-mail</mat-label>
            <input matInput type="email" formControlName="email" autocomplete="username" />
            <mat-error>{{ erroDeFormato(form.controls.email) ?? 'Informe um e-mail válido' }}</mat-error>
          </mat-form-field>
          <mat-form-field>
            <mat-label>Senha</mat-label>
            <input matInput type="password" formControlName="senha" autocomplete="new-password" />
            <mat-hint>Pelo menos 8 caracteres</mat-hint>
            <mat-error>{{ form.controls.senha.getError('servidor') ?? 'A senha deve ter de 8 a 72 caracteres' }}</mat-error>
          </mat-form-field>
          @if (perfil() === 'AGRONOMO') {
            <mat-form-field>
              <mat-label>Registro profissional (CREA)</mat-label>
              <input matInput formControlName="registroProfissional" />
              <mat-hint>Aparece no receituário</mat-hint>
              <mat-error>{{ erroDeFormato(form.controls.registroProfissional) }}</mat-error>
            </mat-form-field>
          }

          @if (erro(); as mensagem) {
            <div class="aviso erro" role="alert"><mat-icon>error</mat-icon> {{ mensagem }}</div>
          }

          <button mat-flat-button type="submit" class="botao-bloco enviar" [disabled]="enviando()">Criar conta</button>
        </form>

        <p class="troca">Já tem conta? <a routerLink="/auth/login">Entrar</a></p>
      </div>
    </main>
  `,
  styleUrl: './auth.scss',
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    perfil: ['PRODUTOR' as Perfil, Validators.required],
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    registroProfissional: ['', Validators.maxLength(30)],
  });
  protected readonly erroDeFormato = erroDeFormato;

  protected readonly perfil = toSignal(this.form.controls.perfil.valueChanges, { initialValue: 'PRODUTOR' as Perfil });

  protected criar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.erro.set(null);
    this.auth.registrar(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: (e) => {
        aplicarErrosDeCampos(e, this.form);
        this.erro.set(mensagensDeErro(e).join(' '));
        this.enviando.set(false);
      },
    });
  }
}
