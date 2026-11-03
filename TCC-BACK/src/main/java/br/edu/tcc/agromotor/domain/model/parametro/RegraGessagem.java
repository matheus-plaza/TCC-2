package br.edu.tcc.agromotor.domain.model.parametro;

import java.math.BigDecimal;

/** Critérios na camada de 20-40 cm: gesso indicado se m% &gt; limite ou Ca²⁺ &lt; limite. */
public record RegraGessagem(BigDecimal limiteMPorcentagem, BigDecimal limiteCaCmolc) {
}
