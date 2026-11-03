package br.edu.tcc.agromotor;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;

/** PostgreSQL 16 real, sem Docker, para os testes de integração e a execução local. */
final class BancoEmbutido {

    private static final int PORTA_LOCAL = 55432;

    private static EmbeddedPostgres deTeste;

    private BancoEmbutido() {
    }

    /** Banco descartável, compartilhado pelos testes da mesma execução. */
    static synchronized EmbeddedPostgres deTeste() {
        if (deTeste == null) {
            try {
                deTeste = EmbeddedPostgres.builder().start();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return deTeste;
    }

    /**
     * URL do banco local persistente. Se a execução anterior foi encerrada à força, o
     * PostgreSQL pode continuar no ar nessa porta: nesse caso, ele é reaproveitado.
     */
    static String urlLocal() throws IOException {
        if (!portaLocalEmUso()) {
            EmbeddedPostgres.builder()
                    .setPort(PORTA_LOCAL)
                    .setDataDirectory(Path.of(System.getProperty("user.home"), ".agromotor", "pgdata"))
                    .setCleanDataDirectory(false)
                    .start();
        }
        return "jdbc:postgresql://localhost:" + PORTA_LOCAL + "/postgres";
    }

    private static boolean portaLocalEmUso() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", PORTA_LOCAL), 500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
