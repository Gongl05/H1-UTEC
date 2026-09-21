package com.tuckersoft.branchengine.event;

import com.tuckersoft.branchengine.entity.Decision;
import com.tuckersoft.branchengine.entity.Playthrough;
import com.tuckersoft.branchengine.entity.RealityLog;
import com.tuckersoft.branchengine.repository.DecisionRepository;
import com.tuckersoft.branchengine.repository.RealityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class BranchNotificationListener {

    private final DecisionRepository decisionRepository;
    private final RealityLogRepository realityLogRepository;
    private final JavaMailSender mailSender;

    @Async("branchExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDecisionCommitted(DecisionCommittedEvent event) {
        Decision decision = decisionRepository.findById(event.getDecisionId()).orElse(null);
        if (decision == null) {
            log.error("Decision no encontrada en listener para id: {}", event.getDecisionId());
            return;
        }

        // 7. decision.status -> PROCESANDO
        decision.setStatus("PROCESANDO");
        decisionRepository.save(decision);

        Playthrough playthrough = decision.getPlaythrough();
        String recipientEmail = playthrough.getUser().getEmail();
        String playerTag = playthrough.getPlayerTag();
        String branchType = decision.getBranchType();
        String impactLevel = decision.getImpactLevel();
        String handlerUnit = decision.getHandlerUnit();
        String outcomeCode = decision.getOutcomeCode();
        String sourceNodeCode = decision.getNode().getNodeCode();
        String resolvedNodeCode = decision.getResolvedNodeCode() != null ? decision.getResolvedNodeCode() : "-";
        String playthroughStatus = playthrough.getStatus();
        Integer lucidity = playthrough.getLucidity();
        Integer controlLevel = playthrough.getControlLevel();
        String endingCode = playthrough.getEndingCode() != null ? playthrough.getEndingCode() : "-";

        String subject = String.format("[TUCKERSOFT] %s en %s | Impacto %s", branchType, playerTag, impactLevel);

        String body = String.format("""
                Hola %s,
                
                Una partida de prueba acaba de ramificarse.
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Decision ID      : #%d
                Jugador          : %s
                Rama             : %s
                Impacto          : %s
                Departamento     : %s
                Consecuencia     : %s
                Nodo origen      : %s
                Nodo destino     : %s
                Estado partida   : %s
                Lucidez          : %d/100
                Nivel de control : %d/100
                Final            : %s
                Registrada       : %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                Decisión original del jugador:
                "%s"
                
                — Tuckersoft Branch Engine, 1984
                """,
                playthrough.getUser().getDisplayName(),
                decision.getId(),
                playerTag,
                branchType,
                impactLevel,
                handlerUnit,
                outcomeCode,
                sourceNodeCode,
                resolvedNodeCode,
                playthroughStatus,
                lucidity,
                controlLevel,
                endingCode,
                decision.getCreatedAt(),
                decision.getRawInput()
        );

        String threadName = Thread.currentThread().getName();

        try {
            // QA Mode simulation check
            if ("MAIL_FAILURE".equals(event.getSimulateHeader())) {
                throw new RuntimeException("Simulación de fallo de correo activa (X-Bandersnatch-Simulate: MAIL_FAILURE)");
            }

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(recipientEmail);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);

            // Exito -> ESTABILIZADA + RealityLog SENT
            decision.setStatus("ESTABILIZADA");
            decision.setUpdatedAt(Instant.now());
            decisionRepository.save(decision);

            RealityLog logEntity = RealityLog.builder()
                    .decision(decision)
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .logStatus("SENT")
                    .errorMessage(null)
                    .sentAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();
            realityLogRepository.save(logEntity);

            log.info("[BRANCH-LOG] Decision ID: {} | Player: {} | Branch: {} | Impact: {} | Unit: {} | Node: {} -> {} | Thread: {} | Status: ESTABILIZADA",
                    decision.getId(), playerTag, branchType, impactLevel, handlerUnit, sourceNodeCode, resolvedNodeCode, threadName);

        } catch (Throwable ex) {
            // Fallo -> ERROR + RealityLog FAILED + log.error()
            decision.setStatus("ERROR");
            decision.setUpdatedAt(Instant.now());
            decisionRepository.save(decision);

            RealityLog logEntity = RealityLog.builder()
                    .decision(decision)
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .logStatus("FAILED")
                    .errorMessage(ex.getMessage())
                    .sentAt(null)
                    .createdAt(Instant.now())
                    .build();
            realityLogRepository.save(logEntity);

            log.error("[BRANCH-LOG] Decision ID: {} | Player: {} | Branch: {} | Impact: {} | Unit: {} | Node: {} -> {} | Thread: {} | Status: ERROR | Error: {}",
                    decision.getId(), playerTag, branchType, impactLevel, handlerUnit, sourceNodeCode, resolvedNodeCode, threadName, ex.getMessage());
        }
    }
}
