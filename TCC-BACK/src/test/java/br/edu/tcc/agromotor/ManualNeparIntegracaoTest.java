package br.edu.tcc.agromotor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confere pela API os dados publicados do manual (migração V6): cada resultado esperado
 * foi lido diretamente da tabela indicada, com a transcrição em docs/dados-do-manual.md.
 */
class ManualNeparIntegracaoTest extends IntegracaoBase {

    @Autowired
    private JdbcTemplate jdbc;

    private String requisicao(long laudoId, long alvo, Long anterior, String rendimento) {
        return requisicaoRecomendacao(laudoId, alvo, anterior).replace("4.5", rendimento);
    }

    @Test
    @DisplayName("A versão vigente é a do manual, conferida, e as culturas seguem as p. 51, 98, 104 e 120")
    void versaoECulturas() throws Exception {
        Conta conta = registrar("AGRONOMO");

        como(conta, get("/api/v1/parametros/vigentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao.codigo").value("NEPAR-2019.1"))
                .andExpect(jsonPath("$.versao.conferidaManual").value(true))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Soja')].vAlvo").value(60.0))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Soja')].vMinimo").value(50.0))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Milho')].vAlvo").value(70.0))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Milho')].vMinimo").value(60.0))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Trigo')].vAlvo").value(70.0))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Trigo')].vMinimo").value(60.0))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Trigo')].phMinIdeal").value(5.0))
                .andExpect(jsonPath("$.culturas[?(@.nome == 'Trigo')].phMaxIdeal").value(5.5))
                .andExpect(jsonPath("$.gessagem.doses[3].doseKgHa").value(3200.0));
    }

    @Test
    @DisplayName("Soja: Tabelas 9.11 e 9.12, FBN e calagem da p. 120")
    void soja() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoId, soja, null, "5.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calagem.necessidadeCalcarioTonHa").value(1.02))
                .andExpect(jsonPath("$.adubacao.classeFosforo").value("Alto"))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.min").value(81.0))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.max").value(100.0))
                .andExpect(jsonPath("$.adubacao.classePotassio").value("Muito alto"))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.min").value(71.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.max").value(90.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(0.0));
    }

    @Test
    @DisplayName("Milho: Tabelas 8.21 a 8.23; a lacuna de 12 para 13 t/ha fica na coluna de baixo")
    void milho() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long milho = culturaPorNome(c.agronomo(), "Milho");
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long trigo = culturaPorNome(c.agronomo(), "Trigo");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoId, milho, soja, "12.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calagem.necessidadeCalcarioTonHa").value(1.97))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.min").value(61.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(120.0))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.min").value(71.0))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.max").value(90.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.min").value(21.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.max").value(40.0));

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoId, milho, trigo, "17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adubacao.doseNkgHa.min").value(261.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(340.0));
    }

    @Test
    @DisplayName("Trigo: Tabelas 8.29 a 8.31; N por M.O. e cultivo anterior")
    void trigo() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long trigo = culturaPorNome(c.agronomo(), "Trigo");
        long milho = culturaPorNome(c.agronomo(), "Milho");

        // M.O. 28,5 g/dm³ = 2,85%: Alto
        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoId, trigo, milho, "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adubacao.classeMateriaOrganica").value("Alto"))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.min").value(71.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(100.0))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.min").value(41.0))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.max").value(50.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.min").value(21.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.max").value(30.0))
                .andExpect(jsonPath("$.micronutrientes[0].avaliado").value(false))
                .andExpect(jsonPath("$.micronutrientes[0].justificativa", containsString("não indica dose")));

        // M.O. 10 g/dm³ = 1%: Baixo, que a tabela agrupa em "≤ Médio"
        long laudoBaixo = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()).replace("\"moGDm3\":28.5", "\"moGDm3\":10"));
        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoBaixo, trigo, milho, "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adubacao.classeMateriaOrganica").value("Baixo"))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.min").value(91.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(120.0));
    }

    @Test
    @DisplayName("Célula Inviável e milho sem cultura anterior são recusados com a explicação")
    void cenariosSemDoseNoManual() throws Exception {
        Cenario c = cenario();
        // P = 2 com argila 45%: Muito baixo
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()).replace("\"pMehlich\":12.0", "\"pMehlich\":2"));
        long milho = culturaPorNome(c.agronomo(), "Milho");
        long soja = culturaPorNome(c.agronomo(), "Soja");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoId, milho, soja, "9.5"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erros[0]", containsString("inviável")))
                .andExpect(jsonPath("$.erros[0]", containsString("P Muito baixo")));

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoId, milho, null, "7"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erros[0]", containsString("sem cultura anterior")));
    }

    @Test
    @DisplayName("Zn e B da soja: classes da Tabela 3.5 e doses da Tabela 9.13")
    void micronutrientesDaSoja() throws Exception {
        Cenario c = cenario();
        // Zn = 0,3: Muito baixo, dose da linha Baixo; B = 0,25: Médio
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId())
                .replace("\"argilaPorcentagem\":45.0", "\"argilaPorcentagem\":45.0,\"znMgDm3\":0.3,\"bMgDm3\":0.25"));
        long soja = culturaPorNome(c.agronomo(), "Soja");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudoId, soja, null, "4.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.micronutrientes[0].simbolo").value("Zn"))
                .andExpect(jsonPath("$.micronutrientes[0].classe").value("Muito baixo"))
                .andExpect(jsonPath("$.micronutrientes[0].doseKgHa").value(2.0))
                .andExpect(jsonPath("$.micronutrientes[0].justificativa", containsString("começa em Baixo")))
                .andExpect(jsonPath("$.micronutrientes[1].simbolo").value("B"))
                .andExpect(jsonPath("$.micronutrientes[1].classe").value("Médio"))
                .andExpect(jsonPath("$.micronutrientes[1].doseKgHa").value(1.0));
    }

    private String comGrupoAnterior(String requisicao, String grupo) {
        return requisicao.replace("\"culturaAnteriorId\":null", "\"culturaAnteriorId\":null,\"grupoAnterior\":\"%s\"".formatted(grupo));
    }

    @Test
    @DisplayName("Tabela 8.21: outra gramínea ou leguminosa dá a mesma dose de N do grupo")
    void grupoGenericoNoMilho() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long milho = culturaPorNome(c.agronomo(), "Milho");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), comGrupoAnterior(requisicao(laudoId, milho, null, "12.5"), "LEGUMINOSA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.antecessora").value("Outra leguminosa"))
                .andExpect(jsonPath("$.grupoAnterior").value("LEGUMINOSA"))
                .andExpect(jsonPath("$.culturaAnterior", nullValue()))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.min").value(61.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(120.0))
                .andExpect(jsonPath("$.adubacao.justificativaN", containsString("Outra leguminosa")));

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), comGrupoAnterior(requisicao(laudoId, milho, null, "12.5"), "GRAMINEA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.antecessora").value("Outra gramínea"))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.min").value(121.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(180.0));
    }

    @Test
    @DisplayName("Tabela 8.29: outra gramínea no trigo; na soja o N continua zero")
    void grupoGenericoNoTrigoENaSoja() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));

        // M.O. 28,5 g/dm³: Alto
        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"),
                comGrupoAnterior(requisicao(laudoId, culturaPorNome(c.agronomo(), "Trigo"), null, "2.0"), "GRAMINEA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adubacao.doseNkgHa.min").value(30.0))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(40.0));
        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"),
                comGrupoAnterior(requisicao(laudoId, culturaPorNome(c.agronomo(), "Soja"), null, "4.5"), "GRAMINEA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(0.0));
    }

    @Test
    @DisplayName("Cultura anterior e grupo juntos são recusados")
    void culturaEGrupoJuntos() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long milho = culturaPorNome(c.agronomo(), "Milho");
        long soja = culturaPorNome(c.agronomo(), "Soja");
        String requisicao = requisicao(laudoId, milho, soja, "12.5")
                .replace("\"culturaAnteriorId\":" + soja, "\"culturaAnteriorId\":" + soja + ",\"grupoAnterior\":\"GRAMINEA\"");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erros[0]", containsString("não os dois")));
    }

    @Test
    @DisplayName("O grupo informado fica gravado na recomendação adotada e sai no receituário")
    void grupoGenericoGravado() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long milho = culturaPorNome(c.agronomo(), "Milho");
        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"),
                comGrupoAnterior(requisicao(laudoId, milho, null, "12.5"), "LEGUMINOSA"))
                .andExpect(status().isCreated())).get("id").asLong();

        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.antecessora").value("Outra leguminosa"));
        como(c.produtor(), get("/api/v1/recomendacoes/" + id))
                .andExpect(jsonPath("$.grupoAnterior").value("LEGUMINOSA"));
        como(c.produtor(), get("/api/v1/relatorios/receituario/" + id)).andExpect(status().isOk());
    }

    private void doseNaSoja(Cenario c, String campo, int posicao, String teor, String classe, double dose) throws Exception {
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId())
                .replace("\"argilaPorcentagem\":45.0", "\"argilaPorcentagem\":45.0,\"%s\":%s".formatted(campo, teor)));
        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"),
                requisicao(laudoId, culturaPorNome(c.agronomo(), "Soja"), null, "4.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.micronutrientes[%d].classe".formatted(posicao)).value(classe))
                .andExpect(jsonPath("$.micronutrientes[%d].doseKgHa".formatted(posicao)).value(dose));
    }

    @Test
    @DisplayName("Cobre da soja: classes da Tabela 3.5 e doses da Tabela 9.13, inclusive nos limites")
    void cobreDaSoja() throws Exception {
        Cenario c = cenario();
        doseNaSoja(c, "cuMgDm3", 2, "0.19", "Muito baixo", 1.0);
        doseNaSoja(c, "cuMgDm3", 2, "0.2", "Baixo", 1.0);
        doseNaSoja(c, "cuMgDm3", 2, "0.5", "Baixo", 1.0);
        doseNaSoja(c, "cuMgDm3", 2, "0.6", "Médio", 0.5);
        doseNaSoja(c, "cuMgDm3", 2, "0.9", "Alto", 0.0);
        doseNaSoja(c, "cuMgDm3", 2, "3.0", "Alto", 0.0);
        doseNaSoja(c, "cuMgDm3", 2, "3.01", "Muito alto", 0.0);
        doseNaSoja(c, "cuMgDm3", 2, "20.01", "Condição a evitar", 0.0);
    }

    @Test
    @DisplayName("Manganês da soja: classes da Tabela 3.5 e doses da Tabela 9.13, inclusive nos limites")
    void manganesDaSoja() throws Exception {
        Cenario c = cenario();
        doseNaSoja(c, "mnMgDm3", 3, "4.99", "Muito baixo", 6.0);
        doseNaSoja(c, "mnMgDm3", 3, "5", "Baixo", 6.0);
        doseNaSoja(c, "mnMgDm3", 3, "15", "Baixo", 6.0);
        doseNaSoja(c, "mnMgDm3", 3, "16", "Médio", 3.0);
        doseNaSoja(c, "mnMgDm3", 3, "31", "Alto", 0.0);
        doseNaSoja(c, "mnMgDm3", 3, "100", "Alto", 0.0);
        doseNaSoja(c, "mnMgDm3", 3, "100.01", "Muito alto", 0.0);
        doseNaSoja(c, "mnMgDm3", 3, "200.01", "Condição a evitar", 0.0);
    }

    @Test
    @DisplayName("Toda dose de manganês leva a orientação do manual; sem dose, não há aviso")
    void avisoDoManganes() throws Exception {
        Cenario c = cenario();
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long baixo = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId())
                .replace("\"argilaPorcentagem\":45.0", "\"argilaPorcentagem\":45.0,\"mnMgDm3\":3"));
        long alto = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId())
                .replace("\"argilaPorcentagem\":45.0", "\"argilaPorcentagem\":45.0,\"mnMgDm3\":50"));

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(baixo, soja, null, "4.5"))
                .andExpect(jsonPath("$.micronutrientes[3].justificativa", containsString("começa em Baixo")))
                .andExpect(jsonPath("$.micronutrientes[3].justificativa", containsString("só com sintomas")))
                .andExpect(jsonPath("$.micronutrientes[3].justificativa", containsString("aplicação foliar")));
        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(alto, soja, null, "4.5"))
                .andExpect(jsonPath("$.micronutrientes[3].justificativa", not(containsString("sintomas"))));
    }

    @Test
    @DisplayName("Cobre e manganês não mudam as doses de milho e trigo, que não têm tabela")
    void cobreEManganesForaDaSoja() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId())
                .replace("\"argilaPorcentagem\":45.0", "\"argilaPorcentagem\":45.0,\"cuMgDm3\":0.1,\"mnMgDm3\":3"));

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"),
                requisicao(laudoId, culturaPorNome(c.agronomo(), "Trigo"), culturaPorNome(c.agronomo(), "Milho"), "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.micronutrientes[2].avaliado").value(false))
                .andExpect(jsonPath("$.micronutrientes[3].avaliado").value(false))
                .andExpect(jsonPath("$.micronutrientes[3].justificativa", containsString("não indica dose")));
    }

    private void classeDoPh(Cenario c, String metodo, String ph, String atributo, String classe) throws Exception {
        String laudo = laudoDoExemplo(c.talhaoId())
                .replace("\"ph\":5.2,\"metodoPh\":\"CACL2\"", "\"ph\":%s,\"metodoPh\":\"%s\"".formatted(ph, metodo));
        comoJson(c.agronomo(), post("/api/v1/laudos"), laudo)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metodoPh").value(metodo))
                .andExpect(jsonPath("$.interpretacao[0].atributo").value(atributo))
                .andExpect(jsonPath("$.interpretacao[0].classe").value(classe));
    }

    @Test
    @DisplayName("Tabela 3.2: cada método de pH usa a sua coluna, inclusive nos limites")
    void classesDoPhPorMetodo() throws Exception {
        Cenario c = cenario();
        String[][] agua = {{"4.69", "Muito baixo"}, {"4.7", "Baixo"}, {"5.1", "Baixo"}, {"5.2", "Médio"}, {"5.6", "Médio"},
                {"5.7", "Alto"}, {"6.2", "Alto"}, {"6.21", "Muito alto"}, {"6.7", "Muito alto"}, {"6.71", "Condição a evitar"}};
        for (String[] caso : agua) {
            classeDoPh(c, "AGUA", caso[0], "PH_AGUA", caso[1]);
        }
        // O mesmo 5,6 em CaCl₂ fica em outra coluna e em outra classe
        classeDoPh(c, "CACL2", "5.6", "PH", "Muito alto");
    }

    @Test
    @DisplayName("O método do pH é obrigatório no laudo")
    void metodoDoPhObrigatorio() throws Exception {
        Cenario c = cenario();
        String semMetodo = laudoDoExemplo(c.talhaoId()).replace("\"metodoPh\":\"CACL2\",", "");

        comoJson(c.agronomo(), post("/api/v1/laudos"), semMetodo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0]", containsString("CaCl₂ ou em H₂O")));
    }

    @Test
    @DisplayName("As doses são as mesmas com o pH em CaCl₂ ou em água")
    void metodoDoPhNaoMudaAsDoses() throws Exception {
        Cenario c = cenario();
        long emCacl2 = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long emAgua = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()).replace("\"metodoPh\":\"CACL2\"", "\"metodoPh\":\"AGUA\""));
        long soja = culturaPorNome(c.agronomo(), "Soja");

        for (long laudo : new long[]{emCacl2, emAgua}) {
            comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao(laudo, soja, null, "5.5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.calagem.necessidadeCalcarioTonHa").value(1.02))
                    .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.min").value(81.0))
                    .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.max").value(100.0))
                    .andExpect(jsonPath("$.adubacao.doseK2OkgHa.min").value(71.0))
                    .andExpect(jsonPath("$.adubacao.doseK2OkgHa.max").value(90.0));
        }
    }

    @Test
    @DisplayName("Recomendação gravada antes da V7, com o pH em phCacl2, continua legível")
    void snapshotAnteriorAoMetodoDoPh() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicao(laudoId, soja, null, "4.5")))
                .get("id").asLong();

        jdbc.update("""
                update recomendacoes set snapshot_auditoria = jsonb_set(
                    snapshot_auditoria #- '{entrada,laudo,ph}' #- '{entrada,laudo,metodoPh}',
                    '{entrada,laudo,phCacl2}', '5.20') where id = ?""", id);

        como(c.agronomo(), get("/api/v1/recomendacoes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laudo.ph").value(5.2))
                .andExpect(jsonPath("$.laudo.metodoPh").value("CACL2"));
    }

    @Test
    @DisplayName("Recomendação gravada com dose única, antes da V5, continua legível como faixa")
    void snapshotAnteriorAoManual() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicao(laudoId, soja, null, "4.5")))
                .get("id").asLong();

        jdbc.update("""
                update recomendacoes set snapshot_auditoria = jsonb_set(jsonb_set(jsonb_set(snapshot_auditoria,
                    '{resultado,adubacao,doseNkgHa}', '0'),
                    '{resultado,adubacao,doseP2O5kgHa}', '80.00'),
                    '{resultado,adubacao,doseK2OkgHa}', '50.00') where id = ?""", id);

        como(c.agronomo(), get("/api/v1/recomendacoes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.min").value(80.0))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.max").value(80.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.min").value(50.0));
    }
}
