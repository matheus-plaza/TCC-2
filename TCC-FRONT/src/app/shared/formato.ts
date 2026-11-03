/** Remove acentos para comparar textos digitados pelo usuário. */
export function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase();
}

export function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/);
  return ((partes[0]?.[0] ?? '') + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase();
}

/** Classe CSS da cultura (cores de identificação). */
export function classeCultura(nome: string | null | undefined): string {
  return 'cultura cultura-' + normalizar(nome ?? '');
}

/**
 * Classe CSS do selo de fertilidade. Os rótulos vêm das tabelas paramétricas, então
 * um rótulo novo apenas fica sem cor específica.
 */
export function classeNivel(classe: string | null | undefined): string {
  return 'chip-nivel nivel-' + normalizar(classe ?? 'sem-classe').replace(/\s+/g, '-');
}

/** Diferença em relação ao esperado, sempre com o sinal: +5,3% ou -2,0%. */
export function diferencaComSinal(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) {
    return '—';
  }
  return `${valor > 0 ? '+' : ''}${valor.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}%`;
}

/** Dispara o download de um arquivo recebido da API. */
export function baixarArquivo(arquivo: Blob, nome: string): void {
  const url = URL.createObjectURL(arquivo);
  const link = document.createElement('a');
  link.href = url;
  link.download = nome;
  link.click();
  URL.revokeObjectURL(url);
}

export function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export const ROTULO_PERFIL = { ADMIN: 'Administrador', AGRONOMO: 'Agrônomo', PRODUTOR: 'Produtor' } as const;

export const ROTULO_MANEJO = { PLANTIO_DIRETO: 'Plantio Direto', CONVENCIONAL: 'Convencional' } as const;

export const ROTULO_METODO_PH = { CACL2: 'CaCl₂', AGUA: 'H₂O' } as const;

export const ROTULO_TEXTURA = {
  LEVE: 'Leve',
  MEDIA: 'Média',
  ARGILOSA: 'Argilosa',
  MUITO_ARGILOSA: 'Muito argilosa',
} as const;
