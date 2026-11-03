package br.edu.tcc.agromotor.infrastructure.pdf;

import br.edu.tcc.agromotor.application.dto.LaudoResponse;
import br.edu.tcc.agromotor.application.dto.RecomendacaoResponseDTO;
import br.edu.tcc.agromotor.application.dto.TalhaoResponse;
import br.edu.tcc.agromotor.application.port.GeradorReceituario;
import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.MetodoPh;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static br.edu.tcc.agromotor.domain.model.Decimais.formatar;

/** Renderiza o receituário agronômico (RF08 / UC11) com OpenPDF. */
@Component
public class OpenPdfGeradorReceituario implements GeradorReceituario {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color VERDE = new Color(31, 74, 49);
    private static final Color CINZA_CLARO = new Color(240, 243, 240);
    private static final Color AMARELO_CLARO = new Color(252, 241, 214);

    private static final Font TITULO = new Font(Font.HELVETICA, 15, Font.BOLD, VERDE);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY);
    private static final Font SECAO = new Font(Font.HELVETICA, 11, Font.BOLD, VERDE);
    private static final Font ROTULO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font TEXTO = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font PEQUENO = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, Color.DARK_GRAY);
    private static final Font CABECALHO_TABELA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);

    @Override
    public byte[] gerar(RecomendacaoResponseDTO r) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 36, 36);
        PdfWriter.getInstance(doc, saida);
        doc.open();

        doc.add(new Paragraph("Receituário Agronômico de Calagem e Adubação", TITULO));
        doc.add(new Paragraph(texto("Base normativa: Manual de Adubação e Calagem para o Estado do Paraná, 2ª ed. "
                + "(SBCS/NEPAR, 2019). Recomendação nº " + r.id() + ", adotada em " + r.dataAdocao().format(DATA)
                + adotadaPor(r.responsavel()) + ". Parâmetros: versão " + r.versaoParametros().codigo() + "."), SUBTITULO));
        adicionarAvisoDeParametros(doc, r);

        adicionarIdentificacao(doc, r);
        adicionarLaudo(doc, r);
        adicionarRecomendacaoProposta(doc, r);
        adicionarJustificativas(doc, r);
        adicionarLista(doc, "Avisos", r.avisos(), TEXTO);
        adicionarResponsavel(doc, r.responsavel());
        adicionarLista(doc, "Memória de cálculo", r.memoriaCalculo(), PEQUENO);

        doc.close();
        return saida.toByteArray();
    }

    private void adicionarAvisoDeParametros(Document doc, RecomendacaoResponseDTO r) {
        if (r.versaoParametros().conferidaManual()) {
            return;
        }
        PdfPTable aviso = tabela(new float[]{1});
        PdfPCell celula = celula("DOCUMENTO DE TESTE: esta recomendação usou parâmetros ainda não conferidos com o Manual "
                + "NEPAR. As doses são ilustrativas e não devem ser aplicadas em campo.", ROTULO);
        celula.setBackgroundColor(AMARELO_CLARO);
        celula.setPadding(8);
        aviso.addCell(celula);
        aviso.setSpacingBefore(8);
        doc.add(aviso);
    }

    private void adicionarIdentificacao(Document doc, RecomendacaoResponseDTO r) {
        TalhaoResponse t = r.talhao();
        secao(doc, "Identificação");
        PdfPTable tabela = tabela(new float[]{1.3f, 2.7f, 1.3f, 2.7f});
        par(tabela, "Produtor", t.produtorNome());
        par(tabela, "Propriedade", t.propriedadeNome() + " (" + t.municipio() + "/" + t.uf() + ")");
        par(tabela, "Talhão", t.identificacao() + " (" + formatar(t.areaHectares()) + " ha)");
        par(tabela, "Data do laudo", r.laudo().dataAnalise().format(DATA));
        par(tabela, "Cultura alvo", r.culturaAlvo().nome());
        par(tabela, "Antecessora", r.antecessora());
        par(tabela, "Manejo", r.sistemaManejo().rotulo());
        par(tabela, "Rendimento esperado", formatar(r.expectativaRendimentoTonHa()) + " t/ha");
        doc.add(tabela);
    }

    private void adicionarLaudo(Document doc, RecomendacaoResponseDTO r) {
        LaudoResponse l = r.laudo();
        RecomendacaoResponseDTO.ResumoQuimico q = r.resumoQuimico();
        secao(doc, "Laudo de solo (" + l.profundidadeCm() + " cm)");
        PdfPTable tabela = tabela(new float[]{1, 1, 1, 1, 1, 1});
        cabecalho(tabela, l.metodoPh() == MetodoPh.AGUA ? "pH H2O" : "pH CaCl2", "M.O. (g/dm³)", "P (mg/dm³)", "K (cmolc/dm³)", "Ca (cmolc/dm³)", "Mg (cmolc/dm³)");
        valores(tabela, l.ph(), l.moGDm3(), l.pMehlich(), l.kCmolc(), l.caCmolc(), l.mgCmolc());
        cabecalho(tabela, "Al (cmolc/dm³)", "H+Al (cmolc/dm³)", "SB (cmolc/dm³)", "T (cmolc/dm³)", "V (%)", "Argila (%)");
        valores(tabela, l.alCmolc(), l.hAlCmolc(), q.somaBases(), q.ctcTotal(), q.v1Porcentagem(), l.argilaPorcentagem());
        doc.add(tabela);
    }

    private void adicionarRecomendacaoProposta(Document doc, RecomendacaoResponseDTO r) {
        BigDecimal area = r.talhao().areaHectares();
        RecomendacaoResponseDTO.Adubacao a = r.adubacao();
        secao(doc, "Recomendação proposta");
        PdfPTable tabela = tabela(new float[]{3.2f, 1.6f, 1.8f});
        cabecalho(tabela, "Corretivo / nutriente", "Dose por hectare", "Total no talhão (" + formatar(area) + " ha)");
        linha(tabela, area, "Calcário (PRNT " + formatar(r.prntCalcario()) + "%)", r.calagem().necessidadeCalcarioTonHa(), "t");
        linha(tabela, area, "Gesso agrícola", r.gessagem().doseKgHa(), "kg");
        linhaEmFaixa(tabela, area, "Nitrogênio (N)", a.doseNkgHa());
        linhaEmFaixa(tabela, area, "Fósforo (P2O5)", a.doseP2O5kgHa());
        linhaEmFaixa(tabela, area, "Potássio (K2O)", a.doseK2OkgHa());
        r.micronutrientes().forEach(m -> linha(tabela, area, m.nutriente() + " (" + m.simbolo() + ")", m.doseKgHa(), "kg"));
        doc.add(tabela);
    }

    private void adicionarJustificativas(Document doc, RecomendacaoResponseDTO r) {
        List<String> itens = new ArrayList<>(List.of(
                "Calagem: " + r.calagem().justificativa() + " " + r.calagem().observacao(),
                "Gessagem: " + r.gessagem().justificativa(),
                "Nitrogênio: " + r.adubacao().justificativaN(),
                "Fósforo: " + r.adubacao().justificativaP(),
                "Potássio: " + r.adubacao().justificativaK(),
                "NPK: o manual indica as doses em faixas; a definição dentro da faixa cabe ao responsável técnico."));
        r.micronutrientes().forEach(m -> itens.add(m.nutriente() + ": " + m.justificativa()));
        adicionarLista(doc, "Justificativas", itens, TEXTO);
    }

    private void adicionarLista(Document doc, String titulo, List<String> itens, Font fonte) {
        if (itens.isEmpty()) {
            return;
        }
        secao(doc, titulo);
        for (String item : itens) {
            Paragraph p = new Paragraph("• " + texto(item), fonte);
            p.setIndentationLeft(6);
            doc.add(p);
        }
    }

    private void adicionarResponsavel(Document doc, RecomendacaoResponseDTO.Responsavel responsavel) {
        Paragraph espaco = new Paragraph(" ");
        espaco.setSpacingBefore(26);
        doc.add(espaco);
        Paragraph assinatura = new Paragraph("_____________________________________________", TEXTO);
        assinatura.setAlignment(Element.ALIGN_CENTER);
        doc.add(assinatura);

        String nome = responsavel == null ? "Responsável técnico (Eng. Agrônomo)"
                : responsavel.nome() + (responsavel.registroProfissional() == null ? "" : " - CREA " + responsavel.registroProfissional());
        Paragraph identificacao = new Paragraph(texto(nome), ROTULO);
        identificacao.setAlignment(Element.ALIGN_CENTER);
        doc.add(identificacao);

        Paragraph aviso = new Paragraph(texto("Documento gerado por Sistema de Apoio à Decisão. Não substitui a avaliação "
                + "técnica presencial nem a assinatura de engenheiro agrônomo habilitado."), PEQUENO);
        aviso.setAlignment(Element.ALIGN_CENTER);
        aviso.setSpacingBefore(12);
        doc.add(aviso);
    }

    private static String adotadaPor(RecomendacaoResponseDTO.Responsavel responsavel) {
        return responsavel == null ? " pelo engenheiro agrônomo responsável"
                : " pelo engenheiro agrônomo " + responsavel.nome();
    }

    private static void secao(Document doc, String titulo) {
        Paragraph p = new Paragraph(texto(titulo), SECAO);
        p.setSpacingBefore(12);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private static PdfPTable tabela(float[] larguras) {
        PdfPTable tabela = new PdfPTable(larguras);
        tabela.setWidthPercentage(100);
        return tabela;
    }

    private static void par(PdfPTable tabela, String rotulo, String valor) {
        PdfPCell celulaRotulo = celula(rotulo, ROTULO);
        celulaRotulo.setBackgroundColor(CINZA_CLARO);
        tabela.addCell(celulaRotulo);
        tabela.addCell(celula(valor, TEXTO));
    }

    private static void cabecalho(PdfPTable tabela, String... titulos) {
        for (String titulo : titulos) {
            PdfPCell c = celula(titulo, CABECALHO_TABELA);
            c.setBackgroundColor(VERDE);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabela.addCell(c);
        }
    }

    private static void valores(PdfPTable tabela, BigDecimal... valores) {
        for (BigDecimal v : valores) {
            PdfPCell c = celula(formatar(v), TEXTO);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabela.addCell(c);
        }
    }

    private static void linha(PdfPTable tabela, BigDecimal area, String descricao, BigDecimal dose, String unidade) {
        tabela.addCell(celula(descricao, TEXTO));
        PdfPCell porHectare = celula(dose == null ? "Não avaliado" : formatar(dose) + " " + unidade + "/ha", dose == null ? PEQUENO : ROTULO);
        porHectare.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(porHectare);
        PdfPCell total = celula(dose == null ? "-" : formatar(dose.multiply(area)) + " " + unidade, TEXTO);
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(total);
    }

    private static void linhaEmFaixa(PdfPTable tabela, BigDecimal area, String descricao, FaixaDose dose) {
        tabela.addCell(celula(descricao, TEXTO));
        PdfPCell porHectare = celula(dose.descricao() + " kg/ha", ROTULO);
        porHectare.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(porHectare);
        FaixaDose total = new FaixaDose(dose.min().multiply(area), dose.max().multiply(area));
        PdfPCell celulaTotal = celula(total.descricao() + " kg", TEXTO);
        celulaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(celulaTotal);
    }

    private static PdfPCell celula(String conteudo, Font fonte) {
        PdfPCell c = new PdfPCell(new Phrase(texto(conteudo), fonte));
        c.setPadding(4);
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(Color.LIGHT_GRAY);
        return c;
    }

    /** As fontes padrão do PDF (WinAnsi) não têm subscritos e alguns símbolos usados na memória. */
    static String texto(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("₂", "2").replace("₅", "5").replace("≥", ">=").replace("≤", "<=")
                .replace("→", "->").replace("−", "-").replace("⁺", "+").replace("—", "-");
    }
}
