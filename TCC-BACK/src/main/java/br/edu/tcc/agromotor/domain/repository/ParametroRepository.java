package br.edu.tcc.agromotor.domain.repository;

import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.VersaoParametros;
import br.edu.tcc.agromotor.domain.model.parametro.DoseGesso;
import br.edu.tcc.agromotor.domain.model.parametro.FaixaInterpretacao;
import br.edu.tcc.agromotor.domain.model.parametro.RegraCalagem;
import br.edu.tcc.agromotor.domain.model.parametro.RegraGessagem;
import br.edu.tcc.agromotor.domain.model.parametro.RegraMicronutriente;
import br.edu.tcc.agromotor.domain.model.parametro.RegraNpk;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de leitura das tabelas paramétricas NEPAR (RNF02). Sempre responde com a
 * versão vigente; o motor decide qual linha se aplica, sem conhecer o banco.
 */
public interface ParametroRepository {

    Optional<VersaoParametros> versaoVigente();

    List<FaixaInterpretacao> faixasInterpretacao(AtributoSolo atributo);

    List<RegraNpk> regrasNpk(Long culturaId);

    Optional<RegraGessagem> regraGessagem();

    List<DoseGesso> dosesGesso();

    Optional<RegraCalagem> regraCalagem(SistemaManejo manejo);

    List<RegraMicronutriente> regrasMicronutriente(Long culturaId, Micronutriente nutriente);
}
