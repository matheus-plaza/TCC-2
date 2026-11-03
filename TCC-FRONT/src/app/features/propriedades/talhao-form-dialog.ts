import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Talhao, TexturaSolo } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { ROTULO_TEXTURA } from '../../shared/formato';
import { aplicarErrosDeCampos, digitos, erroDeFormato } from '../../shared/validacao';

export interface DadosTalhaoDialog {
  propriedadeId: number;
  talhao: Talhao | null;
}

@Component({
  selector: 'app-talhao-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ dados.talhao ? 'Editar talhão' : 'Novo talhão' }}</h2>
    <form [formGroup]="form" (ngSubmit)="salvar()">
      <mat-dialog-content>
        <mat-form-field class="largura-total">
          <mat-label>Identificação</mat-label>
          <input matInput formControlName="identificacao" maxlength="100" placeholder="Ex.: Gleba 01" />
          <mat-error>{{ erroDeFormato(form.controls.identificacao) ?? 'Informe a identificação' }}</mat-error>
        </mat-form-field>
        <mat-form-field class="largura-total">
          <mat-label>Área</mat-label>
          <input matInput type="number" inputmode="decimal" step="any" min="0.01" formControlName="areaHectares" />
          <span matTextSuffix>ha</span>
          <mat-hint>Usada para calcular o total de insumos do talhão</mat-hint>
          <mat-error>{{ erroDeFormato(form.controls.areaHectares) ?? 'Informe a área, maior que zero' }}</mat-error>
        </mat-form-field>
        <mat-form-field class="largura-total">
          <mat-label>Textura predominante</mat-label>
          <mat-select formControlName="texturaPredominante">
            <mat-option [value]="null">Não informada</mat-option>
            @for (t of texturas; track t) {
              <mat-option [value]="t">{{ rotuloTextura[t] }}</mat-option>
            }
          </mat-select>
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
export class TalhaoFormDialog {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject(MatDialogRef<TalhaoFormDialog, Talhao>);
  protected readonly dados = inject<DadosTalhaoDialog>(MAT_DIALOG_DATA);

  protected readonly texturas = Object.keys(ROTULO_TEXTURA) as TexturaSolo[];
  protected readonly rotuloTextura = ROTULO_TEXTURA;
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = this.fb.group({
    identificacao: [this.dados.talhao?.identificacao ?? '', Validators.required],
    areaHectares: [this.dados.talhao?.areaHectares ?? (null as number | null), [Validators.required, Validators.min(0.01), digitos(8, 2)]],
    texturaPredominante: [this.dados.talhao?.texturaPredominante ?? (null as TexturaSolo | null)],
  });
  protected readonly erroDeFormato = erroDeFormato;

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const req = { identificacao: v.identificacao!, areaHectares: v.areaHectares!, texturaPredominante: v.texturaPredominante ?? null };
    const chamada = this.dados.talhao
      ? this.api.atualizarTalhao(this.dados.talhao.id, req)
      : this.api.criarTalhao(this.dados.propriedadeId, req);
    this.salvando.set(true);
    this.erro.set(null);
    chamada.subscribe({
      next: (t) => this.ref.close(t),
      error: (e) => {
        aplicarErrosDeCampos(e, this.form);
        this.erro.set(mensagensDeErro(e).join(' '));
        this.salvando.set(false);
      },
    });
  }
}
