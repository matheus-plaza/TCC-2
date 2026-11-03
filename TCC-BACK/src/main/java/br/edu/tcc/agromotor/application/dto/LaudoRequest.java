package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.MetodoPh;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Entrada do laudo (RF03). As regras de negócio são revalidadas pela entidade de domínio. */
public record LaudoRequest(
        @NotNull(message = "selecione o talhão") Long talhaoId,
        @NotNull(message = "informe a data da coleta") @PastOrPresent(message = "a data da coleta não pode ser futura") LocalDate dataAnalise,
        @NotNull(message = "informe a profundidade") @Pattern(regexp = "0-20|20-40", message = "Profundidade: use 0-20 ou 20-40") String profundidadeCm,
        @NotNull(message = "informe o pH")
        @DecimalMin(value = "3.0", message = "o pH deve estar entre 3,0 e 9,0")
        @DecimalMax(value = "9.0", message = "o pH deve estar entre 3,0 e 9,0")
        @Digits(integer = 2, fraction = 2, message = "pH: use até 2 casas decimais") BigDecimal ph,
        @NotNull(message = "informe se o pH foi medido em CaCl₂ ou em H₂O") MetodoPh metodoPh,
        @NotNull(message = "informe a matéria orgânica") @Digits(integer = 3, fraction = 2, message = "Matéria orgânica: use até 3 dígitos inteiros e 2 casas decimais (máximo 999,99)") BigDecimal moGDm3,
        @NotNull(message = "informe o fósforo") @Digits(integer = 3, fraction = 2, message = "Fósforo: use até 3 dígitos inteiros e 2 casas decimais (máximo 999,99)") BigDecimal pMehlich,
        @NotNull(message = "informe o potássio") @Digits(integer = 2, fraction = 3, message = "Potássio: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)") BigDecimal kCmolc,
        @NotNull(message = "informe o cálcio") @Digits(integer = 2, fraction = 3, message = "Cálcio: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)") BigDecimal caCmolc,
        @NotNull(message = "informe o magnésio") @Digits(integer = 2, fraction = 3, message = "Magnésio: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)") BigDecimal mgCmolc,
        @NotNull(message = "informe o alumínio") @Digits(integer = 2, fraction = 3, message = "Alumínio: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)") BigDecimal alCmolc,
        @NotNull(message = "informe a acidez potencial") @Digits(integer = 2, fraction = 3, message = "Acidez potencial: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)") BigDecimal hAlCmolc,
        @NotNull(message = "informe a argila")
        @DecimalMin(value = "0", message = "a argila deve estar entre 0 e 100%")
        @DecimalMax(value = "100", message = "a argila deve estar entre 0 e 100%")
        @Digits(integer = 3, fraction = 2, message = "Argila: use até 2 casas decimais") BigDecimal argilaPorcentagem,
        @Digits(integer = 4, fraction = 2, message = "Zinco: use até 4 dígitos inteiros e 2 casas decimais (máximo 9.999,99)") BigDecimal znMgDm3,
        @Digits(integer = 4, fraction = 2, message = "Boro: use até 4 dígitos inteiros e 2 casas decimais (máximo 9.999,99)") BigDecimal bMgDm3,
        @Digits(integer = 4, fraction = 2, message = "Cobre: use até 4 dígitos inteiros e 2 casas decimais (máximo 9.999,99)") BigDecimal cuMgDm3,
        @Digits(integer = 4, fraction = 2, message = "Manganês: use até 4 dígitos inteiros e 2 casas decimais (máximo 9.999,99)") BigDecimal mnMgDm3) {
}
