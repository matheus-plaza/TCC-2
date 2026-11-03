package br.edu.tcc.agromotor.domain.model;

public record Propriedade(Long id, Long produtorId, String nome, String municipio, String uf) {

    /** O manual normativo do sistema vale apenas para o Estado do Paraná. */
    public static final String UF_ATENDIDA = "PR";

    public Propriedade {
        new Validacao()
                .obrigatorio(produtorId, "Produtor")
                .obrigatorio(nome, "Nome da propriedade")
                .obrigatorio(municipio, "Município")
                .verdadeiro(UF_ATENDIDA.equals(uf), "O sistema atende apenas propriedades do Paraná (UF = PR)")
                .lancarSeHouverErros();
    }
}
