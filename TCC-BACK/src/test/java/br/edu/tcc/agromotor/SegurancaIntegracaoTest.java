package br.edu.tcc.agromotor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RF01 e RNF05: autenticação e segregação de acesso por perfil e por vínculo. */
class SegurancaIntegracaoTest extends IntegracaoBase {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("Sem token a API responde 401 em Problem Details")
    void exigeAutenticacao() throws Exception {
        mvc.perform(get("/api/v1/propriedades"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Não autenticado"));
    }

    @Test
    @DisplayName("Token inválido é recusado")
    void recusaTokenInvalido() throws Exception {
        mvc.perform(get("/api/v1/propriedades").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RF01: login devolve o JWT e a senha fica cifrada com BCrypt")
    void login() throws Exception {
        Conta conta = registrar("PRODUTOR");

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}""".formatted(conta.email(), SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.perfil").value("PRODUTOR"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());

        String hash = jdbc.queryForObject("select senha from usuarios where id = ?", String.class, conta.id());
        assertThat(hash).startsWith("$2a$").doesNotContain(SENHA);
    }

    @Test
    @DisplayName("Senha errada e e-mail inexistente dão a mesma resposta")
    void credenciaisInvalidas() throws Exception {
        Conta conta = registrar("PRODUTOR");

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"outra-senha"}""".formatted(conta.email())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ninguem@teste.local","senha":"outra-senha"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    @DisplayName("Ninguém se registra como administrador pela rota pública")
    void naoRegistraAdministrador() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Invasor","email":"invasor@teste.local","senha":"senha-qualquer","perfil":"ADMIN"}"""))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("RNF05: produtor vê apenas o que é seu")
    void produtorIsolado() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        Conta outro = registrar("PRODUTOR");

        como(c.produtor(), get("/api/v1/propriedades")).andExpect(jsonPath("$", hasSize(1)));
        como(outro, get("/api/v1/propriedades")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        como(outro, get("/api/v1/laudos")).andExpect(jsonPath("$", hasSize(0)));
        como(outro, get("/api/v1/propriedades/" + c.propriedadeId())).andExpect(status().isForbidden());
        como(outro, get("/api/v1/laudos/" + laudoId)).andExpect(status().isForbidden());
        como(outro, get("/api/v1/talhoes/" + c.talhaoId() + "/historico")).andExpect(status().isForbidden());
        como(outro, get("/api/v1/dashboard/evolucao?talhaoId=" + c.talhaoId())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RNF05: agrônomo vê apenas produtores vinculados a ele")
    void agronomoSomenteVinculados() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        Conta outroAgronomo = registrar("AGRONOMO");
        long soja = culturaPorNome(c.agronomo(), "Soja");

        como(outroAgronomo, get("/api/v1/propriedades")).andExpect(jsonPath("$", hasSize(0)));
        como(outroAgronomo, get("/api/v1/laudos/" + laudoId)).andExpect(status().isForbidden());
        comoJson(outroAgronomo, post("/api/v1/recomendacoes/simular"), requisicaoRecomendacao(laudoId, soja, null))
                .andExpect(status().isForbidden());
        comoJson(outroAgronomo, post("/api/v1/laudos"), laudoDoExemplo(c.talhaoId())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Agrônomo não se vincula sozinho; o vínculo vem do produtor")
    void vinculoPorConsentimento() throws Exception {
        Conta agronomo = registrar("AGRONOMO");
        Conta produtor = registrar("PRODUTOR");

        comoJson(agronomo, post("/api/v1/vinculos"), """
                {"agronomoId":%d,"produtorId":%d}""".formatted(agronomo.id(), produtor.id()))
                .andExpect(status().isForbidden());

        comoJson(produtor, post("/api/v1/vinculos"), """
                {"agronomoEmail":"%s"}""".formatted(agronomo.email()))
                .andExpect(status().isCreated());
        como(agronomo, get("/api/v1/usuarios")).andExpect(jsonPath("$", hasSize(1)));

        como(produtor, delete("/api/v1/vinculos?agronomoId=" + agronomo.id() + "&produtorId=" + produtor.id()))
                .andExpect(status().isNoContent());
        como(agronomo, get("/api/v1/usuarios")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Agrônomo cadastra produtor e fica vinculado; não cadastra outros perfis")
    void agronomoCadastraProdutor() throws Exception {
        Conta agronomo = registrar("AGRONOMO");

        comoJson(agronomo, post("/api/v1/usuarios"), """
                {"nome":"Cliente","email":"cliente.%d@teste.local","senha":"senha-do-cliente","perfil":"PRODUTOR"}"""
                .formatted(agronomo.id()))
                .andExpect(status().isCreated());
        como(agronomo, get("/api/v1/usuarios")).andExpect(jsonPath("$", hasSize(1)));

        comoJson(agronomo, post("/api/v1/usuarios"), """
                {"nome":"Outro","email":"outro.%d@teste.local","senha":"senha-do-outro","perfil":"ADMIN"}"""
                .formatted(agronomo.id()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Somente o agrônomo registra e adota; o produtor não simula")
    void permissoesDeRecomendacao() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        String requisicao = requisicaoRecomendacao(laudoId, soja, null);

        comoJson(c.produtor(), post("/api/v1/recomendacoes/simular"), requisicao).andExpect(status().isForbidden());
        comoJson(c.produtor(), post("/api/v1/recomendacoes"), requisicao).andExpect(status().isForbidden());

        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"), requisicao)).get("id").asLong();
        como(c.produtor(), post("/api/v1/recomendacoes/" + id + "/adotar")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Produtor não lista usuários")
    void produtorNaoListaUsuarios() throws Exception {
        como(registrar("PRODUTOR"), get("/api/v1/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A propriedade só pode pertencer a um produtor, no Paraná")
    void regrasDaPropriedade() throws Exception {
        Conta agronomo = registrar("AGRONOMO");
        comoJson(agronomo, post("/api/v1/propriedades"), """
                {"nome":"Sem dono","municipio":"Toledo"}""")
                .andExpect(status().isUnprocessableEntity());
    }
}
