package br.edu.tcc.agromotor.infrastructure.config;

import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;
import br.edu.tcc.agromotor.domain.service.EngineCalculoAgronomico;
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
        basePackages = "br.edu.tcc.agromotor.application",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
public class UseCaseConfig {

    @Bean
    public EngineCalculoAgronomico engineCalculoAgronomico(ParametroRepository parametros) {
        return new EngineCalculoAgronomico(parametros);
    }
}
