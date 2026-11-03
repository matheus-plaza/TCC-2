import { computed, inject, Injectable } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { SistemaInfo } from '../models/api.models';
import { ApiService } from './api.service';

/** Versão das tabelas paramétricas e contas de demonstração, carregadas uma vez. */
@Injectable({ providedIn: 'root' })
export class SistemaService {
  readonly info = toSignal<SistemaInfo | null>(
    inject(ApiService).sistemaInfo().pipe(catchError(() => of(null))),
    { initialValue: null },
  );

  readonly versao = computed(() => this.info()?.versaoParametros ?? null);

  /** As doses ainda não têm valor agronômico: parâmetros não conferidos com o manual. */
  readonly parametrosNaoConferidos = computed(() => {
    const versao = this.versao();
    return versao !== null && !versao.conferidaManual;
  });
}
