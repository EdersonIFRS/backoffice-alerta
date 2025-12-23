package com.backoffice.alerta.ast;

import java.util.List;
import java.util.Objects;

/**
 * US#69 - Representa um nó de método na árvore AST.
 * Modelo imutável para identificação única de métodos Java.
 */
public class ASTMethodNode {

    private final String className;
    private final String methodName;
    private final List<String> parameterTypes;
    private final String filePath;
    private final int lineStart;
    private final int lineEnd;

    public ASTMethodNode(String className, String methodName, List<String> parameterTypes, 
                         String filePath, int lineStart, int lineEnd) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.filePath = filePath;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineStart() {
        return lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASTMethodNode that = (ASTMethodNode) o;
        return Objects.equals(className, that.className) &&
               Objects.equals(methodName, that.methodName) &&
               Objects.equals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, parameterTypes);
    }

    @Override
    public String toString() {
        return className + "." + methodName + "(" + String.join(", ", parameterTypes) + ")";
    }
}
