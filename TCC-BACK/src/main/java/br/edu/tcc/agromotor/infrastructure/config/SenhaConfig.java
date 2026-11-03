package br.edu.tcc.agromotor.infrastructure.config;

import br.edu.tcc.agromotor.application.port.CifradorSenha;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Senhas cifradas com BCrypt (RF01). */
@Configuration
public class SenhaConfig {

    @Bean
    CifradorSenha cifradorSenha() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return new CifradorSenha() {
            @Override
            public String cifrar(String senha) {
                return encoder.encode(senha);
            }

            @Override
            public boolean confere(String senha, String hash) {
                return encoder.matches(senha, hash);
            }
        };
    }
}
