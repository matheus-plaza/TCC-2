package br.edu.tcc.agromotor.application.dto;

import java.math.BigDecimal;

public record PropriedadeResponse(
        Long id,
        Long produtorId,
        String produtorNome,
        String nome,
        String municipio,
        String uf,
        int quantidadeTalhoes,
        BigDecimal areaTotalHectares) {
}
