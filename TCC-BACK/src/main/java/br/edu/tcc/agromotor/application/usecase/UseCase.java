package br.edu.tcc.agromotor.application.usecase;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um caso de uso. É uma anotação da própria aplicação, sem dependência do
 * Spring: a infraestrutura registra as classes anotadas como beans e abre uma
 * transação em cada chamada, mantendo esta camada independente de framework.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCase {
}
