package br.edu.tcc.agromotor.domain.model;

public enum SistemaManejo {
    PLANTIO_DIRETO("Plantio Direto"),
    CONVENCIONAL("Convencional");

    private final String rotulo;

    SistemaManejo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
