package br.edu.tcc.agromotor.infrastructure.config;

import br.edu.tcc.agromotor.application.usecase.UseCase;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Cada chamada a um caso de uso roda em uma transação, sem que a camada de aplicação
 * precise da anotação {@code @Transactional} do Spring.
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TransacaoCasoDeUsoConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static TransactionInterceptor transacaoDeCasoDeUso() {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());
        interceptor.setTransactionManagerBeanName("transactionManager");
        return interceptor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor transacaoNosCasosDeUso(TransactionInterceptor transacaoDeCasoDeUso) {
        return new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(UseCase.class), transacaoDeCasoDeUso);
    }
}
