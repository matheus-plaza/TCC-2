# Rastreabilidade: especificação (SRS), TCC e implementação

## Requisitos

| Requisito | Onde está | Como é verificado |
|---|---|---|
| RF01 Autenticação | `AuthController`, `JwtConfig`, `SecurityConfig`, `SenhaConfig` (BCrypt) | `SegurancaIntegracaoTest` |
| RF02 Propriedades e talhões | `GerenciarPropriedadesUseCase`, `GerenciarTalhoesUseCase`, tela `features/propriedades` | `SegurancaIntegracaoTest`, navegador |
| RF03 Laudo | `CadastrarLaudoUseCase`, `LaudoSolo`, Tela A em `features/laudos/laudo-form` | `LaudoSoloTest`, `FluxoRecomendacaoIntegracaoTest` |
| RF04 Interpretação | `IndicadoresSolo`, `ClassificadorFertilidade` | `LaudoSoloTest`, `FluxoRecomendacaoIntegracaoTest` |
| RF05 Simulação | `ProcessarRecomendacaoUseCase.simular`, Telas B e C | `simulacaoEmMemoria` compara o contrato e confirma que nada foi gravado |
| RF06 Adoção | `Recomendacao.adotar`, `AdotarRecomendacaoUseCase`, sugestão da antecessora | `adocaoDaRecomendacao`, `adocaoGeraCreditoNaProximaSafra` |
| RF07 Histórico | `ConsultarHistoricoTalhaoUseCase`, `features/propriedades/historico-talhao` | `adocaoDaRecomendacao`, navegador |
| RF08 Receituário | `GerarReceituarioUseCase`, `OpenPdfGeradorReceituario` | `adocaoDaRecomendacao`, conferência do PDF |
| Colheita e gasto com adubação (acréscimo ao SRS) | `RegistroSafra`, `RegistrarSafraUseCase`, `features/recomendacao/safra-card`, painel e linha do tempo | `RegistroSafraIntegracaoTest` |
| Cobre e manganês na soja (acréscimo ao SRS) | `Micronutriente`, `CalculadorMicronutrientes`, migração V9 | `ManualNeparIntegracaoTest` |
| Outra gramínea ou leguminosa como antecessora (acréscimo ao SRS) | `EntradaRecomendacao.grupoAnterior`, `CalculadorAdubacaoNPK`, migração V10 | `ManualNeparIntegracaoTest` |
| RNF01 Isolamento | Pacote `domain` sem framework | `ArquiteturaTest` |
| RNF02 Parametrização | Tabelas `culturas` e `nepar_*`, `ParametroRepository` | `novaVersaoDosParametros` troca a versão e o cálculo muda sem recompilar |
| RNF03 Precisão | `BigDecimal` e `NUMERIC`; `Decimais` com HALF_UP | `ArquiteturaTest` (sem `float`/`double` no domínio), `CasosControleTest` |
| RNF04 Desempenho | | `desempenhoDaSimulacao`: mediana abaixo de 300 ms |
| RNF05 RBAC | `ControleAcesso`, `EscopoProdutores`, `agronomo_produtor` | `SegurancaIntegracaoTest` |
| RNF06 Auditoria | `snapshot_auditoria`, `versao_parametros_id`, gatilho `trg_recomendacao_imutavel` | `snapshotImutavel` |

## Decisões onde a especificação era ambígua ou se contradizia

1. **NC de 1,96 ou 1,97.** A Tela C e o contrato mostram 1,96; o teste da seção 10, com V2 = 70, exige 1,97. A conta exata dá 1,965.
   Segui o teste: HALF_UP, 1,97. Para não acumular erro, o motor calcula `(V2 × T − 100 × SB) / PRNT`, que é a mesma
   fórmula sem a divisão intermediária de V1.
2. **"Teores > 0" (RF03).** Al³⁺ = 0 é um resultado comum em solo corrigido e recusá-lo impediria laudos reais. Aceito
   Al³⁺ ≥ 0; os demais teores continuam maiores que zero.
3. **RF05 sem gravar, RF06 pelo ID.** A simulação responde com `id: null`, mas a adoção pede um ID. Resolvi com três
   passos: `POST /simular` só calcula; `POST /recomendacoes` registra como SIMULACAO, recalculando no servidor;
   `POST /{id}/adotar` muda o status. O botão Adotar da tela faz os dois últimos de uma vez.
4. **Uma classe por linha em `nepar_regra_npk`, mas classes independentes para P e K.** Cada linha da tabela é uma célula
   do manual, com o nutriente, a classe e, quando se aplica, a cultura anterior. Veja a decisão 11.
5. **Rótulos das classes.** A seção 2.3, o RF04, o DDL e a Tela C usam listas diferentes. As classes são texto vindo da
   tabela, então valem os rótulos do manual.
