# Dados tirados do Manual de Adubação e Calagem

Fonte: Manual de Adubação e Calagem para o Estado do Paraná, 2ª ed. (Pauletti e Motta, 2019, SBCS/NEPAR).

Este documento registra cada dado do livro usado pelo sistema e a página de onde saiu. As regras foram resumidas com
outras palavras; os números são os do livro.

**Situação:** publicado no sistema como a versão `NEPAR-2019.1`, nas migrações `V5__estrutura_do_manual.sql` e
`V6__parametros_manual_2019.sql`. Todos os valores foram conferidos no livro. A validação com laudos reais continua
pendente.

## 1. Escopo usado

Conforme a seção 1.4 do TCC: Estado do Paraná, soja, milho e trigo.

| Assunto | Seção do manual | Páginas |
|---|---|---|
| Classes de fertilidade | 3.4 | 39 a 41 |
| Calagem | 5.1 e 5.2 | 50 e 51 |
| Aplicação do calcário | 5.4 e 5.5 | 54 e 55 |
| Gessagem | 5.6 | 55 e 56 |
| Milho | 8.6 Milho Safra | 98 a 100 |
| Trigo | 8.8 | 104 a 106 |
| Soja | 9.4 | 120 a 122 |

Unidades do manual: dose em kg/ha, produtividade em t/ha, argila em g/kg e matéria orgânica em %. O sistema usa argila
em % (200 g/kg = 20%) e matéria orgânica em g/dm³ (1% = 10 g/dm³).

## 2. Calagem

### Fórmula (p. 50)

```text
NC (t/ha) = (V2 − V1) × CTC / PRNT
SB = Ca + Mg + K + Na
CTC = SB + (H + Al)
V% = SB × 100 / CTC
```

A fórmula do sistema é a mesma. O laudo do sistema não tem sódio (Na), porque o campo não faz parte da especificação.

### Por cultura

| Cultura | Aplicar quando V% estiver abaixo de | Elevar V% para (V2) | pH ideal em CaCl₂ (Tabela 5.1) | Página |
|---|---|---|---|---|
| Soja | 50% | 60% | 5,0 a 5,5 | 120 e 51 |
| Milho | 60% | 70% | 5,0 a 5,5 | 98 e 51 |
| Trigo | 60% | 70% | 5,0 a 5,5 | 104 e 51 |

A Tabela 5.1 coloca as três culturas no grupo de baixa tolerância à acidez, com V% de 51 a 70.

### PRNT (p. 51)

- Sem informação sobre o corretivo, o cálculo usa PRNT 100.
- A fórmula já compensa o PRNT: acima de 100, a dose diminui; abaixo de 100, aumenta.

### Aplicação do calcário (p. 55)

- **Semeadura direta consolidada:** a incorporação não é necessária. A dose pode ir na superfície, de uma vez ou parcelada em até três anos.
- **Implantação da semeadura direta:** incorporar, corrigindo pelo menos os 20 cm superficiais.
- **Manejo convencional:** incorporar de forma uniforme em toda a camada arável. Até 5 t/ha em solo cultivado: dose inteira, gradagem, aração e gradagem. Acima de 5 t/ha ou em solo não cultivado: metade antes da primeira gradagem e o restante depois da aração, seguido da segunda gradagem.

O manual **não traz teto de calagem por argila**.

## 3. Gessagem (p. 55 e 56)

Avaliada na camada de 20 a 40 cm. O gesso é indicado se pelo menos um critério for atendido:

| Critério | Limite |
|---|---|
| Saturação por alumínio (m%) | Maior que 20% |
| Cálcio | Menor que 0,5 cmolc/dm³ |

Dose, com efeito residual mínimo de cinco anos:

| Argila no manual | Argila no sistema | Gesso (kg/ha) |
|---|---|---|
| Até 200 g/kg | Até 20% | 700 |
| De 200 a 400 g/kg | Acima de 20% até 40% | 1.200 |
| De 400 a 600 g/kg | Acima de 40% até 60% | 2.200 |
| Mais de 600 g/kg | Acima de 60% | 3.200 |

O gesso não corrige a acidez nem substitui a calagem.

## 4. Classes de fertilidade

O manual define as classes pela produção máxima que o teor proporciona (p. 39): Muito baixo (até 40%), Baixo (40% a 70%),
Médio (70% a 90%), Alto (90% a 100%), Muito alto (pode limitar a produção) e Condição a evitar (pode reduzir a produção
por excesso).

**Tabela 3.2 (p. 40)**

