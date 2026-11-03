package br.edu.tcc.agromotor.domain.model;

import java.util.Arrays;

/** Camada amostrada no laudo. Calagem e NPK usam 0-20 cm; a gessagem avalia 20-40 cm. */
public enum ProfundidadeAmostra {
    CAMADA_0_20("0-20"),
    CAMADA_20_40("20-40");

    private final String codigo;

    ProfundidadeAmostra(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }

    public static ProfundidadeAmostra deCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(p -> p.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Profundidade inválida: " + codigo));
    }
}
