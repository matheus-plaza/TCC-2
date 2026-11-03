package br.edu.tcc.agromotor.infrastructure.config;

import br.edu.tcc.agromotor.application.dto.UsuarioRequest;
import br.edu.tcc.agromotor.application.usecase.usuario.CriarContaUsuario;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador a partir de ADMIN_EMAIL e ADMIN_PASSWORD. Nenhuma senha
 * padrão acompanha o sistema: sem as variáveis, nenhum administrador é criado.
 */
@Component
public class AdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final UsuarioRepository usuarios;
    private final CriarContaUsuario criarConta;
    private final String email;
    private final String senha;

    public AdministradorInicial(UsuarioRepository usuarios, CriarContaUsuario criarConta,
                                @Value("${app.admin.email}") String email, @Value("${app.admin.senha}") String senha) {
        this.usuarios = usuarios;
        this.criarConta = criarConta;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || senha.isBlank() || usuarios.existePerfil(PerfilUsuario.ADMIN)) {
            return;
        }
        criarConta.executar(new UsuarioRequest("Administrador", email, senha, PerfilUsuario.ADMIN, null));
        log.info("Administrador inicial criado: {}", email);
    }
}
