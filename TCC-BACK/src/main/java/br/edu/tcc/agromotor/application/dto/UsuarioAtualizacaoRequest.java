package br.edu.tcc.agromotor.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code senha} vazia mantém a senha atual. O perfil de um usuário não muda depois de criado. */
public record UsuarioAtualizacaoRequest(
        @NotBlank(message = "informe o nome") @Size(max = 150, message = "Nome: use no máximo 150 caracteres") String nome,
        @NotBlank(message = "informe o e-mail") @Email(message = "e-mail inválido")
        @Size(max = 150, message = "E-mail: use no máximo 150 caracteres") String email,
        @Size(min = 8, max = 72, message = "a senha deve ter de 8 a 72 caracteres") String senha,
        @Size(max = 30, message = "Registro profissional: use no máximo 30 caracteres") String registroProfissional) {
}
