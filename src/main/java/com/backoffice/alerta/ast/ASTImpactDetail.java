package com.backoffice.alerta.ast;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US#69 - Detalhe de impacto identificado pela análise AST.
 * Modelo mutável para flexibilidade na construção de respostas.
 */
@Schema(description = "Detalhe de impacto a nível de AST (método/classe)")
public class ASTImpactDetail {

    @Schema(description = "Caminho do arquivo fonte")
    private String filePath;

    @Schema(description = "Nome completo da classe")
    private String className;

    @Schema(description = "Nome do método impactado")
    private String methodName;

    @Schema(description = "Linha de início do método")
    private int lineStart;

    @Schema(description = "Linha de fim do método")
    private int lineEnd;

    @Schema(description = "Razão do impacto (ex: implementa regra REGRA_001)")
    private String reason;

    public ASTImpactDetail() {
    }

    public ASTImpactDetail(String filePath, String className, String methodName, 
                           int lineStart, int lineEnd, String reason) {
        this.filePath = filePath;
        this.className = className;
        this.methodName = methodName;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.reason = reason;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLineStart() {
        return lineStart;
    }

    public void setLineStart(int lineStart) {
        this.lineStart = lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format("%s.%s() [%s:%d-%d] - %s", 
            className, methodName, filePath, lineStart, lineEnd, reason);
    }
}
