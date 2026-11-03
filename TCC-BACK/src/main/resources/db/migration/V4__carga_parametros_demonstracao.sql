-- =====================================================================
-- V4: carga inicial, versão DEMONSTRAÇÃO.
--
-- ATENÇÃO: esta versão NÃO foi conferida com o Manual NEPAR-SBCS (2ª ed.).
--   * Vêm da especificação (SRS): V2 das culturas (2.2.A) e critérios e fator
--     da gessagem (2.2.B).
--   * Valores ILUSTRATIVOS, escolhidos para reproduzir o exemplo da Tela C e
--     para o sistema funcionar: pH ideal, classes de fertilidade, doses de NPK,
--     crédito/débito de N, teto de calagem e micronutrientes.
--
-- A conferida_manual = FALSE faz a interface e o receituário exibirem o aviso.
-- Para publicar os valores reais do manual, veja docs/parametros-nepar.md.
-- =====================================================================

INSERT INTO nepar_versao_parametros (codigo, descricao, conferida_manual, vigente) VALUES
    ('DEMO-2026.1',
     'Demonstração: V2 e gessagem conforme o SRS; classes, doses e demais valores são ilustrativos e NÃO são do Manual NEPAR.',
     FALSE, TRUE);

-- ---------------------------------------------------------------------
-- Culturas. V2 do SRS; no intervalo do SRS (Soja e Milho), o limite superior.
-- Pendência do SRS: confirmar se o V2 do trigo é 60% ou 65%.
-- ---------------------------------------------------------------------
INSERT INTO culturas (nome, v_alvo, ph_min_ideal, ph_max_ideal, grupo) VALUES
    ('Soja',  70.0, 5.50, 6.20, 'LEGUMINOSA'),
    ('Milho', 70.0, 5.50, 6.20, 'GRAMINEA'),
    ('Trigo', 60.0, 5.20, 6.00, 'GRAMINEA');

-- ---------------------------------------------------------------------
-- Gessagem: valores do SRS (seção 2.2.B)
-- ---------------------------------------------------------------------
INSERT INTO nepar_regra_gessagem (versao_id, limite_al_cmolc, limite_v_porcentagem, limite_ca_cmolc, fator_argila_kg_ha)
SELECT id, 0.2, 20, 0.5, 50 FROM nepar_versao_parametros WHERE codigo = 'DEMO-2026.1';

-- ---------------------------------------------------------------------
-- Aplicação do calcário. A observação do Plantio Direto é a do contrato do SRS;
-- os tetos são ilustrativos.
-- ---------------------------------------------------------------------
INSERT INTO nepar_regra_calagem (versao_id, manejo, argila_min, argila_max, teto_ton_ha, observacao)
SELECT v.id, r.manejo::sistema_manejo, r.argila_min, r.argila_max, r.teto, r.observacao
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    ('PLANTIO_DIRETO', 0::numeric, 25::numeric, 2.0::numeric, 'Aplicar calcário dolomítico em superfície.'),
    ('PLANTIO_DIRETO', 25, 60, 3.0, 'Aplicar calcário dolomítico em superfície.'),
    ('PLANTIO_DIRETO', 60, NULL, 4.0, 'Aplicar calcário dolomítico em superfície.'),
    ('CONVENCIONAL', 0, NULL, NULL, 'Incorporar o calcário na camada de 0-20 cm antes da semeadura.')
 ) AS r(manejo, argila_min, argila_max, teto, observacao)
 WHERE v.codigo = 'DEMO-2026.1';

