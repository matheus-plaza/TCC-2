package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.HistoricoTalhaoResponse;
import br.edu.tcc.agromotor.application.dto.PropriedadeRequest;
import br.edu.tcc.agromotor.application.dto.PropriedadeResponse;
import br.edu.tcc.agromotor.application.dto.SugestaoAntecessoraResponse;
import br.edu.tcc.agromotor.application.dto.TalhaoRequest;
import br.edu.tcc.agromotor.application.dto.TalhaoResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.usecase.historico.ConsultarHistoricoTalhaoUseCase;
import br.edu.tcc.agromotor.application.usecase.propriedade.GerenciarPropriedadesUseCase;
import br.edu.tcc.agromotor.application.usecase.propriedade.GerenciarTalhoesUseCase;
import br.edu.tcc.agromotor.application.usecase.recomendacao.ConsultarRecomendacoesUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

/** RF02 e RF07: propriedades, talhões e histórico do talhão. */
@RestController
@RequestMapping("/api/v1")
public class PropriedadeController {

    private final GerenciarPropriedadesUseCase propriedades;
    private final GerenciarTalhoesUseCase talhoes;
    private final ConsultarHistoricoTalhaoUseCase historico;
    private final ConsultarRecomendacoesUseCase recomendacoes;

    public PropriedadeController(GerenciarPropriedadesUseCase propriedades, GerenciarTalhoesUseCase talhoes,
                                 ConsultarHistoricoTalhaoUseCase historico, ConsultarRecomendacoesUseCase recomendacoes) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.historico = historico;
        this.recomendacoes = recomendacoes;
    }

    @GetMapping("/propriedades")
    public List<PropriedadeResponse> listar(UsuarioAutenticado ator) {
        return propriedades.listar(ator);
    }

    @GetMapping("/propriedades/{id}")
    public PropriedadeResponse detalhar(UsuarioAutenticado ator, @PathVariable Long id) {
        return propriedades.detalhar(ator, id);
    }

    @PostMapping("/propriedades")
    @ResponseStatus(HttpStatus.CREATED)
    public PropriedadeResponse criar(UsuarioAutenticado ator, @Valid @RequestBody PropriedadeRequest request) {
        return propriedades.criar(ator, request);
    }

    @PutMapping("/propriedades/{id}")
    public PropriedadeResponse atualizar(UsuarioAutenticado ator, @PathVariable Long id,
                                         @Valid @RequestBody PropriedadeRequest request) {
        return propriedades.atualizar(ator, id, request);
    }

    @DeleteMapping("/propriedades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(UsuarioAutenticado ator, @PathVariable Long id) {
        propriedades.excluir(ator, id);
    }

    @GetMapping("/talhoes")
    public List<TalhaoResponse> listarTalhoes(UsuarioAutenticado ator, @RequestParam(required = false) Long propriedadeId) {
        return talhoes.listar(ator, propriedadeId);
    }

    @GetMapping("/talhoes/{id}")
    public TalhaoResponse detalharTalhao(UsuarioAutenticado ator, @PathVariable Long id) {
        return talhoes.detalhar(ator, id);
    }

    @PostMapping("/propriedades/{propriedadeId}/talhoes")
    @ResponseStatus(HttpStatus.CREATED)
    public TalhaoResponse criarTalhao(UsuarioAutenticado ator, @PathVariable Long propriedadeId,
                                      @Valid @RequestBody TalhaoRequest request) {
        return talhoes.criar(ator, propriedadeId, request);
    }

    @PutMapping("/talhoes/{id}")
    public TalhaoResponse atualizarTalhao(UsuarioAutenticado ator, @PathVariable Long id,
                                          @Valid @RequestBody TalhaoRequest request) {
        return talhoes.atualizar(ator, id, request);
    }

    @DeleteMapping("/talhoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirTalhao(UsuarioAutenticado ator, @PathVariable Long id) {
        talhoes.excluir(ator, id);
    }

    @GetMapping("/talhoes/{id}/historico")
    public HistoricoTalhaoResponse historico(UsuarioAutenticado ator, @PathVariable Long id,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return historico.executar(ator, id, inicio, fim);
    }

    /** Cultura da última recomendação adotada; 204 quando o talhão ainda não tem histórico. */
    @GetMapping("/talhoes/{id}/sugestao-antecessora")
    public ResponseEntity<SugestaoAntecessoraResponse> sugestaoAntecessora(UsuarioAutenticado ator, @PathVariable Long id) {
        return recomendacoes.sugerirAntecessora(ator, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
