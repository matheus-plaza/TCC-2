package br.edu.tcc.agromotor.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * Segurança stateless com JWT Bearer (RF01). As regras de perfil e de propriedade dos
 * dados (RNF05) ficam nos casos de uso; aqui se exige apenas que a chamada esteja autenticada.
 */
@Configuration
public class SecurityConfig {

    private final ObjectMapper json;

    public SecurityConfig(ObjectMapper json) {
        this.json = json;
    }

    @Bean
    SecurityFilterChain filtroDeSeguranca(HttpSecurity http, JwtDecoder decoder) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/sistema/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.decoder(decoder).jwtAuthenticationConverter(conversorDePerfil()))
                        .authenticationEntryPoint((req, res, ex) -> problema(res, HttpStatus.UNAUTHORIZED,
                                "Não autenticado", "Faça login para acessar este recurso"))
                        .accessDeniedHandler((req, res, ex) -> problema(res, HttpStatus.FORBIDDEN,
                                "Acesso negado", "Seu perfil não permite esta operação")))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> problema(res, HttpStatus.UNAUTHORIZED,
                                "Não autenticado", "Faça login para acessar este recurso"))
                        .accessDeniedHandler((req, res, ex) -> problema(res, HttpStatus.FORBIDDEN,
                                "Acesso negado", "Seu perfil não permite esta operação")))
                .build();
    }

    @Bean
    CorsConfigurationSource configuracaoCors(@Value("${app.cors.origens}") List<String> origens) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origens);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Location", "Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static JwtAuthenticationConverter conversorDePerfil() {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(jwt ->
                List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString(JwtConfig.CLAIM_PERFIL))));
        return conversor;
    }

    private void problema(HttpServletResponse response, HttpStatus status, String titulo, String detalhe) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setProperty("erros", List.of(detalhe));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), problema);
    }
}
