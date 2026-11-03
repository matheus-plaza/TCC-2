package br.edu.tcc.fertsolo.infrastructure.config;

import br.edu.tcc.fertsolo.application.usecase.UseCase;
import br.edu.tcc.fertsolo.domain.repository.ParametroRepository;
import br.edu.tcc.fertsolo.domain.service.EngineCalculoAgronomico;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Composição das camadas internas: casos de uso e motor não conhecem o Spring, é a
 * infraestrutura que os registra como beans.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackages = "br.edu.tcc.fertsolo.application",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
public class UseCaseConfig {

    @Bean
    public EngineCalculoAgronomico engineCalculoAgronomico(ParametroRepository parametros) {
        return new EngineCalculoAgronomico(parametros);
    }
}
