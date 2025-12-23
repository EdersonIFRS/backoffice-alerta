package com.backoffice.alerta.controller;

import com.backoffice.alerta.dto.TestSuggestionRequest;
import com.backoffice.alerta.dto.TestSuggestionResponse;
import com.backoffice.alerta.service.TestSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para sugestão automática de testes com base em análise de risco e heurísticas IA.
 */
@RestController
@RequestMapping("/risk")
@Tag(name = "Sugestão de Testes", description = "Sugestões automáticas de testes baseadas em IA e análise de risco")
public class TestSuggestionController {

    private final TestSuggestionService testSuggestionService;

    public TestSuggestionController(TestSuggestionService testSuggestionService) {
        this.testSuggestionService = testSuggestionService;
    }

    @PostMapping("/suggest-tests")
    @Operation(
        summary = "Sugere testes automatizados baseados em risco",
        description = "Analisa alterações de Pull Request e sugere testes unitários, de integração ou mocks " +
                     "usando heurísticas de IA. Identifica arquivos críticos sem cobertura, calcula impacto estimado " +
                     "de cada recomendação e ajusta sugestões conforme nível de cobertura desejado (BAIXA/MÉDIA/ALTA). " +
                     "Tipos de teste: UNIT (testes unitários), INTEGRATION (testes de integração), MOCK (testes com mocks)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Sugestões de testes geradas com sucesso"
    )
    public ResponseEntity<TestSuggestionResponse> suggestTests(@RequestBody TestSuggestionRequest request) {
        TestSuggestionResponse response = testSuggestionService.suggestTests(request);
        return ResponseEntity.ok(response);
    }
}
