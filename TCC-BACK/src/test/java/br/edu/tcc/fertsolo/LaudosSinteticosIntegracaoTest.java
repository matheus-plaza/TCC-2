package br.edu.tcc.fertsolo;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Compara cada laudo sintético com o resultado de {@code docs/validacao/referencia_laudos_sinteticos.py}. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LaudosSinteticosIntegracaoTest extends IntegracaoBase {

    private Cenario cenario;
    private final Map<String, Map<String, String>> laudos = new HashMap<>();
    private final Map<String, Long> laudoSuperficial = new HashMap<>();

    @ParameterizedTest(name = "{0} · condição {1} · {2}")
    @CsvFileSource(resources = "/validacao/referencia-laudos-sinteticos.csv", delimiter = ';', numLinesToSkip = 1)
    void comparaComReferencia(String solo, String condicao, String cultura, String rendimento, String anterior,
                              String sb, String total, String v1, String nc, String gesso,
                              String classeP, String classeK, String kCtc, String classeMo,
                              String nMin, String nMax, String pMin, String pMax, String kMin, String kMax,
                              String zn, String b, String cu, String mn) throws Exception {
        long laudoId = laudoDoCenario(solo, condicao);
        String requisicao = """
                {"analiseSoloId":%d,"culturaAlvoId":%d,"culturaAnteriorId":%d,"sistemaManejo":"PLANTIO_DIRETO",
                 "expectativaRendimentoTonHa":%s,"prntCalcario":100.0}""".formatted(laudoId,
                culturaPorNome(cenario.agronomo(), cultura), culturaPorNome(cenario.agronomo(), anterior), rendimento);
        JsonNode r = corpo(comoJson(cenario.agronomo(), post("/api/v1/recomendacoes/simular"), requisicao)
                .andExpect(status().isOk()));

        assertThat(r.at("/resumoQuimico/somaBases").decimalValue()).as("SB").isEqualByComparingTo(sb);
        assertThat(r.at("/resumoQuimico/ctcTotal").decimalValue()).as("T").isEqualByComparingTo(total);
        assertThat(r.at("/resumoQuimico/v1Porcentagem").decimalValue()).as("V1%").isEqualByComparingTo(v1);
        assertThat(r.at("/calagem/necessidadeCalcarioTonHa").decimalValue()).as("NC").isEqualByComparingTo(nc);
        assertThat(r.at("/gessagem/doseKgHa").decimalValue()).as("Gesso").isEqualByComparingTo(gesso);

        JsonNode a = r.get("adubacao");
        assertThat(a.get("classeFosforo").asText()).as("Classe de P").isEqualTo(classeP);
        assertThat(a.get("classePotassio").asText()).as("Classe de K").isEqualTo(classeK);
        assertThat(a.get("classeMateriaOrganica").asText()).as("Classe de M.O.").isEqualTo(classeMo);
        faixa(a.get("doseNkgHa"), "N", nMin, nMax);
        faixa(a.get("doseP2O5kgHa"), "P2O5", pMin, pMax);
        faixa(a.get("doseK2OkgHa"), "K2O", kMin, kMax);

        Map<String, String> esperado = new HashMap<>();
        esperado.put("Zn", zn);
        esperado.put("B", b);
        esperado.put("Cu", cu);
        esperado.put("Mn", mn);
        for (JsonNode m : r.get("micronutrientes")) {
            String dose = esperado.get(m.get("simbolo").asText());
            if (dose == null) {
                assertThat(m.get("avaliado").asBoolean()).as(m.get("simbolo").asText() + " avaliado").isFalse();
            } else {
                assertThat(m.get("doseKgHa").decimalValue()).as(m.get("simbolo").asText()).isEqualByComparingTo(dose);
            }
        }
    }

    private static void faixa(JsonNode dose, String nome, String min, String max) {
        assertThat(dose.get("min").decimalValue()).as(nome + " mínimo").isEqualByComparingTo(min);
        assertThat(dose.get("max").decimalValue()).as(nome + " máximo").isEqualByComparingTo(max);
    }

    private long laudoDoCenario(String solo, String condicao) throws Exception {
        if (cenario == null) {
            cenario = cenario();
            carregarLaudos();
        }
        String chave = solo + "/" + condicao;
        Long id = laudoSuperficial.get(chave);
        if (id != null) {
            return id;
        }
        long talhaoId = corpo(comoJson(cenario.agronomo(), post("/api/v1/propriedades/" + cenario.propriedadeId() + "/talhoes"), """
                {"identificacao":"%s","areaHectares":10.0,"texturaPredominante":"ARGILOSA"}""".formatted(chave))
                .andExpect(status().isCreated())).get("id").asLong();
        cadastrarLaudo(cenario.agronomo(), laudoJson(talhaoId, laudos.get(chave + "/20-40")));
        id = cadastrarLaudo(cenario.agronomo(), laudoJson(talhaoId, laudos.get(chave + "/0-20")));
        laudoSuperficial.put(chave, id);
        return id;
    }

    private void carregarLaudos() throws Exception {
        try (var leitor = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/validacao/laudos-sinteticos.csv"), StandardCharsets.UTF_8))) {
            List<String> linhas = leitor.lines().toList();
            String[] cabecalho = linhas.get(0).split(";", -1);
            for (String linha : linhas.subList(1, linhas.size())) {
                String[] valores = linha.split(";", -1);
                Map<String, String> laudo = new HashMap<>();
                for (int i = 0; i < cabecalho.length; i++) {
                    laudo.put(cabecalho[i], valores[i]);
                }
                laudos.put(laudo.get("solo") + "/" + laudo.get("condicao") + "/" + laudo.get("camada"), laudo);
            }
        }
    }

    private static String laudoJson(long talhaoId, Map<String, String> l) {
        return """
                {"talhaoId":%d,"dataAnalise":"2026-10-01","profundidadeCm":"%s","ph":%s,"metodoPh":"CACL2","moGDm3":%s,
                 "pMehlich":%s,"kCmolc":%s,"caCmolc":%s,"mgCmolc":%s,"alCmolc":%s,"hAlCmolc":%s,"argilaPorcentagem":%s,
                 "znMgDm3":%s,"bMgDm3":%s,"cuMgDm3":%s,"mnMgDm3":%s}""".formatted(talhaoId, l.get("camada"), l.get("ph"),
                l.get("mo"), l.get("p"), l.get("k"), l.get("ca"), l.get("mg"), l.get("al"), l.get("h_al"), l.get("argila"),
                nulo(l.get("zn")), nulo(l.get("b")), nulo(l.get("cu")), nulo(l.get("mn")));
    }

    private static String nulo(String valor) {
        return valor.isEmpty() ? "null" : new BigDecimal(valor).toPlainString();
    }
}
