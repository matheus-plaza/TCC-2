package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;

import java.util.List;
import java.util.Optional;

public interface LaudoRepository {

    LaudoSolo salvar(LaudoSolo laudo);

    Optional<LaudoSolo> buscarPorId(Long id);

    /** Laudos visíveis no escopo, do mais recente para o mais antigo. */
    List<LaudoSolo> listar(EscopoProdutores escopo, FiltroLaudos filtro);

    boolean existeParaTalhao(Long talhaoId);

    void excluir(Long id);
}
