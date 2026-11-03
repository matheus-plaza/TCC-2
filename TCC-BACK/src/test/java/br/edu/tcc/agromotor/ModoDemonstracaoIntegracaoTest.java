package br.edu.tcc.agromotor;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Perfil "demo": dados de exemplo fictícios, com recomendações geradas pelo motor real sobre as tabelas do manual. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class ModoDemonstracaoIntegracaoTest extends IntegracaoBase {

    @Test
    @DisplayName("Carrega os dados de exemplo sobre a versão conferida com o manual")
    void cargaDeDemonstracao() throws Exception {
        JsonNode info = corpo(mvc.perform(get("/api/v1/sistema/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versaoParametros.codigo").value("NEPAR-2019.1"))
                .andExpect(jsonPath("$.versaoParametros.conferidaManual").value(true)));
        JsonNode agronomo = null;
        for (JsonNode conta : info.get("contasDemonstracao")) {
            if (conta.get("perfil").asText().equals("AGRONOMO")) {
                agronomo = conta;
            }
        }
        assertThat(agronomo).isNotNull();

        String token = corpo(mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}""".formatted(agronomo.get("email").asText(), agronomo.get("senha").asText())))
                .andExpect(status().isOk())).get("token").asText();

        JsonNode resumo = corpo(mvc.perform(get("/api/v1/dashboard/resumo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()));
        assertThat(resumo.get("propriedades").asInt()).isEqualTo(2);
        assertThat(resumo.get("talhoes").asInt()).isEqualTo(4);
        assertThat(resumo.get("recomendacoesAdotadas").asInt()).isEqualTo(7);
        assertThat(resumo.get("simulacoesSalvas").asInt()).isEqualTo(1);

        JsonNode recomendacoes = corpo(mvc.perform(get("/api/v1/recomendacoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()));
        assertThat(recomendacoes).hasSize(8);

        mvc.perform(get("/api/v1/parametros/vigentes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gessagem.limiteMPorcentagem").value(20.0));
    }
}
