# Parâmetros NEPAR

O motor não tem valores agronômicos no código (RNF02): tudo vem das tabelas paramétricas, ligadas a uma **versão**.
A versão vigente é `NEPAR-2019.1`, com os dados do manual. A origem de cada valor, por página, está em
[`dados-do-manual.md`](dados-do-manual.md).

## Onde cada dado fica

| Dado | Tabela |
|---|---|
| V2, limite de V% e pH ideal | `culturas` |
| Classes de fertilidade | `nepar_faixa_interpretacao` |
| Doses de N, P₂O₅ e K₂O | `nepar_regra_npk` |
| Forma de aplicação do calcário | `nepar_regra_calagem` |
| Critérios da gessagem | `nepar_regra_gessagem` |
| Dose de gesso por argila | `nepar_dose_gesso` |
| Doses de Zn, B, Cu e Mn | `nepar_regra_micronutriente` |

## A versão de demonstração

As migrações V1 a V4 criaram a versão `DEMO-2026.1`, com valores ilustrativos, usada antes de o manual estar disponível.
A migração V5 removeu esses valores, para que nunca sejam usados. O registro da versão continua no banco porque as
recomendações geradas com ela guardam a referência. O resultado delas segue preservado em `snapshot_auditoria` (RNF06), e o
sistema continua avisando, nessas recomendações, que os valores não foram conferidos.

## Como publicar uma nova edição do manual

1. Crie uma migração nova em `TCC-BACK/src/main/resources/db/migration`, com o próximo número. Use a
   `V6__parametros_manual_2019.sql` como modelo.
2. Marque a versão atual com `vigente = FALSE` e insira a nova com `vigente = TRUE`.
3. Insira as linhas das tabelas `nepar_*` ligadas à nova versão.
4. Atualize `culturas`, se a nova edição mudar o V2, o limite de V% ou o pH ideal.
5. Reinicie o backend. O Flyway aplica a migração, e a nova versão passa a valer.

A lógica não precisa ser recompilada. As recomendações já registradas continuam com o resultado e a versão da época.

Sobre as tabelas, vale saber que:

- as faixas são semiabertas, `[min, max)`: "até 24" e "acima de 24" viram `[19, 24.01)` e `[24.01, ...)`, porque o laudo tem duas casas decimais;
- faixa ausente ou sobreposta interrompe o cálculo, e a API lista o que falta, em vez de assumir um valor;
- as classes são texto, então devem ser idênticas em `nepar_faixa_interpretacao`, `nepar_regra_npk` e `nepar_regra_micronutriente`;
- em `nepar_regra_npk`, cada linha é uma célula da tabela do manual. Dose nula significa "Inviável";
- o potássio é enquadrado pela porcentagem de K na CTC, e o fósforo cruza o teor com a argila.
