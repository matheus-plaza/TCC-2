-- =====================================================================
-- V6: dados oficiais do Manual de Adubação e Calagem para o Estado do Paraná,
-- 2ª ed. (Pauletti e Motta, 2019, SBCS/NEPAR).
--
-- Cada valor está na página indicada. A transcrição completa e as decisões de
-- leitura estão em docs/dados-do-manual.md.
--
-- Convenção dos intervalos: [min, max). O mínimo é inclusivo, o máximo é exclusivo
-- e NULL significa "sem limite". Um limite como "até 24" vira 24.01 porque o laudo
-- tem duas casas decimais; nas lacunas do livro (de 12 para 13, por exemplo), o valor
-- fica na classe ou coluna de baixo.
-- =====================================================================

UPDATE nepar_versao_parametros SET vigente = FALSE WHERE vigente;

INSERT INTO nepar_versao_parametros (codigo, descricao, conferida_manual, vigente) VALUES
    ('NEPAR-2019.1',
     'Manual de Adubação e Calagem para o Estado do Paraná, 2ª ed. (Pauletti e Motta, 2019, SBCS/NEPAR).',
     TRUE, TRUE);

-- ---------------------------------------------------------------------
-- Culturas: V2 e limite de V% (p. 98, 104 e 120); pH ideal (Tabela 5.1, p. 51)
-- ---------------------------------------------------------------------
UPDATE culturas SET v_alvo = 60, v_minimo = 50, ph_min_ideal = 5.0, ph_max_ideal = 5.5 WHERE nome = 'Soja';
UPDATE culturas SET v_alvo = 70, v_minimo = 60, ph_min_ideal = 5.0, ph_max_ideal = 5.5 WHERE nome = 'Milho';
UPDATE culturas SET v_alvo = 70, v_minimo = 60, ph_min_ideal = 5.0, ph_max_ideal = 5.5 WHERE nome = 'Trigo';
ALTER TABLE culturas ALTER COLUMN v_minimo SET NOT NULL;

-- ---------------------------------------------------------------------
-- Aplicação do calcário por manejo (p. 55)
-- ---------------------------------------------------------------------
INSERT INTO nepar_regra_calagem (versao_id, manejo, observacao)
SELECT v.id, r.manejo::sistema_manejo, r.observacao
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    ('PLANTIO_DIRETO', 'Em área consolidada, aplicar em superfície, de uma vez ou parcelado em até três anos. Na implantação do sistema, incorporar até pelo menos 20 cm.'),
    ('CONVENCIONAL', 'Incorporar de forma uniforme em toda a camada arável. Até 5 t/ha em solo cultivado: dose inteira, gradagem, aração e gradagem. Acima de 5 t/ha ou em solo não cultivado: metade antes da primeira gradagem e o restante depois da aração, seguido da segunda gradagem.')
 ) AS r(manejo, observacao)
 WHERE v.codigo = 'NEPAR-2019.1';

-- ---------------------------------------------------------------------
-- Gessagem na camada de 20-40 cm (p. 56): critérios e dose por argila
-- ---------------------------------------------------------------------
INSERT INTO nepar_regra_gessagem (versao_id, limite_m_porcentagem, limite_ca_cmolc)
SELECT id, 20, 0.5 FROM nepar_versao_parametros WHERE codigo = 'NEPAR-2019.1';

INSERT INTO nepar_dose_gesso (versao_id, argila_min, argila_max, dose_kg_ha)
SELECT v.id, d.argila_min, d.argila_max, d.dose
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    (0::numeric, 20.01::numeric, 700::numeric),
    (20.01, 40.01, 1200),
    (40.01, 60.01, 2200),
    (60.01, NULL, 3200)
 ) AS d(argila_min, argila_max, dose)
 WHERE v.codigo = 'NEPAR-2019.1';

