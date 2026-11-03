package br.edu.tcc.agromotor.application.dto;

import jakarta.validation.constraints.Email;

/**
 * O produtor autoriza um agrônomo informando {@code agronomoEmail}; o administrador
 * informa {@code agronomoId} e {@code produtorId}.
 */
public record VinculoRequest(Long agronomoId, Long produtorId, @Email(message = "e-mail inválido") String agronomoEmail) {
}
