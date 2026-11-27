package br.edu.tcc.fertsolo.application.usecase;

import br.edu.tcc.fertsolo.application.dto.UsuarioAutenticado;
import br.edu.tcc.fertsolo.application.exception.AcessoNegadoException;
import br.edu.tcc.fertsolo.domain.model.EscopoProdutores;
import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import br.edu.tcc.fertsolo.domain.repository.UsuarioRepository;

import java.util.Arrays;
import java.util.Set;

/**
 * Segregação de acesso (RNF05): o agrônomo vê os produtores vinculados a ele e o produtor
 * vê apenas o que é seu. O administrador gerencia contas e vínculos, sem acesso aos dados
 * agronômicos (propriedades, talhões, laudos e recomendações).
 */
@UseCase
public class ControleAcesso {

    private final UsuarioRepository usuarios;

    public ControleAcesso(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public EscopoProdutores escopo(UsuarioAutenticado ator) {
        return switch (ator.perfil()) {
            case ADMIN -> EscopoProdutores.de(Set.of());
            case AGRONOMO -> EscopoProdutores.de(usuarios.produtoresDoAgronomo(ator.id()));
            case PRODUTOR -> EscopoProdutores.de(Set.of(ator.id()));
        };
    }

    public void exigirAcessoAoProdutor(UsuarioAutenticado ator, Long produtorId) {
        if (!escopo(ator).permite(produtorId)) {
            throw new AcessoNegadoException("Você não tem acesso aos dados deste produtor");
        }
    }

    public void exigirPerfil(UsuarioAutenticado ator, PerfilUsuario... perfis) {
        if (Arrays.stream(perfis).noneMatch(p -> p == ator.perfil())) {
            throw new AcessoNegadoException("Seu perfil não permite esta operação");
        }
    }
}
