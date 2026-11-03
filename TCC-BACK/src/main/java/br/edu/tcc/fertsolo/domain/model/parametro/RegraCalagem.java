package br.edu.tcc.fertsolo.domain.model.parametro;

import br.edu.tcc.fertsolo.domain.model.SistemaManejo;

/** Forma de aplicação do calcário em cada sistema de manejo. */
public record RegraCalagem(SistemaManejo manejo, String observacao) {
}
