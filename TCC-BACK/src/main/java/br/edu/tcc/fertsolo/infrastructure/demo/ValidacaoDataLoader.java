package br.edu.tcc.fertsolo.infrastructure.demo;

import br.edu.tcc.fertsolo.application.dto.UsuarioRequest;
import br.edu.tcc.fertsolo.application.usecase.usuario.CriarContaUsuario;
import br.edu.tcc.fertsolo.domain.model.Cultura;
import br.edu.tcc.fertsolo.domain.model.LaudoSolo;
import br.edu.tcc.fertsolo.domain.model.MetodoPh;
import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import br.edu.tcc.fertsolo.domain.model.ProfundidadeAmostra;
import br.edu.tcc.fertsolo.domain.model.Propriedade;
import br.edu.tcc.fertsolo.domain.model.Recomendacao;
import br.edu.tcc.fertsolo.domain.model.SistemaManejo;
import br.edu.tcc.fertsolo.domain.model.Talhao;
import br.edu.tcc.fertsolo.domain.model.TexturaSolo;
import br.edu.tcc.fertsolo.domain.model.Usuario;
import br.edu.tcc.fertsolo.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.fertsolo.domain.repository.CulturaRepository;
import br.edu.tcc.fertsolo.domain.repository.LaudoRepository;
import br.edu.tcc.fertsolo.domain.repository.PropriedadeRepository;
import br.edu.tcc.fertsolo.domain.repository.RecomendacaoRepository;
import br.edu.tcc.fertsolo.domain.repository.TalhaoRepository;
import br.edu.tcc.fertsolo.domain.repository.UsuarioRepository;
import br.edu.tcc.fertsolo.domain.service.EngineCalculoAgronomico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Perfil "demo": grava os laudos sintéticos da validação e uma simulação de cada cultura por laudo. */
@Component
@Profile("demo")
@Order(2)
public class ValidacaoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ValidacaoDataLoader.class);
    private static final LocalDate DATA_LAUDO = LocalDate.parse("2026-10-01");
    private static final Map<String, String> CONDICOES = Map.of("muito-baixa", "Condição muito baixa", "baixa", "Condição baixa",
            "media", "Condição média", "alta", "Condição alta", "muito-alta", "Condição muito alta");

    private record Cenario(String cultura, String anterior, String rendimento) {
    }

    private static final List<Cenario> CENARIOS = List.of(
            new Cenario("Soja", "Trigo", "3.5"),
            new Cenario("Milho", "Soja", "7.5"),
            new Cenario("Trigo", "Soja", "3.0"));

    private final CriarContaUsuario criarConta;
    private final UsuarioRepository usuarios;
    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final LaudoRepository laudos;
    private final CulturaRepository culturas;
    private final RecomendacaoRepository recomendacoes;
    private final EngineCalculoAgronomico engine;
    private final String senha;
    private final String emailAgronomo;
    private final String emailValidacao;

    public ValidacaoDataLoader(CriarContaUsuario criarConta, UsuarioRepository usuarios, PropriedadeRepository propriedades,
                               TalhaoRepository talhoes, LaudoRepository laudos, CulturaRepository culturas,
                               RecomendacaoRepository recomendacoes, EngineCalculoAgronomico engine,
                               @Value("${app.demo.senha}") String senha,
                               @Value("${app.demo.agronomo}") String emailAgronomo,
                               @Value("${app.demo.produtor-validacao}") String emailValidacao) {
        this.criarConta = criarConta;
        this.usuarios = usuarios;
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.laudos = laudos;
        this.culturas = culturas;
        this.recomendacoes = recomendacoes;
        this.engine = engine;
        this.senha = senha;
        this.emailAgronomo = emailAgronomo;
        this.emailValidacao = emailValidacao;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Usuario agronomo = usuarios.buscarPorEmail(emailAgronomo).orElse(null);
        if (agronomo == null || usuarios.existeEmail(emailValidacao)) {
            return;
        }
        Map<String, Cultura> cultura = culturas.listar().stream().collect(Collectors.toMap(Cultura::nome, Function.identity()));
        Usuario produtor = criarConta.executar(new UsuarioRequest(ModoDemonstracaoConfig.NOME_PRODUTOR_VALIDACAO,
                emailValidacao, senha, PerfilUsuario.PRODUTOR, null));
        usuarios.vincular(agronomo.id(), produtor.id());

        Map<String, Map<String, Map<String, String>>> porCenario = new LinkedHashMap<>();
        for (Map<String, String> l : lerLaudos()) {
            porCenario.computeIfAbsent(l.get("solo") + "/" + l.get("condicao"), c -> new HashMap<>()).put(l.get("camada"), l);
        }

        Map<String, Propriedade> porSolo = new HashMap<>();
        LocalDateTime quando = DATA_LAUDO.plusDays(4).atTime(8, 0);
        int simulacoes = 0;
        for (Map<String, Map<String, String>> camadas : porCenario.values()) {
            Map<String, String> sup = camadas.get("0-20");
            Propriedade propriedade = porSolo.computeIfAbsent(sup.get("solo"), s -> propriedades.salvar(
                    new Propriedade(null, produtor.id(), sup.get("solo_nome"), sup.get("municipio"), Propriedade.UF_ATENDIDA)));
            Talhao talhao = talhoes.salvar(new Talhao(null, propriedade.id(), CONDICOES.get(sup.get("condicao")),
                    BigDecimal.TEN, textura(sup.get("argila"))));
            LaudoSolo subsuperficie = laudos.salvar(laudo(talhao, camadas.get("20-40")));
            LaudoSolo laudo = laudos.salvar(laudo(talhao, sup));
            for (Cenario c : CENARIOS) {
                EntradaRecomendacao entrada = new EntradaRecomendacao(laudo, subsuperficie, cultura.get(c.cultura()),
                        cultura.get(c.anterior()), SistemaManejo.PLANTIO_DIRETO, new BigDecimal(c.rendimento()), new BigDecimal("100"));
                quando = quando.plusMinutes(10);
                recomendacoes.salvar(Recomendacao.simulacao(entrada, engine.processar(entrada), agronomo.id(), quando));
                simulacoes++;
            }
        }
        log.warn("Perfil demo: {} laudos sintéticos de validação e {} simulações carregados.", porCenario.size() * 2, simulacoes);
    }

    private static List<Map<String, String>> lerLaudos() {
        try (var leitor = new BufferedReader(new InputStreamReader(
                new ClassPathResource("validacao/laudos-sinteticos.csv").getInputStream(), StandardCharsets.UTF_8))) {
            List<String> linhas = leitor.lines().toList();
            String[] cabecalho = linhas.get(0).split(";", -1);
            return linhas.subList(1, linhas.size()).stream()
                    .map(linha -> {
                        String[] valores = linha.split(";", -1);
                        Map<String, String> laudo = new LinkedHashMap<>();
                        for (int i = 0; i < cabecalho.length; i++) {
                            laudo.put(cabecalho[i], valores[i]);
                        }
                        return laudo;
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler os laudos sintéticos", e);
        }
    }

    private static LaudoSolo laudo(Talhao talhao, Map<String, String> l) {
        ProfundidadeAmostra camada = l.get("camada").equals("0-20") ? ProfundidadeAmostra.CAMADA_0_20 : ProfundidadeAmostra.CAMADA_20_40;
        return new LaudoSolo(null, talhao.id(), DATA_LAUDO, camada, numero(l.get("ph")), MetodoPh.CACL2, numero(l.get("mo")),
                numero(l.get("p")), numero(l.get("k")), numero(l.get("ca")), numero(l.get("mg")), numero(l.get("al")),
                numero(l.get("h_al")), numero(l.get("argila")),
                numero(l.get("zn")), numero(l.get("b")), numero(l.get("cu")), numero(l.get("mn")));
    }

    private static BigDecimal numero(String valor) {
        return valor.isEmpty() ? null : new BigDecimal(valor);
    }

    private static TexturaSolo textura(String argila) {
        BigDecimal valor = new BigDecimal(argila);
        if (valor.compareTo(new BigDecimal("15")) < 0) {
            return TexturaSolo.LEVE;
        }
        if (valor.compareTo(new BigDecimal("35")) <= 0) {
            return TexturaSolo.MEDIA;
        }
        return valor.compareTo(new BigDecimal("60")) <= 0 ? TexturaSolo.ARGILOSA : TexturaSolo.MUITO_ARGILOSA;
    }
}
