package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.*;
import com.tuckersoft.branchengine.entity.*;
import com.tuckersoft.branchengine.event.DecisionCommittedEvent;
import com.tuckersoft.branchengine.exception.ForbiddenException;
import com.tuckersoft.branchengine.exception.ResourceConflictException;
import com.tuckersoft.branchengine.exception.ResourceNotFoundException;
import com.tuckersoft.branchengine.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final PlaythroughRepository playthroughRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final UserRepository userRepository;
    private final RealityLogRepository realityLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DecisionResponse createDecision(DecisionRequest request, String userEmail, String simulateHeader) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Playthrough playthrough = playthroughRepository.findById(request.playthroughId())
                .orElseThrow(() -> new ResourceNotFoundException("Partida no encontrada con id: " + request.playthroughId()));

        // Validar propiedad de la partida (el admin NO puede decidir sobre partidas ajenas)
        if (!playthrough.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("No tienes permiso para decidir sobre una partida ajena");
        }

        // Validar que la partida esté ACTIVA
        if (!"ACTIVA".equals(playthrough.getStatus())) {
            throw new ResourceConflictException("La partida ya se encuentra " + playthrough.getStatus());
        }

        StoryNode sourceNode = playthrough.getCurrentNode();
        String branchType = DecisionClassifier.classify(request.rawInput());
        String handlerUnit = DecisionClassifier.deriveHandlerUnit(branchType);
        String outcomeCode = DecisionClassifier.deriveOutcomeCode(branchType);

        Instant now = Instant.now();

        // 4. Si es ENTRADA_CORRUPTA
        if ("ENTRADA_CORRUPTA".equals(branchType)) {
            Decision decision = Decision.builder()
                    .playthrough(playthrough)
                    .node(sourceNode)
                    .rawInput(request.rawInput())
                    .branchType(branchType)
                    .impactLevel(request.impactLevel())
                    .handlerUnit(handlerUnit)
                    .outcomeCode(outcomeCode)
                    .resolvedNodeCode(null)
                    .status("ERROR")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            decisionRepository.save(decision);

            // No se modifica la partida ni se publica el evento
            return mapToResponse(decision, playthrough);
        }

        // 5. Aplicar stats según impacto
        int lucidityDelta = 0;
        int controlDelta = 0;
        switch (request.impactLevel()) {
            case "LEVE" -> { lucidityDelta = -5; controlDelta = 5; }
            case "MODERADO" -> { lucidityDelta = -15; controlDelta = 10; }
            case "GRAVE" -> { lucidityDelta = -30; controlDelta = 20; }
            case "CRITICO" -> { lucidityDelta = -40; controlDelta = 45; }
        }

        int newLucidity = Math.max(0, Math.min(100, playthrough.getLucidity() + lucidityDelta));
        int newControlLevel = Math.max(0, Math.min(100, playthrough.getControlLevel() + controlDelta));

        playthrough.setLucidity(newLucidity);
        playthrough.setControlLevel(newControlLevel);

        // Resolver nodo destino
        String resolvedNodeCode;
        if ("RUPTURA_CUARTA_PARED".equals(branchType) || "CRITICO".equals(request.impactLevel())) {
            resolvedNodeCode = sourceNode.getGlitchBranchCode();
        } else {
            resolvedNodeCode = sourceNode.getPrimaryBranchCode();
        }

        // Resolver estado de la partida en orden estricto
        if (newControlLevel >= 100) {
            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode("ENDING_PAC_SYMBOL");
        } else if (newLucidity <= 0) {
            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode("ENDING_WHITE_BEAR");
        } else if (resolvedNodeCode == null || storyNodeRepository.findByNodeCode(resolvedNodeCode).isEmpty()) {
            playthrough.setStatus("FINALIZADA");
            playthrough.setEndingCode("ENDING_NETFLIX_CUT");
        } else {
            playthrough.setStatus("ACTIVA");
            StoryNode nextNode = storyNodeRepository.findByNodeCode(resolvedNodeCode).get();
            playthrough.setCurrentNode(nextNode);
        }

        playthrough.setUpdatedAt(now);
        playthroughRepository.save(playthrough);

        // Guardar la decisión
        Decision decision = Decision.builder()
                .playthrough(playthrough)
                .node(sourceNode)
                .rawInput(request.rawInput())
                .branchType(branchType)
                .impactLevel(request.impactLevel())
                .handlerUnit(handlerUnit)
                .outcomeCode(outcomeCode)
                .resolvedNodeCode(resolvedNodeCode)
                .status("REGISTRADA")
                .createdAt(now)
                .updatedAt(now)
                .build();

        decisionRepository.save(decision);

        // Publicar evento para procesamiento asíncrono
        eventPublisher.publishEvent(new DecisionCommittedEvent(decision.getId(), simulateHeader));

        return mapToResponse(decision, playthrough);
    }

    public DecisionResponse getDecisionById(Long id, String userEmail) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decisión no encontrada con id: " + id));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!decision.getPlaythrough().getUser().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("No tienes permiso para acceder a esta decisión");
        }

        return mapToResponse(decision, decision.getPlaythrough());
    }

    public List<RealityLogResponse> getRealityLogsByDecisionId(Long decisionId, String userEmail) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decisión no encontrada con id: " + decisionId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!decision.getPlaythrough().getUser().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("No tienes permiso para ver los logs de esta decisión");
        }

        return realityLogRepository.findByDecisionOrderByIdAsc(decision).stream()
                .map(log -> new RealityLogResponse(
                        log.getId(),
                        log.getDecision().getId(),
                        log.getRecipientEmail(),
                        log.getSubject(),
                        log.getLogStatus(),
                        log.getErrorMessage(),
                        log.getSentAt(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    public PagedDecisionResponse getDecisions(
            String branchType, String impactLevel, String status, Long playthroughId,
            int page, int size, String userEmail) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Specification<Decision> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
                predicates.add(cb.equal(root.get("playthrough").get("user").get("id"), currentUser.getId()));
            }

            if (branchType != null && !branchType.isBlank()) {
                predicates.add(cb.equal(root.get("branchType"), branchType));
            }
            if (impactLevel != null && !impactLevel.isBlank()) {
                predicates.add(cb.equal(root.get("impactLevel"), impactLevel));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (playthroughId != null) {
                predicates.add(cb.equal(root.get("playthrough").get("id"), playthroughId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Decision> paged = decisionRepository.findAll(spec, pageRequest);

        List<DecisionResponse> content = paged.getContent().stream()
                .map(d -> mapToResponse(d, d.getPlaythrough()))
                .toList();

        return new PagedDecisionResponse(
                content,
                paged.getTotalElements(),
                paged.getTotalPages(),
                paged.getNumber(),
                paged.getSize()
        );
    }

    private DecisionResponse mapToResponse(Decision decision, Playthrough playthrough) {
        return new DecisionResponse(
                decision.getId(),
                playthrough.getId(),
                playthrough.getPlayerTag(),
                decision.getNode().getNodeCode(),
                decision.getResolvedNodeCode(),
                decision.getRawInput(),
                decision.getBranchType(),
                decision.getImpactLevel(),
                decision.getHandlerUnit(),
                decision.getOutcomeCode(),
                decision.getStatus(),
                playthrough.getStatus(),
                playthrough.getLucidity(),
                playthrough.getControlLevel(),
                playthrough.getEndingCode(),
                decision.getCreatedAt(),
                decision.getUpdatedAt()
        );
    }
}
