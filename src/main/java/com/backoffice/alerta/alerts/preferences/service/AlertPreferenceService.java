package com.backoffice.alerta.alerts.preferences.service;

import com.backoffice.alerta.alerts.AlertSeverity;
import com.backoffice.alerta.alerts.AlertType;
import com.backoffice.alerta.alerts.notification.NotificationChannel;
import com.backoffice.alerta.alerts.preferences.AlertDeliveryWindow;
import com.backoffice.alerta.alerts.preferences.domain.BusinessRuleAlertPreference;
import com.backoffice.alerta.alerts.preferences.domain.ProjectAlertPreference;
import com.backoffice.alerta.alerts.preferences.dto.AlertPreferenceRequest;
import com.backoffice.alerta.alerts.preferences.dto.AlertPreferenceResponse;
import com.backoffice.alerta.alerts.preferences.dto.EffectiveAlertPreferenceResponse;
import com.backoffice.alerta.alerts.preferences.repository.BusinessRuleAlertPreferenceRepository;
import com.backoffice.alerta.alerts.preferences.repository.ProjectAlertPreferenceRepository;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.dto.ProjectContext;
import com.backoffice.alerta.project.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço para gerenciamento de preferências de alertas
 * 
 * US#57 - Preferências de Alertas por Projeto e Regra de Negócio
 * 
 * HIERARQUIA: Regra > Projeto > Default
 */
