package com.backoffice.alerta.git.client;

import com.backoffice.alerta.git.PullRequestStatus;
import com.backoffice.alerta.git.dto.GitPullRequestData;
import com.backoffice.alerta.git.dto.GitPullRequestFile;
import com.backoffice.alerta.git.dto.GitPullRequestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * US#51 - Implementação dummy do Git Provider Client
 * 
 * ⚠️ Simulação realista para testes (não chama APIs externas)
 * 
 * Retorna dados fictícios mas representativos:
 * - PR com arquivos alterados realistas
 * - Metadados completos
 * - Sem chamadas de rede
 */
@Component
public class DummyGitProviderClient implements GitProviderClient {

    private static final Logger log = LoggerFactory.getLogger(DummyGitProviderClient.class);

    @Override
    public GitPullRequestData fetchPullRequest(GitPullRequestRequest request) {
        log.info("🔍 [DUMMY] Buscando PR #{} do repositório {} ({})", 
                request.getPullRequestNumber(), request.getRepositoryUrl(), request.getProvider());

        // Simular dados realistas de PR
        GitPullRequestData prData = new GitPullRequestData();
        prData.setPullRequestId("PR-2024-" + request.getPullRequestNumber());
        prData.setTitle("feat: Adicionar validação de CPF para pagamento PJ");
        prData.setAuthor("developer@company.com");
        prData.setSourceBranch("feature/cpf-validation-pj");
        prData.setTargetBranch("main");
        prData.setStatus(PullRequestStatus.OPEN);

        // Simular arquivos alterados (paths reais de demo)
        prData.setChangedFiles(Arrays.asList(
            new GitPullRequestFile("src/main/java/com/payment/PaymentService.java", "MODIFIED"),
            new GitPullRequestFile("src/main/java/com/payment/validation/CpfValidator.java", "ADDED"),
            new GitPullRequestFile("src/main/java/com/billing/InvoiceCalculator.java", "MODIFIED"),
            new GitPullRequestFile("src/test/java/com/payment/PaymentServiceTest.java", "MODIFIED")
        ));

        log.info("📄 [DUMMY] PR {} retornado com {} arquivo(s) alterado(s)", 
                prData.getPullRequestId(), prData.getChangedFiles().size());

        return prData;
    }
}
