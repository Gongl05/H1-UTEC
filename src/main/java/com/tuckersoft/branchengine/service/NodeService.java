package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.NodeRequest;
import com.tuckersoft.branchengine.dto.NodeResponse;
import com.tuckersoft.branchengine.entity.StoryNode;
import com.tuckersoft.branchengine.exception.BadRequestException;
import com.tuckersoft.branchengine.exception.ResourceConflictException;
import com.tuckersoft.branchengine.exception.ResourceNotFoundException;
import com.tuckersoft.branchengine.repository.StoryNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NodeService {

    private final StoryNodeRepository storyNodeRepository;

    public NodeResponse createNode(NodeRequest request) {
        if (storyNodeRepository.existsByNodeCode(request.nodeCode())) {
            throw new ResourceConflictException("El nodeCode ya existe: " + request.nodeCode());
        }

        if (request.branchCapacity() != null && request.branchCapacity() <= 0) {
            throw new BadRequestException("La capacidad de ramas debe ser mayor a 0");
        }

        StoryNode node = StoryNode.builder()
                .nodeCode(request.nodeCode())
                .title(request.title())
                .sceneText(request.sceneText())
                .branchCapacity(request.branchCapacity())
                .currentBranches(0)
                .primaryBranchCode(request.primaryBranchCode())
                .glitchBranchCode(request.glitchBranchCode())
                .createdAt(Instant.now())
                .build();

        storyNodeRepository.save(node);

        return mapToResponse(node);
    }

    public NodeResponse getNodeById(Long id) {
        StoryNode node = storyNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nodo no encontrado con id: " + id));
        return mapToResponse(node);
    }

    public List<NodeResponse> getAllNodes() {
        return storyNodeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private NodeResponse mapToResponse(StoryNode node) {
        return new NodeResponse(
                node.getId(),
                node.getNodeCode(),
                node.getTitle(),
                node.getSceneText(),
                node.getBranchCapacity(),
                node.getCurrentBranches(),
                node.getPrimaryBranchCode(),
                node.getGlitchBranchCode(),
                node.getCreatedAt()
        );
    }
}
