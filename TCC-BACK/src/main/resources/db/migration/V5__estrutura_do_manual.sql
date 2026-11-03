-- =====================================================================
-- V5: adapta as tabelas paramétricas à estrutura real do Manual de Adubação
-- e Calagem para o Estado do Paraná, 2ª ed.
--   * doses de NPK em faixas, com a condição "Inviável";
--   * N por cultura anterior, sem crédito ou débito separados;
--   * gesso por faixa de argila, sem fórmula;
--   * calagem acionada por um limite de V% da cultura, sem teto por argila;
--   * micronutrientes por classe de fertilidade.
--
-- Os parâmetros ilustrativos da versão DEMO-2026.1 são removidos para que nunca
-- sejam usados. As recomendações geradas com ela continuam íntegras, porque
-- guardam a cópia completa em snapshot_auditoria (RNF06).
-- =====================================================================

DELETE FROM nepar_faixa_interpretacao;
DELETE FROM nepar_regra_npk;
DELETE FROM nepar_regra_calagem;
DELETE FROM nepar_regra_gessagem;
DELETE FROM nepar_regra_micronutriente;
DROP TABLE nepar_ajuste_nitrogenio;

-- Limite de V% abaixo do qual o manual indica o calcário. O preenchimento e o
-- NOT NULL ficam na V6, junto com os demais dados das culturas.
ALTER TABLE culturas ADD COLUMN v_minimo NUMERIC(5,2);

-- Cada linha é uma célula das tabelas de dose do manual.
--   classe_fertilidade NULL: a tabela não depende de classe (N do milho).
--   grupo_antecessor NULL: a tabela não depende da cultura anterior (P₂O₅ e K₂O).
--   faixa_rendimento_max NULL: última coluna, sem limite ("> 5").
--   dose_min e dose_max NULL: célula "Inviável".
ALTER TABLE nepar_regra_npk
    DROP CONSTRAINT ck_npk_rendimento,
    DROP CONSTRAINT ck_npk_doses,
    DROP COLUMN dose_n,
    DROP COLUMN dose_p2o5,
    DROP COLUMN dose_k2o,
    ALTER COLUMN classe_fertilidade DROP NOT NULL,
    ALTER COLUMN faixa_rendimento_max DROP NOT NULL,
    ADD COLUMN nutriente VARCHAR(10) NOT NULL,
    ADD COLUMN grupo_antecessor grupo_botanico,
    ADD COLUMN dose_min NUMERIC(6,2),
    ADD COLUMN dose_max NUMERIC(6,2),
    ADD CONSTRAINT ck_npk_nutriente CHECK (nutriente IN ('N', 'P2O5', 'K2O')),
    ADD CONSTRAINT ck_npk_rendimento CHECK (faixa_rendimento_max IS NULL OR faixa_rendimento_max > faixa_rendimento_min),
    ADD CONSTRAINT ck_npk_doses CHECK ((dose_min IS NULL AND dose_max IS NULL) OR (dose_min >= 0 AND dose_max >= dose_min));

-- O manual não traz teto por argila: resta a forma de aplicação de cada manejo.
DROP INDEX ix_calagem_versao_manejo;
ALTER TABLE nepar_regra_calagem
    DROP CONSTRAINT ck_calagem_argila,
    DROP COLUMN argila_min,
    DROP COLUMN argila_max,
    DROP COLUMN teto_ton_ha,
    ADD CONSTRAINT uk_calagem UNIQUE (versao_id, manejo);

-- Critérios da gessagem na camada de 20-40 cm: saturação por alumínio e cálcio.
ALTER TABLE nepar_regra_gessagem
    DROP COLUMN limite_al_cmolc,
    DROP COLUMN limite_v_porcentagem,
    DROP COLUMN fator_argila_kg_ha,
    ADD COLUMN limite_m_porcentagem NUMERIC(5,2) NOT NULL;

CREATE TABLE nepar_dose_gesso (
    id         BIGSERIAL PRIMARY KEY,
    versao_id  BIGINT       NOT NULL REFERENCES nepar_versao_parametros (id),
    argila_min NUMERIC(6,2) NOT NULL,
    argila_max NUMERIC(6,2),
    dose_kg_ha NUMERIC(6,2) NOT NULL,
    CONSTRAINT ck_dose_gesso_argila CHECK (argila_max IS NULL OR argila_max > argila_min)
);
CREATE INDEX ix_dose_gesso_versao ON nepar_dose_gesso (versao_id);

-- Dose por classe do nutriente. A observação explica decisões de leitura da tabela.
ALTER TABLE nepar_regra_micronutriente
    DROP CONSTRAINT uk_micro,
    DROP COLUMN teor_critico,
    ADD COLUMN classe VARCHAR(20) NOT NULL,
    ADD COLUMN observacao TEXT,
    ADD CONSTRAINT uk_micro UNIQUE (versao_id, cultura_id, nutriente, classe);

-- As recomendações passam a guardar a faixa de dose. Nas já registradas, o mínimo e
-- o máximo recebem a dose única da época, sem alterar o valor. O gatilho é desligado
-- só nesta mudança de estrutura; o snapshot de auditoria não é tocado.
ALTER TABLE recomendacoes DISABLE TRIGGER trg_recomendacao_imutavel;

ALTER TABLE recomendacoes RENAME COLUMN dose_n_kg_ha TO dose_n_min_kg_ha;
ALTER TABLE recomendacoes RENAME COLUMN dose_p2o5_kg_ha TO dose_p2o5_min_kg_ha;
ALTER TABLE recomendacoes RENAME COLUMN dose_k2o_kg_ha TO dose_k2o_min_kg_ha;
ALTER TABLE recomendacoes
    ADD COLUMN dose_n_max_kg_ha NUMERIC(6,2),
    ADD COLUMN dose_p2o5_max_kg_ha NUMERIC(6,2),
    ADD COLUMN dose_k2o_max_kg_ha NUMERIC(6,2);
UPDATE recomendacoes
   SET dose_n_max_kg_ha = dose_n_min_kg_ha,
       dose_p2o5_max_kg_ha = dose_p2o5_min_kg_ha,
       dose_k2o_max_kg_ha = dose_k2o_min_kg_ha;
ALTER TABLE recomendacoes
    ALTER COLUMN dose_n_max_kg_ha SET NOT NULL,
    ALTER COLUMN dose_p2o5_max_kg_ha SET NOT NULL,
    ALTER COLUMN dose_k2o_max_kg_ha SET NOT NULL;

ALTER TABLE recomendacoes ENABLE TRIGGER trg_recomendacao_imutavel;
