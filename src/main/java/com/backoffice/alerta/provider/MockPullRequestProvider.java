package com.backoffice.alerta.provider;

import com.backoffice.alerta.provider.dto.PullRequestData;
import com.backoffice.alerta.provider.dto.PullRequestFileData;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação mockada de PullRequestProvider.
 * Retorna dados simulados realistas para permitir testes sem dependência externa.
 * Futuramente pode ser substituída por GitHubPullRequestProvider, GitLabPullRequestProvider, etc.
 */
@Component
public class MockPullRequestProvider implements PullRequestProvider {

    private static final Map<String, PullRequestData> MOCK_DATABASE = new HashMap<>();

    static {
        // Cenário 1: PR de baixo risco
        MOCK_DATABASE.put("PR-001", new PullRequestData(
            "PR-001",
            "backoffice/platform",
            "dev.team",
            Arrays.asList(
                new PullRequestFileData("src/main/java/com/app/util/StringHelper.java", 15, 5, true),
                new PullRequestFileData("src/test/java/com/app/util/StringHelperTest.java", 20, 0, null)
            )
        ));

        // Cenário 2: PR crítico sem testes
        MOCK_DATABASE.put("PR-002", new PullRequestData(
            "PR-002",
            "backoffice/billing",
            "dev.team",
            Arrays.asList(
                new PullRequestFileData("src/main/java/com/app/billing/PaymentService.java", 85, 35, false),
                new PullRequestFileData("src/main/java/com/app/billing/InvoiceGenerator.java", 45, 10, false)
            )
        ));

        // Cenário 3: PR com múltiplos módulos críticos
        MOCK_DATABASE.put("PR-003", new PullRequestData(
            "PR-003",
            "backoffice/core",
            "senior.dev",
            Arrays.asList(
                new PullRequestFileData("src/main/java/com/app/payment/PaymentGateway.java", 60, 20, true),
                new PullRequestFileData("src/main/java/com/app/order/OrderProcessor.java", 70, 30, true),
                new PullRequestFileData("src/main/java/com/app/billing/BillingController.java", 40, 15, false)
            )
        ));

        // Cenário 4: PR com arquivo gigante
        MOCK_DATABASE.put("PR-004", new PullRequestData(
            "PR-004",
            "backoffice/monolith",
            "new.dev",
            Arrays.asList(
                new PullRequestFileData("src/main/java/com/app/pricing/PriceCalculator.java", 180, 95, null)
            )
        ));

        // Cenário padrão para testes gerais
        MOCK_DATABASE.put("PR-12345", new PullRequestData(
            "PR-12345",
            "backoffice/api",
            "test.user",
            Arrays.asList(
                new PullRequestFileData("src/main/java/com/app/billing/PaymentService.java", 90, 30, null)
            )
        ));
    }

    @Override
    public PullRequestData fetch(String pullRequestId) {
        PullRequestData data = MOCK_DATABASE.get(pullRequestId);
        
        if (data == null) {
            // Retorna PR genérico para IDs não mapeados
            return new PullRequestData(
                pullRequestId,
                "backoffice/unknown",
                "unknown.user",
                Arrays.asList(
                    new PullRequestFileData("src/main/java/com/app/GenericService.java", 50, 20, null)
                )
            );
        }
        
        return data;
    }
}
