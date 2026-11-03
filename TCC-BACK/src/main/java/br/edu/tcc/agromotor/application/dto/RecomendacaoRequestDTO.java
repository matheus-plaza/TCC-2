package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Contrato da seção 8.1 da especificação.
 *
 * @param culturaAnteriorId nulo, junto com {@code grupoAnterior}, significa pousio/palhada
 * @param prntCalcario      nulo assume o PRNT base de 100%
 * @param grupoAnterior     grupo da cultura anterior que não está cadastrada
 */
public record RecomendacaoRequestDTO(
        @NotNull(message = "selecione o laudo") Long analiseSoloId,
        @NotNull(message = "selecione a cultura alvo") Long culturaAlvoId,
        Long culturaAnteriorId,
        @NotNull(message = "selecione o sistema de manejo") SistemaManejo sistemaManejo,
        @NotNull(message = "informe a expectativa de rendimento")
        @DecimalMin(value = "0.01", message = "a expectativa de rendimento deve ser maior que zero")
        @Digits(integer = 3, fraction = 2, message = "Expectativa de rendimento: use até 3 dígitos inteiros e 2 casas decimais (máximo 999,99)") BigDecimal expectativaRendimentoTonHa,
        @DecimalMin(value = "0.01", message = "o PRNT deve ser maior que zero")
        @DecimalMax(value = "200", message = "o PRNT deve ser no máximo 200%")
        @Digits(integer = 3, fraction = 2, message = "PRNT: use até 2 casas decimais") BigDecimal prntCalcario,
        GrupoBotanico grupoAnterior) {
}
