package br.edu.tcc.agromotor.domain.model;

/** Grupo botânico da cultura: define a Fixação Biológica e o crédito/débito de nitrogênio. */
public enum GrupoBotanico {
    LEGUMINOSA("Leguminosa"),
    GRAMINEA("Gramínea");

    private final String rotulo;

    GrupoBotanico(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
