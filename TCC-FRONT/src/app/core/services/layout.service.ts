import { BreakpointObserver } from '@angular/cdk/layout';
import { inject, Injectable } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

const COMPACTO = '(max-width: 1023.98px)';
const CELULAR = '(max-width: 599.98px)';

function corresponde(consulta: string): boolean {
  return typeof window !== 'undefined' && window.matchMedia(consulta).matches;
}

/** Pontos de quebra da interface (menu em gaveta, listas em cartões). */
@Injectable({ providedIn: 'root' })
export class LayoutService {
  private readonly observer = inject(BreakpointObserver);

  /** Até 1024 px, o menu lateral vira gaveta. */
  readonly compacto = toSignal(this.observer.observe(COMPACTO).pipe(map((r) => r.matches)), {
    initialValue: corresponde(COMPACTO),
  });

  /** Em celular, as tabelas viram cartões. */
  readonly celular = toSignal(this.observer.observe(CELULAR).pipe(map((r) => r.matches)), {
    initialValue: corresponde(CELULAR),
  });
}
