/** Contratos da API REST (Problem Details, DTOs do backend). */

export type Perfil = 'ADMIN' | 'AGRONOMO' | 'PRODUTOR';
export type StatusRecomendacao = 'SIMULACAO' | 'ADOTADA';
export type SistemaManejo = 'PLANTIO_DIRETO' | 'CONVENCIONAL';
export type GrupoBotanico = 'LEGUMINOSA' | 'GRAMINEA';
export type TexturaSolo = 'LEVE' | 'MEDIA' | 'ARGILOSA' | 'MUITO_ARGILOSA';
export type ProfundidadeCm = '0-20' | '20-40';
export type MetodoPh = 'CACL2' | 'AGUA';

export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
  erros?: string[];
  /** Erros de validação por campo enviado. */
  campos?: Record<string, string[]>;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  registroProfissional: string | null;
  dataCriacao: string | null;
}

export interface TokenResponse {
  token: string;
  tipo: string;
  expiraEm: string;
  usuario: Usuario;
}

export interface UsuarioRequest {
  nome: string;
  email: string;
  senha: string;
  perfil: Perfil;
  registroProfissional?: string | null;
}

export interface UsuarioAtualizacao {
  nome: string;
  email: string;
  senha?: string | null;
  registroProfissional?: string | null;
}

export interface Vinculo {
  agronomo: Usuario;
  produtor: Usuario;
}

export interface VersaoParametros {
  id: number;
  codigo: string;
  descricao: string;
  conferidaManual: boolean;
}

export interface ContaDemonstracao {
  perfil: Perfil;
  nome: string;
  email: string;
  senha: string;
}

export interface SistemaInfo {
  versaoParametros: VersaoParametros | null;
  contasDemonstracao: ContaDemonstracao[];
}

export interface Propriedade {
  id: number;
  produtorId: number;
  produtorNome: string | null;
  nome: string;
  municipio: string;
  uf: string;
  quantidadeTalhoes: number;
  areaTotalHectares: number;
}

export interface PropriedadeRequest {
  produtorId?: number | null;
  nome: string;
  municipio: string;
}

export interface Talhao {
  id: number;
  identificacao: string;
  areaHectares: number;
  texturaPredominante: TexturaSolo | null;
  propriedadeId: number;
  propriedadeNome: string;
  municipio: string;
  uf: string;
  produtorId: number;
  produtorNome: string;
}

export interface TalhaoRequest {
  identificacao: string;
  areaHectares: number;
  texturaPredominante: TexturaSolo | null;
}

/** Dose como o manual a apresenta, em kg/ha. Dose fixa tem mínimo igual ao máximo. */
export interface FaixaDose {
  min: number;
  max: number;
}

export interface Cultura {
  id: number;
  nome: string;
  vAlvo: number;
  vMinimo: number | null;
  phMinIdeal: number;
  phMaxIdeal: number;
  grupo: GrupoBotanico;
}

export interface Indicadores {
  somaBases: number;
  ctcEfetiva: number;
  ctcTotal: number;
  saturacaoBases: number;
  saturacaoAluminio: number;
}

export interface IndicadoresRequest {
  caCmolc: number;
  mgCmolc: number;
  kCmolc: number;
  alCmolc: number;
  hAlCmolc: number;
}

export interface ItemInterpretacao {
  atributo: string;
  rotulo: string;
  valor: number;
  unidade: string;
  classe: string | null;
}

export interface Laudo {
  id: number;
  talhaoId: number;
  talhaoIdentificacao: string | null;
  propriedadeId: number | null;
  propriedadeNome: string | null;
  produtorId: number | null;
  produtorNome: string | null;
  dataAnalise: string;
  profundidadeCm: ProfundidadeCm;
  ph: number;
  metodoPh: MetodoPh;
  moGDm3: number;
  pMehlich: number;
  kCmolc: number;
  caCmolc: number;
  mgCmolc: number;
  alCmolc: number;
  hAlCmolc: number;
  argilaPorcentagem: number;
  znMgDm3: number | null;
  bMgDm3: number | null;
  cuMgDm3: number | null;
  mnMgDm3: number | null;
  indicadores: Indicadores;
  interpretacao: ItemInterpretacao[];
}

export interface LaudoRequest {
  talhaoId: number;
  dataAnalise: string;
  profundidadeCm: ProfundidadeCm;
  ph: number;
  metodoPh: MetodoPh;
  moGDm3: number;
  pMehlich: number;
  kCmolc: number;
  caCmolc: number;
  mgCmolc: number;
  alCmolc: number;
  hAlCmolc: number;
  argilaPorcentagem: number;
  znMgDm3?: number | null;
  bMgDm3?: number | null;
  cuMgDm3?: number | null;
  mnMgDm3?: number | null;
}

export interface FiltroLaudos {
  talhaoId?: number | null;
  profundidadeCm?: string | null;
  inicio?: string | null;
  fim?: string | null;
}

export interface RecomendacaoRequest {
  analiseSoloId: number;
  culturaAlvoId: number;
  culturaAnteriorId: number | null;
  grupoAnterior: GrupoBotanico | null;
  sistemaManejo: SistemaManejo;
  expectativaRendimentoTonHa: number;
  prntCalcario: number;
}

