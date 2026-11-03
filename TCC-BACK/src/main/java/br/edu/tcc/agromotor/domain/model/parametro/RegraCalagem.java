package br.edu.tcc.agromotor.domain.model.parametro;

import br.edu.tcc.agromotor.domain.model.SistemaManejo;

/** Forma de aplicação do calcário em cada sistema de manejo. */
public record RegraCalagem(SistemaManejo manejo, String observacao) {
}