@Service
public class AlertPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(AlertPreferenceService.class);

    // Defaults do sistema
    private static final AlertSeverity DEFAULT_MINIMUM_SEVERITY = AlertSeverity.INFO;
    private static final AlertDeliveryWindow DEFAULT_DELIVERY_WINDOW = AlertDeliveryWindow.ANY_TIME;
    private static final Set<NotificationChannel> DEFAULT_CHANNELS = Set.of(NotificationChannel.SLACK, NotificationChannel.TEAMS);

    private final ProjectAlertPreferenceRepository projectPreferenceRepository;
    private final BusinessRuleAlertPreferenceRepository rulePreferenceRepository;
    private final ProjectRepository projectRepository;

    public AlertPreferenceService(
            ProjectAlertPreferenceRepository projectPreferenceRepository,
            BusinessRuleAlertPreferenceRepository rulePreferenceRepository,
            ProjectRepository projectRepository) {
        this.projectPreferenceRepository = projectPreferenceRepository;
        this.rulePreferenceRepository = rulePreferenceRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Cria ou atualiza preferência de alerta para um projeto
     */
    @Transactional
    public AlertPreferenceResponse createOrUpdateProjectPreference(UUID projectId, AlertPreferenceRequest request) {
        log.info("⚙️ Criando/atualizando preferência de alerta - projectId: {}", projectId);

        ProjectAlertPreference preference = projectPreferenceRepository
                .findByProjectId(projectId)
                .orElse(new ProjectAlertPreference(projectId));

        updatePreferenceFromRequest(preference, request);
        
        ProjectAlertPreference saved = projectPreferenceRepository.save(preference);
        
        log.info("✅ Preferência salva - id: {}, projectId: {}", saved.getId(), projectId);
        
        return toResponse(saved);
    }

    /**
     * Busca preferência de alerta de um projeto
     */
    @Transactional(readOnly = true)
    public Optional<AlertPreferenceResponse> getProjectPreference(UUID projectId) {
        return projectPreferenceRepository.findByProjectId(projectId)
                .map(this::toResponse);
    }

    /**
     * Cria ou atualiza preferência de alerta para uma regra de negócio
     */
    @Transactional
    public AlertPreferenceResponse createOrUpdateRulePreference(String businessRuleId, AlertPreferenceRequest request) {
        log.info("⚙️ Criando/atualizando preferência de alerta - businessRuleId: {}", businessRuleId);

        BusinessRuleAlertPreference preference = rulePreferenceRepository
                .findByBusinessRuleId(businessRuleId)
                .orElse(new BusinessRuleAlertPreference(businessRuleId));

        updatePreferenceFromRequest(preference, request);
        
        BusinessRuleAlertPreference saved = rulePreferenceRepository.save(preference);
        
        log.info("✅ Preferência salva - id: {}, businessRuleId: {}", saved.getId(), businessRuleId);
        
        return toResponse(saved);
    }

    /**
     * Busca preferência de alerta de uma regra de negócio
     */
    @Transactional(readOnly = true)
    public Optional<AlertPreferenceResponse> getRulePreference(String businessRuleId) {
        return rulePreferenceRepository.findByBusinessRuleId(businessRuleId)
                .map(this::toResponse);
    }

    /**
     * Resolve a preferência efetiva aplicando hierarquia:
     * Regra > Projeto > Default
     * 
     * Esta é a assinatura OBRIGATÓRIA da US#57
     */
    @Transactional(readOnly = true)
    public EffectiveAlertPreferenceResponse resolveEffectivePreference(UUID projectId, String businessRuleId) {
        log.debug("🔍 Resolvendo preferência efetiva - projectId: {}, businessRuleId: {}", projectId, businessRuleId);

        EffectiveAlertPreferenceResponse response = new EffectiveAlertPreferenceResponse();

        // Contexto
        response.setProjectId(projectId);
        response.setBusinessRuleId(businessRuleId);

        // Enriquecer com contexto de projeto
        if (projectId != null) {
            projectRepository.findById(projectId).ifPresent(project -> {
                response.setProjectName(project.getName());
                response.setProjectContext(toProjectContext(project));
            });
        }

        // 1. PRIORIDADE MÁXIMA: Preferência da Regra
        if (businessRuleId != null) {
            Optional<BusinessRuleAlertPreference> rulePref = rulePreferenceRepository.findByBusinessRuleId(businessRuleId);
            if (rulePref.isPresent()) {
                log.debug("✅ Preferência encontrada em RULE - businessRuleId: {}", businessRuleId);
                applyRulePreference(response, rulePref.get());
                response.setSource(EffectiveAlertPreferenceResponse.PreferenceSource.RULE);
                return response;
            }
        }

        // 2. PRIORIDADE MÉDIA: Preferência do Projeto
        if (projectId != null) {
            Optional<ProjectAlertPreference> projectPref = projectPreferenceRepository.findByProjectId(projectId);
            if (projectPref.isPresent()) {
                log.debug("✅ Preferência encontrada em PROJECT - projectId: {}", projectId);
                applyProjectPreference(response, projectPref.get());
                response.setSource(EffectiveAlertPreferenceResponse.PreferenceSource.PROJECT);
                return response;
            }
        }

        // 3. FALLBACK: Defaults do Sistema
        log.debug("ℹ️ Usando preferências DEFAULT do sistema");
        applyDefaultPreference(response);
        response.setSource(EffectiveAlertPreferenceResponse.PreferenceSource.DEFAULT);
        
        return response;
    }

    /**
     * Verifica se um alerta deve ser enviado baseado nas preferências
     * 
     * @return true se alerta deve ser enviado, false se deve ser suprimido
     */
    public boolean shouldSendAlert(
            UUID projectId,
            String businessRuleId,
            AlertType alertType,
            AlertSeverity alertSeverity,
            NotificationChannel channel) {

        EffectiveAlertPreferenceResponse pref = resolveEffectivePreference(projectId, businessRuleId);

        // 1. Verificar severidade mínima
        if (alertSeverity.ordinal() < pref.getMinimumSeverity().ordinal()) {
            log.debug("🚫 Alerta bloqueado por severidade - alert: {}, minimum: {}", 
                      alertSeverity, pref.getMinimumSeverity());
            return false;
        }

        // 2. Verificar tipo de alerta permitido
        if (pref.getAllowedAlertTypes() != null && !pref.getAllowedAlertTypes().isEmpty()) {
            if (!pref.getAllowedAlertTypes().contains(alertType)) {
                log.debug("🚫 Alerta bloqueado por tipo - alert: {}, allowed: {}", 
                          alertType, pref.getAllowedAlertTypes());
                return false;
            }
        }

        // 3. Verificar canal habilitado
        if (!pref.getChannels().contains(channel)) {
            log.debug("🚫 Alerta bloqueado por canal - channel: {}, enabled: {}", 
                      channel, pref.getChannels());
            return false;
        }

        // TODO: Implementar verificação de delivery window (BUSINESS_HOURS vs ANY_TIME)
        // Por ora, aceita ANY_TIME sempre

        return true;
    }

    // ============ MÉTODOS AUXILIARES ============

    private void updatePreferenceFromRequest(ProjectAlertPreference preference, AlertPreferenceRequest request) {
        preference.setMinimumSeverity(request.getMinimumSeverity());
        preference.setAllowedAlertTypes(request.getAllowedAlertTypes());
        preference.setChannels(request.getChannels());
        preference.setDeliveryWindow(request.getDeliveryWindow());
    }

    private void updatePreferenceFromRequest(BusinessRuleAlertPreference preference, AlertPreferenceRequest request) {
        preference.setMinimumSeverity(request.getMinimumSeverity());
        preference.setAllowedAlertTypes(request.getAllowedAlertTypes());
        preference.setChannels(request.getChannels());
        preference.setDeliveryWindow(request.getDeliveryWindow());
    }

    private void applyRulePreference(EffectiveAlertPreferenceResponse response, BusinessRuleAlertPreference pref) {
        response.setMinimumSeverity(pref.getMinimumSeverity() != null ? pref.getMinimumSeverity() : DEFAULT_MINIMUM_SEVERITY);
        response.setAllowedAlertTypes(pref.getAllowedAlertTypes());
        response.setChannels(pref.getChannels() != null && !pref.getChannels().isEmpty() ? pref.getChannels() : DEFAULT_CHANNELS);
        response.setDeliveryWindow(pref.getDeliveryWindow() != null ? pref.getDeliveryWindow() : DEFAULT_DELIVERY_WINDOW);
    }

    private void applyProjectPreference(EffectiveAlertPreferenceResponse response, ProjectAlertPreference pref) {
        response.setMinimumSeverity(pref.getMinimumSeverity() != null ? pref.getMinimumSeverity() : DEFAULT_MINIMUM_SEVERITY);
        response.setAllowedAlertTypes(pref.getAllowedAlertTypes());
        response.setChannels(pref.getChannels() != null && !pref.getChannels().isEmpty() ? pref.getChannels() : DEFAULT_CHANNELS);
        response.setDeliveryWindow(pref.getDeliveryWindow() != null ? pref.getDeliveryWindow() : DEFAULT_DELIVERY_WINDOW);
    }

    private void applyDefaultPreference(EffectiveAlertPreferenceResponse response) {
        response.setMinimumSeverity(DEFAULT_MINIMUM_SEVERITY);
        response.setAllowedAlertTypes(Set.of()); // Vazio = todos permitidos
        response.setChannels(DEFAULT_CHANNELS);
        response.setDeliveryWindow(DEFAULT_DELIVERY_WINDOW);
    }

    private AlertPreferenceResponse toResponse(ProjectAlertPreference pref) {
        AlertPreferenceResponse response = new AlertPreferenceResponse();
        response.setId(pref.getId());
        response.setProjectId(pref.getProjectId());
        response.setMinimumSeverity(pref.getMinimumSeverity());
        response.setAllowedAlertTypes(pref.getAllowedAlertTypes());
        response.setChannels(pref.getChannels());
        response.setDeliveryWindow(pref.getDeliveryWindow());
        response.setCreatedAt(pref.getCreatedAt());
        response.setUpdatedAt(pref.getUpdatedAt());
        return response;
    }

    private AlertPreferenceResponse toResponse(BusinessRuleAlertPreference pref) {
        AlertPreferenceResponse response = new AlertPreferenceResponse();
        response.setId(pref.getId());
        response.setBusinessRuleId(pref.getBusinessRuleId());
        response.setMinimumSeverity(pref.getMinimumSeverity());
        response.setAllowedAlertTypes(pref.getAllowedAlertTypes());
        response.setChannels(pref.getChannels());
        response.setDeliveryWindow(pref.getDeliveryWindow());
        response.setCreatedAt(pref.getCreatedAt());
        response.setUpdatedAt(pref.getUpdatedAt());
        return response;
    }

    private ProjectContext toProjectContext(Project project) {
        ProjectContext context = new ProjectContext();
        context.setProjectId(project.getId());
        context.setProjectName(project.getName());
        // Project não tem businessArea e riskProfile - US#48 simplificado
        return context;
    }
}