| Classe | pH CaCl₂ | pH H₂O | Ca²⁺ (cmolc/dm³) | Mg²⁺ (cmolc/dm³) | m (%) | V (%) | M.O. (%) |
|---|---|---|---|---|---|---|---|
| Muito baixo | < 4,0 | < 4,7 | < 0,5 | < 0,2 | < 5 | < 20 | < 0,7 |
| Baixo | 4,0-4,4 | 4,7-5,1 | 0,5-1,0 | 0,2-0,4 | 5-10 | 21-35 | 0,7-1,4 |
| Médio | 4,5-4,9 | 5,2-5,6 | 1,1-2,0 | 0,5-1,0 | 11-20 | 36-50 | 1,5-2,4 |
| Alto | 5,0-5,5 | 5,7-6,2 | 2,1-6,0 | 1,1-2,0 | 21-50 | 51-70 | 2,5-3,4 |
| Muito alto | > 5,5 | > 6,2 | > 6,0 | > 2,0 | > 50 | > 70 | > 3,4 |
| Condição a evitar | > 6,0 | > 6,7 | | | | > 90 | |

**Tabela 3.3, fósforo em mg/dm³ por Mehlich-1 (p. 40)**

| Classe | Argila < 250 g/kg | Argila de 250 a 400 g/kg | Argila > 400 g/kg |
|---|---|---|---|
| Muito baixo | < 6 | < 4 | < 3 |
| Baixo | 6-12 | 4-8 | 3-6 |
| Médio | 13-18 | 9-12 | 7-9 |
| Alto | 19-24 | 13-18 | 10-12 |
| Muito alto | > 24 | > 18 | > 12 |
| Condição a evitar | > 120 | > 90 | > 60 |

**Tabela 3.4, potássio: coluna de % de K na CTC a pH 7,0 (p. 40)**

| Classe | % de K na CTC |
|---|---|
| Muito baixo | < 0,5 |
| Baixo | 0,5-1,0 |
| Médio | 1,1-2,0 |
| Alto | 2,1-3,0 |
| Muito alto | > 3,0 |
| Condição a evitar | > 10,0 |

**Tabela 3.5, micronutrientes em mg/dm³ (p. 41)**

| Classe | B | Zn |
|---|---|---|
| Muito baixo | < 0,10 | < 0,4 |
| Baixo | 0,11-0,20 | 0,4-0,8 |
| Médio | 0,21-0,30 | 0,9-1,2 |
| Alto | 0,31-0,60 | 1,3-10,0 |
| Muito alto | > 0,60 | > 10,0 |
| Condição a evitar | > 2,0 | > 30,0 |

## 5. Doses de NPK

Uma célula como `81-100` é a faixa de dose do manual. "Inviável" significa que o manual não indica essa meta de produtividade para o nível do solo.

### Soja

**Nitrogênio (p. 120):** não recebe N mineral, por causa da fixação biológica.

**P₂O₅ em kg/ha: Tabela 9.11 (p. 121)**

| P no solo | Menos de 3 t/ha | 3 a 4 t/ha | 4,1 a 5 t/ha | Mais de 5 t/ha |
|---|---|---|---|---|
| Muito baixo | 81-100 | 101-120 | Inviável | Inviável |
| Baixo | 61-80 | 81-100 | 101-120 | Inviável |
| Médio | 41-60 | 61-80 | 81-100 | 101-120 |
| Alto | 20-40 | 41-60 | 61-80 | 81-100 |
| Muito alto | 0 | 20-40 | 41-60 | 61-80 |
| Condição a evitar | 0 | 0 | 0 | 0 |

**K₂O em kg/ha: Tabela 9.12 (p. 121)**

| K no solo | Menos de 3 t/ha | 3 a 4 t/ha | 4,1 a 5 t/ha | Mais de 5 t/ha |
|---|---|---|---|---|
| Muito baixo | 91-110 | 111-130 | Inviável | Inviável |
| Baixo | 71-90 | 91-110 | 111-130 | Inviável |
| Médio | 51-70 | 71-90 | 91-110 | 111-130 |
| Alto | 30-50 | 51-70 | 71-90 | 91-110 |
| Muito alto | 0 | 0 | 50-70 | 71-90 |
| Condição a evitar | 0 | 0 | 0 | 0 |

**Orientação sem cálculo (p. 121 e 122):** depois de cultivo de inverno bem adubado, o fósforo pode ser dispensado na soja em certas condições, a critério da assistência técnica. Doses de K₂O acima de 80 kg/ha são divididas entre o sulco e a aplicação a lanço.

