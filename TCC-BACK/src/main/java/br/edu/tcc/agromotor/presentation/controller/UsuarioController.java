package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.UsuarioAtualizacaoRequest;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.dto.UsuarioRequest;
import br.edu.tcc.agromotor.application.dto.UsuarioResponse;
import br.edu.tcc.agromotor.application.dto.VinculoRequest;
import br.edu.tcc.agromotor.application.dto.VinculoResponse;
import br.edu.tcc.agromotor.application.usecase.usuario.GerenciarUsuariosUseCase;
import br.edu.tcc.agromotor.application.usecase.usuario.GerenciarVinculosUseCase;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import jakarta.validation.Valid;
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

import java.util.List;

/** Contas de usuário e vínculos agrônomo-produtor (RNF05). */
@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

    private final GerenciarUsuariosUseCase usuarios;
    private final GerenciarVinculosUseCase vinculos;

    public UsuarioController(GerenciarUsuariosUseCase usuarios, GerenciarVinculosUseCase vinculos) {
        this.usuarios = usuarios;
        this.vinculos = vinculos;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listar(UsuarioAutenticado ator, @RequestParam(required = false) PerfilUsuario perfil) {
        return usuarios.listar(ator, perfil);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(UsuarioAutenticado ator, @Valid @RequestBody UsuarioRequest request) {
        return usuarios.criar(ator, request);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioResponse atualizar(UsuarioAutenticado ator, @PathVariable Long id,
                                     @Valid @RequestBody UsuarioAtualizacaoRequest request) {
        return usuarios.atualizar(ator, id, request);
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(UsuarioAutenticado ator, @PathVariable Long id) {
        usuarios.excluir(ator, id);
    }

    @GetMapping("/vinculos")
    public List<VinculoResponse> listarVinculos(UsuarioAutenticado ator) {
        return vinculos.listar(ator);
    }

    @PostMapping("/vinculos")
    @ResponseStatus(HttpStatus.CREATED)
    public VinculoResponse vincular(UsuarioAutenticado ator, @Valid @RequestBody VinculoRequest request) {
        return vinculos.vincular(ator, request);
    }

    @DeleteMapping("/vinculos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincular(UsuarioAutenticado ator, @RequestParam Long agronomoId, @RequestParam Long produtorId) {
        vinculos.desvincular(ator, agronomoId, produtorId);
    }
}