export interface Recomendacao {
  id: number | null;
  status: StatusRecomendacao;
  analiseSoloId: number;
  resumoQuimico: {
    somaBases: number;
    ctcEfetiva: number;
    ctcTotal: number;
    v1Porcentagem: number;
    mPorcentagem: number;
  };
  calagem: {
    necessario: boolean;
    v2AlvoPorcentagem: number;
    necessidadeCalcarioTonHa: number;
    ncCalculadaTonHa: number;
    observacao: string;
    justificativa: string;
  };
  gessagem: {
    necessario: boolean;
    doseKgHa: number;
    avaliada: boolean;
    justificativa: string;
  };
  adubacao: {
    doseNkgHa: FaixaDose;
    doseP2O5kgHa: FaixaDose;
    doseK2OkgHa: FaixaDose;
    justificativaN: string;
    classeFosforo: string;
    justificativaP: string;
    classePotassio: string;
    justificativaK: string;
    classeMateriaOrganica: string;
  };
  micronutrientes: {
    nutriente: string;
    simbolo: string;
    avaliado: boolean;
    teor: number | null;
    classe: string | null;
    doseKgHa: number | null;
    justificativa: string;
  }[];
  culturaAlvo: Cultura;
  culturaAnterior: Cultura | null;
  grupoAnterior: GrupoBotanico | null;
  antecessora: string;
  sistemaManejo: SistemaManejo;
  expectativaRendimentoTonHa: number;
  prntCalcario: number;
  talhao: Talhao | null;
  laudo: Laudo;
  laudoSubsuperficieId: number | null;
  versaoParametros: VersaoParametros;
  dataGeracao: string;
  dataAdocao: string | null;
  responsavel: { id: number; nome: string; registroProfissional: string | null } | null;
  avisos: string[];
  memoriaCalculo: string[];
}

/** Planejado na recomendação adotada e resultado de campo; o resultado fica nulo até ser registrado. */
export interface Safra {
  recomendacaoId: number;
  talhaoId: number;
  talhao: string;
  propriedade: string;
  cultura: string;
  dataAdocao: string;
  produtividadeEsperadaTonHa: number;
  produtividadeColhidaTonHa: number | null;
  dataColheita: string | null;
  diferencaPercentual: number | null;
  custoAdubacaoReais: number | null;
  custoPorHectareReais: number | null;
}

export interface RegistroSafraRequest {
  produtividadeColhidaTonHa: number | null;
  dataColheita: string | null;
  custoAdubacaoReais: number | null;
}

export interface FiltroRecomendacoes {
  talhaoId?: number | null;
  laudoId?: number | null;
  status?: StatusRecomendacao | null;
  inicio?: string | null;
  fim?: string | null;
}

export interface SugestaoAntecessora {
  cultura: Cultura;
  recomendacaoId: number;
  dataAdocao: string;
}

export interface EventoHistorico {
  tipo: 'LAUDO' | 'RECOMENDACAO_ADOTADA' | 'COLHEITA';
  data: string;
  referenciaId: number;
  titulo: string;
  detalhe: string | null;
  laudo: {
    profundidadeCm: ProfundidadeCm;
    ph: number;
    metodoPh: MetodoPh;
    v: number;
    m: number;
    mo: number;
    p: number;
    k: number;
  } | null;
  doses: {
    cultura: string;
    calcarioTonHa: number;
    gessoKgHa: number;
    nKgHa: FaixaDose;
    p2o5KgHa: FaixaDose;
    k2oKgHa: FaixaDose;
  } | null;
  safra: Safra | null;
}

export interface HistoricoTalhao {
  talhao: Talhao;
  eventos: EventoHistorico[];
}

export interface PontoEvolucao {
  data: string;
  laudoId: number;
  ph: number;
  metodoPh: MetodoPh;
  mo: number;
  p: number;
  k: number;
  ca: number;
  mg: number;
  v: number;
  m: number;
  ctc: number;
}

export interface Evolucao {
  talhao: Talhao;
  pontos: PontoEvolucao[];
  adotadas: {
    data: string;
    recomendacaoId: number;
    cultura: string;
    calcarioTonHa: number;
    gessoKgHa: number;
    nKgHa: FaixaDose;
    p2o5KgHa: FaixaDose;
    k2oKgHa: FaixaDose;
  }[];
  safras: Safra[];
}

export interface ResumoPainel {
  propriedades: number;
  talhoes: number;
  areaTotalHectares: number;
  laudos: number;
  recomendacoesAdotadas: number;
  simulacoesSalvas: number;
  colheitasRegistradas: number;
  colheitasRecentes: Safra[];
  laudosRecentes: {
    id: number;
    talhaoId: number;
    data: string;
    profundidadeCm: ProfundidadeCm;
    talhao: string;
    propriedade: string;
    v: number;
    ph: number;
    metodoPh: MetodoPh;
  }[];
  recomendacoesRecentes: {
    id: number;
    data: string;
    status: StatusRecomendacao;
    cultura: string;
    talhao: string;
    propriedade: string;
    calcarioTonHa: number;
  }[];
  talhoesSemLaudo: Talhao[];
}

export interface ParametrosVigentes {
  versao: VersaoParametros | null;
  culturas: Cultura[];
  faixas: {
    atributo: string;
    rotulo: string;
    unidade: string;
    criterioMin: number | null;
    criterioMax: number | null;
    teorMin: number | null;
    teorMax: number | null;
    classe: string;
  }[];
  /** Cada linha é uma célula das tabelas do manual; dose nula significa "Inviável". */
  npk: {
    cultura: string;
    nutriente: string;
    classe: string | null;
    grupoAntecessor: string | null;
    rendimentoMin: number;
    rendimentoMax: number | null;
    doseMin: number | null;
    doseMax: number | null;
  }[];
  gessagem: {
    limiteMPorcentagem: number;
    limiteCaCmolc: number;
    doses: { argilaMin: number; argilaMax: number | null; doseKgHa: number }[];
  } | null;
  calagem: { manejo: string; observacao: string }[];
  micronutrientes: { cultura: string; nutriente: string; classe: string; doseKgHa: number }[];
}
