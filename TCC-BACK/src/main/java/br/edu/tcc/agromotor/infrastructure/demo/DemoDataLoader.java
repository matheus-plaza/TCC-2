package br.edu.tcc.agromotor.infrastructure.demo;

import br.edu.tcc.agromotor.application.dto.UsuarioRequest;
import br.edu.tcc.agromotor.application.usecase.usuario.CriarContaUsuario;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.MetodoPh;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;
import br.edu.tcc.agromotor.domain.model.Propriedade;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.RegistroSafra;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.Talhao;
import br.edu.tcc.agromotor.domain.model.TexturaSolo;
import br.edu.tcc.agromotor.domain.model.Usuario;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.repository.CulturaRepository;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.PropriedadeRepository;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.RegistroSafraRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;
import br.edu.tcc.agromotor.domain.repository.UsuarioRepository;
import br.edu.tcc.agromotor.domain.service.EngineCalculoAgronomico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Perfil "demo": popula o banco vazio com dados FICTÍCIOS para apresentar as telas.
 * As recomendações saem do motor real, sobre as tabelas paramétricas vigentes.
 */
@Component
@Profile("demo")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final CriarContaUsuario criarConta;
    private final UsuarioRepository usuarios;
    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final LaudoRepository laudos;
    private final CulturaRepository culturas;
    private final RecomendacaoRepository recomendacoes;
    private final RegistroSafraRepository safras;
    private final EngineCalculoAgronomico engine;
    private final String senha;
    private final String emailAdmin;
    private final String emailAgronomo;
    private final String emailProdutor;
    private final String emailOutroProdutor;

    public DemoDataLoader(CriarContaUsuario criarConta, UsuarioRepository usuarios, PropriedadeRepository propriedades,
                          TalhaoRepository talhoes, LaudoRepository laudos, CulturaRepository culturas,
                          RecomendacaoRepository recomendacoes, RegistroSafraRepository safras, EngineCalculoAgronomico engine,
                          @Value("${app.demo.senha}") String senha,
                          @Value("${app.demo.admin}") String emailAdmin,
                          @Value("${app.demo.agronomo}") String emailAgronomo,
                          @Value("${app.demo.produtor}") String emailProdutor,
                          @Value("${app.demo.outro-produtor}") String emailOutroProdutor) {
        this.criarConta = criarConta;
        this.usuarios = usuarios;
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.laudos = laudos;
        this.culturas = culturas;
        this.recomendacoes = recomendacoes;
        this.safras = safras;
        this.engine = engine;
        this.senha = senha;
        this.emailAdmin = emailAdmin;
        this.emailAgronomo = emailAgronomo;
        this.emailProdutor = emailProdutor;
        this.emailOutroProdutor = emailOutroProdutor;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existeEmail(emailAdmin)) {
            return;
        }
        Map<String, Cultura> cultura = culturas.listar().stream().collect(Collectors.toMap(Cultura::nome, Function.identity()));

        conta(ModoDemonstracaoConfig.NOME_ADMIN, emailAdmin, PerfilUsuario.ADMIN, null);
        Usuario agronomo = conta(ModoDemonstracaoConfig.NOME_AGRONOMO, emailAgronomo, PerfilUsuario.AGRONOMO, "PR-000000/D");
        Usuario produtor = conta(ModoDemonstracaoConfig.NOME_PRODUTOR, emailProdutor, PerfilUsuario.PRODUTOR, null);
        Usuario outroProdutor = conta(ModoDemonstracaoConfig.NOME_OUTRO_PRODUTOR, emailOutroProdutor, PerfilUsuario.PRODUTOR, null);
        usuarios.vincular(agronomo.id(), produtor.id());

        Propriedade sitio = propriedade(produtor, "Sítio Boa Esperança", "Santa Helena");
        Propriedade fazenda = propriedade(produtor, "Fazenda Três Marias", "Toledo");
        Propriedade chacara = propriedade(outroProdutor, "Chácara Recanto", "Cascavel");

        Talhao gleba1 = talhao(sitio, "Gleba 01", "32.50", TexturaSolo.ARGILOSA);
        Talhao gleba2 = talhao(sitio, "Gleba 02", "18.00", TexturaSolo.MEDIA);
        Talhao norte = talhao(fazenda, "Talhão Norte", "55.00", TexturaSolo.MUITO_ARGILOSA);
        talhao(fazenda, "Talhão Sul", "41.20", TexturaSolo.ARGILOSA);
        Talhao area1 = talhao(chacara, "Área 1", "12.00", TexturaSolo.MEDIA);

        // Gleba 01: quatro safras de evolução; o laudo mais recente é o exemplo da Tela A do SRS
        LaudoSolo g1a = laudo(gleba1, "2023-09-12", camada020(), "4.6", "24.0", "6.5", "0.18", "2.2", "0.7", "0.6", "6.4", "46.0", "0.9", "0.25");
        LaudoSolo g1b = laudo(gleba1, "2024-09-10", camada020(), "4.8", "25.5", "8.2", "0.24", "2.6", "0.8", "0.4", "5.9", "45.0", "1.0", "0.28");
        LaudoSolo g1c = laudo(gleba1, "2025-09-16", camada020(), "5.0", "27.0", "10.4", "0.30", "2.9", "1.0", "0.2", "5.2", "45.0", "1.2", "0.32");
        LaudoSolo g1d = laudo(gleba1, "2026-09-15", camada020(), "5.2", "28.5", "12.0", "0.35", "3.2", "1.1", "0.1", "4.8", "45.0", "1.4", "0.35");
        laudo(gleba1, "2026-09-15", camada2040(), "4.9", "18.0", "4.0", "0.12", "1.4", "0.5", "0.1", "4.5", "52.0", null, null);

        // Gleba 02: fósforo baixo e alumínio em subsuperfície (aciona a gessagem)
        LaudoSolo g2 = laudo(gleba2, "2026-08-20", camada020(), "4.7", "18.5", "7.0", "0.15", "1.8", "0.6", "0.5", "5.6", "28.0", "0.7", "0.20");
        laudo(gleba2, "2026-08-20", camada2040(), "4.3", "12.0", "2.5", "0.07", "0.4", "0.2", "0.7", "6.2", "34.0", null, null);

        // Talhão Norte: solo muito argiloso, já corrigido
        LaudoSolo tn = laudo(norte, "2025-10-05", camada020(), "5.6", "42.0", "14.5", "0.55", "5.4", "1.8", "0", "4.1", "68.0", "2.3", "0.65");
        laudo(area1, "2026-07-08", camada020(), "5.1", "22.0", "9.5", "0.22", "2.5", "0.8", "0.2", "4.6", "33.0", null, null);

        safra(adotada(g1a, cultura.get("Soja"), cultura.get("Trigo"), "4.0", "85", "2023-10-05", agronomo), "3.70", "2024-02-20", "31000.00", produtor);
        safra(adotada(g1a, cultura.get("Trigo"), cultura.get("Soja"), "3.0", "85", "2024-04-18", agronomo), "3.20", "2024-09-25", "17500.00", produtor);
        safra(adotada(g1b, cultura.get("Soja"), cultura.get("Trigo"), "4.2", "90", "2024-10-08", agronomo), "4.40", "2025-02-10", "33800.00", produtor);
        safra(adotada(g1b, cultura.get("Milho"), cultura.get("Soja"), "9.5", "90", "2025-02-14", agronomo), "8.90", "2025-07-20", "46200.00", produtor);
        safra(adotada(g1c, cultura.get("Soja"), cultura.get("Milho"), "4.5", "90", "2025-10-02", agronomo), "4.60", "2026-02-18", "36500.00", produtor);
        safra(adotada(g1c, cultura.get("Trigo"), cultura.get("Soja"), "3.2", "90", "2026-04-20", agronomo), "3.10", "2026-09-28", "18900.00", produtor);
        safra(adotada(tn, cultura.get("Milho"), cultura.get("Soja"), "10.0", "100", "2026-01-25", agronomo), "10.60", "2026-07-05", "84000.00", produtor);

        recomendacoes.salvar(Recomendacao.simulacao(entrada(g2, cultura.get("Milho"), cultura.get("Soja"), "8.0", "100"),
                engine.processar(entrada(g2, cultura.get("Milho"), cultura.get("Soja"), "8.0", "100")),
                agronomo.id(), LocalDate.parse("2026-10-20").atTime(9, 30)));

        log.warn("Perfil demo: dados e parâmetros fictícios carregados. As contas estão em application-demo.properties.");
    }

    private Usuario conta(String nome, String email, PerfilUsuario perfil, String registro) {
        return criarConta.executar(new UsuarioRequest(nome, email, senha, perfil, registro));
    }

    private Propriedade propriedade(Usuario produtor, String nome, String municipio) {
        return propriedades.salvar(new Propriedade(null, produtor.id(), nome, municipio, Propriedade.UF_ATENDIDA));
    }

    private Talhao talhao(Propriedade propriedade, String identificacao, String area, TexturaSolo textura) {
        return talhoes.salvar(new Talhao(null, propriedade.id(), identificacao, new BigDecimal(area), textura));
    }

    private static ProfundidadeAmostra camada020() {
        return ProfundidadeAmostra.CAMADA_0_20;
    }

    private static ProfundidadeAmostra camada2040() {
        return ProfundidadeAmostra.CAMADA_20_40;
    }

    private LaudoSolo laudo(Talhao talhao, String data, ProfundidadeAmostra profundidade, String ph, String mo, String p,
                            String k, String ca, String mg, String al, String hAl, String argila, String zn, String b) {
        return laudos.salvar(new LaudoSolo(null, talhao.id(), LocalDate.parse(data), profundidade, new BigDecimal(ph),
                MetodoPh.CACL2, new BigDecimal(mo), new BigDecimal(p), new BigDecimal(k), new BigDecimal(ca), new BigDecimal(mg),
                new BigDecimal(al), new BigDecimal(hAl), new BigDecimal(argila),
                zn == null ? null : new BigDecimal(zn), b == null ? null : new BigDecimal(b), null, null));
    }

    private EntradaRecomendacao entrada(LaudoSolo laudo, Cultura alvo, Cultura anterior, String rendimento, String prnt) {
        LaudoSolo subsuperficie = laudos.listar(EscopoProdutores.total(),
                        new FiltroLaudos(laudo.talhaoId(), ProfundidadeAmostra.CAMADA_20_40, null, null))
                .stream().findFirst().orElse(null);
        return new EntradaRecomendacao(laudo, subsuperficie, alvo, anterior, SistemaManejo.PLANTIO_DIRETO,
                new BigDecimal(rendimento), new BigDecimal(prnt));
    }

    private Recomendacao adotada(LaudoSolo laudo, Cultura alvo, Cultura anterior, String rendimento, String prnt, String data, Usuario agronomo) {
        EntradaRecomendacao entrada = entrada(laudo, alvo, anterior, rendimento, prnt);
        var quando = LocalDate.parse(data).atTime(10, 0);
        return recomendacoes.salvar(Recomendacao.simulacao(entrada, engine.processar(entrada), agronomo.id(), quando)
                .adotar(agronomo.id(), quando));
    }

    private void safra(Recomendacao recomendacao, String colhida, String data, String custo, Usuario produtor) {
        LocalDate colheita = LocalDate.parse(data);
        safras.salvar(new RegistroSafra(null, recomendacao.id(), new BigDecimal(colhida), colheita, new BigDecimal(custo),
                produtor.id(), colheita.atTime(18, 0)));
    }
}
