import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { Perfil } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { CarregandoService } from '../../core/services/carregando.service';
import { LayoutService } from '../../core/services/layout.service';
import { SistemaService } from '../../core/services/sistema.service';
import { iniciais, ROTULO_PERFIL } from '../formato';

interface ItemMenu {
  rota: string;
  icone: string;
  rotulo: string;
  exato?: boolean;
  perfis?: Perfil[];
}

const TECNICOS: Perfil[] = ['ADMIN', 'AGRONOMO'];

/** Moldura das telas autenticadas: menu conforme o perfil, topo e aviso de parâmetros. */
@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule,
    MatProgressBarModule,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  protected readonly layout = inject(LayoutService);
  protected readonly auth = inject(AuthService);
  protected readonly carregando = inject(CarregandoService);
  protected readonly sistema = inject(SistemaService);
  protected readonly iniciais = iniciais;
  protected readonly rotuloPerfil = ROTULO_PERFIL;

  protected readonly menuAberto = signal(false);

  private readonly itens: ItemMenu[] = [
    { rota: '/dashboard', icone: 'space_dashboard', rotulo: 'Painel' },
    { rota: '/laudos', icone: 'science', rotulo: 'Laudos de solo', exato: true },
    { rota: '/recomendacoes/nova', icone: 'calculate', rotulo: 'Nova recomendação', perfis: TECNICOS },
    { rota: '/recomendacoes', icone: 'assignment', rotulo: 'Recomendações', exato: true },
  ];

  private readonly cadastros: ItemMenu[] = [
    { rota: '/propriedades', icone: 'agriculture', rotulo: 'Propriedades e talhões' },
    { rota: '/parametros', icone: 'table_chart', rotulo: 'Tabelas NEPAR', perfis: TECNICOS },
  ];

  protected readonly menuPrincipal = computed(() => this.itens.filter((i) => this.permitido(i)));

  protected readonly menuCadastros = computed<ItemMenu[]>(() => [
    ...this.cadastros.filter((i) => this.permitido(i)),
    { rota: '/usuarios', icone: 'group', rotulo: this.rotuloUsuarios() },
  ]);

  private readonly rotuloUsuarios = computed(() => {
    switch (this.auth.perfil()) {
      case 'ADMIN':
        return 'Usuários';
      case 'AGRONOMO':
        return 'Meus produtores';
      default:
        return 'Meus agrônomos';
    }
  });

  private readonly navegacao = toSignal(inject(Router).events.pipe(filter((e) => e instanceof NavigationEnd)));

  constructor() {
    // Fecha a gaveta ao navegar ou ao alternar para tela larga
    effect(() => {
      this.navegacao();
      this.layout.compacto();
      this.menuAberto.set(false);
    });
  }

  private permitido(item: ItemMenu): boolean {
    return !item.perfis || this.auth.temPerfil(item.perfis);
  }
}
