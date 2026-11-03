import { Pipe, PipeTransform } from '@angular/core';

/** Número no padrão brasileiro: {@code valor | numero: 2}. Nulo vira travessão. */
export function formatarNumero(valor: number | null | undefined, casas = 2, minimo = casas): string {
  if (valor === null || valor === undefined) {
    return '—';
  }
  return valor.toLocaleString('pt-BR', { minimumFractionDigits: minimo, maximumFractionDigits: casas });
}

@Pipe({ name: 'numero' })
export class NumeroPipe implements PipeTransform {
  transform(valor: number | null | undefined, casas = 2, minimo = casas): string {
    return formatarNumero(valor, casas, minimo);
  }
}