-- ---------------------------------------------------------------------
-- Classes de fertilidade (ilustrativas)
-- ---------------------------------------------------------------------
INSERT INTO nepar_faixa_interpretacao (versao_id, atributo, criterio_min, criterio_max, teor_min, teor_max, classe)
SELECT v.id, f.atributo, f.cmin, f.cmax, f.tmin, f.tmax, f.classe
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    -- Fósforo (mg/dm³) por classe de argila (%)
    ('FOSFORO', 0::numeric, 25::numeric, 0::numeric, 8::numeric, 'Muito Baixo'),
    ('FOSFORO', 0, 25, 8, 16, 'Baixo'),
    ('FOSFORO', 0, 25, 16, 24, 'Médio'),
    ('FOSFORO', 0, 25, 24, 48, 'Alto'),
    ('FOSFORO', 0, 25, 48, NULL, 'Muito Alto'),
    ('FOSFORO', 25, 40, 0, 6, 'Muito Baixo'),
    ('FOSFORO', 25, 40, 6, 12, 'Baixo'),
    ('FOSFORO', 25, 40, 12, 18, 'Médio'),
    ('FOSFORO', 25, 40, 18, 36, 'Alto'),
    ('FOSFORO', 25, 40, 36, NULL, 'Muito Alto'),
    ('FOSFORO', 40, 60, 0, 4, 'Muito Baixo'),
    ('FOSFORO', 40, 60, 4, 8, 'Baixo'),
    ('FOSFORO', 40, 60, 8, 16, 'Médio'),
    ('FOSFORO', 40, 60, 16, 30, 'Alto'),
    ('FOSFORO', 40, 60, 30, NULL, 'Muito Alto'),
    ('FOSFORO', 60, NULL, 0, 3, 'Muito Baixo'),
    ('FOSFORO', 60, NULL, 3, 6, 'Baixo'),
    ('FOSFORO', 60, NULL, 6, 12, 'Médio'),
    ('FOSFORO', 60, NULL, 12, 24, 'Alto'),
    ('FOSFORO', 60, NULL, 24, NULL, 'Muito Alto'),
    -- Potássio (cmolc/dm³)
    ('POTASSIO', NULL, NULL, 0, 0.10, 'Muito Baixo'),
    ('POTASSIO', NULL, NULL, 0.10, 0.20, 'Baixo'),
    ('POTASSIO', NULL, NULL, 0.20, 0.30, 'Médio'),
    ('POTASSIO', NULL, NULL, 0.30, 0.50, 'Adequado'),
    ('POTASSIO', NULL, NULL, 0.50, NULL, 'Alto'),
    -- Matéria orgânica (g/dm³)
    ('MATERIA_ORGANICA', NULL, NULL, 0, 20, 'Baixo'),
    ('MATERIA_ORGANICA', NULL, NULL, 20, 40, 'Médio'),
    ('MATERIA_ORGANICA', NULL, NULL, 40, NULL, 'Alto'),
    -- pH em CaCl₂
    ('PH', NULL, NULL, 0, 4.5, 'Muito Baixo'),
    ('PH', NULL, NULL, 4.5, 5.0, 'Baixo'),
    ('PH', NULL, NULL, 5.0, 5.5, 'Médio'),
    ('PH', NULL, NULL, 5.5, 6.2, 'Adequado'),
    ('PH', NULL, NULL, 6.2, NULL, 'Alto'),
    -- Cálcio e magnésio (cmolc/dm³)
    ('CALCIO', NULL, NULL, 0, 2, 'Baixo'),
    ('CALCIO', NULL, NULL, 2, 4, 'Médio'),
    ('CALCIO', NULL, NULL, 4, NULL, 'Alto'),
    ('MAGNESIO', NULL, NULL, 0, 0.5, 'Baixo'),
    ('MAGNESIO', NULL, NULL, 0.5, 1, 'Médio'),
    ('MAGNESIO', NULL, NULL, 1, NULL, 'Alto'),
    -- Saturações (%)
    ('SATURACAO_BASES', NULL, NULL, 0, 40, 'Baixo'),
    ('SATURACAO_BASES', NULL, NULL, 40, 60, 'Médio'),
    ('SATURACAO_BASES', NULL, NULL, 60, 80, 'Adequado'),
    ('SATURACAO_BASES', NULL, NULL, 80, NULL, 'Alto'),
    ('SATURACAO_ALUMINIO', NULL, NULL, 0, 5, 'Baixo'),
    ('SATURACAO_ALUMINIO', NULL, NULL, 5, 20, 'Médio'),
    ('SATURACAO_ALUMINIO', NULL, NULL, 20, NULL, 'Alto'),
    -- Micronutrientes (mg/dm³)
    ('ZINCO', NULL, NULL, 0, 1, 'Baixo'),
    ('ZINCO', NULL, NULL, 1, 2, 'Médio'),
    ('ZINCO', NULL, NULL, 2, NULL, 'Adequado'),
    ('BORO', NULL, NULL, 0, 0.3, 'Baixo'),
    ('BORO', NULL, NULL, 0.3, 0.6, 'Médio'),
    ('BORO', NULL, NULL, 0.6, NULL, 'Adequado')
 ) AS f(atributo, cmin, cmax, tmin, tmax, classe)
 WHERE v.codigo = 'DEMO-2026.1';