6. **Gessagem com laudo por camada.** O laudo agora tem uma única profundidade. A recomendação parte do laudo de 0-20 cm
   e avalia a gessagem com o laudo de 20-40 cm do mesmo talhão de data mais próxima. Sem ele, a gessagem aparece como
   "não avaliada", em vez de "não necessária", para não sugerir uma avaliação que não houve.
7. **pH abaixo do ideal com V1 acima do limite da cultura.** O SRS manda aplicar calagem, mas o método da saturação por bases
   não dá dose. O motor indica `necessario: true`, dose 0 e pede avaliação técnica, sem inventar um valor.
8. **Ampliações do contrato da API.** Os campos do exemplo da seção 8.1 foram mantidos, com acréscimos: justificativas,
   classes, micronutrientes, versão dos parâmetros, avisos e memória de cálculo.
9. **Perfil do produtor.** O SRS o descreve como leitor; o TCC o coloca como ator do UC06. Ele pode cadastrar as próprias
   propriedades e laudos, mas não simula nem adota: a recomendação é decisão técnica do agrônomo.
10. **Vínculo agrônomo-produtor.** O acesso nasce do consentimento: o produtor autoriza pelo e-mail do agrônomo, o
    agrônomo fica vinculado aos produtores que cadastra, ou o administrador vincula. Um agrônomo não consegue acessar sozinho
    os dados de um produtor qualquer.
11. **O manual prevalece sobre o SRS nos valores e nas regras agronômicas.** O SRS manda seguir o NEPAR, e o livro diverge dele
    em vários pontos: V2 da soja e do trigo, limite de V% para aplicar calcário, critérios e dose de gesso, doses em faixa,
    N em tabela direta e micronutrientes por classe. A lista completa, o efeito nos exemplos e as decisões de leitura estão em
    [`dados-do-manual.md`](dados-do-manual.md). Por isso, o contrato da seção 8.1 mudou: `doseNkgHa`, `doseP2O5kgHa` e
    `doseK2OkgHa` agora são objetos com `min` e `max`.

## O que mudou em relação ao DER e aos diagramas do TCC 1

Estes pontos mudaram durante a implementação:

| TCC 1 | Implementado, conforme o SRS |
|---|---|
| `usuarios.id` UUID | BIGSERIAL |
| `analises_solo` com `usuario_id` e `identificacao_talhao` | Tabelas `propriedades` e `talhoes`; `analises_solo.talhao_id` |
| `ph_agua` | `ph` e `metodo_ph` (CaCl₂ ou água). O SRS pede só `ph_cacl2`; a escolha do método é uma ampliação, que afeta apenas a classe mostrada, nenhuma dose |
| `ca_sub_cmolc`, `al_sub_cmolc` e `sat_al_sub_porcentagem` no mesmo laudo | Um laudo por camada: `profundidade_cm` |
| `regras_adubacao_fosforo/potassio/nitrogenio` | `nepar_regra_npk` e `nepar_faixa_interpretacao` |
| N dividido em semeadura e cobertura | Faixa de dose total de N, como nas tabelas do manual |
| Crédito e débito de N como ajuste calculado | Tabela do manual por cultura anterior: o efeito já está na dose |
| `regras_adubacao_enxofre`, `s_mg_dm3`, `dose_enxofre_s` | Fora do SRS: não há dose de S; o gesso é tratado como a fonte de enxofre |
| `ConfirmarRecomendacaoUseCase` / `EfetivarRecomendacaoUseCase` | `AdotarRecomendacaoUseCase` (status SIMULACAO → ADOTADA) |
| `SimularRecomendacaoUseCase`, `CalculoNeparEngine` | `ProcessarRecomendacaoUseCase`, `EngineCalculoAgronomico` |
| `/api/analises`, `AnaliseController` | `/api/v1/laudos`, `LaudoController` |

Seguem iguais ao TCC: Clean Architecture, Flyway, tabelas paramétricas, UC10 (`GerarDashboardEvolucaoUseCase`), UC11
(`GerarReceituarioUseCase`), gessagem por Ca e saturação por Al em profundidade e micronutrientes Zn e B (Cu e Mn foram acrescentados depois), com dose abaixo do
nível crítico. No manual, a dose vem da classe do teor e só existe para a soja.

## Pendências

1. **Validação com 50 a 100 laudos reais** (seção 3.4 do TCC). O mecanismo está pronto em `CasosControleTest`: cada linha de
   `casos-controle.csv` é um caso. Os casos atuais são sintéticos, cobrem as fórmulas e a tabela de gesso e foram calculados à
   parte, com frações exatas, por `docs/validacao/gerar_casos_controle.py`. Agora que as tabelas são as do manual, a comparação
   também pode incluir as classes e as faixas de dose.
2. **Deploy em nuvem**, marcado como desejável no TCC.

As duas verificações da seção 12 do SRS foram resolvidas pelo livro: o trigo tem V2 de 70%, e o fósforo tem uma única faixa de
argila acima de 40%.
