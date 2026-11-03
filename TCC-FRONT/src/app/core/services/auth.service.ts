import { HttpClient, HttpContext } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { Perfil, TokenResponse, Usuario, UsuarioRequest } from '../models/api.models';
import { API, ERRO_TRATADO_NA_TELA } from './api.service';

const CHAVE_SESSAO = 'agromotor.sessao';

interface Sessao {
  token: string;
  expiraEm: string;
  usuario: Usuario;
}

function lerSessao(): Sessao | null {
  try {
    const salva = localStorage.getItem(CHAVE_SESSAO);
    const sessao = salva ? (JSON.parse(salva) as Sessao) : null;
    return sessao && new Date(sessao.expiraEm).getTime() > Date.now() ? sessao : null;
  } catch {
    return null;
  }
}

/** Sessão do usuário: guarda o JWT e expõe a identidade e o perfil. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sessao = signal<Sessao | null>(lerSessao());

  readonly usuario = computed(() => this.sessao()?.usuario ?? null);
  readonly perfil = computed<Perfil | null>(() => this.usuario()?.perfil ?? null);
  readonly autenticado = computed(() => this.sessao() !== null);
  readonly agronomo = computed(() => this.perfil() === 'AGRONOMO');
  readonly admin = computed(() => this.perfil() === 'ADMIN');
  readonly produtor = computed(() => this.perfil() === 'PRODUTOR');

  get token(): string | null {
    return this.sessao()?.token ?? null;
  }

  login(email: string, senha: string): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${API}/auth/login`, { email, senha }, this.opcoes())
      .pipe(tap((r) => this.abrirSessao(r)));
  }

  registrar(req: UsuarioRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${API}/auth/registro`, req, this.opcoes())
      .pipe(tap((r) => this.abrirSessao(r)));
  }

  /** Encerra a sessão e volta ao login, lembrando a tela para o retorno. */
  sair(retorno?: string): void {
    this.sessao.set(null);
    try {
      localStorage.removeItem(CHAVE_SESSAO);
    } catch {
      // armazenamento indisponível: a sessão já foi encerrada em memória
    }
    this.router.navigate(['/auth/login'], retorno ? { queryParams: { retorno } } : undefined);
  }

  temPerfil(perfis: Perfil[]): boolean {
    const atual = this.perfil();
    return atual !== null && perfis.includes(atual);
  }

  private abrirSessao(r: TokenResponse): void {
    const sessao: Sessao = { token: r.token, expiraEm: r.expiraEm, usuario: r.usuario };
    this.sessao.set(sessao);
    try {
      localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
    } catch {
      // armazenamento indisponível: a sessão vale até a página ser recarregada
    }
  }

  private opcoes(): { context: HttpContext } {
    return { context: new HttpContext().set(ERRO_TRATADO_NA_TELA, true) };
  }
}
