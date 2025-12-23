package com.backoffice.alerta.adapter;

import com.backoffice.alerta.dto.PullRequestRequest;
import com.backoffice.alerta.provider.dto.PullRequestData;
import com.backoffice.alerta.provider.dto.PullRequestFileData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter que converte dados externos de Pull Request (formato de APIs externas)
 * para o modelo interno do sistema (FileChange).
 * 
 * Esta é a ÚNICA camada que conhece ambos os formatos.
 * O core do sistema NÃO conhece PullRequestData.
 */
@Component
public class PullRequestAdapter {

    /**
     * Converte dados externos para modelo interno.
     * 
     * @param externalData Dados do Pull Request vindos de provider externo
     * @return Lista de FileChange no formato interno do sistema
     */
    public List<PullRequestRequest.FileChange> convertToInternalModel(PullRequestData externalData) {
        if (externalData == null || externalData.getFiles() == null) {
            throw new IllegalArgumentException("PullRequestData ou files não podem ser null");
        }

        return externalData.getFiles().stream()
            .map(this::convertFile)
            .collect(Collectors.toList());
    }

    private PullRequestRequest.FileChange convertFile(PullRequestFileData externalFile) {
        // Regra de conversão: linesChanged = additions + deletions
        int linesChanged = (externalFile.getAdditions() != null ? externalFile.getAdditions() : 0)
                         + (externalFile.getDeletions() != null ? externalFile.getDeletions() : 0);

        // hasTest pode ser null - será tratado defensivamente pelos serviços
        return new PullRequestRequest.FileChange(
            externalFile.getFilePath(),
            linesChanged,
            externalFile.getHasTest()
        );
    }
}
