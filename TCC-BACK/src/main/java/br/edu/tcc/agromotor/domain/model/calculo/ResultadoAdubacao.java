package br.edu.tcc.agromotor.domain.model.calculo;

import br.edu.tcc.agromotor.domain.model.FaixaDose;

import java.util.List;

public record ResultadoAdubacao(
        FaixaDose doseNkgHa,
        FaixaDose doseP2O5kgHa,
        FaixaDose doseK2OkgHa,
        String classeFosforo,
        String classePotassio,
        String classeMateriaOrganica,
        String justificativaN,
        String justificativaP,
        String justificativaK,
        List<String> memoria) {
}