### Milho

**N em kg/ha: Tabela 8.21 (p. 99).** Vale para cultura anterior com mais de 2 t/ha de matéria seca.

| Cultura anterior | Menos de 8 t/ha | 8 a 12 t/ha | 13 a 16 t/ha | Mais de 16 t/ha |
|---|---|---|---|---|
| Gramínea | 80-120 | 121-180 | 181-260 | 261-340 |
| Leguminosa | 20-60 | 61-120 | 121-200 | 201-280 |

**P₂O₅ em kg/ha: Tabela 8.22 (p. 99)**

| P no solo | Menos de 8 t/ha | 8 a 12 t/ha | 13 a 16 t/ha | Mais de 16 t/ha |
|---|---|---|---|---|
| Muito baixo | 111-130 | Inviável | Inviável | Inviável |
| Baixo | 91-110 | 111-130 | 131-150 | Inviável |
| Médio | 71-90 | 91-110 | 111-130 | 131-150 |
| Alto | 51-70 | 71-90 | 91-110 | 111-130 |
| Muito alto | 30-50 | 51-70 | 71-90 | 91-110 |
| Condição a evitar | 0 | 0 | 0 | 0 |

**K₂O em kg/ha: Tabela 8.23 (p. 99)**

| K no solo | Menos de 8 t/ha | 8 a 12 t/ha | 13 a 16 t/ha | Mais de 16 t/ha |
|---|---|---|---|---|
| Muito baixo | 101-130 | Inviável | Inviável | Inviável |
| Baixo | 71-100 | 101-130 | 131-160 | 161-190 |
| Médio | 41-70 | 71-100 | 101-130 | 131-160 |
| Alto | 21-40 | 41-70 | 71-100 | 101-130 |
| Muito alto | 20 | 21-40 | 41-70 | 71-100 |
| Condição a evitar | 0 | 0 | 0 | 0 |

**Orientação sem cálculo (p. 98 e 99):** parte do N vai na semeadura e o restante em cobertura. K₂O acima de 60 kg/ha também é dividido entre o sulco e a cobertura.

### Trigo

**N em kg/ha: Tabela 8.29 (p. 105)**

| Cultivo anterior | M.O. no solo | Menos de 2,5 t/ha | 2,5 a 3,5 t/ha | 3,6 a 4,5 t/ha | Mais de 4,5 t/ha |
|---|---|---|---|---|---|
| Gramínea | Até Médio | 50-60 | 61-90 | 91-120 | 121-150 |
| Gramínea | Alto | 30-40 | 41-70 | 71-100 | 101-130 |
| Gramínea | Muito alto | 20-30 | 31-40 | 41-70 | 71-110 |
| Leguminosa | Até Médio | 30-50 | 51-70 | 71-90 | 91-120 |
| Leguminosa | Alto | 20-30 | 31-50 | 51-80 | 81-100 |
| Leguminosa | Muito alto | 20-30 | 31-40 | 41-50 | 51-80 |

**P₂O₅ em kg/ha: Tabela 8.30 (p. 105).** As linhas Alto e Muito alto têm os mesmos valores no livro.

| P no solo | Menos de 2,5 t/ha | 2,5 a 3,5 t/ha | 3,6 a 4,5 t/ha | Mais de 4,5 t/ha |
|---|---|---|---|---|
| Muito baixo | 51-60 | 61-80 | 81-100 | 101-120 |
| Baixo | 41-50 | 51-60 | 61-80 | 81-100 |
| Médio | 31-40 | 41-50 | 51-60 | 61-80 |
| Alto | 20-30 | 31-40 | 41-50 | 51-60 |
| Muito alto | 20-30 | 31-40 | 41-50 | 51-60 |
| Condição a evitar | 0 | 0 | 0 | 0 |

**K₂O em kg/ha: Tabela 8.31 (p. 106)**

| K no solo | Menos de 2,5 t/ha | 2,5 a 3,5 t/ha | 3,6 a 4,5 t/ha | Mais de 4,5 t/ha |
|---|---|---|---|---|
| Muito baixo | 41-50 | 51-60 | 61-80 | 81-100 |
| Baixo | 31-40 | 41-50 | 51-60 | 61-80 |
| Médio | 21-30 | 31-40 | 41-50 | 51-60 |
| Alto | 10-20 | 21-30 | 31-40 | 41-50 |
| Muito alto | 10-20 | 10-20 | 21-30 | 31-40 |
| Condição a evitar | 0 | 0 | 0 | 0 |