-- ---------------------------------------------------------------------
-- Classes de fertilidade (p. 39 a 41)
-- ---------------------------------------------------------------------
INSERT INTO nepar_faixa_interpretacao (versao_id, atributo, criterio_min, criterio_max, teor_min, teor_max, classe)
SELECT v.id, f.atributo, f.cmin, f.cmax, f.tmin, f.tmax, f.classe
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    -- Tabela 3.2, p. 40. pH em CaCl₂
    ('PH', NULL::numeric, NULL::numeric, 0::numeric, 4::numeric, 'Muito baixo'),
    ('PH', NULL, NULL, 4, 4.5, 'Baixo'),
    ('PH', NULL, NULL, 4.5, 5, 'Médio'),
    ('PH', NULL, NULL, 5, 5.51, 'Alto'),
    ('PH', NULL, NULL, 5.51, 6.01, 'Muito alto'),
    ('PH', NULL, NULL, 6.01, NULL, 'Condição a evitar'),
    -- Tabela 3.2. Cálcio (cmolc/dm³): sem Condição a evitar
    ('CALCIO', NULL, NULL, 0, 0.5, 'Muito baixo'),
    ('CALCIO', NULL, NULL, 0.5, 1.1, 'Baixo'),
    ('CALCIO', NULL, NULL, 1.1, 2.1, 'Médio'),
    ('CALCIO', NULL, NULL, 2.1, 6.001, 'Alto'),
    ('CALCIO', NULL, NULL, 6.001, NULL, 'Muito alto'),
    -- Tabela 3.2. Magnésio (cmolc/dm³): sem Condição a evitar
    ('MAGNESIO', NULL, NULL, 0, 0.2, 'Muito baixo'),
    ('MAGNESIO', NULL, NULL, 0.2, 0.5, 'Baixo'),
    ('MAGNESIO', NULL, NULL, 0.5, 1.1, 'Médio'),
    ('MAGNESIO', NULL, NULL, 1.1, 2.001, 'Alto'),
    ('MAGNESIO', NULL, NULL, 2.001, NULL, 'Muito alto'),
    -- Tabela 3.2. Saturação por alumínio (%): sem Condição a evitar
    ('SATURACAO_ALUMINIO', NULL, NULL, 0, 5, 'Muito baixo'),
    ('SATURACAO_ALUMINIO', NULL, NULL, 5, 11, 'Baixo'),
    ('SATURACAO_ALUMINIO', NULL, NULL, 11, 21, 'Médio'),
    ('SATURACAO_ALUMINIO', NULL, NULL, 21, 50.1, 'Alto'),
    ('SATURACAO_ALUMINIO', NULL, NULL, 50.1, NULL, 'Muito alto'),
    -- Tabela 3.2. Saturação por bases (%)
    ('SATURACAO_BASES', NULL, NULL, 0, 20, 'Muito baixo'),
    ('SATURACAO_BASES', NULL, NULL, 20, 36, 'Baixo'),
    ('SATURACAO_BASES', NULL, NULL, 36, 51, 'Médio'),
    ('SATURACAO_BASES', NULL, NULL, 51, 70.1, 'Alto'),
    ('SATURACAO_BASES', NULL, NULL, 70.1, 90.1, 'Muito alto'),
    ('SATURACAO_BASES', NULL, NULL, 90.1, NULL, 'Condição a evitar'),
    -- Tabela 3.2. Matéria orgânica: o manual usa %; o sistema, g/dm³ (1% = 10 g/dm³)
    ('MATERIA_ORGANICA', NULL, NULL, 0, 7, 'Muito baixo'),
    ('MATERIA_ORGANICA', NULL, NULL, 7, 15, 'Baixo'),
    ('MATERIA_ORGANICA', NULL, NULL, 15, 25, 'Médio'),
    ('MATERIA_ORGANICA', NULL, NULL, 25, 34.01, 'Alto'),
    ('MATERIA_ORGANICA', NULL, NULL, 34.01, NULL, 'Muito alto'),
    -- Tabela 3.3, p. 40. Fósforo (mg/dm³) com argila abaixo de 25%
    ('FOSFORO', 0, 25, 0, 6, 'Muito baixo'),
    ('FOSFORO', 0, 25, 6, 13, 'Baixo'),
    ('FOSFORO', 0, 25, 13, 19, 'Médio'),
    ('FOSFORO', 0, 25, 19, 24.01, 'Alto'),
    ('FOSFORO', 0, 25, 24.01, 120.01, 'Muito alto'),
    ('FOSFORO', 0, 25, 120.01, NULL, 'Condição a evitar'),
    -- Tabela 3.3. Fósforo com argila de 25% a 40%
    ('FOSFORO', 25, 40.01, 0, 4, 'Muito baixo'),
    ('FOSFORO', 25, 40.01, 4, 9, 'Baixo'),
    ('FOSFORO', 25, 40.01, 9, 13, 'Médio'),
    ('FOSFORO', 25, 40.01, 13, 18.01, 'Alto'),
    ('FOSFORO', 25, 40.01, 18.01, 90.01, 'Muito alto'),
    ('FOSFORO', 25, 40.01, 90.01, NULL, 'Condição a evitar'),
    -- Tabela 3.3. Fósforo com argila acima de 40%
    ('FOSFORO', 40.01, NULL, 0, 3, 'Muito baixo'),
    ('FOSFORO', 40.01, NULL, 3, 7, 'Baixo'),
    ('FOSFORO', 40.01, NULL, 7, 10, 'Médio'),
    ('FOSFORO', 40.01, NULL, 10, 12.01, 'Alto'),
    ('FOSFORO', 40.01, NULL, 12.01, 60.01, 'Muito alto'),
    ('FOSFORO', 40.01, NULL, 60.01, NULL, 'Condição a evitar'),
    -- Tabela 3.4, p. 40. Potássio: porcentagem de K na CTC a pH 7,0
    ('POTASSIO', NULL, NULL, 0, 0.5, 'Muito baixo'),
    ('POTASSIO', NULL, NULL, 0.5, 1.1, 'Baixo'),
    ('POTASSIO', NULL, NULL, 1.1, 2.1, 'Médio'),
    ('POTASSIO', NULL, NULL, 2.1, 3.1, 'Alto'),
    ('POTASSIO', NULL, NULL, 3.1, 10.1, 'Muito alto'),
    ('POTASSIO', NULL, NULL, 10.1, NULL, 'Condição a evitar'),
    -- Tabela 3.5, p. 41. Zinco (mg/dm³)
    ('ZINCO', NULL, NULL, 0, 0.4, 'Muito baixo'),
    ('ZINCO', NULL, NULL, 0.4, 0.9, 'Baixo'),
    ('ZINCO', NULL, NULL, 0.9, 1.3, 'Médio'),
    ('ZINCO', NULL, NULL, 1.3, 10.01, 'Alto'),
    ('ZINCO', NULL, NULL, 10.01, 30.01, 'Muito alto'),
    ('ZINCO', NULL, NULL, 30.01, NULL, 'Condição a evitar'),
    -- Tabela 3.5. Boro (mg/dm³)
    ('BORO', NULL, NULL, 0, 0.1, 'Muito baixo'),
    ('BORO', NULL, NULL, 0.1, 0.21, 'Baixo'),
    ('BORO', NULL, NULL, 0.21, 0.31, 'Médio'),
    ('BORO', NULL, NULL, 0.31, 0.61, 'Alto'),
    ('BORO', NULL, NULL, 0.61, 2.01, 'Muito alto'),
    ('BORO', NULL, NULL, 2.01, NULL, 'Condição a evitar')
 ) AS f(atributo, cmin, cmax, tmin, tmax, classe)
 WHERE v.codigo = 'NEPAR-2019.1';

