package com.backoffice.alerta.bootstrap;

import com.backoffice.alerta.notification.*;
import com.backoffice.alerta.project.domain.Project;
import com.backoffice.alerta.project.domain.ProjectBusinessRule;
import com.backoffice.alerta.project.domain.ProjectType;
import com.backoffice.alerta.project.domain.RepositoryType;
import com.backoffice.alerta.project.repository.ProjectBusinessRuleRepository;
import com.backoffice.alerta.project.repository.ProjectRepository;
import com.backoffice.alerta.rules.*;
import com.backoffice.alerta.repository.*;
import com.backoffice.alerta.sla.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Inicializador de dados DEMO para ambiente executivo
 * 
 * 🎯 US#32 - Seed de Dados & Ambiente Demo Executivo
 * 
 * ⚠️ Apenas createBusinessRules() habilitado para testes US#35
 * 
 * @author GitHub Copilot
 * @since 1.0.0
 */
@Component
@Profile({"demo", "dev"})
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final RiskDecisionAuditRepository auditRepository;
    private final BusinessRuleIncidentRepository incidentRepository;
    private final RiskDecisionFeedbackRepository feedbackRepository;
    private final RiskSlaTrackingRepository slaRepository;
    private final RiskNotificationRepository notificationRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final BusinessRuleDependencyRepository dependencyRepository;
    private final FileBusinessRuleMappingRepository fileMappingRepository;
    private final ProjectRepository projectRepository; // US#48
    private final ProjectBusinessRuleRepository projectBusinessRuleRepository; // US#49

    // UUIDs fixos para regras de negócio
    private static final UUID RULE_PAYMENT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID RULE_TAX_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    private static final UUID RULE_USER_REG_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");

    public DemoDataInitializer(
            RiskDecisionAuditRepository auditRepository,
            BusinessRuleIncidentRepository incidentRepository,
            RiskDecisionFeedbackRepository feedbackRepository,
            RiskSlaTrackingRepository slaRepository,
            RiskNotificationRepository notificationRepository,
            BusinessRuleRepository businessRuleRepository,
            BusinessRuleDependencyRepository dependencyRepository,
            FileBusinessRuleMappingRepository fileMappingRepository,
            ProjectRepository projectRepository, // US#48
            ProjectBusinessRuleRepository projectBusinessRuleRepository) { // US#49
        this.auditRepository = auditRepository;
        this.incidentRepository = incidentRepository;
        this.feedbackRepository = feedbackRepository;
        this.slaRepository = slaRepository;
        this.notificationRepository = notificationRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.dependencyRepository = dependencyRepository;
        this.fileMappingRepository = fileMappingRepository;
        this.projectRepository = projectRepository; // US#48
        this.projectBusinessRuleRepository = projectBusinessRuleRepository; // US#49
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("🌱 [DEMO] Verificando necessidade de seed de dados...");

        logger.info("🚀 [DEMO] Iniciando seed de dados demo executivo...");

        try {
            logger.info("� [DEMO] Criando projetos organizacionais...");
            createProjects(); // US#48
            
            logger.info("�📝 [DEMO] Criando regras de negócio...");
            createBusinessRules();            
            logger.info("🔗 [DEMO] Associando regras aos projetos...");
            createProjectBusinessRules(); // US#49            
            logger.info("📝 [DEMO] Criando dependências entre regras...");
            createBusinessRuleDependencies();
            
            logger.info("📝 [DEMO] Criando mapeamento de arquivos...");
            createFileMappings();
            
            logger.info("📝 [DEMO] Criando auditorias...");
            List<RiskDecisionAudit> audits = createAudits();
            
            logger.info("📝 [DEMO] Criando incidentes...");
            createIncidents(audits);
            
            logger.info("📝 [DEMO] Criando feedbacks...");
            createFeedbacks(audits);
            
            logger.info("📝 [DEMO] Criando SLAs...");
            createSlas(audits);
            
            logger.info("📝 [DEMO] Criando notificações...");
            createNotifications(audits);

            logger.info("✅ [DEMO] Seed concluído com sucesso!");
            logger.info("📊 [DEMO] {} regras de negócio criadas", businessRuleRepository.findAll().size());
            logger.info("📊 [DEMO] {} dependências criadas", dependencyRepository.findAll().size());
            logger.info("📊 [DEMO] {} mapeamentos de arquivos criados", fileMappingRepository.findAll().size());
            logger.info("📊 [DEMO] {} auditorias criadas", audits.size());
            logger.info("📊 [DEMO] {} incidentes criados", incidentRepository.count());
            logger.info("📊 [DEMO] {} feedbacks criados", feedbackRepository.count());
            logger.info("📊 [DEMO] {} SLAs criados", slaRepository.count());
            logger.info("📊 [DEMO] {} notificações criadas", notificationRepository.count());

        } catch (Exception e) {
            logger.error("❌ [DEMO] Erro durante seed de dados: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no seed de dados demo", e);
        }
    }

    private List<RiskDecisionAudit> createAudits() {
        List<RiskDecisionAudit> audits = new ArrayList<>();

        // Audit 1: CRITICO em PRODUCTION
        audits.add(auditRepository.save(new RiskDecisionAudit(
                "PR-2024-001",
                Environment.PRODUCTION,
                RiskLevel.CRITICO,
                95,
                FinalDecision.APROVADO,
                List.of("PAYMENT_VALIDATION"),
                Map.of(),
                List.of(),
                true,
                "AI reviewed: High complexity payment changes",
                "Policy snapshot v1.0"
        )));

        // Audit 2: MEDIO em STAGING
        audits.add(auditRepository.save(new RiskDecisionAudit(
                "PR-2024-002",
                Environment.STAGING,
                RiskLevel.MEDIO,
                55,
                FinalDecision.APROVADO,
                List.of("USER_REGISTRATION"),
                Map.of(),
                List.of(),
                false,
                null,
                "Policy snapshot v1.0"
        )));

        // Audit 3: ALTO em PRODUCTION (com incidente)
        audits.add(auditRepository.save(new RiskDecisionAudit(
                "PR-2024-003",
                Environment.PRODUCTION,
                RiskLevel.ALTO,
                75,
                FinalDecision.APROVADO,
                List.of("TAX_CALCULATION"),
                Map.of(),
                List.of("Requires manual rollback"),
                true,
                "AI detected tax calculation changes",
                "Policy snapshot v1.0"
        )));

        // Audit 4: BAIXO em DEV
        audits.add(auditRepository.save(new RiskDecisionAudit(
                "PR-2024-004",
                Environment.DEV,
                RiskLevel.BAIXO,
                25,
                FinalDecision.APROVADO,
                List.of("API_GATEWAY"),
                Map.of(),
                List.of(),
                false,
                null,
                "Policy snapshot v1.0"
        )));

        // Audit 5: CRITICO BLOQUEADO
        audits.add(auditRepository.save(new RiskDecisionAudit(
                "PR-2024-005",
                Environment.PRODUCTION,
                RiskLevel.CRITICO,
                98,
                FinalDecision.BLOQUEADO,
                List.of("REFUND_PROCESSING"),
                Map.of(),
                List.of("Critical security vulnerability"),
                true,
                "AI flagged security issue",
                "Policy snapshot v1.1"
        )));

        logger.info("✅ [DEMO] {} auditorias criadas", audits.size());
        return audits;
    }

    private void createIncidents(List<RiskDecisionAudit> audits) {
        Instant now = Instant.now();

        // Incidente para audit 3 (TAX_CALCULATION)
        incidentRepository.save(new BusinessRuleIncident(
                RULE_TAX_ID,
                "Tax Calculation Error",
                "Incorrect tax values for international transactions",
                IncidentSeverity.HIGH,
                now.minus(20, ChronoUnit.DAYS)
        ));

        logger.info("✅ [DEMO] 1 incidente criado");
    }

    private void createFeedbacks(List<RiskDecisionAudit> audits) {
        // Feedback para primeiras 4 auditorias
        for (int i = 0; i < 4; i++) {
            RiskDecisionAudit audit = audits.get(i);
            feedbackRepository.save(new RiskDecisionFeedback(
                    audit.getId(),
                    audit.getPullRequestId(),
                    audit.getFinalDecision(),
                    audit.getRiskLevel(),
                    i == 2 ? FeedbackOutcome.INCIDENT : FeedbackOutcome.SUCCESS,
                    i == 2 ? "Incident detected post-deploy" : "Deployed successfully",
                    "risk_manager"
            ));
        }

        logger.info("✅ [DEMO] 4 feedbacks criados");
    }

    private void createSlas(List<RiskDecisionAudit> audits) {
        RiskDecisionAudit audit1 = audits.get(0);
        UUID notifId = UUID.randomUUID();

        slaRepository.save(new RiskSlaTracking(
                notifId,
                audit1.getId(),
                audit1.getPullRequestId(),
                audit1.getRiskLevel(),
                EscalationLevel.SECONDARY,
                Instant.now().plus(24, ChronoUnit.HOURS),
                SlaStatus.PENDING,
                Instant.now()
        ));

        logger.info("✅ [DEMO] 1 SLA criado");
    }

    private void createNotifications(List<RiskDecisionAudit> audits) {
        RiskDecisionAudit audit1 = audits.get(0);

        notificationRepository.save(new RiskNotification(
                audit1.getId(),
                audit1.getPullRequestId(),
                RULE_PAYMENT_ID,
                "Platform Team",
                TeamType.ENGINEERING,
                OwnershipRole.PRIMARY_OWNER,
                NotificationTrigger.HIGH_RISK_PRODUCTION,
                NotificationSeverity.CRITICAL,
                NotificationChannel.SLACK,
                "CRITICAL risk decision for payment validation"
        ));

        logger.info("✅ [DEMO] 1 notificação criada");
    }

    private void createBusinessRules() {
        // BR-001: REGRA_CALCULO_HORAS_PJ (Critical Payment Rule)
        BusinessRule rule1 = new BusinessRule(
                RULE_PAYMENT_ID.toString(),
                "REGRA_CALCULO_HORAS_PJ",
                Domain.PAYMENT,
                "Regra crítica para cálculo de horas de trabalho de Pessoa Jurídica no sistema de pagamento",
                Criticality.CRITICA,
                "Platform Team"
        );
        businessRuleRepository.save(rule1);

        // BR-003: REGRA_CALCULO_TRIBUTOS (Critical Billing Rule)
        BusinessRule rule2 = new BusinessRule(
                RULE_TAX_ID.toString(),
                "REGRA_CALCULO_TRIBUTOS",
                Domain.BILLING,
                "Regra para cálculo automático de tributos e impostos sobre faturamento",
                Criticality.ALTA,
                "Finance Team"
        );
        businessRuleRepository.save(rule2);

        // BR-004: REGRA_VALIDACAO_CADASTRO_USUARIO (Medium User Rule)
        BusinessRule rule3 = new BusinessRule(
                RULE_USER_REG_ID.toString(),
                "REGRA_VALIDACAO_CADASTRO_USUARIO",
                Domain.USER,
                "Validação de dados obrigatórios no cadastro de novos usuários",
                Criticality.MEDIA,
                "User Experience Team"
        );
        businessRuleRepository.save(rule3);

        logger.info("✅ [DEMO] {} regras de negócio criadas", businessRuleRepository.findAll().size());
    }
    
    private void createBusinessRuleDependencies() {
        // Cenário 1: Cálculo PJ → Tributos → Validação Usuário
        // "Se mudarmos o PaymentService.java, afetamos o cálculo de impostos e o cadastro de usuários"
        
        // BR-001 (PAYMENT/CRITICA) FEEDS → BR-003 (BILLING/ALTA)
        BusinessRuleDependency dep1 = new BusinessRuleDependency(
                RULE_PAYMENT_ID.toString(),
                RULE_TAX_ID.toString(),
                BusinessRuleDependencyType.FEEDS,
                "O cálculo de horas PJ alimenta o cálculo de tributos sobre faturamento"
        );
        dependencyRepository.save(dep1);
        
        // BR-003 (BILLING/ALTA) AGGREGATES → BR-004 (USER/MEDIA)
        BusinessRuleDependency dep2 = new BusinessRuleDependency(
                RULE_TAX_ID.toString(),
                RULE_USER_REG_ID.toString(),
                BusinessRuleDependencyType.AGGREGATES,
                "Os tributos agregam dados de usuários para cálculo de impostos personalizados"
        );
        dependencyRepository.save(dep2);
        
        logger.info("✅ [DEMO] {} dependências criadas", dependencyRepository.findAll().size());
    }
    
    private void createFileMappings() {
        // Mapeamento realista: arquivos Java → regras
        
        // PaymentService.java implementa BR-001
        FileBusinessRuleMapping mapping1 = new FileBusinessRuleMapping(
                "src/main/java/com/app/payment/PaymentService.java",
                RULE_PAYMENT_ID.toString(),
                ImpactType.DIRECT
        );
        fileMappingRepository.save(mapping1);
        
        // TaxCalculator.java implementa BR-003
        FileBusinessRuleMapping mapping2 = new FileBusinessRuleMapping(
                "src/main/java/com/app/billing/TaxCalculator.java",
                RULE_TAX_ID.toString(),
                ImpactType.DIRECT
        );
        fileMappingRepository.save(mapping2);
        
        // UserValidator.java implementa BR-004
        FileBusinessRuleMapping mapping3 = new FileBusinessRuleMapping(
                "src/main/java/com/app/user/UserValidator.java",
                RULE_USER_REG_ID.toString(),
                ImpactType.DIRECT
        );
        fileMappingRepository.save(mapping3);
        
        logger.info("✅ [DEMO] {} mapeamentos criados", fileMappingRepository.findAll().size());
    }

    /**
     * US#48 - Cria projetos organizacionais de exemplo
     */
    private void createProjects() {
        // Projeto 1: Backoffice Pagamentos
        Project paymentProject = new Project(
                "Backoffice Pagamentos",
                ProjectType.BACKEND,
                RepositoryType.GITHUB,
                "https://github.com/company/payment-backoffice",
                "main"
        );
        paymentProject.setDescription("Sistema de processamento de pagamentos para clientes PJ e PF");
        projectRepository.save(paymentProject);

        // Projeto 2: Portal do Cliente
        Project portalProject = new Project(
                "Portal do Cliente",
                ProjectType.FRONTEND,
                RepositoryType.GITLAB,
                "https://gitlab.com/company/customer-portal",
                "develop"
        );
        portalProject.setDescription("Portal web para autoatendimento de clientes");
        projectRepository.save(portalProject);

        logger.info("✅ [DEMO] {} projetos criados", projectRepository.findAll().size());
    }

    /**
     * US#49 - Cria associações entre Projects e BusinessRules
     */
    private void createProjectBusinessRules() {
        // Buscar projetos criados
        List<Project> projects = projectRepository.findAll();
        if (projects.size() < 2) {
            logger.warn("⚠️ [DEMO] Projetos insuficientes para criar associações");
            return;
        }

        Project backendProject = projects.stream()
                .filter(p -> p.getName().equals("Backoffice Pagamentos"))
                .findFirst()
                .orElse(null);

        Project frontendProject = projects.stream()
                .filter(p -> p.getName().equals("Portal do Cliente"))
                .findFirst()
                .orElse(null);

        if (backendProject == null || frontendProject == null) {
            logger.warn("⚠️ [DEMO] Projetos não encontrados para associação");
            return;
        }

        // Associação 1: Backend → Regra de Pagamentos (REGRA_CALCULO_HORAS_PJ)
        ProjectBusinessRule assoc1 = new ProjectBusinessRule(
                backendProject.getId(),
                RULE_PAYMENT_ID.toString(),
                "demo-seed"
        );
        projectBusinessRuleRepository.save(assoc1);

        // Associação 2: Backend → Regra de Tributos (REGRA_CALCULO_TRIBUTOS)
        ProjectBusinessRule assoc2 = new ProjectBusinessRule(
                backendProject.getId(),
                RULE_TAX_ID.toString(),
                "demo-seed"
        );
        projectBusinessRuleRepository.save(assoc2);

        // Associação 3: Frontend → Regra de Validação de Usuário (REGRA_VALIDACAO_CADASTRO_USUARIO)
        ProjectBusinessRule assoc3 = new ProjectBusinessRule(
                frontendProject.getId(),
                RULE_USER_REG_ID.toString(),
                "demo-seed"
        );
        projectBusinessRuleRepository.save(assoc3);

        long totalAssociations = projectBusinessRuleRepository.count();
        logger.info("✅ [DEMO] {} ProjectBusinessRule associations created", totalAssociations);
        logger.info("   📦 Backoffice Pagamentos: 2 regras");
        logger.info("   📦 Portal do Cliente: 1 regra");
    }
}