**Orientação sem cálculo (p. 105):** parte do N vai no sulco, com quantidade que depende da cultura anterior, e o restante em cobertura.

## 6. Micronutrientes

O escopo inicial do TCC considerava zinco e boro. Acrescentei cobre e manganês depois, para usar a Tabela 9.13 inteira.

| Cultura | O que o manual traz | Página |
|---|---|---|
| Soja | Dose conforme a classe do nutriente no solo | 122 |
| Milho | A seção não traz tabela de dose | 99 e 100 |
| Trigo | Até o momento, sem resposta a micronutrientes em pesquisas no Paraná | 106 |

**Soja, kg/ha: Tabela 9.13 (p. 122).** Aplicação na semeadura, quando há deficiência.

| Nutriente no solo | B | Cu | Mn | Zn |
|---|---|---|---|---|
| Baixo | 1,5 | 1,0 | 6,0 | 2,0 |
| Médio | 1,0 | 0,5 | 3,0 | 1,5 |
| Alto | 0 | 0 | 0 | 0 |
| Muito alto | 0 | 0 | 0 | 0 |
| Condição a evitar | 0 | 0 | 0 | 0 |

**Classes de cobre e manganês: Tabela 3.5, em mg/dm³ por Mehlich-1 (p. 41)**

| Classe | Cu | Mn |
|---|---|---|
| Muito baixo | < 0,2 | < 5 |
| Baixo | 0,2-0,5 | 5-15 |
| Médio | 0,6-0,8 | 16-30 |
| Alto | 0,9-3,0 | 31-100 |
| Muito alto | > 3,0 | > 100 |
| Condição a evitar | > 20 | > 200 |

**Restrição do manganês (p. 122).** O manual considera a análise de Mn no solo pouco confiável, por falta de calibração específica, e orienta aplicar quando houver sintomas na planta. Associa a deficiência à calagem excessiva e ao pH acima de 6,0 e recomenda a aplicação foliar: duas aplicações que somam 0,5 a 1 kg/ha. Por isso, o sistema mostra a dose da Tabela 9.13 sempre com essa orientação na justificativa e no receituário.

O manual define o nível crítico como o teor que proporciona 90% da produção máxima (p. 39), que é o início da classe Alto.
Por isso, a regra de limiar descrita no TCC continua válida: abaixo do nível crítico há dose; a partir dele, a dose é zero.

## 7. Decisões de interpretação

O livro não define estes pontos de forma explícita. A tabela registra a leitura que adotei em cada um.

| Ponto | Decisão | Motivo |
|---|---|---|
| Dose em faixa | A tela e o receituário mostram a faixa, e o total do talhão também é uma faixa | Nenhum número fora do livro |
| Classe do potássio | Usa a coluna de % de K na CTC | É o cruzamento com a CTC descrito no TCC, e a coluna tem as seis classes das tabelas de dose |
| Zn, B, Cu ou Mn em Muito baixo, na soja | Usa a dose da linha Baixo e registra isso na justificativa e na memória de cálculo | A Tabela 9.13 começa em Baixo |
| "Inviável" | O sistema recusa o cenário e explica o motivo, sem dar dose | Não há valor no livro |
| Cultura anterior fora da lista | O usuário escolhe "Outra gramínea" ou "Outra leguminosa", e o sistema usa a linha desse grupo. A classificação cabe ao agrônomo. Brássicas, como nabo forrageiro e canola, não têm linha na tabela | As Tabelas 8.21 e 8.29 usam só o grupo da cultura anterior |
| Milho ou trigo sem antecessora (pousio) | O sistema recusa o cenário e explica o motivo. Na soja, segue normalmente, com N igual a 0 | As tabelas de N só têm gramínea e leguminosa |
| M.O. "até Médio" no trigo | Vale para Muito baixo, Baixo e Médio | Leitura direta da tabela |
| Lacunas de produtividade | Uma meta entre colunas fica na coluna da esquerda: 4,05 t/ha de soja cai em 3 a 4; 12,5 t/ha de milho, em 8 a 12; 3,55 t/ha de trigo, em 2,5 a 3,5 | O livro pula de 4 para 4,1, de 12 para 13 e de 3,5 para 3,6 |
| Lacunas de classes | Um teor entre classes fica na classe de baixo: fósforo 12,5 com argila abaixo de 25% é Baixo | As tabelas pulam de 12 para 13, por exemplo |
| Valor no início de uma lacuna | Boro de exatamente 0,10 é Baixo, e V% de exatamente 20 é Baixo | O livro diz "< 0,10" e "0,11-0,20", sem incluir o 0,10; o mesmo vale para V% |
| Classe de K e de V% | São calculadas com uma casa decimal, como na tabela | A precisão das tabelas é de uma casa |
| Argila em limite duplo | 40% fica na faixa de 20 a 40%, e 60% na de 40 a 60% | O manual usa "até" na primeira faixa e "mais de" na última |
| pH baixo com V% acima do limite | Aponta a condição, com dose zero, e pede avaliação técnica | O método da saturação por bases não dá dose nesse caso |
| pH em água | O usuário informa o método no laudo, e a classe vem da coluna H₂O da Tabela 3.2. O valor não é convertido, nenhuma dose muda e o aviso de pH abaixo do ideal fica desligado | A Tabela 5.1 só dá o pH ideal em CaCl₂, e o manual não traz fórmula de conversão |
| Milho | Usa a seção 8.6, Milho Safra | O escopo do TCC é a safra principal. A seção 8.7, Milho Segunda Safra, ficou fora |

