package br.edu.tcc.fertsolo.application.port;

/** Registro das ações administrativas sobre contas e vínculos. */
public interface RegistroAuditoria {

    String CRIACAO_CONTA = "CRIACAO_CONTA";
    String EDICAO_CONTA = "EDICAO_CONTA";
    String EXCLUSAO_CONTA = "EXCLUSAO_CONTA";
    String AUTOEXCLUSAO_CONTA = "AUTOEXCLUSAO_CONTA";
    String CRIACAO_VINCULO = "CRIACAO_VINCULO";
    String REMOCAO_VINCULO = "REMOCAO_VINCULO";

    void registrar(Long atorId, String acao, Long alvoId);
}
