package com.backoffice.alerta.simulation;

import com.backoffice.alerta.rules.ChangeType;
import com.backoffice.alerta.rules.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Cenário "E se?" para simulação de decisão de risco
 * 
 * US#33 - Simulação Executiva de Decisão de Risco
 */
public class WhatIfScenario {
    
    private Environment overrideEnvironment;
    private ChangeType overrideChangeType;
    private List<String> excludeFiles = new ArrayList<>();

    public Environment getOverrideEnvironment() {
        return overrideEnvironment;
    }

    public void setOverrideEnvironment(Environment overrideEnvironment) {
        this.overrideEnvironment = overrideEnvironment;
    }

    public ChangeType getOverrideChangeType() {
        return overrideChangeType;
    }

    public void setOverrideChangeType(ChangeType overrideChangeType) {
        this.overrideChangeType = overrideChangeType;
    }

    public List<String> getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(List<String> excludeFiles) {
        this.excludeFiles = excludeFiles != null ? excludeFiles : new ArrayList<>();
    }
}
