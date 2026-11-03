import { HttpClient, HttpContext, HttpContextToken, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Cultura,
  Evolucao,
  FiltroLaudos,
  FiltroRecomendacoes,
  HistoricoTalhao,
  Indicadores,
  IndicadoresRequest,
  Laudo,
  LaudoRequest,
  ParametrosVigentes,
  Perfil,
  Propriedade,
  PropriedadeRequest,
  Recomendacao,
  RecomendacaoRequest,
  RegistroSafraRequest,
  ResumoPainel,
  Safra,
  SistemaInfo,
  SugestaoAntecessora,
  Talhao,
  TalhaoRequest,
  Usuario,
  UsuarioAtualizacao,
  UsuarioRequest,
  Vinculo,
} from '../models/api.models';

export const API = '/api/v1';

/** A tela apresenta o erro por conta própria; o interceptor não mostra o aviso geral. */
export const ERRO_TRATADO_NA_TELA = new HttpContextToken<boolean>(() => false);

function comErroNaTela(): { context: HttpContext } {
  return { context: new HttpContext().set(ERRO_TRATADO_NA_TELA, true) };
}

function parametros(filtro: object): HttpParams {
  let params = new HttpParams();
  for (const [chave, valor] of Object.entries(filtro)) {
    if (valor !== null && valor !== undefined && valor !== '') {
      params = params.set(chave, String(valor));
    }
  }
  return params;
}

