package br.edu.tcc.fertsolo;

import org.springframework.boot.SpringApplication;

import java.io.IOException;

/**
 * Sobe o sistema com PostgreSQL embutido e dados de demonstração, sem Docker:
 * {@code ./mvnw spring-boot:test-run}. Os dados ficam em ~/.fertsolo/pgdata.
 */
public final class TestFertSoloApplication {

    private TestFertSoloApplication() {
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("spring.datasource.url", BancoEmbutido.urlLocal());
        System.setProperty("spring.datasource.username", "postgres");
        System.setProperty("spring.datasource.password", "postgres");
        System.setProperty("spring.profiles.active", System.getProperty("spring.profiles.active", "demo"));
        SpringApplication.run(FertSoloApplication.class, args);
    }
}