-- ---------------------------------------------------------------------
-- Doses de NPK (ilustrativas). A dose_n vale para as classes de M.O.; as demais
-- classes ficam com zero. Faixas: Soja [4, 5) com P Médio = 80 e K Adequado = 50.
-- ---------------------------------------------------------------------
WITH faixas (cultura, indice, minimo, maximo) AS (VALUES
    ('Soja', 0, 0.0::numeric, 3.0::numeric), ('Soja', 1, 3.0, 4.0), ('Soja', 2, 4.0, 5.0), ('Soja', 3, 5.0, 999.99),
    ('Milho', 0, 0.0, 6.0), ('Milho', 1, 6.0, 9.0), ('Milho', 2, 9.0, 12.0), ('Milho', 3, 12.0, 999.99),
    ('Trigo', 0, 0.0, 2.5), ('Trigo', 1, 2.5, 3.5), ('Trigo', 2, 3.5, 4.5), ('Trigo', 3, 4.5, 999.99)),
classes (classe, base_p, base_k, n_milho, n_trigo) AS (VALUES
    ('Muito Baixo', 100, 70, 0, 0),
    ('Baixo', 80, 50, 110, 60),
    ('Médio', 60, 40, 90, 50),
    ('Adequado', 50, 30, 0, 0),
    ('Alto', 40, 20, 70, 40),
    ('Muito Alto', 20, 10, 0, 0))
INSERT INTO nepar_regra_npk (versao_id, cultura_id, classe_fertilidade, faixa_rendimento_min, faixa_rendimento_max, dose_n, dose_p2o5, dose_k2o)
SELECT v.id, c.id, k.classe, f.minimo, f.maximo,
       CASE c.nome
           WHEN 'Soja' THEN 0
           WHEN 'Milho' THEN k.n_milho + CASE WHEN k.n_milho > 0 THEN 20 * f.indice ELSE 0 END
           ELSE k.n_trigo + CASE WHEN k.n_trigo > 0 THEN 10 * f.indice ELSE 0 END
       END,
       k.base_p + 10 * f.indice + CASE WHEN c.nome = 'Milho' THEN 20 ELSE 0 END,
       k.base_k + 10 * f.indice
  FROM faixas f
  JOIN culturas c ON c.nome = f.cultura
 CROSS JOIN classes k
 CROSS JOIN nepar_versao_parametros v
 WHERE v.codigo = 'DEMO-2026.1';

-- ---------------------------------------------------------------------
-- Crédito (-) e débito (+) de N pela antecessora (ilustrativos)
-- ---------------------------------------------------------------------
INSERT INTO nepar_ajuste_nitrogenio (versao_id, cultura_id, grupo_antecessor, ajuste_kg_ha)
SELECT v.id, c.id, a.grupo::grupo_botanico, a.ajuste
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    ('Milho', 'LEGUMINOSA', -20::numeric),
    ('Milho', 'GRAMINEA', 20),
    ('Trigo', 'LEGUMINOSA', -15),
    ('Trigo', 'GRAMINEA', 15)
 ) AS a(cultura, grupo, ajuste)
  JOIN culturas c ON c.nome = a.cultura
 WHERE v.codigo = 'DEMO-2026.1';

-- ---------------------------------------------------------------------
-- Micronutrientes (ilustrativos)
-- ---------------------------------------------------------------------
INSERT INTO nepar_regra_micronutriente (versao_id, cultura_id, nutriente, teor_critico, dose_kg_ha)
SELECT v.id, c.id, m.nutriente, m.critico, m.dose
  FROM nepar_versao_parametros v
 CROSS JOIN culturas c
 CROSS JOIN (VALUES ('ZINCO', 1.0::numeric, 3.0::numeric), ('BORO', 0.3, 1.0)) AS m(nutriente, critico, dose)
 WHERE v.codigo = 'DEMO-2026.1';
