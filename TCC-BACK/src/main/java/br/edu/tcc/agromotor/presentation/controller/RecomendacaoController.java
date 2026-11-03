package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.RecomendacaoRequestDTO;
import br.edu.tcc.agromotor.application.dto.RecomendacaoResponseDTO;
import br.edu.tcc.agromotor.application.dto.RegistroSafraRequest;
import br.edu.tcc.agromotor.application.dto.SafraResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.usecase.recomendacao.AdotarRecomendacaoUseCase;
import br.edu.tcc.agromotor.application.usecase.recomendacao.ConsultarRecomendacoesUseCase;
import br.edu.tcc.agromotor.application.usecase.recomendacao.ProcessarRecomendacaoUseCase;
import br.edu.tcc.agromotor.application.usecase.recomendacao.RegistrarSafraUseCase;
import br.edu.tcc.agromotor.domain.model.FiltroRecomendacoes;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** RF05 a RF07: simulação em memória, registro, adoção e consulta de recomendações. */
@RestController
@RequestMapping("/api/v1/recomendacoes")
public class RecomendacaoController {

    private final ProcessarRecomendacaoUseCase processar;
    private final AdotarRecomendacaoUseCase adotar;
    private final ConsultarRecomendacoesUseCase consultar;
    private final RegistrarSafraUseCase safra;

    public RecomendacaoController(ProcessarRecomendacaoUseCase processar, AdotarRecomendacaoUseCase adotar,
                                  ConsultarRecomendacoesUseCase consultar, RegistrarSafraUseCase safra) {
        this.processar = processar;
        this.adotar = adotar;
        this.consultar = consultar;
        this.safra = safra;
    }

    /** RF05: calcula em memória; não grava nada e responde com {@code id} nulo. */
    @PostMapping("/simular")
    public RecomendacaoResponseDTO simular(UsuarioAutenticado ator, @Valid @RequestBody RecomendacaoRequestDTO request) {
        return processar.simular(ator, request);
    }

    /** Registra o cenário com status SIMULACAO, recalculando no servidor. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecomendacaoResponseDTO registrar(UsuarioAutenticado ator, @Valid @RequestBody RecomendacaoRequestDTO request) {
        return processar.registrarSimulacao(ator, request);
    }

    /** RF06: altera o status de SIMULACAO para ADOTADA. */
    @PostMapping("/{id}/adotar")
    public RecomendacaoResponseDTO adotar(UsuarioAutenticado ator, @PathVariable Long id) {
        return adotar.executar(ator, id);
    }

    @GetMapping
    public List<RecomendacaoResponseDTO> listar(UsuarioAutenticado ator,
                                                @RequestParam(required = false) Long talhaoId,
                                                @RequestParam(required = false) Long laudoId,
                                                @RequestParam(required = false) StatusRecomendacao status,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return consultar.listar(ator, new FiltroRecomendacoes(talhaoId, laudoId, status, inicio, fim));
    }

    @GetMapping("/{id}")
    public RecomendacaoResponseDTO detalhar(UsuarioAutenticado ator, @PathVariable Long id) {
        return consultar.detalhar(ator, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(UsuarioAutenticado ator, @PathVariable Long id) {
        consultar.excluir(ator, id);
    }

    /** Produtividade esperada, colheita e gasto com adubação da safra. */
    @GetMapping("/{id}/safra")
    public SafraResponse safra(UsuarioAutenticado ator, @PathVariable Long id) {
        return safra.consultar(ator, id);
    }

    @PutMapping("/{id}/safra")
    public SafraResponse registrarSafra(UsuarioAutenticado ator, @PathVariable Long id,
                                        @Valid @RequestBody RegistroSafraRequest request) {
        return safra.salvar(ator, id, request);
    }
}
