package br.edu.tcc.fertsolo;

import br.edu.tcc.fertsolo.application.dto.UsuarioRequest;
import br.edu.tcc.fertsolo.application.usecase.usuario.CriarContaUsuario;
import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Escopo do administrador, eliminação de conta pelo titular e registro das ações administrativas. */
class PrivacidadeIntegracaoTest extends IntegracaoBase {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private CriarContaUsuario criarConta;

    @Test
    @DisplayName("O administrador não vê propriedades, talhões, laudos nem recomendações")
    void adminSemDadosAgronomicos() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long recomendacaoId = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"),
                requisicaoRecomendacao(laudoId, soja, null))).get("id").asLong();
        Conta admin = administrador();

        como(admin, get("/api/v1/propriedades")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        como(admin, get("/api/v1/laudos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        como(admin, get("/api/v1/propriedades/" + c.propriedadeId())).andExpect(status().isForbidden());
        como(admin, get("/api/v1/laudos/" + laudoId)).andExpect(status().isForbidden());
        como(admin, get("/api/v1/recomendacoes/" + recomendacaoId)).andExpect(status().isForbidden());
        como(admin, get("/api/v1/talhoes/" + c.talhaoId() + "/historico")).andExpect(status().isForbidden());
        comoJson(admin, post("/api/v1/recomendacoes/simular"), requisicaoRecomendacao(laudoId, soja, null))
                .andExpect(status().isForbidden());

        como(admin, get("/api/v1/usuarios")).andExpect(status().isOk());
        como(admin, get("/api/v1/vinculos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("O produtor exclui a própria conta e leva junto propriedades, laudos, recomendações e vínculos")
    void autoexclusaoDoProdutor() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long soja = culturaPorNome(c.agronomo(), "Soja");
        long adotada = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"),
                requisicaoRecomendacao(laudoId, soja, null))).get("id").asLong();
        como(c.agronomo(), post("/api/v1/recomendacoes/" + adotada + "/adotar")).andExpect(status().isOk());
        comoJson(c.produtor(), put("/api/v1/recomendacoes/" + adotada + "/safra"), """
                {"produtividadeColhidaTonHa":4.2,"dataColheita":"%s","custoAdubacaoReais":null}""".formatted(LocalDate.now()))
                .andExpect(status().isOk());
        long simulacao = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"),
                requisicaoRecomendacao(laudoId, soja, null))).get("id").asLong();

        como(c.produtor(), delete("/api/v1/usuarios/me")).andExpect(status().isNoContent());

        assertThat(contar("SELECT count(*) FROM usuarios WHERE id = ?", c.produtor().id())).isZero();
        assertThat(contar("SELECT count(*) FROM propriedades WHERE produtor_id = ?", c.produtor().id())).isZero();
        assertThat(contar("SELECT count(*) FROM talhoes WHERE id = ?", c.talhaoId())).isZero();
        assertThat(contar("SELECT count(*) FROM analises_solo WHERE id = ?", laudoId)).isZero();
        assertThat(contar("SELECT count(*) FROM recomendacoes WHERE id IN (?, ?)", adotada, simulacao)).isZero();
        assertThat(contar("SELECT count(*) FROM registros_safra WHERE recomendacao_id = ?", adotada)).isZero();
        assertThat(contar("SELECT count(*) FROM agronomo_produtor WHERE produtor_id = ?", c.produtor().id())).isZero();
        assertThat(acoes(c.produtor().id())).containsExactly("AUTOEXCLUSAO_CONTA");
        como(c.agronomo(), get("/api/v1/usuarios")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Fora da exclusão de conta, a recomendação adotada continua imutável")
    void adotadaContinuaImutavel() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"),
                requisicaoRecomendacao(laudoId, culturaPorNome(c.agronomo(), "Soja"), null))).get("id").asLong();
        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar")).andExpect(status().isOk());

        assertThatThrownBy(() -> jdbc.update("DELETE FROM recomendacoes WHERE id = ?", id))
                .hasMessageContaining("imutável");
        assertThatThrownBy(() -> jdbc.update("UPDATE recomendacoes SET adotado_por_id = NULL WHERE id = ?", id))
                .hasMessageContaining("imutável");
    }

    @Test
    @DisplayName("O admin exclui um produtor com propriedades, em cascata, e a ação fica registrada")
    void adminExcluiProdutor() throws Exception {
        Cenario c = cenario();
        cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        Conta admin = administrador();

        como(admin, delete("/api/v1/usuarios/" + c.produtor().id())).andExpect(status().isNoContent());

        assertThat(contar("SELECT count(*) FROM usuarios WHERE id = ?", c.produtor().id())).isZero();
        assertThat(contar("SELECT count(*) FROM propriedades WHERE id = ?", c.propriedadeId())).isZero();
        assertThat(acoes(admin.id())).containsExactly("EXCLUSAO_CONTA");
        assertThat(jdbc.queryForObject("SELECT alvo_id FROM auditoria_administrativa WHERE ator_id = ?",
                Long.class, admin.id())).isEqualTo(c.produtor().id());
    }

    @Test
    @DisplayName("O agrônomo exclui a conta: saem os vínculos, a recomendação adotada fica com o produtor")
    void autoexclusaoDoAgronomo() throws Exception {
        Cenario c = cenario();
        long laudoId = cadastrarLaudo(c.agronomo(), laudoDoExemplo(c.talhaoId()));
        long id = corpo(comoJson(c.agronomo(), post("/api/v1/recomendacoes"),
                requisicaoRecomendacao(laudoId, culturaPorNome(c.agronomo(), "Soja"), null))).get("id").asLong();
        como(c.agronomo(), post("/api/v1/recomendacoes/" + id + "/adotar")).andExpect(status().isOk());

        como(c.agronomo(), delete("/api/v1/usuarios/me")).andExpect(status().isNoContent());

        assertThat(contar("SELECT count(*) FROM usuarios WHERE id = ?", c.agronomo().id())).isZero();
        assertThat(contar("SELECT count(*) FROM agronomo_produtor WHERE agronomo_id = ?", c.agronomo().id())).isZero();
        assertThat(contar("SELECT count(*) FROM recomendacoes WHERE id = ? AND status = 'ADOTADA'"
                + " AND gerado_por_id IS NULL AND adotado_por_id IS NULL", id)).isEqualTo(1);
        como(c.produtor(), get("/api/v1/recomendacoes/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavel").doesNotExist());
        assertThat(acoes(c.agronomo().id())).containsExactly("AUTOEXCLUSAO_CONTA");
    }

    @Test
    @DisplayName("O único administrador não exclui a própria conta")
    void unicoAdministrador() throws Exception {
        List<Long> outros = jdbc.queryForList("SELECT id FROM usuarios WHERE perfil = 'ADMIN'", Long.class);
        Conta admin;
        try {
            outros.forEach(id -> jdbc.update("UPDATE usuarios SET perfil = 'PRODUTOR' WHERE id = ?", id));
            admin = administrador();
            como(admin, delete("/api/v1/usuarios/me")).andExpect(status().isConflict());
            como(admin, delete("/api/v1/usuarios/" + admin.id())).andExpect(status().isConflict());
        } finally {
            outros.forEach(id -> jdbc.update("UPDATE usuarios SET perfil = 'ADMIN' WHERE id = ?", id));
        }

        Conta outro = administrador();
        como(admin, delete("/api/v1/usuarios/me")).andExpect(status().isNoContent());
        assertThat(contar("SELECT count(*) FROM usuarios WHERE id = ?", admin.id())).isZero();
        assertThat(acoes(admin.id())).containsExactly("AUTOEXCLUSAO_CONTA");
        assertThat(outro.id()).isNotEqualTo(admin.id());
    }

    @Test
    @DisplayName("Criação e edição de conta e criação e remoção de vínculo pelo admin ficam registradas")
    void auditoriaDoAdmin() throws Exception {
        Conta admin = administrador();
        Conta agronomo = registrar("AGRONOMO");

        long produtorId = corpo(comoJson(admin, post("/api/v1/usuarios"), """
                {"nome":"Cliente","email":"cliente.%s@teste.local","senha":"senha-do-cliente","perfil":"PRODUTOR"}"""
                .formatted(UUID.randomUUID()))
                .andExpect(status().isCreated())).get("id").asLong();
        comoJson(admin, put("/api/v1/usuarios/" + produtorId), """
                {"nome":"Cliente Editado","email":"editado.%s@teste.local"}""".formatted(UUID.randomUUID()))
                .andExpect(status().isOk());
        comoJson(admin, post("/api/v1/vinculos"), """
                {"agronomoId":%d,"produtorId":%d}""".formatted(agronomo.id(), produtorId))
                .andExpect(status().isCreated());
        como(admin, delete("/api/v1/vinculos?agronomoId=" + agronomo.id() + "&produtorId=" + produtorId))
                .andExpect(status().isNoContent());

        assertThat(acoes(admin.id()))
                .containsExactly("CRIACAO_CONTA", "EDICAO_CONTA", "CRIACAO_VINCULO", "REMOCAO_VINCULO");
        assertThat(contar("SELECT count(*) FROM auditoria_administrativa WHERE ator_id = ? AND alvo_id <> ?",
                admin.id(), produtorId)).isZero();
    }

    private Conta administrador() throws Exception {
        String email = "admin." + UUID.randomUUID() + "@teste.local";
        long id = criarConta.executar(new UsuarioRequest("Admin de Teste", email, SENHA, PerfilUsuario.ADMIN, null)).id();
        String token = corpo(mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}""".formatted(email, SENHA)))
                .andExpect(status().isOk())).get("token").asText();
        return new Conta(token, id, email);
    }

    private long contar(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private List<String> acoes(long atorId) {
        return jdbc.queryForList("SELECT acao FROM auditoria_administrativa WHERE ator_id = ? ORDER BY id",
                String.class, atorId);
    }
}
