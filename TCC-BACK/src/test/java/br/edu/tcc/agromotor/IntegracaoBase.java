package br.edu.tcc.agromotor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de integração: API, segurança, JPA e Flyway contra PostgreSQL real. */
@SpringBootTest
@AutoConfigureMockMvc
abstract class IntegracaoBase {

    protected static final String SENHA = "senha-de-teste";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", () -> BancoEmbutido.deTeste().getJdbcUrl("postgres", "postgres"));
        registro.add("spring.datasource.username", () -> "postgres");
        registro.add("spring.datasource.password", () -> "postgres");
    }

    protected record Conta(String token, long id, String email) {
    }

    protected Conta registrar(String perfil) throws Exception {
        String email = perfil.toLowerCase() + "." + UUID.randomUUID() + "@teste.local";
        JsonNode resposta = corpo(mvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Teste %s","email":"%s","senha":"%s","perfil":"%s","registroProfissional":"PR-1/D"}
                                """.formatted(perfil, email, SENHA, perfil)))
                .andExpect(status().isCreated()));
        return new Conta(resposta.get("token").asText(), resposta.get("usuario").get("id").asLong(), email);
    }

    protected ResultActions como(Conta conta, MockHttpServletRequestBuilder requisicao) throws Exception {
        return mvc.perform(requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + conta.token()));
    }

    protected ResultActions comoJson(Conta conta, MockHttpServletRequestBuilder requisicao, String corpo) throws Exception {
        return como(conta, requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    protected JsonNode corpo(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }

    /** Agrônomo já vinculado a um produtor recém-criado, com propriedade e talhão. */
    protected record Cenario(Conta agronomo, Conta produtor, long propriedadeId, long talhaoId) {
    }

    protected Cenario cenario() throws Exception {
        Conta agronomo = registrar("AGRONOMO");
        Conta produtor = registrar("PRODUTOR");
        comoJson(produtor, post("/api/v1/vinculos"), """
                {"agronomoEmail":"%s"}""".formatted(agronomo.email()))
                .andExpect(status().isCreated());
        long propriedadeId = corpo(comoJson(agronomo, post("/api/v1/propriedades"), """
                {"produtorId":%d,"nome":"Sítio de Teste","municipio":"Santa Helena"}""".formatted(produtor.id()))
                .andExpect(status().isCreated())).get("id").asLong();
        long talhaoId = corpo(comoJson(agronomo, post("/api/v1/propriedades/" + propriedadeId + "/talhoes"), """
                {"identificacao":"Gleba 01","areaHectares":10.5,"texturaPredominante":"ARGILOSA"}""")
                .andExpect(status().isCreated())).get("id").asLong();
        return new Cenario(agronomo, produtor, propriedadeId, talhaoId);
    }

    /** Laudo da Tela A da especificação. */
    protected String laudoDoExemplo(long talhaoId) {
        return """
                {"talhaoId":%d,"dataAnalise":"2026-10-01","profundidadeCm":"0-20","ph":5.2,"metodoPh":"CACL2","moGDm3":28.5,
                 "pMehlich":12.0,"kCmolc":0.35,"caCmolc":3.2,"mgCmolc":1.1,"alCmolc":0.1,"hAlCmolc":4.8,
                 "argilaPorcentagem":45.0}""".formatted(talhaoId);
    }

    protected long cadastrarLaudo(Conta conta, String corpoLaudo) throws Exception {
        return corpo(comoJson(conta, post("/api/v1/laudos"), corpoLaudo).andExpect(status().isCreated())).get("id").asLong();
    }

    protected long culturaPorNome(Conta conta, String nome) throws Exception {
        for (JsonNode cultura : corpo(como(conta, get("/api/v1/culturas")))) {
            if (cultura.get("nome").asText().equals(nome)) {
                return cultura.get("id").asLong();
            }
        }
        throw new IllegalStateException("Cultura não encontrada: " + nome);
    }

    protected String requisicaoRecomendacao(long laudoId, long alvoId, Long anteriorId) {
        return """
                {"analiseSoloId":%d,"culturaAlvoId":%d,"culturaAnteriorId":%s,"sistemaManejo":"PLANTIO_DIRETO",
                 "expectativaRendimentoTonHa":4.5,"prntCalcario":100.0}""".formatted(laudoId, alvoId, anteriorId);
    }
}
