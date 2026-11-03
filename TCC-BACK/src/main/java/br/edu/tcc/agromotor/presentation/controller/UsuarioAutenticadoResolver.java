package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** Permite aos controllers receber o usuário do token como parâmetro comum. */
@Configuration
public class UsuarioAutenticadoResolver implements HandlerMethodArgumentResolver, WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UsuarioAutenticado.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) autenticacao.getPrincipal();
        return new UsuarioAutenticado(Long.valueOf(jwt.getSubject()), PerfilUsuario.valueOf(jwt.getClaimAsString("perfil")));
    }
}
