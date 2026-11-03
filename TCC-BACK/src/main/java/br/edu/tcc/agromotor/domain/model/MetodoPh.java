package br.edu.tcc.agromotor.domain.model;

/**
 * Como o laboratório mediu o pH. Cada método tem sua coluna de classes na Tabela 3.2 do
 * manual; os valores não são convertidos de um método para o outro.
 */
public enum MetodoPh {
    CACL2(AtributoSolo.PH),
    AGUA(AtributoSolo.PH_AGUA);

    private final AtributoSolo atributo;

    MetodoPh(AtributoSolo atributo) {
        this.atributo = atributo;
    }

    public AtributoSolo atributo() {
        return atributo;
    }
}
