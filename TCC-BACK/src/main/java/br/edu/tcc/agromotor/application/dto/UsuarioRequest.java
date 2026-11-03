package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados de registro público e de criação de usuário por administrador ou agrônomo. */
public record UsuarioRequest(
        @NotBlank(message = "informe o nome") @Size(max = 150, message = "Nome: use no máximo 150 caracteres") String nome,
        @NotBlank(message = "informe o e-mail") @Email(message = "e-mail inválido")
        @Size(max = 150, message = "E-mail: use no máximo 150 caracteres") String email,
        @NotBlank(message = "informe a senha") @Size(min = 8, max = 72, message = "a senha deve ter de 8 a 72 caracteres") String senha,
        @NotNull(message = "informe o perfil") PerfilUsuario perfil,
        @Size(max = 30, message = "Registro profissional: use no máximo 30 caracteres") String registroProfissional) {
}
