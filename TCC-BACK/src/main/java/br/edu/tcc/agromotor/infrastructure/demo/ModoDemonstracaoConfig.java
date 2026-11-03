package br.edu.tcc.agromotor.infrastructure.demo;

import br.edu.tcc.agromotor.application.dto.SistemaInfoResponse.ContaDemonstracao;
import br.edu.tcc.agromotor.application.port.ModoDemonstracao;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class ModoDemonstracaoConfig {

    public static final String NOME_ADMIN = "Administração AgroMotor";
    public static final String NOME_AGRONOMO = "Carla Mendes";
    public static final String NOME_PRODUTOR = "José Antunes";
    public static final String NOME_OUTRO_PRODUTOR = "Marta Ribeiro";

    @Bean
    @Profile("!demo")
    ModoDemonstracao semContasDeDemonstracao() {
        return List::of;
    }

    @Bean
    @Profile("demo")
    ModoDemonstracao contasDeDemonstracao(@Value("${app.demo.senha}") String senha,
                                          @Value("${app.demo.admin}") String admin,
                                          @Value("${app.demo.agronomo}") String agronomo,
                                          @Value("${app.demo.produtor}") String produtor) {
        List<ContaDemonstracao> contas = List.of(
                new ContaDemonstracao(PerfilUsuario.AGRONOMO, NOME_AGRONOMO, agronomo, senha),
                new ContaDemonstracao(PerfilUsuario.PRODUTOR, NOME_PRODUTOR, produtor, senha),
                new ContaDemonstracao(PerfilUsuario.ADMIN, NOME_ADMIN, admin, senha));
        return () -> contas;
    }
}
