package br.edu.tcc.agromotor.application.usecase.parametro;

import br.edu.tcc.agromotor.application.dto.CulturaResponse;
import br.edu.tcc.agromotor.application.dto.ParametrosVigentesResponse;
import br.edu.tcc.agromotor.application.dto.SistemaInfoResponse;
import br.edu.tcc.agromotor.application.port.ModoDemonstracao;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.repository.CulturaRepository;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.util.ArrayList;
import java.util.List;

/** Mostra a versão vigente das tabelas: é ela que define o resultado das recomendações. */
@UseCase
public class ConsultarParametrosUseCase {

    private final ParametroRepository parametros;
    private final CulturaRepository culturas;
    private final ModoDemonstracao demonstracao;

    public ConsultarParametrosUseCase(ParametroRepository parametros, CulturaRepository culturas, ModoDemonstracao demonstracao) {
        this.parametros = parametros;
        this.culturas = culturas;
        this.demonstracao = demonstracao;
    }

    public SistemaInfoResponse informacoesDoSistema() {
        return new SistemaInfoResponse(parametros.versaoVigente().orElse(null), demonstracao.contas());
    }

    public List<CulturaResponse> listarCulturas() {
        return culturas.listar().stream().map(CulturaResponse::de).toList();
    }

    /** O manual enquadra o potássio pela porcentagem na CTC, não pelo teor do laudo. */
    private static String unidadeDaFaixa(AtributoSolo atributo) {
        return atributo == AtributoSolo.POTASSIO ? "% de K na CTC a pH 7,0" : atributo.unidade();
    }

    public ParametrosVigentesResponse vigentes() {
        List<Cultura> todas = culturas.listar();

        List<ParametrosVigentesResponse.Faixa> faixas = new ArrayList<>();
        for (AtributoSolo atributo : AtributoSolo.values()) {
            parametros.faixasInterpretacao(atributo).forEach(f -> faixas.add(new ParametrosVigentesResponse.Faixa(
                    atributo.name(), atributo.rotulo(), unidadeDaFaixa(atributo), f.criterio().min(), f.criterio().max(),
                    f.teor().min(), f.teor().max(), f.classe())));
        }

        List<ParametrosVigentesResponse.Npk> npk = new ArrayList<>();
        List<ParametrosVigentesResponse.Micro> micros = new ArrayList<>();
        for (Cultura cultura : todas) {
            parametros.regrasNpk(cultura.id()).forEach(r -> npk.add(new ParametrosVigentesResponse.Npk(
                    cultura.nome(), r.nutriente().rotulo(), r.classeFertilidade(),
                    r.grupoAntecessor() == null ? null : r.grupoAntecessor().rotulo(),
                    r.rendimento().min(), r.rendimento().max(),
                    r.inviavel() ? null : r.dose().min(), r.inviavel() ? null : r.dose().max())));
            for (Micronutriente nutriente : Micronutriente.values()) {
                parametros.regrasMicronutriente(cultura.id(), nutriente).forEach(m -> micros.add(
                        new ParametrosVigentesResponse.Micro(cultura.nome(), nutriente.rotulo(), m.classe(), m.doseKgHa())));
            }
        }

        List<ParametrosVigentesResponse.Calagem> calagem = new ArrayList<>();
        for (SistemaManejo manejo : SistemaManejo.values()) {
            parametros.regraCalagem(manejo).ifPresent(c ->
                    calagem.add(new ParametrosVigentesResponse.Calagem(manejo.rotulo(), c.observacao())));
        }

        List<ParametrosVigentesResponse.DoseGesso> dosesGesso = parametros.dosesGesso().stream()
                .map(d -> new ParametrosVigentesResponse.DoseGesso(d.argila().min(), d.argila().max(), d.doseKgHa()))
                .toList();

        return new ParametrosVigentesResponse(
                parametros.versaoVigente().orElse(null),
                todas.stream().map(CulturaResponse::de).toList(),
                faixas,
                npk,
                parametros.regraGessagem()
                        .map(g -> new ParametrosVigentesResponse.Gessagem(g.limiteMPorcentagem(), g.limiteCaCmolc(), dosesGesso))
                        .orElse(null),
                calagem,
                micros);
    }
}
