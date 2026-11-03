package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.CulturaResponse;
import br.edu.tcc.agromotor.application.dto.ParametrosVigentesResponse;
import br.edu.tcc.agromotor.application.dto.SistemaInfoResponse;
import br.edu.tcc.agromotor.application.usecase.parametro.ConsultarParametrosUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consulta das tabelas paramétricas em uso (RNF02 e RNF06). */
@RestController
@RequestMapping("/api/v1")
public class ParametroController {

    private final ConsultarParametrosUseCase parametros;

    public ParametroController(ConsultarParametrosUseCase parametros) {
        this.parametros = parametros;
    }

    @GetMapping("/sistema/info")
    public SistemaInfoResponse informacoes() {
        return parametros.informacoesDoSistema();
    }

    @GetMapping("/culturas")
    public List<CulturaResponse> culturas() {
        return parametros.listarCulturas();
    }

    @GetMapping("/parametros/vigentes")
    public ParametrosVigentesResponse vigentes() {
        return parametros.vigentes();
    }
}
