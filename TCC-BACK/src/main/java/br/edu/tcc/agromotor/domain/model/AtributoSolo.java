package br.edu.tcc.agromotor.domain.model;

/**
 * Atributos do laudo enquadrados em classes de fertilidade (RF04).
 * As faixas de cada atributo ficam em {@code nepar_faixa_interpretacao}.
 */
public enum AtributoSolo {
    PH("pH (CaCl₂)", ""),
    PH_AGUA("pH (H₂O)", ""),
    MATERIA_ORGANICA("Matéria orgânica", "g/dm³"),
    FOSFORO("Fósforo (P)", "mg/dm³"),
    POTASSIO("Potássio (K⁺)", "cmolc/dm³"),
    CALCIO("Cálcio (Ca²⁺)", "cmolc/dm³"),
    MAGNESIO("Magnésio (Mg²⁺)", "cmolc/dm³"),
    SATURACAO_BASES("Saturação por bases (V%)", "%"),
    SATURACAO_ALUMINIO("Saturação por alumínio (m%)", "%"),
    ZINCO("Zinco (Zn)", "mg/dm³"),
    BORO("Boro (B)", "mg/dm³"),
    COBRE("Cobre (Cu)", "mg/dm³"),
    MANGANES("Manganês (Mn)", "mg/dm³");

    private final String rotulo;
    private final String unidade;

    AtributoSolo(String rotulo, String unidade) {
        this.rotulo = rotulo;
        this.unidade = unidade;
    }

    public String rotulo() {
        return rotulo;
    }

    public String unidade() {
        return unidade;
    }
}
