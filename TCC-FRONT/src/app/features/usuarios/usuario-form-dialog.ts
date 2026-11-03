import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Perfil, Usuario } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { ROTULO_PERFIL } from '../../shared/formato';
import { aplicarErrosDeCampos, erroDeFormato } from '../../shared/validacao';

export interface DadosUsuarioDialog {
  usuario: Usuario | null;
  /** Perfis que quem está cadastrando pode criar. */
  perfis: Perfil[];
}

@Component({
  selector: 'app-usuario-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ dados.usuario ? 'Editar usuário' : 'Novo ' + rotuloPerfil[perfil() ?? 'PRODUTOR'].toLowerCase() }}</h2>
    <form [formGroup]="form" (ngSubmit)="salvar()">
      <mat-dialog-content>
        @if (!dados.usuario && dados.perfis.length > 1) {
          <mat-form-field class="largura-total">
            <mat-label>Perfil</mat-label>
            <mat-select formControlName="perfil">
              @for (p of dados.perfis; track p) {
                <mat-option [value]="p">{{ rotuloPerfil[p] }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }
        <mat-form-field class="largura-total">
          <mat-label>Nome completo</mat-label>
          <input matInput formControlName="nome" maxlength="150" />
          <mat-error>{{ erroDeFormato(form.controls.nome) ?? 'Informe o nome' }}</mat-error>
        </mat-form-field>
        <mat-form-field class="largura-total">
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="email" maxlength="150" autocomplete="off" />
          <mat-error>{{ erroDeFormato(form.controls.email) ?? 'Informe um e-mail válido' }}</mat-error>
        </mat-form-field>
        <mat-form-field class="largura-total">
          <mat-label>{{ dados.usuario ? 'Nova senha' : 'Senha inicial' }}</mat-label>
          <input matInput type="password" formControlName="senha" autocomplete="new-password" />
          <mat-hint>{{ dados.usuario ? 'Deixe em branco para manter a atual' : 'Pelo menos 8 caracteres' }}</mat-hint>
          <mat-error>{{ form.controls.senha.getError('servidor') ?? 'A senha deve ter de 8 a 72 caracteres' }}</mat-error>
        </mat-form-field>
        @if (perfil() === 'AGRONOMO') {
          <mat-form-field class="largura-total">
            <mat-label>Registro profissional (CREA)</mat-label>
            <input matInput formControlName="registroProfissional" maxlength="30" />
            <mat-hint>Aparece no receituário</mat-hint>
            <mat-error>{{ erroDeFormato(form.controls.registroProfissional) }}</mat-error>
          </mat-form-field>
        }
        @if (erro(); as mensagem) {
          <div class="aviso erro" role="alert"><mat-icon>error</mat-icon> {{ mensagem }}</div>
        }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancelar</button>
        <button mat-flat-button type="submit" [disabled]="salvando()">Salvar</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `.largura-total { width: 100%; }`,
})
export class UsuarioFormDialog {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject(MatDialogRef<UsuarioFormDialog, Usuario>);
  protected readonly dados = inject<DadosUsuarioDialog>(MAT_DIALOG_DATA);

  protected readonly rotuloPerfil = ROTULO_PERFIL;
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = this.fb.group({
    perfil: [this.dados.usuario?.perfil ?? this.dados.perfis[0], Validators.required],
    nome: [this.dados.usuario?.nome ?? '', Validators.required],
    email: [this.dados.usuario?.email ?? '', [Validators.required, Validators.email]],
    senha: ['', this.dados.usuario ? [Validators.minLength(8), Validators.maxLength(72)] : [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    registroProfissional: [this.dados.usuario?.registroProfissional ?? ''],
  });
  protected readonly erroDeFormato = erroDeFormato;

  protected readonly perfil = toSignal(this.form.controls.perfil.valueChanges, {
    initialValue: this.form.controls.perfil.value,
  });

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const chamada = this.dados.usuario
      ? this.api.atualizarUsuario(this.dados.usuario.id, {
          nome: v.nome!,
          email: v.email!,
          senha: v.senha || null,
          registroProfissional: v.registroProfissional,
        })
      : this.api.criarUsuario({
          nome: v.nome!,
          email: v.email!,
          senha: v.senha!,
          perfil: v.perfil!,
          registroProfissional: v.registroProfissional,
        });
    this.salvando.set(true);
    this.erro.set(null);
    chamada.subscribe({
      next: (u) => this.ref.close(u),
      error: (e) => {
        aplicarErrosDeCampos(e, this.form);
        this.erro.set(mensagensDeErro(e).join(' '));
        this.salvando.set(false);
      },
    });
  }
}
