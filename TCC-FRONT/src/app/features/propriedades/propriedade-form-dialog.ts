import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Propriedade, Usuario } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { carga } from '../../core/services/carga';
import { aplicarErrosDeCampos, erroDeFormato } from '../../shared/validacao';

@Component({
  selector: 'app-propriedade-form-dialog',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ atual ? 'Editar propriedade' : 'Nova propriedade' }}</h2>
    <form [formGroup]="form" (ngSubmit)="salvar()">
      <mat-dialog-content>
        @if (escolheProdutor) {
          @if (!produtores.carregando() && produtores.valor().length === 0) {
            <div class="aviso alerta">
              <mat-icon>person_off</mat-icon>
              <div>
                Nenhum produtor disponível.
                <a routerLink="/usuarios" mat-dialog-close>Cadastre um produtor</a> antes da propriedade.
              </div>
            </div>
          }
          <mat-form-field class="largura-total">
            <mat-label>Produtor</mat-label>
            <mat-select formControlName="produtorId">
              @for (p of produtores.valor(); track p.id) {
                <mat-option [value]="p.id">{{ p.nome }}</mat-option>
              }
            </mat-select>
            <mat-error>Selecione o produtor</mat-error>
          </mat-form-field>
        }
        <mat-form-field class="largura-total">
          <mat-label>Nome da propriedade</mat-label>
          <input matInput formControlName="nome" maxlength="150" />
          <mat-error>{{ erroDeFormato(form.controls.nome) ?? 'Informe o nome' }}</mat-error>
        </mat-form-field>
        <mat-form-field class="largura-total">
          <mat-label>Município (PR)</mat-label>
          <input matInput formControlName="municipio" maxlength="100" />
          <mat-hint>O sistema atende apenas o Estado do Paraná</mat-hint>
          <mat-error>{{ erroDeFormato(form.controls.municipio) ?? 'Informe o município' }}</mat-error>
        </mat-form-field>
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
export class PropriedadeFormDialog {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject(MatDialogRef<PropriedadeFormDialog, Propriedade>);
  protected readonly atual = inject<Propriedade | null>(MAT_DIALOG_DATA);

  /** O dono da propriedade é definido na criação; o produtor cadastra sempre para si. */
  protected readonly escolheProdutor = !this.atual && !this.auth.produtor();

  protected readonly produtores = carga(
    () => (this.escolheProdutor ? true : null),
    () => this.api.usuarios('PRODUTOR'),
    [] as Usuario[],
  );

  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = this.fb.group({
    produtorId: [null as number | null, this.escolheProdutor ? Validators.required : []],
    nome: [this.atual?.nome ?? '', Validators.required],
    municipio: [this.atual?.municipio ?? '', Validators.required],
  });
  protected readonly erroDeFormato = erroDeFormato;

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const req = { produtorId: v.produtorId, nome: v.nome!, municipio: v.municipio! };
    const chamada = this.atual ? this.api.atualizarPropriedade(this.atual.id, req) : this.api.criarPropriedade(req);
    this.salvando.set(true);
    this.erro.set(null);
    chamada.subscribe({
      next: (p) => this.ref.close(p),
      error: (e) => {
        aplicarErrosDeCampos(e, this.form);
        this.erro.set(mensagensDeErro(e).join(' '));
        this.salvando.set(false);
      },
    });
  }
}
