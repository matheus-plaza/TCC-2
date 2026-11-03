package br.edu.tcc.agromotor.presentation.controller;

import br.edu.tcc.agromotor.application.dto.EvolucaoResponse;
import br.edu.tcc.agromotor.application.dto.ResumoPainelResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.usecase.dashboard.GerarDashboardEvolucaoUseCase;
import br.edu.tcc.agromotor.application.usecase.dashboard.GerarResumoPainelUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** UC10: painel de indicadores e evolução do solo. */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final GerarResumoPainelUseCase resumo;
    private final GerarDashboardEvolucaoUseCase evolucao;

    public DashboardController(GerarResumoPainelUseCase resumo, GerarDashboardEvolucaoUseCase evolucao) {
        this.resumo = resumo;
        this.evolucao = evolucao;
    }

    @GetMapping("/resumo")
    public ResumoPainelResponse resumo(UsuarioAutenticado ator) {
        return resumo.executar(ator);
    }

    @GetMapping("/evolucao")
    public EvolucaoResponse evolucao(UsuarioAutenticado ator, @RequestParam Long talhaoId) {
        return evolucao.executar(ator, talhaoId);
    }
}
