/** Explicações em linguagem simples dos termos técnicos exibidos na interface. */
export const GLOSSARIO = {
  SB: 'Soma de bases: Ca + Mg + K, em cmolc/dm³.',
  t: 'CTC efetiva: soma de bases mais o alumínio. Mostra o que o solo retém no pH atual.',
  CTC: 'CTC a pH 7,0 (T): o quanto o solo consegue reter de nutrientes. É a soma de bases mais a acidez potencial (H+Al).',
  V: 'Saturação por bases (V%): parte da CTC ocupada por Ca, Mg e K. A calagem eleva o V% atual até o valor ideal da cultura.',
  m: 'Saturação por alumínio (m%): parte da CTC efetiva ocupada pelo alumínio, que é tóxico às raízes.',
  HAl: 'H+Al (acidez potencial): acidez retida nas partículas do solo, que a calagem precisa neutralizar.',
  PRNT: 'Poder Relativo de Neutralização Total: eficiência do calcário, informada pelo fabricante na nota ou na embalagem. Quanto maior o PRNT, menor a dose.',
  gesso: 'Gesso agrícola: leva cálcio e enxofre até 20-40 cm de profundidade e reduz o alumínio tóxico. Não corrige o pH.',
  mehlich: 'Mehlich-1: método do laboratório para extrair o fósforo disponível do solo.',
  antecessora: 'Cultura plantada antes no talhão. Leguminosas, como a soja, deixam nitrogênio no solo; gramíneas, como milho e trigo, deixam palhada que consome nitrogênio. Se a cultura não estiver na lista, escolha o grupo dela; a classificação cabe ao agrônomo. Brássicas, como nabo forrageiro e canola, não são gramíneas nem leguminosas, e as tabelas do manual não têm linha para elas. No milho, a tabela vale para cultura anterior com mais de 2 t/ha de matéria seca.',
  rendimento: 'Produtividade esperada para a safra, em toneladas por hectare. Metas maiores exigem doses maiores.',
  profundidade: 'Calagem e adubação usam a camada de 0-20 cm. A gessagem é avaliada com o laudo de 20-40 cm do mesmo talhão.',
  argila: 'Teor de argila do solo. Muda a classe de fósforo e a dose de gesso.',
  manganes: 'O manual pede restrição na análise de manganês no solo. Mesmo com o teor baixo, aplique só se houver sintomas de deficiência na planta.',
  metodoPh:'O laudo informa se o pH foi medido em CaCl₂ ou em H₂O (água), e os valores são diferentes. O método só define a classe mostrada; as doses não dependem dele.',
  manejo: 'No Plantio Direto o calcário vai em superfície, sem revolver o solo. No Convencional ele é incorporado.',
} as const;

export type TermoGlossario = keyof typeof GLOSSARIO;