-- ---------------------------------------------------------------------
-- Doses de NPK. Dose NULL = "Inviável" no manual. A soja não tem linhas de N,
-- porque a fixação biológica dispensa o N mineral (p. 120).
-- ---------------------------------------------------------------------
INSERT INTO nepar_regra_npk (versao_id, cultura_id, nutriente, classe_fertilidade, grupo_antecessor,
                             faixa_rendimento_min, faixa_rendimento_max, dose_min, dose_max)
SELECT v.id, c.id, r.nutriente, r.classe, r.grupo::grupo_botanico, r.rmin, r.rmax, r.dmin, r.dmax
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    -- Tabela 9.11, p. 121. P₂O₅ na soja
    ('Soja', 'P2O5', 'Muito baixo'::text, NULL::text, 0::numeric, 3::numeric, 81::numeric, 100::numeric),
    ('Soja', 'P2O5', 'Muito baixo', NULL, 3, 4.1, 101, 120),
    ('Soja', 'P2O5', 'Muito baixo', NULL, 4.1, 5.01, NULL, NULL),
    ('Soja', 'P2O5', 'Muito baixo', NULL, 5.01, NULL, NULL, NULL),
    ('Soja', 'P2O5', 'Baixo', NULL, 0, 3, 61, 80),
    ('Soja', 'P2O5', 'Baixo', NULL, 3, 4.1, 81, 100),
    ('Soja', 'P2O5', 'Baixo', NULL, 4.1, 5.01, 101, 120),
    ('Soja', 'P2O5', 'Baixo', NULL, 5.01, NULL, NULL, NULL),
    ('Soja', 'P2O5', 'Médio', NULL, 0, 3, 41, 60),
    ('Soja', 'P2O5', 'Médio', NULL, 3, 4.1, 61, 80),
    ('Soja', 'P2O5', 'Médio', NULL, 4.1, 5.01, 81, 100),
    ('Soja', 'P2O5', 'Médio', NULL, 5.01, NULL, 101, 120),
    ('Soja', 'P2O5', 'Alto', NULL, 0, 3, 20, 40),
    ('Soja', 'P2O5', 'Alto', NULL, 3, 4.1, 41, 60),
    ('Soja', 'P2O5', 'Alto', NULL, 4.1, 5.01, 61, 80),
    ('Soja', 'P2O5', 'Alto', NULL, 5.01, NULL, 81, 100),
    ('Soja', 'P2O5', 'Muito alto', NULL, 0, 3, 0, 0),
    ('Soja', 'P2O5', 'Muito alto', NULL, 3, 4.1, 20, 40),
    ('Soja', 'P2O5', 'Muito alto', NULL, 4.1, 5.01, 41, 60),
    ('Soja', 'P2O5', 'Muito alto', NULL, 5.01, NULL, 61, 80),
    ('Soja', 'P2O5', 'Condição a evitar', NULL, 0, 3, 0, 0),
    ('Soja', 'P2O5', 'Condição a evitar', NULL, 3, 4.1, 0, 0),
    ('Soja', 'P2O5', 'Condição a evitar', NULL, 4.1, 5.01, 0, 0),
    ('Soja', 'P2O5', 'Condição a evitar', NULL, 5.01, NULL, 0, 0),
    -- Tabela 9.12, p. 121. K₂O na soja
    ('Soja', 'K2O', 'Muito baixo', NULL, 0, 3, 91, 110),
    ('Soja', 'K2O', 'Muito baixo', NULL, 3, 4.1, 111, 130),
    ('Soja', 'K2O', 'Muito baixo', NULL, 4.1, 5.01, NULL, NULL),
    ('Soja', 'K2O', 'Muito baixo', NULL, 5.01, NULL, NULL, NULL),
    ('Soja', 'K2O', 'Baixo', NULL, 0, 3, 71, 90),
    ('Soja', 'K2O', 'Baixo', NULL, 3, 4.1, 91, 110),
    ('Soja', 'K2O', 'Baixo', NULL, 4.1, 5.01, 111, 130),
    ('Soja', 'K2O', 'Baixo', NULL, 5.01, NULL, NULL, NULL),
    ('Soja', 'K2O', 'Médio', NULL, 0, 3, 51, 70),
    ('Soja', 'K2O', 'Médio', NULL, 3, 4.1, 71, 90),
    ('Soja', 'K2O', 'Médio', NULL, 4.1, 5.01, 91, 110),
    ('Soja', 'K2O', 'Médio', NULL, 5.01, NULL, 111, 130),
    ('Soja', 'K2O', 'Alto', NULL, 0, 3, 30, 50),
    ('Soja', 'K2O', 'Alto', NULL, 3, 4.1, 51, 70),
    ('Soja', 'K2O', 'Alto', NULL, 4.1, 5.01, 71, 90),
    ('Soja', 'K2O', 'Alto', NULL, 5.01, NULL, 91, 110),
    ('Soja', 'K2O', 'Muito alto', NULL, 0, 3, 0, 0),
    ('Soja', 'K2O', 'Muito alto', NULL, 3, 4.1, 0, 0),
    ('Soja', 'K2O', 'Muito alto', NULL, 4.1, 5.01, 50, 70),
    ('Soja', 'K2O', 'Muito alto', NULL, 5.01, NULL, 71, 90),
    ('Soja', 'K2O', 'Condição a evitar', NULL, 0, 3, 0, 0),
    ('Soja', 'K2O', 'Condição a evitar', NULL, 3, 4.1, 0, 0),
    ('Soja', 'K2O', 'Condição a evitar', NULL, 4.1, 5.01, 0, 0),
    ('Soja', 'K2O', 'Condição a evitar', NULL, 5.01, NULL, 0, 0),
    -- Tabela 8.21, p. 99. N no milho, sem classe de fertilidade
    ('Milho', 'N', NULL, 'GRAMINEA', 0, 8, 80, 120),
    ('Milho', 'N', NULL, 'GRAMINEA', 8, 13, 121, 180),
    ('Milho', 'N', NULL, 'GRAMINEA', 13, 16.01, 181, 260),
    ('Milho', 'N', NULL, 'GRAMINEA', 16.01, NULL, 261, 340),
    ('Milho', 'N', NULL, 'LEGUMINOSA', 0, 8, 20, 60),
    ('Milho', 'N', NULL, 'LEGUMINOSA', 8, 13, 61, 120),
    ('Milho', 'N', NULL, 'LEGUMINOSA', 13, 16.01, 121, 200),
    ('Milho', 'N', NULL, 'LEGUMINOSA', 16.01, NULL, 201, 280),
    -- Tabela 8.22, p. 99. P₂O₅ no milho
    ('Milho', 'P2O5', 'Muito baixo', NULL, 0, 8, 111, 130),
    ('Milho', 'P2O5', 'Muito baixo', NULL, 8, 13, NULL, NULL),
    ('Milho', 'P2O5', 'Muito baixo', NULL, 13, 16.01, NULL, NULL),
    ('Milho', 'P2O5', 'Muito baixo', NULL, 16.01, NULL, NULL, NULL),
    ('Milho', 'P2O5', 'Baixo', NULL, 0, 8, 91, 110),
    ('Milho', 'P2O5', 'Baixo', NULL, 8, 13, 111, 130),
    ('Milho', 'P2O5', 'Baixo', NULL, 13, 16.01, 131, 150),
    ('Milho', 'P2O5', 'Baixo', NULL, 16.01, NULL, NULL, NULL),
    ('Milho', 'P2O5', 'Médio', NULL, 0, 8, 71, 90),
    ('Milho', 'P2O5', 'Médio', NULL, 8, 13, 91, 110),
    ('Milho', 'P2O5', 'Médio', NULL, 13, 16.01, 111, 130),
    ('Milho', 'P2O5', 'Médio', NULL, 16.01, NULL, 131, 150),
    ('Milho', 'P2O5', 'Alto', NULL, 0, 8, 51, 70),
    ('Milho', 'P2O5', 'Alto', NULL, 8, 13, 71, 90),
    ('Milho', 'P2O5', 'Alto', NULL, 13, 16.01, 91, 110),
    ('Milho', 'P2O5', 'Alto', NULL, 16.01, NULL, 111, 130),
    ('Milho', 'P2O5', 'Muito alto', NULL, 0, 8, 30, 50),
    ('Milho', 'P2O5', 'Muito alto', NULL, 8, 13, 51, 70),
    ('Milho', 'P2O5', 'Muito alto', NULL, 13, 16.01, 71, 90),
    ('Milho', 'P2O5', 'Muito alto', NULL, 16.01, NULL, 91, 110),
    ('Milho', 'P2O5', 'Condição a evitar', NULL, 0, 8, 0, 0),
    ('Milho', 'P2O5', 'Condição a evitar', NULL, 8, 13, 0, 0),
    ('Milho', 'P2O5', 'Condição a evitar', NULL, 13, 16.01, 0, 0),
    ('Milho', 'P2O5', 'Condição a evitar', NULL, 16.01, NULL, 0, 0),
    -- Tabela 8.23, p. 99. K₂O no milho
    ('Milho', 'K2O', 'Muito baixo', NULL, 0, 8, 101, 130),
    ('Milho', 'K2O', 'Muito baixo', NULL, 8, 13, NULL, NULL),
    ('Milho', 'K2O', 'Muito baixo', NULL, 13, 16.01, NULL, NULL),
    ('Milho', 'K2O', 'Muito baixo', NULL, 16.01, NULL, NULL, NULL),
    ('Milho', 'K2O', 'Baixo', NULL, 0, 8, 71, 100),
    ('Milho', 'K2O', 'Baixo', NULL, 8, 13, 101, 130),
    ('Milho', 'K2O', 'Baixo', NULL, 13, 16.01, 131, 160),
    ('Milho', 'K2O', 'Baixo', NULL, 16.01, NULL, 161, 190),
    ('Milho', 'K2O', 'Médio', NULL, 0, 8, 41, 70),
    ('Milho', 'K2O', 'Médio', NULL, 8, 13, 71, 100),
    ('Milho', 'K2O', 'Médio', NULL, 13, 16.01, 101, 130),
    ('Milho', 'K2O', 'Médio', NULL, 16.01, NULL, 131, 160),
    ('Milho', 'K2O', 'Alto', NULL, 0, 8, 21, 40),
    ('Milho', 'K2O', 'Alto', NULL, 8, 13, 41, 70),
    ('Milho', 'K2O', 'Alto', NULL, 13, 16.01, 71, 100),
    ('Milho', 'K2O', 'Alto', NULL, 16.01, NULL, 101, 130),
    ('Milho', 'K2O', 'Muito alto', NULL, 0, 8, 20, 20),
    ('Milho', 'K2O', 'Muito alto', NULL, 8, 13, 21, 40),
    ('Milho', 'K2O', 'Muito alto', NULL, 13, 16.01, 41, 70),
    ('Milho', 'K2O', 'Muito alto', NULL, 16.01, NULL, 71, 100),
    ('Milho', 'K2O', 'Condição a evitar', NULL, 0, 8, 0, 0),
    ('Milho', 'K2O', 'Condição a evitar', NULL, 8, 13, 0, 0),
    ('Milho', 'K2O', 'Condição a evitar', NULL, 13, 16.01, 0, 0),
    ('Milho', 'K2O', 'Condição a evitar', NULL, 16.01, NULL, 0, 0),
    -- Tabela 8.29, p. 105. N no trigo. A linha '≤ Médio' vale para Muito baixo, Baixo e Médio
    ('Trigo', 'N', 'Muito baixo', 'GRAMINEA', 0, 2.5, 50, 60),
    ('Trigo', 'N', 'Muito baixo', 'GRAMINEA', 2.5, 3.6, 61, 90),
    ('Trigo', 'N', 'Muito baixo', 'GRAMINEA', 3.6, 4.51, 91, 120),
    ('Trigo', 'N', 'Muito baixo', 'GRAMINEA', 4.51, NULL, 121, 150),
    ('Trigo', 'N', 'Baixo', 'GRAMINEA', 0, 2.5, 50, 60),
    ('Trigo', 'N', 'Baixo', 'GRAMINEA', 2.5, 3.6, 61, 90),
    ('Trigo', 'N', 'Baixo', 'GRAMINEA', 3.6, 4.51, 91, 120),
    ('Trigo', 'N', 'Baixo', 'GRAMINEA', 4.51, NULL, 121, 150),
    ('Trigo', 'N', 'Médio', 'GRAMINEA', 0, 2.5, 50, 60),
    ('Trigo', 'N', 'Médio', 'GRAMINEA', 2.5, 3.6, 61, 90),
    ('Trigo', 'N', 'Médio', 'GRAMINEA', 3.6, 4.51, 91, 120),
    ('Trigo', 'N', 'Médio', 'GRAMINEA', 4.51, NULL, 121, 150),
    ('Trigo', 'N', 'Alto', 'GRAMINEA', 0, 2.5, 30, 40),
    ('Trigo', 'N', 'Alto', 'GRAMINEA', 2.5, 3.6, 41, 70),
    ('Trigo', 'N', 'Alto', 'GRAMINEA', 3.6, 4.51, 71, 100),
    ('Trigo', 'N', 'Alto', 'GRAMINEA', 4.51, NULL, 101, 130),
    ('Trigo', 'N', 'Muito alto', 'GRAMINEA', 0, 2.5, 20, 30),
    ('Trigo', 'N', 'Muito alto', 'GRAMINEA', 2.5, 3.6, 31, 40),
    ('Trigo', 'N', 'Muito alto', 'GRAMINEA', 3.6, 4.51, 41, 70),
    ('Trigo', 'N', 'Muito alto', 'GRAMINEA', 4.51, NULL, 71, 110),
    ('Trigo', 'N', 'Muito baixo', 'LEGUMINOSA', 0, 2.5, 30, 50),
    ('Trigo', 'N', 'Muito baixo', 'LEGUMINOSA', 2.5, 3.6, 51, 70),
    ('Trigo', 'N', 'Muito baixo', 'LEGUMINOSA', 3.6, 4.51, 71, 90),
    ('Trigo', 'N', 'Muito baixo', 'LEGUMINOSA', 4.51, NULL, 91, 120),
    ('Trigo', 'N', 'Baixo', 'LEGUMINOSA', 0, 2.5, 30, 50),
    ('Trigo', 'N', 'Baixo', 'LEGUMINOSA', 2.5, 3.6, 51, 70),
    ('Trigo', 'N', 'Baixo', 'LEGUMINOSA', 3.6, 4.51, 71, 90),
    ('Trigo', 'N', 'Baixo', 'LEGUMINOSA', 4.51, NULL, 91, 120),
    ('Trigo', 'N', 'Médio', 'LEGUMINOSA', 0, 2.5, 30, 50),
    ('Trigo', 'N', 'Médio', 'LEGUMINOSA', 2.5, 3.6, 51, 70),
    ('Trigo', 'N', 'Médio', 'LEGUMINOSA', 3.6, 4.51, 71, 90),
    ('Trigo', 'N', 'Médio', 'LEGUMINOSA', 4.51, NULL, 91, 120),
    ('Trigo', 'N', 'Alto', 'LEGUMINOSA', 0, 2.5, 20, 30),
    ('Trigo', 'N', 'Alto', 'LEGUMINOSA', 2.5, 3.6, 31, 50),
    ('Trigo', 'N', 'Alto', 'LEGUMINOSA', 3.6, 4.51, 51, 80),
    ('Trigo', 'N', 'Alto', 'LEGUMINOSA', 4.51, NULL, 81, 100),
    ('Trigo', 'N', 'Muito alto', 'LEGUMINOSA', 0, 2.5, 20, 30),
    ('Trigo', 'N', 'Muito alto', 'LEGUMINOSA', 2.5, 3.6, 31, 40),
    ('Trigo', 'N', 'Muito alto', 'LEGUMINOSA', 3.6, 4.51, 41, 50),
    ('Trigo', 'N', 'Muito alto', 'LEGUMINOSA', 4.51, NULL, 51, 80),
    -- Tabela 8.30, p. 105. P₂O₅ no trigo
    ('Trigo', 'P2O5', 'Muito baixo', NULL, 0, 2.5, 51, 60),
    ('Trigo', 'P2O5', 'Muito baixo', NULL, 2.5, 3.6, 61, 80),
    ('Trigo', 'P2O5', 'Muito baixo', NULL, 3.6, 4.51, 81, 100),
    ('Trigo', 'P2O5', 'Muito baixo', NULL, 4.51, NULL, 101, 120),
    ('Trigo', 'P2O5', 'Baixo', NULL, 0, 2.5, 41, 50),
    ('Trigo', 'P2O5', 'Baixo', NULL, 2.5, 3.6, 51, 60),
    ('Trigo', 'P2O5', 'Baixo', NULL, 3.6, 4.51, 61, 80),
    ('Trigo', 'P2O5', 'Baixo', NULL, 4.51, NULL, 81, 100),
    ('Trigo', 'P2O5', 'Médio', NULL, 0, 2.5, 31, 40),
    ('Trigo', 'P2O5', 'Médio', NULL, 2.5, 3.6, 41, 50),
    ('Trigo', 'P2O5', 'Médio', NULL, 3.6, 4.51, 51, 60),
    ('Trigo', 'P2O5', 'Médio', NULL, 4.51, NULL, 61, 80),
    ('Trigo', 'P2O5', 'Alto', NULL, 0, 2.5, 20, 30),
    ('Trigo', 'P2O5', 'Alto', NULL, 2.5, 3.6, 31, 40),
    ('Trigo', 'P2O5', 'Alto', NULL, 3.6, 4.51, 41, 50),
    ('Trigo', 'P2O5', 'Alto', NULL, 4.51, NULL, 51, 60),
    ('Trigo', 'P2O5', 'Muito alto', NULL, 0, 2.5, 20, 30),
    ('Trigo', 'P2O5', 'Muito alto', NULL, 2.5, 3.6, 31, 40),
    ('Trigo', 'P2O5', 'Muito alto', NULL, 3.6, 4.51, 41, 50),
    ('Trigo', 'P2O5', 'Muito alto', NULL, 4.51, NULL, 51, 60),
    ('Trigo', 'P2O5', 'Condição a evitar', NULL, 0, 2.5, 0, 0),
    ('Trigo', 'P2O5', 'Condição a evitar', NULL, 2.5, 3.6, 0, 0),
    ('Trigo', 'P2O5', 'Condição a evitar', NULL, 3.6, 4.51, 0, 0),
    ('Trigo', 'P2O5', 'Condição a evitar', NULL, 4.51, NULL, 0, 0),
    -- Tabela 8.31, p. 106. K₂O no trigo
    ('Trigo', 'K2O', 'Muito baixo', NULL, 0, 2.5, 41, 50),
    ('Trigo', 'K2O', 'Muito baixo', NULL, 2.5, 3.6, 51, 60),
    ('Trigo', 'K2O', 'Muito baixo', NULL, 3.6, 4.51, 61, 80),
    ('Trigo', 'K2O', 'Muito baixo', NULL, 4.51, NULL, 81, 100),
    ('Trigo', 'K2O', 'Baixo', NULL, 0, 2.5, 31, 40),
    ('Trigo', 'K2O', 'Baixo', NULL, 2.5, 3.6, 41, 50),
    ('Trigo', 'K2O', 'Baixo', NULL, 3.6, 4.51, 51, 60),
    ('Trigo', 'K2O', 'Baixo', NULL, 4.51, NULL, 61, 80),
    ('Trigo', 'K2O', 'Médio', NULL, 0, 2.5, 21, 30),
    ('Trigo', 'K2O', 'Médio', NULL, 2.5, 3.6, 31, 40),
    ('Trigo', 'K2O', 'Médio', NULL, 3.6, 4.51, 41, 50),
    ('Trigo', 'K2O', 'Médio', NULL, 4.51, NULL, 51, 60),
    ('Trigo', 'K2O', 'Alto', NULL, 0, 2.5, 10, 20),
    ('Trigo', 'K2O', 'Alto', NULL, 2.5, 3.6, 21, 30),
    ('Trigo', 'K2O', 'Alto', NULL, 3.6, 4.51, 31, 40),
    ('Trigo', 'K2O', 'Alto', NULL, 4.51, NULL, 41, 50),
    ('Trigo', 'K2O', 'Muito alto', NULL, 0, 2.5, 10, 20),
    ('Trigo', 'K2O', 'Muito alto', NULL, 2.5, 3.6, 10, 20),
    ('Trigo', 'K2O', 'Muito alto', NULL, 3.6, 4.51, 21, 30),
    ('Trigo', 'K2O', 'Muito alto', NULL, 4.51, NULL, 31, 40),
    ('Trigo', 'K2O', 'Condição a evitar', NULL, 0, 2.5, 0, 0),
    ('Trigo', 'K2O', 'Condição a evitar', NULL, 2.5, 3.6, 0, 0),
    ('Trigo', 'K2O', 'Condição a evitar', NULL, 3.6, 4.51, 0, 0),
    ('Trigo', 'K2O', 'Condição a evitar', NULL, 4.51, NULL, 0, 0)
 ) AS r(cultura, nutriente, classe, grupo, rmin, rmax, dmin, dmax)
  JOIN culturas c ON c.nome = r.cultura
 WHERE v.codigo = 'NEPAR-2019.1';

