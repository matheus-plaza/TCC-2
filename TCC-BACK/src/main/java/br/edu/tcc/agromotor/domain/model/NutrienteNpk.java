package br.edu.tcc.agromotor.domain.model;

/** Nutriente de uma célula das tabelas de dose do manual. */
public enum NutrienteNpk {
    N("N"),
    P2O5("P₂O₅"),
    K2O("K₂O");

    private final String rotulo;

    NutrienteNpk(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
