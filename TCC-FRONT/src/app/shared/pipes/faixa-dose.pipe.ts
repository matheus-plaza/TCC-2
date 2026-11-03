import { Pipe, PipeTransform } from '@angular/core';
import { FaixaDose } from '../../core/models/api.models';
import { formatarNumero } from './numero.pipe';

/** Dose como o manual a apresenta: "81 a 100". Na dose fixa, só o valor. */
export function formatarFaixa(faixa: FaixaDose, casasValorUnico = 0): string {
  if (faixa.min === faixa.max) {
    return formatarNumero(faixa.min, casasValorUnico, casasValorUnico);
  }
  return `${formatarNumero(faixa.min, casasValorUnico, 0)} a ${formatarNumero(faixa.max, casasValorUnico, 0)}`;
}

@Pipe({ name: 'faixaDose' })
export class FaixaDosePipe implements PipeTransform {
  transform(faixa: FaixaDose): string {
    return formatarFaixa(faixa);
  }
}
