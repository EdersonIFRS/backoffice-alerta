package com.backoffice.alerta.ast;

import java.util.Objects;

/**
 * US#69 - Representa uma aresta no grafo de chamadas (call graph).
 * Modelo imutável para relacionamento caller -> callee.
 */
public class ASTCallGraphEdge {

    private final ASTMethodNode caller;
    private final ASTMethodNode callee;

    public ASTCallGraphEdge(ASTMethodNode caller, ASTMethodNode callee) {
        this.caller = caller;
        this.callee = callee;
    }

    public ASTMethodNode getCaller() {
        return caller;
    }

    public ASTMethodNode getCallee() {
        return callee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASTCallGraphEdge that = (ASTCallGraphEdge) o;
        return Objects.equals(caller, that.caller) &&
               Objects.equals(callee, that.callee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller, callee);
    }

    @Override
    public String toString() {
        return caller + " -> " + callee;
    }
}