-- ---------------------------------------------------------------------
-- Micronutrientes: só a soja tem dose (Tabela 9.13, p. 122). O manual não traz
-- tabela para o milho e não registra resposta do trigo (p. 106).
-- A Tabela 9.13 começa em Baixo; Muito baixo recebe a dose dessa linha.
-- ---------------------------------------------------------------------
INSERT INTO nepar_regra_micronutriente (versao_id, cultura_id, nutriente, classe, dose_kg_ha, observacao)
SELECT v.id, c.id, m.nutriente, m.classe, m.dose, m.observacao
  FROM nepar_versao_parametros v
  JOIN culturas c ON c.nome = 'Soja'
 CROSS JOIN (VALUES
    ('BORO', 'Muito baixo', 1.5::numeric, 'A tabela do manual começa em Baixo; foi usada a dose dessa linha.'),
    ('BORO', 'Baixo', 1.5, NULL),
    ('BORO', 'Médio', 1.0, NULL),
    ('BORO', 'Alto', 0, NULL),
    ('BORO', 'Muito alto', 0, NULL),
    ('BORO', 'Condição a evitar', 0, NULL),
    ('ZINCO', 'Muito baixo', 2.0, 'A tabela do manual começa em Baixo; foi usada a dose dessa linha.'),
    ('ZINCO', 'Baixo', 2.0, NULL),
    ('ZINCO', 'Médio', 1.5, NULL),
    ('ZINCO', 'Alto', 0, NULL),
    ('ZINCO', 'Muito alto', 0, NULL),
    ('ZINCO', 'Condição a evitar', 0, NULL)
 ) AS m(nutriente, classe, dose, observacao)
 WHERE v.codigo = 'NEPAR-2019.1';
