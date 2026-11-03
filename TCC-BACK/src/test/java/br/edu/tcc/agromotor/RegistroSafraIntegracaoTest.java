package br.edu.tcc.agromotor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Colheita e gasto com adubação de uma recomendação adotada, comparados com o esperado. */
class RegistroSafraIntegracaoTest extends IntegracaoBase {

    private static final String HOJE = LocalDate.now().toString();

    private long recomendacao(Cenario c) throws Exception {
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        // A expectativa de produtividade do cenário é de 4,5 t/ha
        return corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicaoRecomendacao(laudoId, soja, null))
                .andExpect(status().isCreated())).get("id").asLong();
    }

    private long adotada(Cenario c) throws Exception {
        long id = recomendacao(c);
        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar")).andExpect(status().isOk());
        return id;
    }

    private String corpoSafra(String colhida, String data, String custo) {
        return """
                {"produtividadeColhidaTonHa":%s,"dataColheita":%s,"custoAdubacaoReais":%s}"""
                .formatted(colhida, data == null ? "null" : "\"" + data + "\"", custo);
    }

    @Test
    @DisplayName("O produtor registra a colheita e o gasto, comparados com o esperado; o gasto vira R$/ha pela área")
    void comparaComOEsperado() throws Exception {
        Cenario c = cenario();
        long id = adotada(c);

        como(c.produtor(), get("/api/v1/recomendacoes/" + id + "/safra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtividadeEsperadaTonHa").value(4.5))
                .andExpect(jsonPath("$.produtividadeColhidaTonHa", nullValue()));

        // Talhão de 10,5 ha
        comoJson(c.produtor(), put("/api/v1/recomendacoes/" + id + "/safra"), corpoSafra("4.86", HOJE, "21000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtividadeColhidaTonHa").value(4.86))
                .andExpect(jsonPath("$.diferencaPercentual").value(8.0))
                .andExpect(jsonPath("$.custoPorHectareReais").value(2000.0));

        // Colheita abaixo do esperado, agora pelo agrônomo: substitui o registro
        comoJson(c.agronomo(), put("/api/v1/recomendacoes/" + id + "/safra"), corpoSafra("4.05", HOJE, "21000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diferencaPercentual").value(-10.0));
    }

    @Test
    @DisplayName("O gasto pode ser informado antes da colheita; com tudo vazio, o registro é removido")
    void gastoAntesDaColheita() throws Exception {
        Cenario c = cenario();
        long id = adotada(c);

        comoJson(c.produtor(), put("/api/v1/recomendacoes/" + id + "/safra"), corpoSafra("null", null, "5000.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoAdubacaoReais").value(5000.5))
                .andExpect(jsonPath("$.diferencaPercentual", nullValue()));

        comoJson(c.produtor(), put("/api/v1/recomendacoes/" + id + "/safra"), corpoSafra("null", null, "null"))
                .andExpect(status().isOk());
        como(c.produtor(), get("/api/v1/recomendacoes/" + id + "/safra"))
                .andExpect(jsonPath("$.custoAdubacaoReais", nullValue()));
    }

    @Test
    @DisplayName("Colheita sem data, produtividade zerada, gasto negativo e data futura são recusados")
    void validaOsCampos() throws Exception {
        Cenario c = cenario();
        String rota = "/api/v1/recomendacoes/" + adotada(c) + "/safra";

        comoJson(c.produtor(), put(rota), corpoSafra("4.0", null, "null"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erros[0]", containsString("junto com a data")));
        comoJson(c.produtor(), put(rota), corpoSafra("0", HOJE, "null"))
                .andExpect(status().isUnprocessableEntity());
        comoJson(c.produtor(), put(rota), corpoSafra("null", null, "-1"))
                .andExpect(status().isUnprocessableEntity());
        comoJson(c.produtor(), put(rota), corpoSafra("4.0", LocalDate.now().plusDays(1).toString(), "null"))
                .andExpect(status().isBadRequest());
        comoJson(c.produtor(), put(rota), corpoSafra("4.0", LocalDate.now().minusDays(1).toString(), "null"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erros[0]", containsString("anterior à adoção")));
    }

    @Test
    @DisplayName("Simulação não tem safra, e quem está fora do escopo não acessa")
    void permissoes() throws Exception {
        Cenario c = cenario();
        long simulacao = recomendacao(c);
        long id = adotada(c);
        String corpo = corpoSafra("4.0", HOJE, "100");

        comoJson(c.agronomo(), put("/api/v1/recomendacoes/" + simulacao + "/safra"), corpo)
                .andExpect(status().isConflict());
        comoJson(registrar("PRODUTOR"), put("/api/v1/recomendacoes/" + id + "/safra"), corpo)
                .andExpect(status().isForbidden());
        como(registrar("AGRONOMO"), get("/api/v1/recomendacoes/" + id + "/safra"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A safra aparece no painel e na linha do tempo, e não altera a recomendação adotada")
    void apareceNoPainelENoHistorico() throws Exception {
        Cenario c = cenario();
        long id = adotada(c);
        String doseAntes = corpo(como(c.agronomo(), get("/api/v1/recomendacoes/" + id))).get("adubacao").toString();

        comoJson(c.produtor(), put("/api/v1/recomendacoes/" + id + "/safra"), corpoSafra("4.86", HOJE, "21000"))
                .andExpect(status().isOk());

        como(c.produtor(), get("/api/v1/dashboard/evolucao?talhaoId=" + c.talhaoId()))
                .andExpect(jsonPath("$.safras", hasSize(1)))
                .andExpect(jsonPath("$.safras[0].cultura").value("Soja"))
                .andExpect(jsonPath("$.safras[0].produtividadeEsperadaTonHa").value(4.5))
                .andExpect(jsonPath("$.safras[0].produtividadeColhidaTonHa").value(4.86));
        como(c.produtor(), get("/api/v1/dashboard/resumo"))
                .andExpect(jsonPath("$.colheitasRegistradas").value(1))
                .andExpect(jsonPath("$.colheitasRecentes", hasSize(1)));
        como(c.produtor(), get("/api/v1/talhoes/" + c.talhaoId() + "/historico"))
                .andExpect(jsonPath("$.eventos[*].tipo", hasItem("COLHEITA")));
        como(c.produtor(), get("/api/v1/talhoes/" + c.talhaoId() + "/historico?fim=2000-01-01"))
                .andExpect(jsonPath("$.eventos", hasSize(0)));

        assertThat(corpo(como(c.agronomo(), get("/api/v1/recomendacoes/" + id))).get("adubacao").toString())
                .isEqualTo(doseAntes);
    }
}
