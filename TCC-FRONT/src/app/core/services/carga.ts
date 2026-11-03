import { computed, signal, Signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { catchError, Observable, of, switchMap, tap } from 'rxjs';

export interface Carga<T> {
  readonly valor: Signal<T>;
  readonly carregando: Signal<boolean>;
  readonly falhou: Signal<boolean>;
  recarregar(): void;
}

/**
 * Liga uma consulta da API a sinais: refaz a busca quando os parâmetros mudam ou em
 * {@link Carga.recarregar}. Deve ser criada em contexto de injeção (campo do componente).
 * Devolver {@code null} em {@code parametros} indica que ainda não há o que buscar.
 */
export function carga<P, T>(parametros: () => P | null, busca: (p: P) => Observable<T>, inicial: T): Carga<T> {
  const versao = signal(0);
  const valor = signal<T>(inicial);
  const carregando = signal(false);
  const falhou = signal(false);
  const gatilho = computed(() => ({ parametro: parametros(), versao: versao() }));

  toObservable(gatilho)
    .pipe(
      tap(({ parametro }) => {
        carregando.set(parametro !== null);
        falhou.set(false);
      }),
      switchMap(({ parametro }) =>
        parametro === null
          ? of(inicial)
          : busca(parametro).pipe(
              catchError(() => {
                falhou.set(true);
                return of(inicial);
              }),
            ),
      ),
      takeUntilDestroyed(),
    )
    .subscribe((resultado) => {
      valor.set(resultado);
      carregando.set(false);
    });

  return {
    valor: valor.asReadonly(),
    carregando: carregando.asReadonly(),
    falhou: falhou.asReadonly(),
    recarregar: () => versao.update((v) => v + 1),
  };
}
