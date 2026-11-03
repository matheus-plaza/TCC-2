package br.edu.tcc.agromotor.infrastructure.config;

import br.edu.tcc.agromotor.application.port.GeradorToken;
import br.edu.tcc.agromotor.domain.model.Usuario;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/** Emissão e validação de JWT com HMAC-SHA256. O subject do token é o id do usuário. */
@Configuration
public class JwtConfig {

    public static final String CLAIM_PERFIL = "perfil";

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);
    private static final int BYTES_MINIMOS_DO_SEGREDO = 32;

    private final SecretKey chave;
    private final Duration validade;

    public JwtConfig(@Value("${app.jwt.segredo}") String segredo,
                     @Value("${app.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.chave = new SecretKeySpec(bytesDoSegredo(segredo), "HmacSHA256");
        this.validade = Duration.ofMinutes(expiracaoMinutos);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    @Bean
    GeradorToken geradorToken(JwtEncoder encoder) {
        return (Usuario usuario) -> {
            Instant agora = Instant.now();
            Instant expiraEm = agora.plus(validade);
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("agromotor")
                    .subject(usuario.id().toString())
                    .issuedAt(agora)
                    .expiresAt(expiraEm)
                    .claim(CLAIM_PERFIL, usuario.perfil().name())
                    .build();
            String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                    .getTokenValue();
            return new GeradorToken.TokenGerado(token, expiraEm);
        };
    }

    private static byte[] bytesDoSegredo(String segredo) {
        if (segredo == null || segredo.isBlank()) {
            log.warn("JWT_SECRET não definido: usando segredo aleatório; os tokens deixam de valer ao reiniciar.");
            byte[] aleatorio = new byte[BYTES_MINIMOS_DO_SEGREDO];
            new SecureRandom().nextBytes(aleatorio);
            return aleatorio;
        }
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < BYTES_MINIMOS_DO_SEGREDO) {
            throw new IllegalStateException("JWT_SECRET deve ter pelo menos " + BYTES_MINIMOS_DO_SEGREDO + " bytes");
        }
        return bytes;
    }
}
