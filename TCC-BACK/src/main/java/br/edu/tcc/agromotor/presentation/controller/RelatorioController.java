package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.usecase.relatorio.GerarReceituarioUseCase;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RF08 / UC11: exportação do receituário em PDF. */
@RestController
@RequestMapping("/api/v1/relatorios")
public class RelatorioController {

    private final GerarReceituarioUseCase receituario;

    public RelatorioController(GerarReceituarioUseCase receituario) {
        this.receituario = receituario;
    }

    @GetMapping(value = "/receituario/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> receituario(UsuarioAutenticado ator, @PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("receituario-" + id + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(receituario.executar(ator, id));
    }
}