/** Acesso à API REST do AgroMotor. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  // ---------- Sistema ----------
  sistemaInfo(): Observable<SistemaInfo> {
    return this.http.get<SistemaInfo>(`${API}/sistema/info`);
  }

  culturas(): Observable<Cultura[]> {
    return this.http.get<Cultura[]>(`${API}/culturas`);
  }

  parametrosVigentes(): Observable<ParametrosVigentes> {
    return this.http.get<ParametrosVigentes>(`${API}/parametros/vigentes`);
  }

  // ---------- Usuários e vínculos ----------
  usuarios(perfil?: Perfil | null): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${API}/usuarios`, { params: parametros({ perfil }) });
  }

  criarUsuario(req: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${API}/usuarios`, req, comErroNaTela());
  }

  atualizarUsuario(id: number, req: UsuarioAtualizacao): Observable<Usuario> {
    return this.http.put<Usuario>(`${API}/usuarios/${id}`, req, comErroNaTela());
  }

  excluirUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/usuarios/${id}`);
  }

  vinculos(): Observable<Vinculo[]> {
    return this.http.get<Vinculo[]>(`${API}/vinculos`);
  }

  vincular(req: { agronomoEmail?: string; agronomoId?: number; produtorId?: number }): Observable<Vinculo> {
    return this.http.post<Vinculo>(`${API}/vinculos`, req, comErroNaTela());
  }

  desvincular(agronomoId: number, produtorId: number): Observable<void> {
    return this.http.delete<void>(`${API}/vinculos`, { params: parametros({ agronomoId, produtorId }) });
  }

  // ---------- Propriedades e talhões ----------
  propriedades(): Observable<Propriedade[]> {
    return this.http.get<Propriedade[]>(`${API}/propriedades`);
  }

  propriedade(id: number): Observable<Propriedade> {
    return this.http.get<Propriedade>(`${API}/propriedades/${id}`);
  }

  criarPropriedade(req: PropriedadeRequest): Observable<Propriedade> {
    return this.http.post<Propriedade>(`${API}/propriedades`, req, comErroNaTela());
  }

  atualizarPropriedade(id: number, req: PropriedadeRequest): Observable<Propriedade> {
    return this.http.put<Propriedade>(`${API}/propriedades/${id}`, req, comErroNaTela());
  }

  excluirPropriedade(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/propriedades/${id}`);
  }

  talhoes(propriedadeId?: number | null): Observable<Talhao[]> {
    return this.http.get<Talhao[]>(`${API}/talhoes`, { params: parametros({ propriedadeId }) });
  }

  criarTalhao(propriedadeId: number, req: TalhaoRequest): Observable<Talhao> {
    return this.http.post<Talhao>(`${API}/propriedades/${propriedadeId}/talhoes`, req, comErroNaTela());
  }

  atualizarTalhao(id: number, req: TalhaoRequest): Observable<Talhao> {
    return this.http.put<Talhao>(`${API}/talhoes/${id}`, req, comErroNaTela());
  }

  excluirTalhao(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/talhoes/${id}`);
  }

  historicoTalhao(id: number, inicio?: string | null, fim?: string | null): Observable<HistoricoTalhao> {
    return this.http.get<HistoricoTalhao>(`${API}/talhoes/${id}/historico`, { params: parametros({ inicio, fim }) });
  }

  /** Corpo nulo (204) quando o talhão ainda não tem recomendação adotada. */
  sugestaoAntecessora(talhaoId: number): Observable<SugestaoAntecessora | null> {
    return this.http.get<SugestaoAntecessora | null>(`${API}/talhoes/${talhaoId}/sugestao-antecessora`);
  }

  // ---------- Laudos ----------
  laudos(filtro: FiltroLaudos = {}): Observable<Laudo[]> {
    return this.http.get<Laudo[]>(`${API}/laudos`, { params: parametros(filtro) });
  }

  laudo(id: number): Observable<Laudo> {
    return this.http.get<Laudo>(`${API}/laudos/${id}`);
  }

  cadastrarLaudo(req: LaudoRequest): Observable<Laudo> {
    return this.http.post<Laudo>(`${API}/laudos`, req, comErroNaTela());
  }

  excluirLaudo(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/laudos/${id}`);
  }

  /** Pré-visualização da Tela A: as fórmulas ficam somente no backend. */
  indicadores(req: IndicadoresRequest): Observable<Indicadores> {
    return this.http.post<Indicadores>(`${API}/laudos/indicadores`, req, comErroNaTela());
  }

  // ---------- Recomendações ----------
  simular(req: RecomendacaoRequest): Observable<Recomendacao> {
    return this.http.post<Recomendacao>(`${API}/recomendacoes/simular`, req, comErroNaTela());
  }

  registrarSimulacao(req: RecomendacaoRequest): Observable<Recomendacao> {
    return this.http.post<Recomendacao>(`${API}/recomendacoes`, req, comErroNaTela());
  }

  adotar(id: number): Observable<Recomendacao> {
    return this.http.post<Recomendacao>(`${API}/recomendacoes/${id}/adotar`, null);
  }

  recomendacoes(filtro: FiltroRecomendacoes = {}): Observable<Recomendacao[]> {
    return this.http.get<Recomendacao[]>(`${API}/recomendacoes`, { params: parametros(filtro) });
  }

  recomendacao(id: number): Observable<Recomendacao> {
    return this.http.get<Recomendacao>(`${API}/recomendacoes/${id}`);
  }

  safra(recomendacaoId: number): Observable<Safra> {
    return this.http.get<Safra>(`${API}/recomendacoes/${recomendacaoId}/safra`);
  }

  salvarSafra(recomendacaoId: number, req: RegistroSafraRequest): Observable<Safra> {
    return this.http.put<Safra>(`${API}/recomendacoes/${recomendacaoId}/safra`, req, comErroNaTela());
  }

  excluirRecomendacao(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/recomendacoes/${id}`);
  }

  receituarioPdf(id: number): Observable<Blob> {
    return this.http.get(`${API}/relatorios/receituario/${id}`, { responseType: 'blob' });
  }

  // ---------- Painel ----------
  resumoPainel(): Observable<ResumoPainel> {
    return this.http.get<ResumoPainel>(`${API}/dashboard/resumo`);
  }

  evolucao(talhaoId: number): Observable<Evolucao> {
    return this.http.get<Evolucao>(`${API}/dashboard/evolucao`, { params: parametros({ talhaoId }) });
  }
}
