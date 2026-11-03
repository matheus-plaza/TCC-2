import { Pipe, PipeTransform } from '@angular/core';

/** Data ISO (yyyy-MM-dd, com ou sem hora) para dd/MM/yyyy, sem conversão de fuso. */
export function formatarData(iso: string | null | undefined): string {
  if (!iso) {
    return '—';
  }
  const [ano, mes, dia] = iso.substring(0, 10).split('-');
  return `${dia}/${mes}/${ano}`;
}

@Pipe({ name: 'dataBr' })
export class DataBrPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return formatarData(iso);
  }
}
