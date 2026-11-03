package br.edu.tcc.agromotor.presentation.exception;

import br.edu.tcc.agromotor.application.exception.AcessoNegadoException;
import br.edu.tcc.agromotor.application.exception.ConflitoException;
import br.edu.tcc.agromotor.application.exception.CredenciaisInvalidasException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Traduz as exceções das camadas internas para o formato Problem Details (RFC 9457).
 * A propriedade "erros" lista as mensagens que a interface apresenta ao usuário.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final List<String> FORMATO_INVALIDO = List.of("Verifique o formato dos campos enviados");

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, List<String>> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparingInt((FieldError e) -> posicaoDoCampo(ex.getTarget(), e.getField())))
                .forEach(e -> campos.computeIfAbsent(e.getField(), c -> new ArrayList<>())
                        .add(e.getDefaultMessage() == null ? e.getField() + " inválido" : e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(comCampos(
                problema(HttpStatus.BAD_REQUEST, "Dados inválidos", campos.values().stream().flatMap(List::stream).toList()),
                campos));
    }

    /** Lista os erros na ordem em que os campos aparecem no DTO, que é a ordem da tela. */
    private static int posicaoDoCampo(Object alvo, String campo) {
        RecordComponent[] componentes = alvo == null ? null : alvo.getClass().getRecordComponents();
        if (componentes == null) {
            return Integer.MAX_VALUE;
        }
        return IntStream.range(0, componentes.length)
                .filter(i -> componentes[i].getName().equals(campo))
                .findFirst()
                .orElse(Integer.MAX_VALUE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        String campo = ex.getCause() instanceof JsonMappingException mapeamento
                ? mapeamento.getPath().stream().map(JsonMappingException.Reference::getFieldName)
                        .filter(Objects::nonNull).collect(Collectors.joining("."))
                : "";
        if (campo.isEmpty()) {
            return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Requisição malformada", FORMATO_INVALIDO));
        }
        List<String> erros = List.of("Campo " + campo + ": valor em formato inválido");
        return ResponseEntity.badRequest().body(comCampos(
                problema(HttpStatus.BAD_REQUEST, "Requisição malformada", erros), Map.of(campo, erros)));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatusCode status,
                                                                          WebRequest request) {
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Requisição malformada",
                List.of("Parâmetro obrigatório ausente: " + ex.getParameterName())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail parametroMalformado(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição malformada",
                List.of("Parâmetro " + ex.getName() + ": valor em formato inválido"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail argumentoInvalido(IllegalArgumentException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição malformada", FORMATO_INVALIDO);
    }

    @ExceptionHandler(RegraNegocioException.class)
    ProblemDetail regraNegocio(RegraNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.erros());
    }

    @ExceptionHandler(ParametroAusenteException.class)
    ProblemDetail parametroAusente(ParametroAusenteException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Parâmetros NEPAR não cadastrados", ex.pendencias());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ProblemDetail credenciais(CredenciaisInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    ProblemDetail acessoNegado(AcessoNegadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail conflito(ConflitoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", List.of(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflito",
                List.of("A operação viola a integridade dos dados (registro duplicado ou com vínculos)"));
    }

    /** "campos" associa cada erro ao campo enviado, para a interface destacá-lo no formulário. */
    private static ProblemDetail comCampos(ProblemDetail problema, Map<String, List<String>> campos) {
        problema.setProperty("campos", campos);
        return problema;
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, List<String> erros) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, String.join("; ", erros));
        problema.setTitle(titulo);
        problema.setProperty("erros", erros);
        return problema;
    }
}
