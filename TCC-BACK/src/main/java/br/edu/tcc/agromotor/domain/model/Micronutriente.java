package br.edu.tcc.agromotor.domain.model;

public enum Micronutriente {
    ZINCO("Zn", "Zinco"),
    BORO("B", "Boro"),
    COBRE("Cu", "Cobre"),
    MANGANES("Mn", "Manganês");

    private final String simbolo;
    private final String rotulo;

    Micronutriente(String simbolo, String rotulo) {
        this.simbolo = simbolo;
        this.rotulo = rotulo;
    }

    public String simbolo() {
        return simbolo;
    }

    public String rotulo() {
        return rotulo;
    }
}
