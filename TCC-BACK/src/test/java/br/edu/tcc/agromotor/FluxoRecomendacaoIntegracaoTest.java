package br.edu.tcc.agromotor;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo RF03 a RF08 pela API, com o cenário de exemplo da especificação. */
class FluxoRecomendacaoIntegracaoTest extends IntegracaoBase {

    @Autowired
    private JdbcTemplate jdbc;

    private int totalDeRecomendacoes() {
        return jdbc.queryForObject("select count(*) from recomendacoes", Integer.class);
    }

    @Test
    @DisplayName("RF04: laudo gravado volta com os indicadores calculados e as classes de fertilidade")
    void interpretaLaudo() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));

        como(c.agronomo(), get("/api/v1/laudos/" + laudoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicadores.somaBases").value(4.65))
                .andExpect(jsonPath("$.indicadores.ctcEfetiva").value(4.75))
                .andExpect(jsonPath("$.indicadores.ctcTotal").value(9.45))
                .andExpect(jsonPath("$.indicadores.saturacaoBases").value(49.2))
                .andExpect(jsonPath("$.indicadores.saturacaoAluminio").value(2.1))
                .andExpect(jsonPath("$.interpretacao[?(@.atributo == 'FOSFORO')].classe").value("Alto"))
                .andExpect(jsonPath("$.interpretacao[?(@.atributo == 'POTASSIO')].classe").value("Muito alto"));
    }

    @Test
    @DisplayName("Pré-visualização da Tela A calcula sem gravar")
    void previewDeIndicadores() throws Exception {
        Conta produtor = registrar("PRODUTOR");
        comoJson(produtor, post("/api/v1/laudos/indicadores"), """
                {"caCmolc":3.2,"mgCmolc":1.1,"kCmolc":0.35,"alCmolc":0.1,"hAlCmolc":4.8}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saturacaoBases").value(49.2));
    }

    @Test
    @DisplayName("RF03: rejeita pH e argila fora do intervalo")
    void validaLaudo() throws Exception {
        Cenario c = cenario();
        String invalido = laudoDoExemplo(c.talhaoId()).replace("\"ph\":5.2", "\"ph\":9.5")
                .replace("\"argilaPorcentagem\":45.0", "\"argilaPorcentagem\":101");

        comoJson(c.agronomo(), post("/api/v1/laudos"), invalido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros", hasSize(2)));
    }

    @Test
    @DisplayName("Erro de formato indica cada campo incorreto, na ordem do formulário")
    void indicaOsCamposInvalidos() throws Exception {
        Cenario c = cenario();
        String invalido = laudoDoExemplo(c.talhaoId()).replace("\"moGDm3\":28.5", "\"moGDm3\":12321")
                .replace("\"alCmolc\":0.1", "\"alCmolc\":312312");

        comoJson(c.agronomo(), post("/api/v1/laudos"), invalido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0]", containsString("Matéria orgânica")))
                .andExpect(jsonPath("$.erros[1]", containsString("Alumínio")))
                .andExpect(jsonPath("$.campos.moGDm3[0]", containsString("máximo 999,99")))
                .andExpect(jsonPath("$.campos.alCmolc[0]", containsString("máximo 99,999")));
    }

    @Test
    @DisplayName("Valor de tipo errado e parâmetro ausente indicam o campo")
    void indicaCampoMalformado() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        String manejoInvalido = requisicaoRecomendacao(laudoId, soja, null).replace("PLANTIO_DIRETO", "OUTRO");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), manejoInvalido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.sistemaManejo[0]", containsString("sistemaManejo")));
        como(c.agronomo(), get("/api/v1/dashboard/evolucao"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0]", containsString("talhaoId")));
        como(c.agronomo(), get("/api/v1/dashboard/evolucao").param("talhaoId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0]", containsString("talhaoId")));
        como(c.agronomo(), get("/api/v1/laudos").param("profundidadeCm", "10-15"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erros[0]", containsString("Profundidade")));
    }

    @Test
    @DisplayName("Talhão repetido na propriedade informa qual identificação já existe")
    void identificacaoDeTalhaoRepetida() throws Exception {
        Cenario c = cenario();
        String talhao = "{\"identificacao\":\"%s\",\"areaHectares\":3}";

        comoJson(c.agronomo(), post("/api/v1/propriedades/" + c.propriedadeId() + "/talhoes"), talhao.formatted("Gleba 01"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erros[0]", containsString("Gleba 01")));
        comoJson(c.agronomo(), put("/api/v1/talhoes/" + c.talhaoId()), talhao.formatted("Gleba 01"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RF05: simulação segue o contrato da seção 8.1 e não grava nada")
    void simulacaoEmMemoria() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long milho = culturaPorNome(c.agronomo(), "Milho");
        int antes = totalDeRecomendacoes();

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicaoRecomendacao(laudoId, soja, milho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(nullValue()))
                .andExpect(jsonPath("$.status").value("SIMULACAO"))
                .andExpect(jsonPath("$.analiseSoloId").value(laudoId))
                .andExpect(jsonPath("$.resumoQuimico.somaBases").value(4.65))
                .andExpect(jsonPath("$.resumoQuimico.ctcTotal").value(9.45))
                .andExpect(jsonPath("$.resumoQuimico.v1Porcentagem").value(49.2))
                .andExpect(jsonPath("$.calagem.necessario").value(true))
                .andExpect(jsonPath("$.calagem.v2AlvoPorcentagem").value(60.0))
                .andExpect(jsonPath("$.calagem.necessidadeCalcarioTonHa").value(1.02))
                .andExpect(jsonPath("$.calagem.observacao", containsString("superfície")))
                .andExpect(jsonPath("$.gessagem.necessario").value(false))
                .andExpect(jsonPath("$.gessagem.doseKgHa").value(closeTo(0.0, 0.0)))
                .andExpect(jsonPath("$.adubacao.doseNkgHa.max").value(closeTo(0.0, 0.0)))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.min").value(61.0))
                .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.max").value(80.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.min").value(50.0))
                .andExpect(jsonPath("$.adubacao.doseK2OkgHa.max").value(70.0))
                .andExpect(jsonPath("$.adubacao.justificativaN").value("FBN ativada. Antecessora Gramínea compensada."));

        assertThat(totalDeRecomendacoes()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Laudo de 20-40 cm do talhão aciona a gessagem na recomendação do laudo de 0-20 cm")
    void gessagemPorLaudoDeSubsuperficie() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId())
                .replace("\"0-20\"", "\"20-40\"")
                .replace("\"alCmolc\":0.1", "\"alCmolc\":2.0")
                .replace("\"argilaPorcentagem\":45.0", "\"argilaPorcentagem\":50"));
        long soja = culturaPorNome(c.agronomo(), "Soja");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicaoRecomendacao(laudoId, soja, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gessagem.necessario").value(true))
                .andExpect(jsonPath("$.gessagem.doseKgHa").value(2200.0))
                .andExpect(jsonPath("$.gessagem.justificativa", containsString("m%")));
    }

    @Test
    @DisplayName("Laudo de 20-40 cm não serve de base para a recomendação")
    void recusaBaseDeSubsuperficie() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()).replace("\"0-20\"", "\"20-40\""));
        long soja = culturaPorNome(c.agronomo(), "Soja");

        comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicaoRecomendacao(laudoId, soja, null))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erros[0]", containsString("0-20 cm")));
    }

    @Test
    @DisplayName("RF06 a RF08: registra, adota, consulta o histórico e emite o receituário")
    void adocaoDaRecomendacao() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long milho = culturaPorNome(c.agronomo(), "Milho");

        JsonNode registrada = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicaoRecomendacao(laudoId, soja, milho))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SIMULACAO")));
        long id = registrada.get("id").asLong();

        // Antes de adotar: receituário bloqueado e produtor sem acesso ao rascunho
        como(c.agronomo(), get("/api/v1/relatorios/receituario/" + id)).andExpect(status().isUnprocessableEntity());
        como(c.produtor(), get("/api/v1/recomendacoes/" + id)).andExpect(status().isNotFound());

        // RF06: adoção
        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADOTADA"))
                .andExpect(jsonPath("$.responsavel.id").value(c.agronomo().id()));
        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar")).andExpect(status().isUnprocessableEntity());

        // RF07: linha do tempo do talhão
        como(c.produtor(), get("/api/v1/talhoes/" + c.talhaoId() + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos", hasSize(2)))
                .andExpect(jsonPath("$.eventos[?(@.tipo == 'RECOMENDACAO_ADOTADA')].doses.cultura").value("Soja"));

        // RF08: receituário em PDF
        byte[] pdf = como(c.produtor(), get("/api/v1/relatorios/receituario/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("receituario-" + id + ".pdf")))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        // Adotada não pode ser descartada
        como(c.agronomo(), delete("/api/v1/recomendacoes/" + id)).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("RF06: a recomendação adotada define o crédito de N da safra seguinte")
    void adocaoGeraCreditoNaProximaSafra() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long milho = culturaPorNome(c.agronomo(), "Milho");

        como(c.agronomo(), get("/api/v1/talhoes/" + c.talhaoId() + "/sugestao-antecessora")).andExpect(status().isNoContent());

        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicaoRecomendacao(laudoId, soja, milho))).get("id").asLong();
        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar")).andExpect(status().isOk());

        como(c.agronomo(), get("/api/v1/talhoes/" + c.talhaoId() + "/sugestao-antecessora"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cultura.nome").value("Soja"))
                .andExpect(jsonPath("$.recomendacaoId").value(id));

        String milhoAposSoja = requisicaoRecomendacao(laudoId, milho, soja).replace("4.5", "8.0");
        JsonNode resposta = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), milhoAposSoja).andExpect(status().isOk()));
        assertThat(resposta.get("adubacao").get("justificativaN").asText()).contains("crédito").contains("Soja");
    }

    @Test
    @DisplayName("RNF06: alterar as tabelas não muda a recomendação registrada; o banco bloqueia a edição da adotada")
    void snapshotImutavel() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long milho = culturaPorNome(c.agronomo(), "Milho");
        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicaoRecomendacao(laudoId, soja, milho))).get("id").asLong();
        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar")).andExpect(status().isOk());

        BigDecimal vOriginal = jdbc.queryForObject("select v_alvo from culturas where id = ?", BigDecimal.class, soja);
        jdbc.update("update culturas set v_alvo = 80 where id = ?", soja);
        try {
            como(c.agronomo(), get("/api/v1/recomendacoes/" + id))
                    .andExpect(jsonPath("$.calagem.v2AlvoPorcentagem").value(60.0))
                    .andExpect(jsonPath("$.calagem.necessidadeCalcarioTonHa").value(1.02))
                    .andExpect(jsonPath("$.versaoParametros.codigo").value("NEPAR-2019.1"));
        } finally {
            jdbc.update("update culturas set v_alvo = ? where id = ?", vOriginal, soja);
        }

        assertThatThrownBy(() -> jdbc.update("update recomendacoes set dose_n_min_kg_ha = 99 where id = ?", id))
                .hasMessageContaining("imutável");
        assertThatThrownBy(() -> jdbc.update("delete from recomendacoes where id = ?", id))
                .hasMessageContaining("imutável");
    }

    @Test
    @DisplayName("Simulação salva pode ser descartada; laudo com recomendação não pode ser excluído")
    void descarteDeSimulacao() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicaoRecomendacao(laudoId, soja, null))).get("id").asLong();

        como(c.agronomo(), delete("/api/v1/laudos/" + laudoId)).andExpect(status().isConflict());
        como(c.agronomo(), delete("/api/v1/recomendacoes/" + id)).andExpect(status().isNoContent());
        como(c.agronomo(), delete("/api/v1/laudos/" + laudoId)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("RNF02: publicar uma nova versão muda o cálculo sem recompilar; sem parâmetros, a API lista o que falta")
    void novaVersaoDosParametros() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");

        Long vigente = jdbc.queryForObject("select id from nepar_versao_parametros where vigente", Long.class);
        jdbc.update("update nepar_versao_parametros set vigente = false where id = ?", vigente);
        Long nova = jdbc.queryForObject("insert into nepar_versao_parametros (codigo, descricao, vigente) "
                + "values ('TESTE-VAZIA', 'Sem parâmetros', true) returning id", Long.class);
        try {
            comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicaoRecomendacao(laudoId, soja, null))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Parâmetros NEPAR não cadastrados"))
                    .andExpect(jsonPath("$.erros[*]", hasItem(containsString("Regra de calagem"))));

            jdbc.update("insert into nepar_regra_calagem (versao_id, manejo, observacao) "
                    + "values (?, 'PLANTIO_DIRETO'::sistema_manejo, 'Aplicação de teste.')", nova);
            jdbc.update("insert into nepar_faixa_interpretacao (versao_id, atributo, teor_min, classe) values "
                    + "(?, 'FOSFORO', 0, 'X'), (?, 'POTASSIO', 0, 'X'), (?, 'MATERIA_ORGANICA', 0, 'X')", nova, nova, nova);
            jdbc.update("insert into nepar_regra_npk (versao_id, cultura_id, nutriente, classe_fertilidade, faixa_rendimento_min, "
                    + "dose_min, dose_max) values (?, ?, 'P2O5', 'X', 0, 11, 12), (?, ?, 'K2O', 'X', 0, 22, 23)", nova, soja, nova, soja);

            comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicaoRecomendacao(laudoId, soja, null))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.adubacao.doseP2O5kgHa.min").value(11.0))
                    .andExpect(jsonPath("$.adubacao.doseK2OkgHa.max").value(23.0))
                    .andExpect(jsonPath("$.calagem.observacao").value("Aplicação de teste."))
                    .andExpect(jsonPath("$.versaoParametros.codigo").value("TESTE-VAZIA"));
        } finally {
            jdbc.update("delete from nepar_regra_npk where versao_id = ?", nova);
            jdbc.update("delete from nepar_faixa_interpretacao where versao_id = ?", nova);
            jdbc.update("delete from nepar_regra_calagem where versao_id = ?", nova);
            jdbc.update("delete from nepar_versao_parametros where id = ?", nova);
            jdbc.update("update nepar_versao_parametros set vigente = true where id = ?", vigente);
        }
    }

    @Test
    @DisplayName("RNF04: a simulação responde em menos de 300 ms")
    void desempenhoDaSimulacao() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        String requisicao = requisicaoRecomendacao(laudoId, soja, null);

        // As primeiras chamadas aquecem a JVM; a medição usa a mediana das seguintes
        for (int i = 0; i < 10; i++) {
            comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao).andExpect(status().isOk());
        }
        List<Long> tempos = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            long inicio = System.nanoTime();
            comoJson(c.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao).andExpect(status().isOk());
            tempos.add((System.nanoTime() - inicio) / 1_000_000);
        }
        tempos.sort(Comparator.naturalOrder());
        assertThat(tempos.get(tempos.size() / 2)).isLessThan(300L);
    }
}