## 8. O que mudou no sistema

| Item | Antes (SRS ou `DEMO-2026.1`) | Com o manual |
|---|---|---|
| V2 da soja | 70% | **60%** |
| V2 do trigo | 60% (o SRS pedia conferir 60 ou 65) | **70%** |
| Quando aplicar calcário | Sempre que V1 < V2 | Só quando V1 estiver abaixo do limite da cultura |
| pH ideal | 5,5 a 6,2 (soja e milho) e 5,2 a 6,0 (trigo) | 5,0 a 5,5 nas três |
| Teto de calagem | Ilustrativo, por argila | Removido |
| Critérios do gesso | Al³⁺ > 0,2; V% < 20; Ca²⁺ < 0,5 | **m% > 20 ou Ca²⁺ < 0,5** |
| Dose de gesso | Argila × 50 | **Tabela de 700 a 3.200 kg/ha** |
| Doses de NPK | Um número, ilustrativo | Faixa do manual, ou recusa se "Inviável" |
| N no milho | Dose por M.O. + crédito ou débito | Tabela direta por cultura anterior e produtividade |
| N no trigo | Dose por M.O. + crédito ou débito | Tabela direta por M.O., cultivo anterior e produtividade |
| Classe de K | Teor em cmolc/dm³ | Porcentagem de K na CTC |
| P com argila acima de 60% | Pendência do SRS | O manual usa uma única faixa acima de 40% |
| Micronutrientes | Teor crítico por cultura, nas três | Por classe na soja; sem dose em milho e trigo |
| pH do laudo | Só em CaCl₂ | Em CaCl₂ ou em água, escolhido no registro, apenas para a classe mostrada |

**Efeito no laudo de exemplo da Tela A** (V1 = 49,2%, T = 9,45, P = 12, K = 0,35, argila 45%, M.O. 28,5 g/dm³):

| Cenário | Antes | Com o manual |
|---|---|---|
| Soja, 4,5 t/ha, PRNT 100: calcário | 1,97 t/ha | **1,02 t/ha** = (60 × 9,45 − 100 × 4,65) / 100 |
| Soja: P₂O₅ | 80 kg/ha (classe Médio) | **61 a 80 kg/ha** (classe Alto) |
| Soja: K₂O | 50 kg/ha (classe Adequado) | **50 a 70 kg/ha** (classe Muito alto: 3,7% da CTC) |
| Gleba 02, camada de 20-40 cm (argila 34%, m% = 51,1): gesso | 1.700 kg/ha | **1.200 kg/ha** |

O teste literal da seção 10 do SRS (NC = 1,97 com V2 = 70) continua válido como teste da fórmula, mas deixou de representar a soja: hoje, 1,97 é o resultado desse laudo para milho ou trigo.

**O manual diverge do SRS nesses pontos e prevaleceu.** As divergências estão em [`rastreabilidade.md`](rastreabilidade.md).

### Fora do escopo

O manual traz, mas o sistema não implementa:

- outras culturas e o Milho Segunda Safra;
- cobalto, molibdênio e enxofre;
- análise foliar e tabelas de demanda de nutrientes, como a 9.14, que não trazem dose;
- classes de Al³⁺, T e t, e carbono orgânico;
- classes de olerícolas, florestais, pastagens, alfafa e café;
- escolha de fórmulas NPK e adubos orgânicos (p. 64);
- cálculo de PN, RE e PRNT do corretivo (p. 52 e 54);
- fator de correção da profundidade de incorporação (p. 51);
- divisão automática entre semeadura e cobertura.
