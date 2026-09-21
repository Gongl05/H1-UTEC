package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.*;
import com.tuckersoft.branchengine.entity.Decision;
import com.tuckersoft.branchengine.entity.Playthrough;
import com.tuckersoft.branchengine.entity.StoryNode;
import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.exception.BadRequestException;
import com.tuckersoft.branchengine.exception.ForbiddenException;
import com.tuckersoft.branchengine.exception.ResourceConflictException;
import com.tuckersoft.branchengine.exception.ResourceNotFoundException;
import com.tuckersoft.branchengine.repository.DecisionRepository;
import com.tuckersoft.branchengine.repository.PlaythroughRepository;
import com.tuckersoft.branchengine.repository.StoryNodeRepository;
import com.tuckersoft.branchengine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaythroughService {

    private final PlaythroughRepository playthroughRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final UserRepository userRepository;
    private final DecisionRepository decisionRepository;

    @Transactional
    public PlaythroughResponse createPlaythrough(PlaythroughRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        StoryNode startNode = storyNodeRepository.findByNodeCodeForUpdate(request.startNodeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Nodo no encontrado con nodeCode: " + request.startNodeCode()));

        if (playthroughRepository.existsByPlayerTag(request.playerTag())) {
            throw new ResourceConflictException("El playerTag ya existe: " + request.playerTag());
        }

        if (startNode.getCurrentBranches() >= startNode.getBranchCapacity()) {
            throw new BadRequestException("El nodo inicial " + startNode.getNodeCode() + " está lleno");
        }

        // Incrementar currentBranches del nodo de inicio
        startNode.setCurrentBranches(startNode.getCurrentBranches() + 1);
        storyNodeRepository.save(startNode);

        Instant now = Instant.now();
        Playthrough playthrough = Playthrough.builder()
                .playerTag(request.playerTag())
                .user(user)
                .startNodeCode(startNode.getNodeCode())
                .currentNode(startNode)
                .lucidity(100)
                .controlLevel(0)
                .status("ACTIVA")
                .endingCode(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        playthroughRepository.save(playthrough);

        return mapToResponse(playthrough);
    }

    public List<PlaythroughResponse> getPlaythroughs(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Playthrough> list;
        if ("ROLE_ADMIN".equals(user.getRole())) {
            list = playthroughRepository.findAllByOrderByCreatedAtDesc();
        } else {
            list = playthroughRepository.findByUserOrderByCreatedAtDesc(user);
        }

        return list.stream().map(this::mapToResponse).toList();
    }

    public PlaythroughResponse getPlaythroughById(Long id, String userEmail) {
        Playthrough playthrough = playthroughRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partida no encontrada con id: " + id));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!playthrough.getUser().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("No tienes permiso para acceder a esta partida");
        }

        return mapToResponse(playthrough);
    }

    public PlaythroughPathResponse getPlaythroughPath(Long id, String userEmail) {
        Playthrough playthrough = playthroughRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partida no encontrada con id: " + id));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!playthrough.getUser().getId().equals(currentUser.getId()) && !"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new ForbiddenException("No tienes permiso para acceder a esta partida");
        }

        List<Decision> decisions = decisionRepository.findByPlaythroughOrderByCreatedAtAscIdAsc(playthrough);
        List<PlaythroughPathStep> steps = new ArrayList<>();

        int order = 1;
        for (Decision d : decisions) {
            if (d.getResolvedNodeCode() != null) {
                steps.add(new PlaythroughPathStep(
                        order++,
                        d.getId(),
                        d.getNode().getNodeCode(),
                        d.getResolvedNodeCode(),
                        d.getBranchType(),
                        d.getImpactLevel(),
                        d.getCreatedAt()
                ));
            }
        }

        return new PlaythroughPathResponse(
                playthrough.getId(),
                playthrough.getPlayerTag(),
                playthrough.getStatus(),
                playthrough.getEndingCode(),
                playthrough.getStartNodeCode(),
                playthrough.getCurrentNode().getNodeCode(),
                steps
        );
    }

    private PlaythroughResponse mapToResponse(Playthrough playthrough) {
        return new PlaythroughResponse(
                playthrough.getId(),
                playthrough.getPlayerTag(),
                playthrough.getUser().getEmail(),
                playthrough.getStartNodeCode(),
                playthrough.getCurrentNode().getNodeCode(),
                playthrough.getLucidity(),
                playthrough.getControlLevel(),
                playthrough.getStatus(),
                playthrough.getEndingCode(),
                playthrough.getCreatedAt(),
                playthrough.getUpdatedAt()
        );
    }
}
