import { computed, Injectable, signal } from '@angular/core';

/** Contador de requisições em andamento, para a barra de progresso no topo. */
@Injectable({ providedIn: 'root' })
export class CarregandoService {
  private readonly pendentes = signal(0);
  readonly ativo = computed(() => this.pendentes() > 0);

  iniciar(): void {
    this.pendentes.update((n) => n + 1);
  }

  finalizar(): void {
    this.pendentes.update((n) => Math.max(0, n - 1));
  }
}
