package com.backoffice.alerta.rules;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementação V1 das regras de risco (regras originais)
 */
@Component
public class RiskRuleSetV1 implements RiskRuleSet {

    private static final Map<String, Integer> INCIDENT_HISTORY = new HashMap<>();

    static {
        INCIDENT_HISTORY.put("billing", 3);
        INCIDENT_HISTORY.put("payment", 2);
        INCIDENT_HISTORY.put("order", 1);
    }

    private static final String[] CRITICAL_KEYWORDS = {"billing", "payment", "pricing", "order"};

    @Override
    public String getVersion() {
        return "v1";
    }

    @Override
    public boolean isCriticalFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        String lowerPath = filePath.toLowerCase();
        for (String keyword : CRITICAL_KEYWORDS) {
            if (lowerPath.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSemiCriticalFile(String filePath) {
        // V1 não tem conceito de semi-crítico
        return false;
    }

    @Override
    public int getIncidentHistory(String filePath) {
        if (filePath == null) {
            return 0;
        }

        String lowerPath = filePath.toLowerCase();
        for (Map.Entry<String, Integer> entry : INCIDENT_HISTORY.entrySet()) {
            if (lowerPath.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0;
    }

    @Override
    public int getCriticalFileScore() {
        return 30;
    }

    @Override
    public int getSemiCriticalFileScore() {
        return 0; // V1 não usa
    }

    @Override
    public int getLinesOver100Score() {
        return 20;
    }

    @Override
    public int getLines50To100Score() {
        return 10;
    }

    @Override
    public int getNoTestScore() {
        return 20;
    }

    @Override
    public int getIncidentScore() {
        return 5;
    }

    @Override
    public int getMaxIncidentScore() {
        return 20;
    }

    @Override
    public int getMaxScore() {
        return 100;
    }
}
