package com.backoffice.alerta.rules;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementação V2 das regras de risco (regras evoluídas)
 * Diferenças em relação à V1:
 * - controller é semi-crítico (+15 pontos)
 * - billing=4 incidentes, payment=3, order=2
 * - ausência de teste = +25 pontos
 */
@Component
public class RiskRuleSetV2 implements RiskRuleSet {

    private static final Map<String, Integer> INCIDENT_HISTORY = new HashMap<>();

    static {
        INCIDENT_HISTORY.put("billing", 4);
        INCIDENT_HISTORY.put("payment", 3);
        INCIDENT_HISTORY.put("order", 2);
    }

    private static final String[] CRITICAL_KEYWORDS = {"billing", "payment", "pricing", "order"};
    private static final String[] SEMI_CRITICAL_KEYWORDS = {"controller"};

    @Override
    public String getVersion() {
        return "v2";
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
        if (filePath == null) {
            return false;
        }
        
        String lowerPath = filePath.toLowerCase();
        for (String keyword : SEMI_CRITICAL_KEYWORDS) {
            if (lowerPath.contains(keyword)) {
                return true;
            }
        }
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
        return 15;
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
        return 25; // V2 penaliza mais a ausência de testes
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
