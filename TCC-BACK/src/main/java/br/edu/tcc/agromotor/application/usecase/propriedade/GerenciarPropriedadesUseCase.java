package br.edu.tcc.agromotor.application.usecase.propriedade;

import br.edu.tcc.agromotor.application.dto.PropriedadeRequest;
import br.edu.tcc.agromotor.application.dto.PropriedadeResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.ConflitoException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Propriedade;
import br.edu.tcc.agromotor.domain.model.Usuario;
import br.edu.tcc.agromotor.domain.repository.PropriedadeRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;
import br.edu.tcc.agromotor.domain.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** RF02: cadastro de propriedades rurais associadas a produtores. */
@UseCase
public class GerenciarPropriedadesUseCase {

    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final UsuarioRepository usuarios;
    private final ControleAcesso acesso;

    public GerenciarPropriedadesUseCase(PropriedadeRepository propriedades, TalhaoRepository talhoes,
                                        UsuarioRepository usuarios, ControleAcesso acesso) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.usuarios = usuarios;
        this.acesso = acesso;
    }

    public List<PropriedadeResponse> listar(UsuarioAutenticado ator) {
        EscopoProdutores escopo = acesso.escopo(ator);
        List<Propriedade> visiveis = propriedades.listar(escopo);
        Map<Long, List<ContextoTalhao>> talhoesPorPropriedade = talhoes.listarContextos(escopo, null).stream()
                .collect(Collectors.groupingBy(ContextoTalhao::propriedadeId));
        Map<Long, String> nomes = usuarios.listarPorIds(visiveis.stream().map(Propriedade::produtorId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Usuario::id, Usuario::nome, (a, b) -> a));
        return visiveis.stream()
                .map(p -> resposta(p, nomes.get(p.produtorId()), talhoesPorPropriedade.getOrDefault(p.id(), List.of())))
                .toList();
    }

    public PropriedadeResponse detalhar(UsuarioAutenticado ator, Long id) {
        Propriedade p = buscarVisivel(ator, id);
        return resposta(p, nomeDoProdutor(p.produtorId()), talhoes.listarContextos(acesso.escopo(ator), id));
    }

    public PropriedadeResponse criar(UsuarioAutenticado ator, PropriedadeRequest request) {
        Long produtorId = ator.perfil() == PerfilUsuario.PRODUTOR ? ator.id() : request.produtorId();
        if (produtorId == null) {
            throw new RegraNegocioException("Selecione o produtor dono da propriedade");
        }
        acesso.exigirAcessoAoProdutor(ator, produtorId);
        exigirProdutor(produtorId);
        Propriedade salva = propriedades.salvar(new Propriedade(null, produtorId, request.nome().trim(),
                request.municipio().trim(), Propriedade.UF_ATENDIDA));
        return resposta(salva, nomeDoProdutor(produtorId), List.of());
    }

    public PropriedadeResponse atualizar(UsuarioAutenticado ator, Long id, PropriedadeRequest request) {
        Propriedade atual = buscarVisivel(ator, id);
        Propriedade salva = propriedades.salvar(new Propriedade(id, atual.produtorId(), request.nome().trim(),
                request.municipio().trim(), atual.uf()));
        return resposta(salva, nomeDoProdutor(salva.produtorId()), talhoes.listarContextos(acesso.escopo(ator), id));
    }

    public void excluir(UsuarioAutenticado ator, Long id) {
        buscarVisivel(ator, id);
        if (talhoes.existeParaPropriedade(id)) {
            throw new ConflitoException("Exclua os talhões antes de excluir a propriedade");
        }
        propriedades.excluir(id);
    }

    private Propriedade buscarVisivel(UsuarioAutenticado ator, Long id) {
        Propriedade p = propriedades.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Propriedade", id));
        acesso.exigirAcessoAoProdutor(ator, p.produtorId());
        return p;
    }

    private void exigirProdutor(Long id) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produtor", id));
        if (usuario.perfil() != PerfilUsuario.PRODUTOR) {
            throw new RegraNegocioException("A propriedade deve pertencer a um usuário com perfil de produtor");
        }
    }

    private String nomeDoProdutor(Long id) {
        return usuarios.buscarPorId(id).map(Usuario::nome).orElse(null);
    }

    private static PropriedadeResponse resposta(Propriedade p, String produtorNome, List<ContextoTalhao> talhoes) {
        BigDecimal area = talhoes.stream().map(ContextoTalhao::areaHectares).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PropriedadeResponse(p.id(), p.produtorId(), produtorNome, p.nome(), p.municipio(), p.uf(), talhoes.size(), area);
    }
}
