import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormArray, FormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ProblemDetail } from '../core/models/api.models';

function maximo(inteiros: number, decimais: number): string {
  return (10 ** inteiros - 10 ** -decimais).toLocaleString('pt-BR', {
    minimumFractionDigits: decimais,
    maximumFractionDigits: decimais,
  });
}

/** Mesmo limite do backend (@Digits): quantidade de dígitos inteiros e de casas decimais. */
export function digitos(inteiros: number, decimais: number): ValidatorFn {
  return (controle: AbstractControl): ValidationErrors | null => {
    const valor = controle.value as number | null;
    if (valor === null || valor === undefined || (valor as unknown) === '') {
      return null;
    }
    const texto = String(Math.abs(Number(valor)));
    const [parteInteira, parteDecimal = ''] = texto.split('.');
    if (texto.includes('e') || parteInteira.length > inteiros) {
      return { digitos: `Use no máximo ${maximo(inteiros, decimais)}` };
    }
    return parteDecimal.length > decimais ? { digitos: `Use até ${decimais} casas decimais` } : null;
  };
}

/** Mensagem dos erros de formato e dos que o backend apontou no campo. */
export function erroDeFormato(controle: AbstractControl): string | null {
  const servidor = controle.getError('servidor') as string | null;
  if (servidor) {
    return servidor;
  }
  const limite = controle.getError('digitos') as string | null;
  if (limite) {
    return limite;
  }
  const tamanho = controle.getError('maxlength') as { requiredLength: number } | null;
  return tamanho ? `Use no máximo ${tamanho.requiredLength} caracteres` : null;
}

function localizar(raiz: AbstractControl, nome: string): AbstractControl | null {
  if (!(raiz instanceof FormGroup || raiz instanceof FormArray)) {
    return null;
  }
  const direto = raiz.get(nome);
  if (direto) {
    return direto;
  }
  for (const filho of Object.values(raiz.controls)) {
    const encontrado = localizar(filho, nome);
    if (encontrado) {
      return encontrado;
    }
  }
  return null;
}

/** Marca no formulário os campos que o backend recusou, pela propriedade "campos" do erro. */
export function aplicarErrosDeCampos(erro: unknown, formulario: AbstractControl): void {
  if (!(erro instanceof HttpErrorResponse)) {
    return;
  }
  const campos = (erro.error as ProblemDetail | null)?.campos ?? {};
  for (const [nome, mensagens] of Object.entries(campos)) {
    const controle = localizar(formulario, nome);
    if (controle && mensagens.length) {
      controle.setErrors({ ...controle.errors, servidor: mensagens[0] });
      controle.markAsTouched();
    }
  }
}
