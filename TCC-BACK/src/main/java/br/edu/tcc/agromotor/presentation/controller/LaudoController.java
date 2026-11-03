package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.IndicadoresRequest;
import br.edu.tcc.agromotor.application.dto.LaudoRequest;
import br.edu.tcc.agromotor.application.dto.LaudoResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.usecase.laudo.CadastrarLaudoUseCase;
import br.edu.tcc.agromotor.application.usecase.laudo.CalcularIndicadoresUseCase;
import br.edu.tcc.agromotor.application.usecase.laudo.ConsultarLaudosUseCase;
import br.edu.tcc.agromotor.application.usecase.laudo.ExcluirLaudoUseCase;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** RF03, RF04 e UC07: registro, interpretação e consulta de laudos de solo. */
@RestController
@RequestMapping("/api/v1/laudos")
public class LaudoController {

    private final CadastrarLaudoUseCase cadastrar;
    private final ConsultarLaudosUseCase consultar;
    private final ExcluirLaudoUseCase excluir;
    private final CalcularIndicadoresUseCase calcularIndicadores;

    public LaudoController(CadastrarLaudoUseCase cadastrar, ConsultarLaudosUseCase consultar, ExcluirLaudoUseCase excluir,
                           CalcularIndicadoresUseCase calcularIndicadores) {
        this.cadastrar = cadastrar;
        this.consultar = consultar;
        this.excluir = excluir;
        this.calcularIndicadores = calcularIndicadores;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LaudoResponse cadastrar(UsuarioAutenticado ator, @Valid @RequestBody LaudoRequest request) {
        return cadastrar.executar(ator, request);
    }

    @GetMapping
    public List<LaudoResponse> listar(UsuarioAutenticado ator,
                                      @RequestParam(required = false) Long talhaoId,
                                      @RequestParam(required = false) String profundidadeCm,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return consultar.listar(ator, new FiltroLaudos(talhaoId, filtroDeProfundidade(profundidadeCm), inicio, fim));
    }

    private static ProfundidadeAmostra filtroDeProfundidade(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        try {
            return ProfundidadeAmostra.deCodigo(codigo);
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Profundidade: use 0-20 ou 20-40");
        }
    }

    @GetMapping("/{id}")
    public LaudoResponse detalhar(UsuarioAutenticado ator, @PathVariable Long id) {
        return consultar.detalhar(ator, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(UsuarioAutenticado ator, @PathVariable Long id) {
        excluir.executar(ator, id);
    }

    /** Pré-visualização dos valores calculados da Tela A, sem gravar nada. */
    @PostMapping("/indicadores")
    public IndicadoresSolo indicadores(@Valid @RequestBody IndicadoresRequest request) {
        return calcularIndicadores.executar(request);
    }
}
