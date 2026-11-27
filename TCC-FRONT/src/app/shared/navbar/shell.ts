import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
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
import { ApiService } from '../../core/services/api.service';
import { confirmar } from '../confirmacao-dialog';
import { iniciais, ROTULO_PERFIL } from '../formato';

interface ItemMenu {
  rota: string;
  icone: string;
  rotulo: string;
  exato?: boolean;
  perfis?: Perfil[];
}

const TECNICOS: Perfil[] = ['ADMIN', 'AGRONOMO'];
const DADOS_AGRONOMICOS: Perfil[] = ['AGRONOMO', 'PRODUTOR'];

const AVISO_EXCLUSAO: Record<Perfil, string> = {
  PRODUTOR: 'Serão apagados seu cadastro, suas propriedades, talhões, laudos, recomendações e registros de safra, e os vínculos com agrônomos.',
  AGRONOMO: 'Serão apagados seu cadastro e seus vínculos com produtores. As recomendações que você registrou continuam com os produtores, sem o seu nome.',
  ADMIN: 'Será apagado seu cadastro. Se você for o único administrador, a exclusão não é permitida.',
};

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
    { rota: '/dashboard', icone: 'space_dashboard', rotulo: 'Painel', perfis: DADOS_AGRONOMICOS },
    { rota: '/laudos', icone: 'science', rotulo: 'Laudos de solo', exato: true, perfis: DADOS_AGRONOMICOS },
    { rota: '/recomendacoes/nova', icone: 'calculate', rotulo: 'Nova recomendação', perfis: ['AGRONOMO'] },
    { rota: '/recomendacoes', icone: 'assignment', rotulo: 'Recomendações', exato: true, perfis: DADOS_AGRONOMICOS },
  ];

  private readonly cadastros: ItemMenu[] = [
    { rota: '/propriedades', icone: 'agriculture', rotulo: 'Propriedades e talhões', perfis: DADOS_AGRONOMICOS },
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

  private readonly dialog = inject(MatDialog);
  private readonly api = inject(ApiService);

  protected excluirMinhaConta(): void {
    const perfil = this.auth.perfil();
    if (!perfil) {
      return;
    }
    confirmar(this.dialog, {
      titulo: 'Excluir minha conta',
      mensagem: `${AVISO_EXCLUSAO[perfil]} Esta ação é irreversível: os dados não podem ser recuperados.`,
      confirmar: 'Excluir definitivamente',
    }).subscribe((ok) => {
      if (ok) {
        this.api.excluirMinhaConta().subscribe(() => this.auth.sair());
      }
    });
  }

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
